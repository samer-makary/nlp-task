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
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import engine.util.NamedEntityTags;
import engine.util.PartOfSpeechTags;

public class HybridExtractor extends Extractor {

	/**
	 * Factor that specifies how much above-average that token average-depth
	 * needs to be, in order for the extractor to select it as a concept.
	 */
	public static final double FACTOR_OF_AVG_DEPTH = 1.0;

	private StanfordCoreNLP annotator;
	private WordNetDatabase wnDatabase;

	public HybridExtractor() {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
		annotator = new StanfordCoreNLP(props);
		System.setProperty("wordnet.database.dir", "./dict/");
		wnDatabase = WordNetDatabase.getFileInstance();
	}
	
	public ClassifiedTokens process(String question) {

		Annotation questionAnnotations = new Annotation(question);
		annotator.annotate(questionAnnotations);
		List<CoreMap> sentences = questionAnnotations
				.get(SentencesAnnotation.class);

		List<CandidateConcept> allTokens = toCandidateConcepts(sentences);
		allTokens = groupByNE(allTokens);
		allTokens = groupByPOS(allTokens);
		double overallAvgDepth = computeAvgSynsetsDepth(allTokens);
		Set<String> concepts = new HashSet<String>();
		Set<String> notConcepts = new HashSet<String>();

		for (CandidateConcept cc : allTokens) {
			if (!cc.isSingleToken()
					|| Double.compare(cc.avgDepth, FACTOR_OF_AVG_DEPTH
							* overallAvgDepth) >= 0)
				concepts.add(cc.toString());
			else
				notConcepts.add(cc.toString());
		}

		ClassifiedTokens ct = new ClassifiedTokens();
		ct.setConcepts(concepts);
		ct.setNotConcepts(notConcepts);
		return ct;
	}

	private double computeAvgSynsetsDepth(List<CandidateConcept> allTokens) {

		double overallAvgDepth = 0.0;
		int counterAvgs = 0;
		for (CandidateConcept cc : allTokens) {
			if (!cc.isSingleToken() || valuableSingleTokenPOS(cc)) {
				Synset[] synsets = wnDatabase.getSynsets(cc.toString(),
						SynsetType.NOUN);
				if (synsets != null && synsets.length > 0) {
					overallAvgDepth += setAvgDepth(cc, synsets);
					counterAvgs++;
				}
			}
		}
		return overallAvgDepth / counterAvgs;
	}

	private double setAvgDepth(CandidateConcept cc, Synset[] synsets) {
		double depth = 0.0;
		for (Synset synset : synsets) {
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
		depth /= synsets.length;
		cc.avgDepth = depth;
		return depth;
	}

	private boolean valuableSingleTokenPOS(CandidateConcept cc) {
		PartOfSpeechTags pos = cc.getSingleTokenPOS();
		return pos != null
				&& (pos.isNoun() || pos.isVerb() || pos.isAdjective());
	}

	private List<CandidateConcept> groupByNE(List<CandidateConcept> allTokens) {
		List<CandidateConcept> groupedTokens = new ArrayList<CandidateConcept>(
				allTokens.size());
		if (allTokens.size() == 0)
			return groupedTokens;
		Iterator<CandidateConcept> itr = allTokens.iterator();
		CandidateConcept currToken = itr.next();
		while (itr.hasNext()) {
			groupedTokens.add(currToken);
			if (currToken.neTag == null) {
				currToken = itr.next();
				continue;
			}

			while (itr.hasNext()) {
				CandidateConcept nxtToken = itr.next();
				if (currToken.neTag == nxtToken.neTag) {
					currToken.tokens.addAll(nxtToken.tokens);
				} else {
					currToken = nxtToken;
					break;
				}
			}
		}
		groupedTokens.add(currToken);
		return groupedTokens;
	}

	private List<CandidateConcept> groupByPOS(List<CandidateConcept> allTokens) {
		List<CandidateConcept> groupedTokens = new ArrayList<CandidateConcept>(
				allTokens.size());
		if (allTokens.size() == 0)
			return groupedTokens;
		Iterator<CandidateConcept> itr = allTokens.iterator();
		CandidateConcept currToken = itr.next();
		while (itr.hasNext()) {
			groupedTokens.add(currToken);
			if (!currToken.isSingleToken()
					|| !valuableSingleTokenPOS(currToken)) {
				currToken = itr.next();
				continue;
			}

			while (itr.hasNext()) {
				CandidateConcept nxtToken = itr.next();
				if (nxtToken.isSingleToken()
						&& areSamePOSGroup(currToken, nxtToken)) {
					currToken.tokens.addAll(nxtToken.tokens);
				} else {
					currToken = nxtToken;
					break;
				}
			}
		}
		groupedTokens.add(currToken);
		return groupedTokens;
	}

	private boolean areSamePOSGroup(CandidateConcept currToken,
			CandidateConcept nxtToken) {
		PartOfSpeechTags pos1 = currToken.getSingleTokenPOS();
		PartOfSpeechTags pos2 = nxtToken.getSingleTokenPOS();
		if (pos1 == null || pos2 == null)
			return false;
		return (pos1.isNoun() && pos2.isNoun())
				|| (pos1.isVerb() && pos2.isVerb())
				|| (pos1.isAdjective() && pos2.isAdjective());
	}

	/**
	 * Transform the tokens of the sentences into {@link CandidateConcept}s
	 * list.
	 * 
	 * @param sentences
	 *            sentence with tokens annotated
	 * @return list of candidate concepts each holding a single label
	 */
	private List<CandidateConcept> toCandidateConcepts(List<CoreMap> sentences) {
		List<CandidateConcept> cc = new ArrayList<CandidateConcept>();
		for (CoreMap cm : sentences) {
			List<CoreLabel> labels = cm.get(TokensAnnotation.class);
			for (CoreLabel cl : labels)
				cc.add(new CandidateConcept(cl));
		}
		return cc;
	}

	/**
	 * Wrapper class that holds every token information.
	 * 
	 * @author Samer
	 * 
	 */
	private class CandidateConcept {
		double avgDepth = 0.0;
		NamedEntityTags neTag;
		List<CoreLabel> tokens = new ArrayList<CoreLabel>();

		public CandidateConcept(CoreLabel cl) {
			tokens.add(cl);
			neTag = NamedEntityTags.get(cl.get(NamedEntityTagAnnotation.class));
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (CoreLabel cl : tokens) {
				sb.append(cl.get(TextAnnotation.class));
				sb.append(' ');
			}
			return sb.toString().trim();
		}

		public boolean isSingleToken() {
			return tokens.size() == 1;
		}

		public PartOfSpeechTags getSingleTokenPOS() {
			return PartOfSpeechTags.get(tokens.get(0).get(
					PartOfSpeechAnnotation.class));
		}
	}

}
