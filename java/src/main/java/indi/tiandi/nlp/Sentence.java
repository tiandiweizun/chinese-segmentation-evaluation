package indi.tiandi.nlp;

import java.util.List;

/**
 * Sentence
 * Sentence
 *
 * @author 储兵兵
 * @date 2019/3/4
 */
public class Sentence {
    private List<Term> terms;

    public Sentence(List<Term> terms) {
        this.terms = terms;
    }

    public String getString() {
        StringBuilder sb = new StringBuilder();
        for (Term term : terms) {
            sb.append(term.getWord());
        }
        return sb.toString();
    }
}
