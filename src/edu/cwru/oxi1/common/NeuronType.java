package edu.cwru.oxi1.common;

public enum NeuronType {
	MOTOR("SimResults_13-Jul-2018motorNewDelX.xlsx"),
	SENSORY("SimResults_27-Jun-2018sensory.xlsx");
	
	String fileName;
	NeuronType(String fileName){
		this.fileName = fileName;
	}
	public String getFileName(){
		return fileName;
	}
}
