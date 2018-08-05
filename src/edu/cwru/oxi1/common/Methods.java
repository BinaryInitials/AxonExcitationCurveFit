package edu.cwru.oxi1.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
}
