package engine.core;

import java.util.Collection;

public abstract class Extractor {
	
	public final ClassifiedTokens classifyQuestionTokens(String question) {
		if (question == null || question.length() == 0)
			throw new IllegalArgumentException("Invalid question: " + question);
		return process(question);
	}
	
	protected abstract ClassifiedTokens process(String question);

	public class ClassifiedTokens {
		private Collection<String> concepts;
		private Collection<String> notConcepts;
		/**
		 * @return the concepts
		 */
		public Collection<String> getConcepts() {
			return concepts;
		}
		/**
		 * @param concepts the concepts to set
		 */
		public void setConcepts(Collection<String> concepts) {
			this.concepts = concepts;
		}
		/**
		 * @return the notConcepts
		 */
		public Collection<String> getNotConcepts() {
			return notConcepts;
		}
		/**
		 * @param notConcepts the notConcepts to set
		 */
		public void setNotConcepts(Collection<String> notConcepts) {
			this.notConcepts = notConcepts;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "ClassifiedTokens [concepts=" + concepts + ", notConcepts="
					+ notConcepts + "]";
		}
	}

}
