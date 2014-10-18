package engine.core;

import java.util.Collection;

/**
 * The base class for all the concept extractor classes.
 * 
 * @author Samer
 * 
 */
public abstract class Extractor {

	/**
	 * The function takes as an input a string representing the question. It
	 * will then classify every token in the question whether it is a
	 * <b>concept</b> or not.
	 * 
	 * @param question
	 *            Question whose concepts will be extracted
	 * @return Classification of the question tokens in either concept or
	 *         not-concept category.
	 */
	public final ClassifiedTokens classifyQuestionTokens(String question) {
		if (question == null || question.length() == 0)
			throw new IllegalArgumentException("Invalid question: " + question);
		return process(question);
	}

	/**
	 * Internal method that is implemented by every concept extractor.
	 * 
	 * @param question
	 *            Question to process
	 * @return Tokens classification.
	 */
	protected abstract ClassifiedTokens process(String question);

	/**
	 * Wrapper class that holds the classification of the tokens of the input
	 * question.
	 * 
	 * @author Samer
	 * 
	 */
	public class ClassifiedTokens {
		private Collection<String> concepts;
		private Collection<String> notConcepts;

		/**
		 * Get the list of tokens that was classified as <b>Concepts</b>
		 * 
		 * @return the concepts
		 */
		public Collection<String> getConcepts() {
			return concepts;
		}

		/**
		 * @param concepts
		 *            the concepts to set
		 */
		protected void setConcepts(Collection<String> concepts) {
			this.concepts = concepts;
		}

		/**
		 * Get the list of tokens that was classified as <b>Not-Concepts</b>
		 * 
		 * @return the notConcepts
		 */
		public Collection<String> getNotConcepts() {
			return notConcepts;
		}

		/**
		 * @param notConcepts
		 *            the notConcepts to set
		 */
		protected void setNotConcepts(Collection<String> notConcepts) {
			this.notConcepts = notConcepts;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "ClassifiedTokens [concepts=" + concepts + ", notConcepts="
					+ notConcepts + "]";
		}
	}

}
