package engine.eval;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import engine.core.Extractor;
import engine.core.Extractor.ClassifiedTokens;

/**
 * Evaluator that is used to compute IR metrics to measure the performance of
 * the {@link Extractor}s.
 * 
 * @author Samer
 * 
 */
public class IREvaluator {

	private List<String> questions;
	private List<List<String>> concepts;
	private int truePos;
	private int falsePos;
	private int trueNeg;
	private int falseNeg;

	/**
	 * Create an instance of the evaluator given questions and their
	 * corresponding concepts.
	 * 
	 * @param questions
	 *            The input questions that will be used for the evaluation.
	 * @param concepts
	 *            The Ground-Truth concepts that will be used for evaluation.
	 */
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

	/**
	 * Starts evaluating an extractor on the given set of questions and concepts
	 * that were given during the instantiation of the evaluator.
	 * 
	 * @param extractor
	 *            Extractor to be evaluated.
	 */
	public void eval(Extractor extractor) {
		int N = questions.size();
		for (int i = 0; i < N; i++) {
			String q = questions.get(i);
			try {
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
			} catch (Exception e) {
				System.err.println("Question #" + i + ": " + q);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Resets the counters of the evaluator.
	 */
	public void resetEvaluator() {
		truePos = trueNeg = falsePos = falseNeg = 0;
	}

	/**
	 * Get the Precision of the extractor. Represents how many of the concepts
	 * that the extractor returned as really concepts of the question.
	 * 
	 * @return a value between 0 & 1 exclusive. The higher, the better the
	 *         extractor.
	 */
	public double getPrecision() {
		return ((double) truePos) / (truePos + falsePos);
	}

	/**
	 * Get the Recall of the extractor. Represents how many of the concepts that
	 * it should have extracted, it really did.
	 * 
	 * @return a value between 0 & 1 exclusive. The higher, the better the
	 *         extractor.
	 */
	public double getRecall() {
		return ((double) truePos) / (truePos + falseNeg);
	}

	/**
	 * Get the accuracy of the extractor. Usually it is not a good indicator of
	 * the quality of the extractor.
	 * 
	 * @return a value between 0 & 1 exclusive. The higher, the better the
	 *         extractor.
	 */
	public double getAccuracy() {
		return ((double) (truePos + trueNeg))
				/ (truePos + falsePos + trueNeg + falseNeg);
	}

	/**
	 * Get the F-Measure score of the extractor. Represents a score that
	 * combines both Precision and Recall.
	 * 
	 * @return a value between 0 & 1 exclusive. The higher, the better the
	 *         extractor.
	 */
	public double getFMeasure() {
		double p = getPrecision();
		double r = getRecall();
		return (2.0 * p * r) / (p + r);
	}
}
