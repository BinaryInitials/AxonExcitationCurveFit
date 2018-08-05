package edu.cwru.oxi1.common;

import java.util.List;

public class SimpleMathFunctions {
	
	public static double average(List<Double> list){
		double sum = 0.0; 
		for(double item : list)
			sum+=item;
		return sum/list.size();
	}

}
