package libs;

import org.junit.Test;
import static org.junit.Assert.*;

import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;

public class WordNetTest {

	@Test
	public void running() {
		try {
			System.setProperty("wordnet.database.dir", "./dict/");
			NounSynset nounSynset;
			NounSynset[] hyponyms;

			WordNetDatabase database = WordNetDatabase.getFileInstance();
			Synset[] synsets = database.getSynsets("fly", SynsetType.NOUN);
			for (int i = 0; i < synsets.length; i++) {
				nounSynset = (NounSynset) (synsets[i]);
				hyponyms = nounSynset.getHyponyms();
				System.out.println(nounSynset.getWordForms()[0] + ": ("
						+ nounSynset.getDefinition() + ") has " + hyponyms.length
						+ " hyponyms");
			}
		} catch (Exception e) {
			fail("WordNet is not running properly");
			e.printStackTrace();
		}
		
	}

}
