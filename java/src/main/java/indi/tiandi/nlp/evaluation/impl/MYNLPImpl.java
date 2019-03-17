package indi.tiandi.nlp.evaluation.impl;

import com.mayabot.nlp.segment.Lexer;
import com.mayabot.nlp.segment.Lexers;
import com.mayabot.nlp.segment.Sentence;
import com.mayabot.nlp.segment.WordTerm;
import indi.tiandi.nlp.Seg;
import indi.tiandi.nlp.Term;

import java.util.ArrayList;
import java.util.List;

public class MYNLPImpl extends Seg {
    Lexer lexer = Lexers.builder().basic().core().keepOriCharOutput().build();

    @Override
    public List<Term> segment(String sentence) {
        Sentence result = lexer.scan(sentence);
        List<Term> terms = new ArrayList<>();
        for (WordTerm term : result.toList()) {
            terms.add(new Term(term.getWord()));
        }
        return terms;
    }
}
