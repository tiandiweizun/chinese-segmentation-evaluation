package indi.tiandi.nlp.evaluation.impl;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import indi.tiandi.nlp.Seg;
import indi.tiandi.nlp.Term;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class StanfordCoreNLPImpl extends Seg {
    public static StanfordCoreNLP stanfordCoreNLP;

    static {
        try {
            Properties props = new Properties();
            props.load(StanfordCoreNLPImpl.class.getClassLoader().getResourceAsStream("StanfordCoreNLP-chinese.properties"));
            props.setProperty("annotators", "tokenize,ssplit,pos");
            stanfordCoreNLP = new StanfordCoreNLP(props);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Term> segment(String sentence) {
        CoreDocument exampleDocument = new CoreDocument(sentence);
        // annotate document
        stanfordCoreNLP.annotate(exampleDocument);
        // access tokens from a CoreDocument
        // a token is represented by a CoreLabel
        List<CoreLabel> firstSentenceTokens = exampleDocument.sentences().get(0).tokens();
        // this for loop will print out all of the tokens and the character offset info
        List<Term> terms = new ArrayList<>();
        for (CoreLabel token : firstSentenceTokens) {
            terms.add(new Term(token.word(), token.tag()));
        }
        return terms;
    }
}
