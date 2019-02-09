package edu.cwru.oxi1.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import edu.cwru.oxi1.common.Methods;

public class Part0_ConvertToText {

	
	public static final String RAW_DATA_TXT = "OffSetDiameterPulseWidthVeD2Ve.txt";
	
	public static void main(String[] args) {
			
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
		
			String offSet = "0";
			Date tic = new Date();
			List<String> lines = new ArrayList<String>();
			try {
				Workbook workbook = WorkbookFactory.create(new File(inputFileName));

				System.out.println("Converting Spreadsheets to text...");
				for(int diameter = 2; diameter<=20;diameter++){
					for(int pulsewidth = 10; pulsewidth<=500; pulsewidth+=10){
						List<Double> Ves = new ArrayList<Double>();
				        List<Double> d2Ves = new ArrayList<Double>();
					
						
						int columnVe = pulsewidth/10*3-3;
						int columnd2Ve = pulsewidth/10*3-2;
						Sheet sheet = workbook.getSheet("o" + offSet + "d" + diameter);
				        int rowIndex = 3;
				        while(sheet.getRow(rowIndex) != null && sheet.getRow(rowIndex).getCell(columnVe) != null && sheet.getRow(rowIndex).getCell(columnd2Ve) != null){
				        	Ves.add(Double.valueOf(Methods.printCellValue(sheet.getRow(rowIndex).getCell(columnVe))));
			        		d2Ves.add(Double.valueOf(Methods.printCellValue(sheet.getRow(rowIndex).getCell(columnd2Ve))));
				        	rowIndex++;
				        }
						for(int i=0;i<d2Ves.size();i++)
							lines.add(offSet + "\t" + diameter + "\t" + pulsewidth + "\t" + Ves.get(i) + "\t" + d2Ves.get(i));
					}
				}
					
			} catch (EncryptedDocumentException e) {
				e.printStackTrace();
			} catch (InvalidFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
				
			try {
				Methods.writeToFile(RAW_DATA_TXT, lines);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			Date toc = new Date();
			System.out.println(0.001*(toc.getTime()-tic.getTime()) + " seconds.");
		System.out.println("Done.");
	}
}
