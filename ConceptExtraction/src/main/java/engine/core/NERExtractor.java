package engine.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import engine.util.NamedEntityTags;

public class NERExtractor implements Extractor {

	private StanfordCoreNLP annotator;

	public NERExtractor() {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
		annotator = new StanfordCoreNLP(props);
	}

	public Set<String> getConcepts(String question) {
		if (question == null || question.length() == 0)
			throw new IllegalArgumentException("Invalid question: " + question);

		Set<String> concepts = new HashSet<String>();
		Annotation questionAnnotations = new Annotation(question);
		annotator.annotate(questionAnnotations);
		List<CoreMap> sentences = questionAnnotations
				.get(SentencesAnnotation.class);

		List<List<CoreLabel>> llcl = new ArrayList<List<CoreLabel>>();
		for (CoreMap sentence : sentences) {
			llcl.add(sentence.get(TokensAnnotation.class));
		}

		HashMap<String, HashMap<String, Integer>> entities = extractEntities(llcl);
		for (NamedEntityTags tag : NamedEntityTags.values()) {
			if (entities.containsKey(tag.toString())) {
				concepts.addAll(entities.get(tag.toString()).keySet());
			}
		}
		return concepts;
	}

	protected HashMap<String, HashMap<String, Integer>> extractEntities(
			List<List<CoreLabel>> llcl) {

		HashMap<String, HashMap<String, Integer>> entities = new HashMap<String, HashMap<String, Integer>>();

		for (List<CoreLabel> lcl : llcl) {

			Iterator<CoreLabel> iterator = lcl.iterator();

			if (!iterator.hasNext())
				continue;

			CoreLabel cl = iterator.next();

			while (iterator.hasNext()) {
				String answer = cl.getString(NamedEntityTagAnnotation.class);

				if (answer.equals("O")) {
					cl = iterator.next();
					continue;
				}

				if (!entities.containsKey(answer))
					entities.put(answer, new HashMap<String, Integer>());

				String value = cl.getString(TextAnnotation.class);

				while (iterator.hasNext()) {
					cl = iterator.next();
					if (answer.equals(cl
							.getString(NamedEntityTagAnnotation.class)))
						value = value + " "
								+ cl.getString(TextAnnotation.class);
					else {
						if (!entities.get(answer).containsKey(value))
							entities.get(answer).put(value, 0);

						entities.get(answer).put(value,
								entities.get(answer).get(value) + 1);

						break;
					}
				}

				if (!iterator.hasNext())
					break;
			}
		}

		return entities;
	}

}
