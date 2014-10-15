import engine.core.Extractor;
import engine.core.NERExtractor;


public class Main {

	public static void main(String[] args) {

		String text = "Stanford University is located in California in the United States. "
				+ "It is a great university, founded in 1891.";
		Extractor ext = new NERExtractor();
		System.out.println(ext.getConcepts(text));
	}
}
