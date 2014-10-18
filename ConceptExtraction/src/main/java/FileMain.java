import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import engine.core.Extractor;
import engine.core.HybridExtractor;
import engine.core.NERExtractor;
import engine.core.WordNetExtractor;
import engine.eval.IREvaluator;
import engine.util.FileLinesReader;

/**
 * This class will run both algorithm on the sample input and then use the
 * {@link IREvaluator} to compute the Accuracy, Precision, Recall, & FMeasure of
 * each approach.
 * 
 * @author Samer
 * 
 */
public class FileMain {

	public static void main(String[] args) throws FileNotFoundException {
		List<String> questions = FileLinesReader.readLines(new File(
				"samples.input"));
		List<List<String>> concepts = new ArrayList<List<String>>(
				questions.size());
		List<String> consLines = FileLinesReader.readLines(new File(
				"samples.output"));
		for (String conceptsLine : consLines) {
			List<String> cons = new ArrayList<String>();
			String[] consArr = conceptsLine.split(",");
			for (String c : consArr) {
				c = c.trim();
				cons.add(c);
			}
			concepts.add(cons);
		}

		runNER(questions, concepts);
		runWordNet(questions, concepts);
		runHybrid(questions, concepts);
	}

	private static void runNER(List<String> questions,
			List<List<String>> concepts) {
		Extractor extractor = new NERExtractor();
		IREvaluator evaluator = new IREvaluator(questions, concepts);
		evaluator.eval(extractor);
		System.out.println("Results for NER:");
		System.out.println(String.format("Accuracy = %.4f", evaluator.getAccuracy()));
		System.out.println(String.format("Precision = %.4f", evaluator.getPrecision()));
		System.out.println(String.format("Recall = %.4f", evaluator.getRecall()));
		System.out.println(String.format("F-Measure = %.4f", evaluator.getFMeasure()));
	}

	private static void runWordNet(List<String> questions,
			List<List<String>> concepts) {
		Extractor extractor = new WordNetExtractor();
		IREvaluator evaluator = new IREvaluator(questions, concepts);
		evaluator.eval(extractor);
		System.out.println("Results for WordNet");
		System.out.println(String.format("Accuracy = %.4f", evaluator.getAccuracy()));
		System.out.println(String.format("Precision = %.4f", evaluator.getPrecision()));
		System.out.println(String.format("Recall = %.4f", evaluator.getRecall()));
		System.out.println(String.format("F-Measure = %.4f", evaluator.getFMeasure()));
	}
	
	private static void runHybrid(List<String> questions,
			List<List<String>> concepts) {
		Extractor extractor = new HybridExtractor();
		IREvaluator evaluator = new IREvaluator(questions, concepts);
		evaluator.eval(extractor);
		System.out.println("Results for Hybrid");
		System.out.println(String.format("Accuracy = %.4f", evaluator.getAccuracy()));
		System.out.println(String.format("Precision = %.4f", evaluator.getPrecision()));
		System.out.println(String.format("Recall = %.4f", evaluator.getRecall()));
		System.out.println(String.format("F-Measure = %.4f", evaluator.getFMeasure()));
	}
}
