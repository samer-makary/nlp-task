import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import engine.core.Extractor;
import engine.core.NERExtractor;
import engine.core.WordNetExtractor;
import engine.eval.IREvaluator;
import engine.util.FileLinesReader;

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
		
		System.out.println("Results for NER:");
		runNER(questions, concepts);
		
		System.out.println("Results for WordNet");
		runWordNet(questions, concepts);
	}
	
	private static void runNER(List<String> questions,
			List<List<String>> concepts) {
		Extractor extractor = new NERExtractor();
		IREvaluator evaluator = new IREvaluator(questions, concepts);
		evaluator.eval(extractor);
		System.out.println("Acc = " + evaluator.getAccuracy());
		System.out.println("Pre = " + evaluator.getPrecision());
		System.out.println("Rec = " + evaluator.getRecall());
		System.out.println("FMe = " + evaluator.getFMeasure());
	}
	
	private static void runWordNet(List<String> questions,
			List<List<String>> concepts) {
		Extractor extractor = new WordNetExtractor();
		IREvaluator evaluator = new IREvaluator(questions, concepts);
		evaluator.eval(extractor);
		System.out.println("Acc = " + evaluator.getAccuracy());
		System.out.println("Pre = " + evaluator.getPrecision());
		System.out.println("Rec = " + evaluator.getRecall());
		System.out.println("FMe = " + evaluator.getFMeasure());
	}
}
