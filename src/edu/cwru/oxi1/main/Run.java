package edu.cwru.oxi1.main;

import java.io.IOException;

public class Run {

	public static void main(String[] args) throws IOException {
		System.out.println("Part 0: Converting to Text...");
		Part0ConvertToText.main(args);
		System.out.println("Part 1: Initialization...");
		Part1SpliceData.main(args);
		System.out.println("Part 2: Optimization of diameters");
		Part2ParameterOptimizationDiameter.main(args);
		System.out.println("Part 3: Optimization of pulsewidth");
		Part3ParameterOptimizationPulsewidth.main(args);
		System.out.println("Part 4: Validation");
		Part4ModelValidationResiduals.main(args);
	}
}
