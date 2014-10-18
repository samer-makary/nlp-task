import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import engine.core.Extractor;
import engine.core.NERExtractor;
import engine.core.WordNetExtractor;

/**
 * The class displays simple input dialogs to read question input from the user
 * and display the output.
 * 
 * @author Samer
 * 
 */
public class GUIMain {

	private static final int POS_NER = 0;
	private static final int WORD_NET = 1;

	public static void main(String[] args) {
		Map<String, Integer> algos = new HashMap<String, Integer>();
		algos.put("POS + NER", POS_NER);
		algos.put("WordNet", WORD_NET);
		String[] algosArr = new String[algos.size()];
		int i = 0;
		for (String s : algos.keySet())
			algosArr[i++] = s;

		JFrame frame = new JFrame("Algorithm Selection");
		String algo = (String) JOptionPane.showInputDialog(frame,
				"Which algorithm would like to use?", "Algorithm",
				JOptionPane.QUESTION_MESSAGE, null, algosArr, algosArr[0]);

		if (algo == null)
			System.exit(0);

		Extractor extractor = null;
		switch (algos.get(algo)) {
		case POS_NER:
			extractor = new NERExtractor();
			break;

		case WORD_NET:
			extractor = new WordNetExtractor();
			break;
		}

		if (extractor == null)
			System.exit(0);

		boolean keepRunning = true;
		do {

			String question = JOptionPane.showInputDialog(new JFrame(
					"Question Input"), "Please enter your next question:");
			keepRunning = !(question == null || question.length() == 0);
			if (keepRunning) {
				JOptionPane.showMessageDialog(new JFrame("Concepts"), extractor
						.classifyQuestionTokens(question).getConcepts());
			}
		} while (keepRunning);

		System.exit(0);

	}
}
