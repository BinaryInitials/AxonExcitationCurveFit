package edu.cwru.oxi1.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Jama.Matrix;
import edu.cwru.oxi1.common.Methods;
import edu.cwru.oxi1.common.NeuronType;
import edu.cwru.oxi1.common.OffSet;
import edu.cwru.oxi1.common.DiameterParameter;
import edu.cwru.oxi1.common.Parameter;
import edu.cwru.oxi1.common.SimpleMathFunctions;


public class TestPart3ParameterOptimizationPulsewidth {

	public static void main(String[] args) throws IOException {
		HashMap<NeuronType, HashMap<OffSet, HashMap<Parameter, HashMap<DiameterParameter, List<Double>>>>> data = new HashMap<NeuronType, HashMap<OffSet, HashMap<Parameter, HashMap<DiameterParameter, List<Double>>>>>();
		//Initializing data
		for(NeuronType nt : NeuronType.values()){
			HashMap<OffSet, HashMap<Parameter, HashMap<DiameterParameter, List<Double>>>> dataOneType = new HashMap<OffSet, HashMap<Parameter, HashMap<DiameterParameter, List<Double>>>>();
			for(OffSet offSet : OffSet.values()){
				HashMap<Parameter, HashMap<DiameterParameter, List<Double>>> parameterColumn = new HashMap<Parameter, HashMap<DiameterParameter, List<Double>>>();
				for(Parameter parameter : Parameter.values()){
					HashMap<DiameterParameter, List<Double>> column = new HashMap<DiameterParameter, List<Double>>();
					for(DiameterParameter param : DiameterParameter.values())
						column.put(param, new ArrayList<Double>());
					parameterColumn.put(parameter, column);
				}
				dataOneType.put(offSet, parameterColumn);
			}
			data.put(nt, dataOneType);
		}
		
		//Reading file and populating "database"
		for(NeuronType nt : NeuronType.values()){
			for(Parameter parameter : Parameter.values()){
				String fileName = nt + "_" + parameter + "_" + Part2ParameterOptimizationDiameter.FILE_NAME;
				List<String[]> input = Methods.readFile(fileName,true);
				for(String[] columns : input){
					OffSet offSet = Methods.convertToOffSet(columns[0]);
					data.get(nt).get(offSet).get(parameter).get(DiameterParameter.D).add(Double.valueOf(columns[2]));
					data.get(nt).get(offSet).get(parameter).get(DiameterParameter.C).add(Double.valueOf(columns[3]));
					data.get(nt).get(offSet).get(parameter).get(DiameterParameter.B).add(Double.valueOf(columns[4]));
					data.get(nt).get(offSet).get(parameter).get(DiameterParameter.A).add(Double.valueOf(columns[5]));
				}
			}
		}
		
		/*
		 * Parameter optimization
		 * For this, we are going to optimize the variables to P0*exp(-tau*PW)+Pinf
		 */
		
		System.out.println("NEURON\tOFFSET\tPARMA\tPWPARAM\tTAU\tP0\tPinf\tRES");
		NeuronType nt = NeuronType.MOTOR;
		OffSet offSet = OffSet.ZERO;
		Parameter param = Parameter.MU;
		DiameterParameter pwp = DiameterParameter.B;
		List<Double> x = new ArrayList<Double>();
		List<Double> y = new ArrayList<Double>();
		for(int i=0;i<data.get(nt).get(offSet).get(param).get(pwp).size();i++){
			x.add((i+1)*10.0);
			y.add(data.get(nt).get(offSet).get(param).get(pwp).get(i));
		}
		
		//Initialization of parameters:
		double Pinf = SimpleMathFunctions.average(y.subList(y.size()-10, y.size()));
		System.out.println(y.get(5) + "\t" + Pinf + "\t" + y.get(10) + "\t" + x.get(5) + "\t" + x.get(10));
		System.out.println((y.get(5)-Pinf)/(y.get(10)-Pinf));
		double tau = Math.log((y.get(5)-Pinf)/(y.get(10)-Pinf))/(x.get(5)-x.get(10));
		tau = (y.get(5)-Pinf)/(y.get(10)-Pinf) < 0.0 ? 0.000001 : tau;
		
		System.out.println("Initial parameters: ");
		System.out.println(Pinf + "\t" + tau);
		
		double maxTau = 1000*tau;
		double minTau = 0.001*tau;
		List<Double> coefs = new ArrayList<Double>();
		while(Math.abs(maxTau-minTau) > 0.00000001){
			List<Double> coefsMax = getCoefs(x, y, maxTau);
			List<Double> coefsMin = getCoefs(x, y, minTau);
			double varianceMax = coefsMax.get(coefsMax.size()-1);
			double varianceMin = coefsMin.get(coefsMin.size()-1);
			if(varianceMax < varianceMin){
				minTau = (maxTau + minTau)/2.0;
				coefs = coefsMax;
			}else{
				maxTau = (maxTau + minTau)/2.0;
				coefs = coefsMin;
			}
		}
		System.out.println(nt + "\t" + offSet + "\t" + param + "\t" + pwp + "\t" + (maxTau+minTau)/2.0 + "\t" + coefs.toString().replaceAll("\\[|\\]","").replaceAll(", ","\t"));
	}
		
	
	
	public static List<Double> getCoefs(List<Double> x, List<Double> y, double tau){
		//Creating Vondermonde matrix
		double[][] xMatrix = new double[x.size()][2];
		double[] yMatrix = new double[y.size()];
		
		for(int i=0;i<y.size();i++)
			yMatrix[i] = y.get(i);

		//Creating a Vandermonde Matrix
		for(int row = 0;row<x.size(); row++){
			xMatrix[row][0] = Math.exp(tau*x.get(row));
			xMatrix[row][1] = 1.0;
		}
		
		List<Double> coefficients = new ArrayList<Double>();
		Matrix a = new Matrix(xMatrix);
		Matrix b = new Matrix(yMatrix, y.size());
		Matrix c = (a.transpose().times(a)).inverse().times(a.transpose()).times(b);
		double[][] coef = c.getArray();
		for(double[] coefOne : coef)
			coefficients.add(coefOne[0]);
		double variance = 0.0;
		for(int i=0;i<y.size();i++)
			variance += Math.pow(coefficients.get(0)*Math.exp(x.get(i)*tau) + coefficients.get(1)- y.get(i), 2.0);
		coefficients.add(variance);
		return coefficients;
	}
}