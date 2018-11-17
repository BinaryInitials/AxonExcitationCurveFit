package edu.cwru.oxi1.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import Jama.Matrix;
import edu.cwru.oxi1.common.DiameterParameter;
import edu.cwru.oxi1.common.Methods;
import edu.cwru.oxi1.common.OffSet;
import edu.cwru.oxi1.common.Parameter;
import edu.cwru.oxi1.common.SimpleMathFunctions;


public class Part3ParameterOptimizationPulsewidth {

	public static final String FILE_NAME = "ModelResult.txt";
	
	public static void main(String[] args) throws IOException {
		Date tic = new Date();
		System.out.println("Calculating Ae^(-PW/Tau) fit for Pulsewidth...");
		
		//Initializing data
		HashMap<OffSet, HashMap<Parameter, HashMap<DiameterParameter, List<Double>>>> data = new HashMap<OffSet, HashMap<Parameter, HashMap<DiameterParameter, List<Double>>>>();
		for(OffSet offSet : OffSet.values()){
			HashMap<Parameter, HashMap<DiameterParameter, List<Double>>> parameterColumn = new HashMap<Parameter, HashMap<DiameterParameter, List<Double>>>();
			for(Parameter parameter : Parameter.values()){
				HashMap<DiameterParameter, List<Double>> column = new HashMap<DiameterParameter, List<Double>>();
				for(DiameterParameter param : DiameterParameter.values())
					column.put(param, new ArrayList<Double>());
				parameterColumn.put(parameter, column);
			}
			data.put(offSet, parameterColumn);
		}
		
		//Reading file and populating "database"
		for(Parameter parameter : Parameter.values()){
			String fileName = parameter + "_" + Part2ParameterOptimizationDiameter.FILE_NAME;
			List<String[]> input = Methods.readFile(fileName,true);
			for(String[] columns : input){
				OffSet offSet = Methods.convertToOffSet(columns[0]);
				data.get(offSet).get(parameter).get(DiameterParameter.D).add(Double.valueOf(columns[2]));
				data.get(offSet).get(parameter).get(DiameterParameter.C).add(Double.valueOf(columns[3]));
				data.get(offSet).get(parameter).get(DiameterParameter.B).add(Double.valueOf(columns[4]));
				data.get(offSet).get(parameter).get(DiameterParameter.A).add(Double.valueOf(columns[5]));
			}
		}
		
		/*
		 * Parameter optimization
		 * For this, we are going to optimize the variables to P0*exp(-tau*PW)+Pinf
		 */
		String header = "NEURON\tOFFSET\tPARAM\tDPARAM\tTAU\tP0\tPinf\tRES";
		List<String> lines = new ArrayList<String>();
		lines.add(header);
		for(OffSet offSet : OffSet.values()){
			for(Parameter param : Parameter.values()){
				for(DiameterParameter diamp : DiameterParameter.values()){
					List<Double> x = new ArrayList<Double>();
					List<Double> y = new ArrayList<Double>();
					for(int i=0;i<data.get(offSet).get(param).get(diamp).size();i++){
						x.add((i+1)*10.0);
						y.add(data.get(offSet).get(param).get(diamp).get(i));
					}
					
					//Initialization of parameters:
					double Pinf = SimpleMathFunctions.average(y.subList(y.size()-10, y.size()));
					double ratio = (y.get(5)-Pinf)/(y.get(10)-Pinf);
					double tau = ratio < 0.0 ? 0.00001 : Math.log(ratio)/(x.get(5)-x.get(10));
					
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
					lines.add(offSet + "\t" + param + "\t" + diamp + "\t" + (maxTau+minTau)/2.0 + "\t" + coefs.toString().replaceAll("\\[|\\]","").replaceAll(", ","\t"));
				}
			}
		}
		Methods.writeToFile(FILE_NAME, lines);
		
		Date toc = new Date();
		System.out.println(0.001*(toc.getTime()-tic.getTime()) + " seconds.");
		System.out.println("Done.");
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