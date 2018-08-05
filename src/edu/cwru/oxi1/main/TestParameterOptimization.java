package edu.cwru.oxi1.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Jama.Matrix;

public class TestParameterOptimization {

	public static void main(String[] args) throws NumberFormatException, IOException {
		//1. Capture data from file 
		BufferedReader buffer = new BufferedReader(new FileReader(new File("raw/data2.txt")));
		
		List<Double> Ve = new ArrayList<Double>();
		List<Double> d2Ve = new ArrayList<Double>();
		
		String line;
		while((line=buffer.readLine()) != null){
			String[] columns = line.split("\t");
			Ve.add(Double.valueOf(columns[0]));
			d2Ve.add(Double.valueOf(columns[1]));
		}
		buffer.close();
		
		List<Double> mus = new ArrayList<Double>();
		List<Double> nus = new ArrayList<Double>();

		final int iterations = 1000;
		double avgMu = 0.01;
		double minMu = 0.1*avgMu;
		double maxMu = 10.0*avgMu;
		double deltaMu = (maxMu-minMu)/(0.0+iterations); 
		
		double avgNu = 0.001;
		double minNu = 0.01*avgNu;
		double maxNu = 10*avgNu;
		double deltaNu = (maxNu-minNu)/(0.0+iterations); 
		
		
		for(int i=0;i<iterations;i++){
			mus.add(minMu + i*deltaMu);
			nus.add(minNu + i*deltaNu);
		}

		double minVariance = 1000000000;
		int i=0;
		for(double mu : mus){
			i++;
			int j=0;
			maxNu = Collections.max(nus);
			minNu = Collections.min(nus);
			while(maxNu-minNu > 0.00000001){
				List<Double> coefsMax = getCoefs(Ve, d2Ve, mu, maxNu);
				List<Double> coefsMin = getCoefs(Ve, d2Ve, mu, minNu);
				double varianceMax = coefsMax.get(coefsMax.size()-1);
				double varianceMin = coefsMin.get(coefsMin.size()-1);
				double variance;
				List<Double> coefs = new ArrayList<Double>();
				if(varianceMax < varianceMin){
					variance = varianceMax;
					minNu = (maxNu+minNu)/2.0;
					coefs = coefsMax;
				}else{
					variance = varianceMin;
					maxNu = (maxNu+minNu)/2.0;
					coefs = coefsMin;
				}
//				System.out.println(j++ + "\t" + result);
				if(variance < minVariance){
					minVariance = variance;
//					result = variance + "\t" + mu + "\t" + maxNu + "\t" + coefs.get(0) + "\t" + coefs.get(1);
					System.out.println(i + "\t" + j++ + "\t" + variance + "\t" + mu + "\t" + (maxNu+minNu)/2.0 + "\t" + coefs);
				}
			}
		}
		System.out.println("Done.");
	}
	
	public static List<Double> getCoefs(List<Double> Ve, List<Double> d2Ve, double mu, double nu){
		//2. Creating Vondermonde matrix
		double[] x1 = new double[Ve.size()];
		double[] x2 = new double[Ve.size()];
		double[] y = new double[d2Ve.size()];
		
		for(int i=0;i<d2Ve.size();i++)
			y[i] = d2Ve.get(i);

		for(int i=0;i<Ve.size();i++){
			x1[i] = Math.exp(-mu*Ve.get(i));
			x2[i] = Math.exp(-nu*Ve.get(i));
		}
		
		double[][] matrixX = new double[Ve.size()][2];
		
		for(int row = 0;row<Ve.size(); row++){
			matrixX[row][0] = x1[row]; 
			matrixX[row][1] = x2[row]; 
		}

		List<Double> coefficients = new ArrayList<Double>();
		Matrix a = new Matrix(matrixX);
		Matrix b = new Matrix(y, y.length);
		Matrix c = (a.transpose().times(a)).inverse().times(a.transpose()).times(b);
		double[][] coef = c.getArray();
		for(double[] coefOne : coef)
			coefficients.add(coefOne[0]);
		double variance = 0.0;
		for(int i=0;i<y.length;i++)
			variance += Math.pow(coefficients.get(0) * x1[i] + coefficients.get(1) * x2[i] - y[i], 2.0);
		coefficients.add(variance);
		return coefficients;
	}
}