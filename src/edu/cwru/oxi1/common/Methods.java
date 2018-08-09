package edu.cwru.oxi1.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;

public class Methods {
	
	public static final int MIN_PW = 10; 
	public static final int MAX_PW = 500; 
	
	public static void writeToFile(String fileName, List<String> lines) throws IOException{
		File file = new File(fileName);
		file.createNewFile();
		FileWriter writer = new FileWriter(file.getAbsoluteFile());
		BufferedWriter buffer = new BufferedWriter(writer);
		for(String line : lines)
			buffer.write(line + "\n");
		buffer.close();
	}
	
	public static List<String[]> readFile(String fileName, boolean skipHeader) throws IOException{
		BufferedReader buffer = new BufferedReader(new FileReader(new File(fileName)));
		String line;
		if(skipHeader)
			buffer.readLine();
		List<String[]> lines = new ArrayList<String[]>();
		while((line=buffer.readLine()) != null)
			lines.add(line.split("\t"));
		buffer.close();
		return lines;
	}
	
	public static OffSet convertToOffSet(String offset) {
		if(offset.equals("0.0")){
			return OffSet.ZERO;
		}else if(offset.equals("0.25")){
			return OffSet.QUARTER;
		}else if(offset.equals("0.5")){
			return OffSet.HALF;
		}
		return null;
	}
	
	public static OffSet convertToOffSetFromText(String text){
		for(OffSet offset : OffSet.values())
			if(offset.toString().equals(text))
				return offset;
		return null;
	}
	
	public static NeuronType convertToNeuronTypeFromText(String text){
		for(NeuronType nt : NeuronType.values())
			if(nt.toString().equals(text))
				return nt;
		return null;
	}
	
	public static Parameter convertToParameterTypeFromText(String text){
		for(Parameter p : Parameter.values())
			if(p.toString().equals(text))
				return p;
		return null;
	}
	
	public static DiameterParameter convertToDiameterParameterTypeFromText(String text){
		for(DiameterParameter p : DiameterParameter.values())
			if(p.toString().equals(text))
				return p;
		return null;
	}

	public static PulseWidthParameter convertToPulseWidthParameterTypeFromText(String text){
		for(PulseWidthParameter p : PulseWidthParameter.values())
			if(p.toString().equals(text))
				return p;
		return null;
	}
	
	public static String printCellValue(Cell cell) {
	    String result = "";
		switch (cell.getCellTypeEnum()) {
	        case BOOLEAN:
	            result = "" + cell.getBooleanCellValue();
	            break;
	        case STRING:
	            result = cell.getRichStringCellValue().getString();
	            break;
	        case NUMERIC:
	            if (DateUtil.isCellDateFormatted(cell)) {
	                result = "" + cell.getDateCellValue();
	            } else {
	                result = "" + cell.getNumericCellValue();
	            }
	            break;
	        case FORMULA:
	            result = "" + cell.getCellFormula();
	            break;
	        case BLANK:
	            result = "\t";
	            break;
	        default:
	    }
		return result + "\t";
	}

}
