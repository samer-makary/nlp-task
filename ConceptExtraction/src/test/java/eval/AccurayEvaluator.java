package eval;

import java.util.List;

public class AccurayEvaluator {
	
	@SuppressWarnings("unused")
	private List<String> questions;
	@SuppressWarnings("unused")
	private List<List<String>> concepts;

	public AccurayEvaluator(List<String> questions, List<List<String>> concepts) {
		if (questions == null || concepts == null)
			throw new IllegalArgumentException("Invalid questions/concpets lists");
		this.questions = questions;
		this.concepts = concepts;
	}

	public double getAccForSoftEval() {
		throw new UnsupportedOperationException();
	}
	
	public double getAccForExactEval() {
		throw new UnsupportedOperationException();
	}
	
}
