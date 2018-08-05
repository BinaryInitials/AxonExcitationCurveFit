package edu.cwru.oxi1.common;

public enum OffSet {
	ZERO(0.0),
	QUARTER(0.25),
	HALF(0.5);
	
	double value;
	OffSet(double value){
		this.value = value;
	}
	public double getValue(){
		return value;
	}
}
