package indi.tiandi.nlp.evaluation.impl;

import com.hankcs.hanlp.HanLP;
import indi.tiandi.nlp.Seg;
import indi.tiandi.nlp.Term;

import java.util.ArrayList;
import java.util.List;

public class HanLPImpl extends Seg {

    @Override
    public List<Term> segment(String sentence) {
//        List<com.hankcs.hanlp.seg.common.Term> segment = BasicTokenizer.segment(sentence);
        List<com.hankcs.hanlp.seg.common.Term> segment = HanLP.segment(sentence);
        List<Term> terms = new ArrayList<>();
        for (com.hankcs.hanlp.seg.common.Term term : segment) {
            terms.add(new Term(term.word));
        }
        return terms;
    }
}
