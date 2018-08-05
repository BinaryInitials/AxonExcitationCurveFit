package edu.cwru.oxi1.main;


import static edu.cwru.oxi1.common.Methods.MAX_PW;
import static edu.cwru.oxi1.common.Methods.MIN_PW;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Jama.Matrix;
import edu.cwru.oxi1.common.Methods;
import edu.cwru.oxi1.common.NeuronType;
import edu.cwru.oxi1.common.OffSet;
import edu.cwru.oxi1.common.Parameter;

public class Part2ParameterOptimizationDiameter {

	public static final String FILE_NAME = "CubicFit.txt";
	
	public static void main(String[] args) throws IOException {
		for(NeuronType neuronType : NeuronType.values()){
			//Initializing hacky "database"
			HashMap<OffSet, HashMap<Integer, HashMap<Parameter, List<Double>>>> data = new HashMap<OffSet, HashMap<Integer, HashMap<Parameter, List<Double>>>>();
			for(OffSet os : OffSet.values()){
				HashMap<Integer, HashMap<Parameter, List<Double>>> rowRow = new HashMap<Integer, HashMap<Parameter, List<Double>>>();
				for(int pulsewidth = MIN_PW;pulsewidth<=MAX_PW;pulsewidth+=MIN_PW){
					HashMap<Parameter, List<Double>> row = new HashMap<Parameter,List<Double>>();
					for(Parameter p : Parameter.values()){
						row.put(p, new ArrayList<Double>());
					}
					rowRow.put(pulsewidth, row);
				}
				data.put(os, rowRow);
			}
			
			//Parsing results from Step1 and inserting into database.
			List<String[]> input = Methods.readFile(neuronType.toString() + "_" + Part1SpliceData.FILE_NAME, true);
			for(String[] columns : input){
				OffSet offset = Methods.convertToOffSet(columns[0]);
				int pulsewidth = Integer.valueOf(columns[2].replaceAll("\\..*",""));
				double alpha = Double.valueOf(columns[3]);
				double mu = Double.valueOf(columns[4]);
				double beta = Double.valueOf(columns[5]);
				double nu = Double.valueOf(columns[6]);
				data.get(offset).get(pulsewidth).get(Parameter.ALPHA).add(alpha);
				data.get(offset).get(pulsewidth).get(Parameter.BETA).add(beta);
				data.get(offset).get(pulsewidth).get(Parameter.MU).add(mu);
				data.get(offset).get(pulsewidth).get(Parameter.NU).add(nu);
			}

			/*
			 * Parameter estimation of alpha, beta, mu, nu with respect to diameters for all pulsewidths, offsets and neuron type.
			 * Through analysis, it was determined that alpha, beta, mu and nu gave the lowest residuals when fit to a cubic function 
			 * raised to an exponential, i.e. exp(Ax^3+Bx^2+Cx+D). Notice that a coefficient outside of the exponential is unnecessary 
			 * as the cubic polynomial captures that information already. 
			 */
			for(Parameter parameter : Parameter.values()){
				List<String> output = new ArrayList<String>();
				String header = "OFFSET\tPW\tD\tC\tB\tA\tRES";
				output.add(header);
				for(OffSet offSet : OffSet.values()){
					HashMap<Integer, HashMap<Parameter, List<Double>>> dataOffSet = data.get(offSet);
					int sign = parameter == Parameter.MU || parameter == Parameter.NU ? -1 : 1;
					for(int pulsewidth = MIN_PW;pulsewidth<=MAX_PW;pulsewidth+=MIN_PW){
						List<Double> x = new ArrayList<Double>();
						List<Double> y = new ArrayList<Double>();
						for(int i=0;i<dataOffSet.get(pulsewidth).get(parameter).size();i++){
							//Diameter size
							x.add(i+2.0);
							//Values of parameters, mV for alpha, beta and us for mu and nu
							y.add(Math.log(sign*dataOffSet.get(pulsewidth).get(parameter).get(i)));
						}
						List<Double> coefs = getCoefs(x,y,3);
						output.add(offSet.getValue() + "\t" + pulsewidth + "\t" + coefs.toString().replaceAll("\\[|\\]","").replaceAll(", ","\t"));
					}
					Methods.writeToFile(neuronType.toString() + "_" + parameter.toString() + "_" + FILE_NAME, output);
				}
			}
			
		}
		
	}

	public static List<Double> getCoefs(List<Double> x, List<Double> y, int power){
		//Creating Vondermonde matrix
		double[][] xMatrix = new double[x.size()][power+1];
		double[] yMatrix = new double[y.size()];
		
		for(int i=0;i<y.size();i++)
			yMatrix[i] = y.get(i);

		//Creating a Vandermonde Matrix
		for(int row = 0;row<x.size(); row++)
			for(int col=0;col<power+1;col++)
				xMatrix[row][col] = Math.pow(x.get(row),col);
		
		List<Double> coefficients = new ArrayList<Double>();
		Matrix a = new Matrix(xMatrix);
		Matrix b = new Matrix(yMatrix, y.size());
		Matrix c = (a.transpose().times(a)).inverse().times(a.transpose()).times(b);
		double[][] coef = c.getArray();
		for(double[] coefOne : coef)
			coefficients.add(coefOne[0]);
		double variance = 0.0;
		for(int i=0;i<y.size();i++){
			double sum=0;
			for(int p=0;p<=power;p++)
				sum+=coefficients.get(p)*Math.pow(x.get(i), p);
			variance += Math.pow(sum - y.get(i), 2.0);
		}
		coefficients.add(variance);
		return coefficients;
	}
}