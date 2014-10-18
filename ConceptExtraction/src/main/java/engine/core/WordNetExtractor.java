package engine.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import engine.util.PartOfSpeechTags;

/**
 * Initial extract based on <b>WordNet</b>. This approach still needs a lot of
 * improvement, but it should some promising results. <br/>
 * This approach tries to select tokens with the highest information content IC
 * and specificity. <br/>
 * In order to achieve that, it uses WordNet graph to extract all concepts
 * related to every token. After that it finds that depth of every token in the
 * knowledge graph. The deeper the concept, the more its IC. <br/>
 * After that it averages the depth of all token concepts, finally it selects
 * tokens with highest average depth relative to the rest of the tokens in the
 * question. <br/>
 * Final step of refinement that should some minor improvement, it groups
 * selected consecutive token that share the same POS.
 * 
 * @author Samer
 * 
 */
public class WordNetExtractor extends Extractor {

	/**
	 * Factor that specifies how much above-average that token average-depth
	 * needs to be, in order for the extractor to select it as a concept.
	 */
	public static final double FACTOR_OF_AVG_DEPTH = 1.5;

	private StanfordCoreNLP annotator;
	private WordNetDatabase wnDatabase;

	public WordNetExtractor() {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos");
		annotator = new StanfordCoreNLP(props);
		System.setProperty("wordnet.database.dir", "./dict/");
		wnDatabase = WordNetDatabase.getFileInstance();
	}

	public ClassifiedTokens process(String question) {

		Annotation questionAnnotations = new Annotation(question);
		annotator.annotate(questionAnnotations);
		List<CoreMap> sentences = questionAnnotations
				.get(SentencesAnnotation.class);

		List<TokenData> allTokens = new ArrayList<TokenData>();
		int index = 0;
		for (CoreMap sentence : sentences) {
			List<CoreLabel> coreLabels = sentence.get(TokensAnnotation.class);
			for (CoreLabel coreLabel : coreLabels) {
				String word = coreLabel.getString(TextAnnotation.class);
				PartOfSpeechTags lblPOSTag = PartOfSpeechTags.get(coreLabel
						.getString(PartOfSpeechAnnotation.class));

				if (lblPOSTag != null) {
					TokenData tokenData = new TokenData(index, lblPOSTag,
							coreLabel);
					if (lblPOSTag.isNoun() || lblPOSTag.isVerb()
							|| lblPOSTag.isAdjective()) {
						Synset[] synsets = wnDatabase.getSynsets(word,
								SynsetType.NOUN);
						if (synsets != null && synsets.length > 0) {
							tokenData = new TokenData(index, lblPOSTag,
									coreLabel, synsets);
						}
					}
					allTokens.add(tokenData);
				}
				index++;
			}
		}

		double avgAllDepth = 0.0;
		for (TokenData td : allTokens)
			avgAllDepth += td.depth;
		avgAllDepth /= allTokens.size();

		List<TokenData> selectedTokens = new ArrayList<TokenData>();
		Set<String> notConcepts = new HashSet<String>();
		for (TokenData td : allTokens) {
			if (Double.compare(td.depth, FACTOR_OF_AVG_DEPTH * avgAllDepth) >= 0)
				selectedTokens.add(td);
			else
				notConcepts.add(td.tokenLbl.get(TextAnnotation.class));
		}
		Set<String> concepts = new HashSet<String>(groupTokens(selectedTokens));

		ClassifiedTokens ct = new ClassifiedTokens();
		ct.setConcepts(concepts);
		ct.setNotConcepts(notConcepts);
		return ct;
	}

	private List<String> groupTokens(List<TokenData> selectedTokens) {
		List<String> concepts = new ArrayList<String>();
		if (selectedTokens.size() == 0)
			return concepts;
		Iterator<TokenData> itr = selectedTokens.iterator();
		TokenData currToken = itr.next();
		while (itr.hasNext()) {
			String tmp = currToken.tokenLbl.get(TextAnnotation.class);
			PartOfSpeechTags tmpPOS = currToken.pos;
			int prevIndx = currToken.index;

			while (itr.hasNext()) {
				currToken = itr.next();
				if (currToken.index == prevIndx + 1 && currToken.pos == tmpPOS) {
					tmp = tmp + " "
							+ currToken.tokenLbl.get(TextAnnotation.class);
				} else
					break;
			}

			concepts.add(tmp);
			if (!itr.hasNext()) {
				concepts.add(currToken.tokenLbl.get(TextAnnotation.class));
			}
		}
		return concepts;
	}

	/**
	 * Wrapper class that holds every token information.
	 * 
	 * @author Samer
	 * 
	 */
	private class TokenData {
		int index;
		PartOfSpeechTags pos;
		CoreLabel tokenLbl;
		Synset[] tokenSyns = null;
		double depth = 0.0;

		public TokenData(int index, PartOfSpeechTags pos, CoreLabel lbl) {
			this.index = index;
			this.pos = pos;
			tokenLbl = lbl;
		}

		public TokenData(int index, PartOfSpeechTags pos, CoreLabel lbl,
				Synset[] synsets) {
			this.index = index;
			this.pos = pos;
			tokenLbl = lbl;
			tokenSyns = synsets;
			for (Synset synset : tokenSyns) {
				boolean terminate = false;
				List<String> path = new ArrayList<String>();
				NounSynset syns = (NounSynset) synset;
				while (!terminate) {
					String word = syns.getWordForms()[0];
					path.add(word);
					NounSynset[] hyper = syns.getHypernyms();
					NounSynset[] instHyper = syns.getInstanceHypernyms();
					terminate = hyper.length == 0 && instHyper.length == 0;
					if (!terminate) {
						if (hyper.length > 0)
							syns = hyper[0];
						else
							syns = instHyper[0];
					}
				}

				depth += path.size();
			}
			depth /= tokenSyns.length;
		}

		public String toString() {
			return String.format("[%s, %s, %d, %.4f]",
					tokenLbl.get(TextAnnotation.class), pos, index, depth);
		}
	}

}
