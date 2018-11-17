package edu.cwru.oxi1.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import edu.cwru.oxi1.common.DiameterParameter;
import edu.cwru.oxi1.common.Methods;
import edu.cwru.oxi1.common.OffSet;
import edu.cwru.oxi1.common.Parameter;
import edu.cwru.oxi1.common.PulseWidthParameter;

public class Part4ModelValidationResiduals {

	public static void main(String[] args) throws IOException {
		
		//Initializing data
		HashMap<OffSet, HashMap<Parameter, HashMap<DiameterParameter, HashMap<PulseWidthParameter, Double>>>> data = new HashMap<OffSet, HashMap<Parameter, HashMap<DiameterParameter, HashMap<PulseWidthParameter, Double>>>>();
		for(OffSet offSet : OffSet.values()){
			HashMap<Parameter, HashMap<DiameterParameter, HashMap<PulseWidthParameter, Double>>> parameterColumn = new HashMap<Parameter, HashMap<DiameterParameter, HashMap<PulseWidthParameter, Double>>>();
			for(Parameter parameter : Parameter.values()){
				HashMap<DiameterParameter, HashMap<PulseWidthParameter, Double>> diameterColumn = new HashMap<DiameterParameter, HashMap<PulseWidthParameter, Double>>();
				for(DiameterParameter diameter: DiameterParameter.values()){
					HashMap<PulseWidthParameter, Double> pulseWidthColumn = new HashMap<PulseWidthParameter, Double>();
					for(PulseWidthParameter pulseWidth : PulseWidthParameter.values())
						pulseWidthColumn.put(pulseWidth, 0.0);
					diameterColumn.put(diameter, pulseWidthColumn);
				}
				parameterColumn.put(parameter, diameterColumn);
			}
			data.put(offSet, parameterColumn);
		}
		
		List<String[]> model = Methods.readFile(Part3ParameterOptimizationPulsewidth.FILE_NAME, true);
		for(String[] line : model){
			OffSet offSet = Methods.convertToOffSetFromText(line[0]);
			Parameter param = Methods.convertToParameterTypeFromText(line[1]);
			DiameterParameter dp = Methods.convertToDiameterParameterTypeFromText(line[2]);
			data.get(offSet).get(param).get(dp).put(PulseWidthParameter.Tau, Double.valueOf(line[3]));
			data.get(offSet).get(param).get(dp).put(PulseWidthParameter.P0, Double.valueOf(line[4]));
			data.get(offSet).get(param).get(dp).put(PulseWidthParameter.Pinf, Double.valueOf(line[5]));
		}

		//Storing raw values;
//		HashMap<OffSet, HashMap<Integer, HashMap<Integer, HashMap<String, List<Double>>>>> rawData = new HashMap<OffSet, HashMap<Integer, HashMap<Integer, HashMap<String, List<Double>>>>>();
		String offSet = "0";
		try {
			Workbook workbook = WorkbookFactory.create(new File("raw/Sensory.xlsx"));
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


				//Validation
				double residual = validation(data, OffSet.ZERO, Integer.valueOf(diameter), pulsewidth, Ves, d2Ves);
				System.out.println(OffSet.ZERO + "\t" + diameter + "\t" + pulsewidth + "\t" + residual);
				}
			}
		} catch (EncryptedDocumentException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static double validation(HashMap<OffSet, HashMap<Parameter, HashMap<DiameterParameter,HashMap<PulseWidthParameter, Double>>>> model, OffSet os, int diameter, int pulsewidth, List<Double> Ve, List<Double> d2Ve){
		double alphaA = model.get(os).get(Parameter.ALPHA).get(DiameterParameter.A).get(PulseWidthParameter.P0) * Math.exp(pulsewidth * model.get(os).get(Parameter.ALPHA).get(DiameterParameter.A).get(PulseWidthParameter.Tau)) + model.get(os).get(Parameter.ALPHA).get(DiameterParameter.A).get(PulseWidthParameter.Pinf);
		double alphaB = model.get(os).get(Parameter.ALPHA).get(DiameterParameter.B).get(PulseWidthParameter.P0) * Math.exp(pulsewidth * model.get(os).get(Parameter.ALPHA).get(DiameterParameter.B).get(PulseWidthParameter.Tau)) + model.get(os).get(Parameter.ALPHA).get(DiameterParameter.B).get(PulseWidthParameter.Pinf);
		double alphaC = model.get(os).get(Parameter.ALPHA).get(DiameterParameter.C).get(PulseWidthParameter.P0) * Math.exp(pulsewidth * model.get(os).get(Parameter.ALPHA).get(DiameterParameter.C).get(PulseWidthParameter.Tau)) + model.get(os).get(Parameter.ALPHA).get(DiameterParameter.C).get(PulseWidthParameter.Pinf);
		double alphaD = model.get(os).get(Parameter.ALPHA).get(DiameterParameter.D).get(PulseWidthParameter.P0) * Math.exp(pulsewidth * model.get(os).get(Parameter.ALPHA).get(DiameterParameter.D).get(PulseWidthParameter.Tau)) + model.get(os).get(Parameter.ALPHA).get(DiameterParameter.D).get(PulseWidthParameter.Pinf);
//		System.out.println(alphaD + "\t" + alphaC + "\t" + alphaB + "\t" + alphaA);
		double alpha = Math.exp(alphaA*Math.pow(diameter, 3) + alphaB*Math.pow(diameter, 2) + alphaC*diameter + alphaD);

		double betaA = model.get(os).get(Parameter.BETA).get(DiameterParameter.A).get(PulseWidthParameter.P0) * Math.exp(pulsewidth * model.get(os).get(Parameter.BETA).get(DiameterParameter.A).get(PulseWidthParameter.Tau)) + model.get(os).get(Parameter.BETA).get(DiameterParameter.A).get(PulseWidthParameter.Pinf);
		double betaB = model.get(os).get(Parameter.BETA).get(DiameterParameter.B).get(PulseWidthParameter.P0) * Math.exp(pulsewidth * model.get(os).get(Parameter.BETA).get(DiameterParameter.B).get(PulseWidthParameter.Tau)) + model.get(os).get(Parameter.BETA).get(DiameterParameter.B).get(PulseWidthParameter.Pinf);
		double betaC = model.get(os).get(Parameter.BETA).get(DiameterParameter.C).get(PulseWidthParameter.P0) * Math.exp(pulsewidth * model.get(os).get(Parameter.BETA).get(DiameterParameter.C).get(PulseWidthParameter.Tau)) + model.get(os).get(Parameter.BETA).get(DiameterParameter.C).get(PulseWidthParameter.Pinf);
		double betaD = model.get(os).get(Parameter.BETA).get(DiameterParameter.D).get(PulseWidthParameter.P0) * Math.exp(pulsewidth * model.get(os).get(Parameter.BETA).get(DiameterParameter.D).get(PulseWidthParameter.Tau)) + model.get(os).get(Parameter.BETA).get(DiameterParameter.D).get(PulseWidthParameter.Pinf);
//		System.out.println(betaD + "\t" + betaC + "\t" + betaB + "\t" + betaA);
		double beta = Math.exp(betaA*Math.pow(diameter, 3) + betaB*Math.pow(diameter, 2) + betaC*diameter + betaD);

		double muA = model.get(os).get(Parameter.MU).get(DiameterParameter.A).get(PulseWidthParameter.P0) * Math.exp(pulsewidth * model.get(os).get(Parameter.MU).get(DiameterParameter.A).get(PulseWidthParameter.Tau)) + model.get(os).get(Parameter.MU).get(DiameterParameter.A).get(PulseWidthParameter.Pinf);
		double muB = model.get(os).get(Parameter.MU).get(DiameterParameter.B).get(PulseWidthParameter.P0) * Math.exp(pulsewidth * model.get(os).get(Parameter.MU).get(DiameterParameter.B).get(PulseWidthParameter.Tau)) + model.get(os).get(Parameter.MU).get(DiameterParameter.B).get(PulseWidthParameter.Pinf);
		double muC = model.get(os).get(Parameter.MU).get(DiameterParameter.C).get(PulseWidthParameter.P0) * Math.exp(pulsewidth * model.get(os).get(Parameter.MU).get(DiameterParameter.C).get(PulseWidthParameter.Tau)) + model.get(os).get(Parameter.MU).get(DiameterParameter.C).get(PulseWidthParameter.Pinf);
		double muD = model.get(os).get(Parameter.MU).get(DiameterParameter.D).get(PulseWidthParameter.P0) * Math.exp(pulsewidth * model.get(os).get(Parameter.MU).get(DiameterParameter.D).get(PulseWidthParameter.Tau)) + model.get(os).get(Parameter.MU).get(DiameterParameter.D).get(PulseWidthParameter.Pinf);
		double mu = -Math.exp(muA*Math.pow(diameter, 3) + muB*Math.pow(diameter, 2) + muC*diameter + muD);

		double nuA = model.get(os).get(Parameter.NU).get(DiameterParameter.A).get(PulseWidthParameter.P0) * Math.exp(pulsewidth * model.get(os).get(Parameter.NU).get(DiameterParameter.A).get(PulseWidthParameter.Tau)) + model.get(os).get(Parameter.NU).get(DiameterParameter.A).get(PulseWidthParameter.Pinf);
		double nuB = model.get(os).get(Parameter.NU).get(DiameterParameter.B).get(PulseWidthParameter.P0) * Math.exp(pulsewidth * model.get(os).get(Parameter.NU).get(DiameterParameter.B).get(PulseWidthParameter.Tau)) + model.get(os).get(Parameter.NU).get(DiameterParameter.B).get(PulseWidthParameter.Pinf);
		double nuC = model.get(os).get(Parameter.NU).get(DiameterParameter.C).get(PulseWidthParameter.P0) * Math.exp(pulsewidth * model.get(os).get(Parameter.NU).get(DiameterParameter.C).get(PulseWidthParameter.Tau)) + model.get(os).get(Parameter.NU).get(DiameterParameter.C).get(PulseWidthParameter.Pinf);
		double nuD = model.get(os).get(Parameter.NU).get(DiameterParameter.D).get(PulseWidthParameter.P0) * Math.exp(pulsewidth * model.get(os).get(Parameter.NU).get(DiameterParameter.D).get(PulseWidthParameter.Tau)) + model.get(os).get(Parameter.NU).get(DiameterParameter.D).get(PulseWidthParameter.Pinf);
		double nu = -Math.exp(nuA*Math.pow(diameter, 3) + nuB*Math.pow(diameter, 2) + nuC*diameter + nuD);

		double residuals = 0.0;
//		System.out.println(alpha + "\t" + beta + "\t" + mu + "\t" + nu);
		for(int i=0;i<Ve.size();i++){
			double d2VeHat = alpha*Math.exp(mu*Ve.get(i)) + beta * Math.exp(nu*Ve.get(i));
//			System.out.println(Ve.get(i) + "\t" +d2Ve.get(i) + "\t" + d2VeHat);
			residuals += Math.pow((d2VeHat - d2Ve.get(i)), 2.0);
		}
		
		return Math.sqrt(residuals/Ve.size());
	}

}
