package edu.cwru.oxi1.common;

import java.util.ArrayList;
import java.util.List;

import Jama.Matrix;

public class MathMethods {
	
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