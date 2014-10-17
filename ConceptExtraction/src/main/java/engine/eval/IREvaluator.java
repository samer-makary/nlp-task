package engine.eval;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import engine.core.Extractor;
import engine.core.Extractor.ClassifiedTokens;

public class IREvaluator {

	private List<String> questions;
	private List<List<String>> concepts;
	private int truePos;
	private int falsePos;
	private int trueNeg;
	private int falseNeg;

	public IREvaluator(List<String> questions, List<List<String>> concepts) {
		if (questions == null || concepts == null)
			throw new IllegalArgumentException(
					"Invalid questions/concpets lists");
		if (questions.size() != concepts.size())
			throw new IllegalArgumentException("Both questions/concpets lists "
					+ "must have the same size");

		this.questions = questions;
		this.concepts = concepts;
	}

	public void eval(Extractor extractor) {
		int N = questions.size();
		for (int i = 0; i < N; i++) {
			String q = questions.get(i);
			Set<String> gtConcepts = new HashSet<String>(concepts.get(i));

			// process the current question
			ClassifiedTokens tokens = extractor.classifyQuestionTokens(q);
			Collection<String> retrieved = tokens.getConcepts();
			Collection<String> notRetrieved = tokens.getNotConcepts();

			for (String retCon : retrieved) {
				if (gtConcepts.contains(retCon))
					truePos++;
				else
					falsePos++;
			}

			for (String nRetCon : notRetrieved) {
				if (gtConcepts.contains(nRetCon))
					falseNeg++;
				else
					trueNeg++;
			}
		}
	}

	public void resetEvaluator() {
		truePos = trueNeg = falsePos = falseNeg = 0;
	}

	public double getPrecision() {
		return ((double) truePos) / (truePos + falsePos);
	}

	public double getRecall() {
		return ((double) truePos) / (truePos + falseNeg);
	}

	public double getAccuracy() {
		return ((double) (truePos + trueNeg))
				/ (truePos + falsePos + trueNeg + falseNeg);
	}

	public double getFMeasure() {
		double p = getPrecision();
		double r = getRecall();
		return (2.0 * p * r) / (p + r);
	}
}
