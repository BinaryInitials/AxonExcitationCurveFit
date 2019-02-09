package edu.cwru.oxi1.main;

import static edu.cwru.oxi1.common.Methods.MAX_PW;
import static edu.cwru.oxi1.common.Methods.MIN_PW;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import edu.cwru.oxi1.common.MathMethods;
import edu.cwru.oxi1.common.Methods;
import edu.cwru.oxi1.common.OffSet;
import edu.cwru.oxi1.common.Parameter;

public class Part1_ParameterOptimizationDiameter {

	public static final String FILE_NAME = "OffsetDiamPulseWidthAlphaMuBetaNuMotorData.txt";
	public static void main(String[] args) { 
		
		Date tic = new Date();
		
		String inputFileName = "";
		if(args != null && args.length > 0){
			inputFileName = args[0];
			if(!inputFileName.endsWith("xlsx")){
				System.out.println("Invalid input file. Expecting excel filetype");
				System.exit(-1);
			}
		}else {
			inputFileName = "raw/Sensory.xlsx";
		}
		
		
		System.out.print("Parsing data...");
		try {
			Workbook workbook = WorkbookFactory.create(new File(inputFileName));
	        Sheet sheet = workbook.getSheet("FitStep1");
	        
	        HashMap<OffSet, HashMap<Integer, HashMap<Parameter, List<Double>>>> data = initializeMap(); 
	        int rowIndex = 1;
			
			while(sheet.getRow(rowIndex) != null && sheet.getRow(rowIndex).getCell(0) != null){
				int i=0;
				OffSet offset = Methods.convertToOffSet("" + sheet.getRow(rowIndex).getCell(i++).getNumericCellValue());
				int pulsewidth = (int)(sheet.getRow(rowIndex).getCell(i++).getNumericCellValue());
				double diameter = sheet.getRow(rowIndex).getCell(i++).getNumericCellValue();
				double alpha = sheet.getRow(rowIndex).getCell(i++).getNumericCellValue();
				double mu = sheet.getRow(rowIndex).getCell(i++).getNumericCellValue();
				double beta = sheet.getRow(rowIndex).getCell(i++).getNumericCellValue();
				double nu = sheet.getRow(rowIndex).getCell(i++).getNumericCellValue();
				System.out.println(diameter + "\t" + alpha + "\t" + mu + "\t" + beta + "\t" + nu);
				rowIndex++;
				
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
						List<Double> coefs = MathMethods.getCoefs(x, y, 3);
						output.add(offSet.getValue() + "\t" + pulsewidth + "\t" + coefs.toString().replaceAll("\\[|\\]","").replaceAll(", ","\t"));
					}
					Methods.writeToFile("Part1_" + parameter.toString() + "_" + FILE_NAME, output);
				}
			}
		} catch (EncryptedDocumentException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Date toc = new Date();
		System.out.println(0.001*(toc.getTime()-tic.getTime()) + " seconds.");
		System.out.println("Done.");
	}
	
	private static HashMap<OffSet, HashMap<Integer, HashMap<Parameter, List<Double>>>> initializeMap(){
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
		return data;
	}
}
