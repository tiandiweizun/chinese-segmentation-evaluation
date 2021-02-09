package indi.tiandi.nlp.evaluation.impl;

import indi.tiandi.nlp.Seg;
import indi.tiandi.nlp.Term;
import io.github.yizhiru.thulac4j.Segmenter;

import java.util.ArrayList;
import java.util.List;
/**
 * 清华分词
 *
 * @date 2019/3/4
 * @author tiandi
 */
public class ThulacImpl extends Seg {
    @Override
    public List<Term> segment(String sentence) {
        List<String> segment = Segmenter.segment(sentence);
        List<Term> terms=new ArrayList<>();
        for (String s : segment) {
            terms.add(new Term(s));
        }
        return terms;
    }
}
