package edu.cwru.oxi1.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import edu.cwru.oxi1.common.Methods;
import edu.cwru.oxi1.common.NeuronType;

public class Part1SpliceData {

	public static final String FILE_NAME = "offsetDiamPulseWidthAlphaMuBetaNuMotorData.txt";
	public static void main(String[] args) { 
		for(NeuronType neuronType : NeuronType.values()){
			Date tic = new Date();
			System.out.print("Parsing data for " + neuronType.toString() + " nerves\t");
			try {
				Workbook workbook = WorkbookFactory.create(new File("raw/" + neuronType.getFileName()));
		        Sheet sheet = workbook.getSheet("fitResults");
		        
		        DataFormatter dataFormatter = new DataFormatter();

				List<String> lines = new ArrayList<String>();
				String header = "OFFSET\tdiameter\tPW\tALPHA\tMU\tBETA\tNU\tRES";
				lines.add(header);
				for(int colIndex = 0;colIndex<=392;colIndex+=7){
		        	int rowIndex = 0;
			        String info = dataFormatter.formatCellValue(sheet.getRow(rowIndex++).getCell(colIndex+1));
			        double offset = Double.valueOf(info.replaceAll("^o(.*)d.*$", "$1"));
			        int diameter = Integer.valueOf(info.replaceAll("^o.*d([0-9]+)$", "$1"));
			        rowIndex++;
			        
			        while(sheet.getRow(rowIndex) != null && sheet.getRow(rowIndex).getCell(colIndex) != null){
			        	String line = (offset + "\t" + diameter + "\t");
			        	for(int i=0;i<6;i++)
			        		line += (Methods.printCellValue(sheet.getRow(rowIndex).getCell(colIndex+i)));
			        	lines.add(line);
			        	rowIndex++;
			        }
		        }
				Methods.writeToFile(neuronType.toString() + "_" + FILE_NAME, lines);
			} catch (EncryptedDocumentException e) {
				e.printStackTrace();
			} catch (InvalidFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Date toc = new Date();
			System.out.println(0.001*(toc.getTime()-tic.getTime()) + " seconds.");
		}
		System.out.println("Done.");
	}
}
