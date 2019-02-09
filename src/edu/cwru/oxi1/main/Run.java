package edu.cwru.oxi1.main;

import java.io.IOException;

public class Run {

	public static void main(String[] args) throws IOException {
		System.out.println("Part 0: Converting to Text...");
		Part0_ConvertToText.main(args);
		
		System.out.println("Part 1: Optimization of diameters");
		Part1_ParameterOptimizationDiameter.main(args);
		
		System.out.println("Part 2: Optimization of pulsewidth");
		Part2_ParameterOptimizationPulsewidth.main(args);
		
		System.out.println("Part 3: Validation");
		Part3_ModelValidationResiduals.main(args);
	}
}
