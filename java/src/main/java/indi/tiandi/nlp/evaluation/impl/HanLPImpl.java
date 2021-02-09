package indi.tiandi.nlp.evaluation.impl;

import com.hankcs.hanlp.HanLP;
import indi.tiandi.nlp.Seg;
import indi.tiandi.nlp.Term;

import java.util.ArrayList;
import java.util.List;
/**
 * HanLP分词
 *
 * @date 2019/3/4
 * @author tiandi
 */
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
