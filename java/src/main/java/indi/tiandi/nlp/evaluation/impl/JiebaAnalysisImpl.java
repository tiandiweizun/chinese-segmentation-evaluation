package indi.tiandi.nlp.evaluation.impl;

import com.huaban.analysis.jieba.JiebaSegmenter;
import indi.tiandi.nlp.Seg;
import indi.tiandi.nlp.Term;

import java.util.ArrayList;
import java.util.List;

/**
 * JiebaAnalysisImpl
 * JiebaAnalysisImpl
 *
 * @author tiandi
 * @date 2019/3/7
 */
public class JiebaAnalysisImpl extends Seg {
    private static final JiebaSegmenter seg = new JiebaSegmenter();

    @Override
    public List<Term> segment(String sentence) {
        List<String> strings = seg.sentenceProcess(sentence);
        List<Term> terms = new ArrayList<>();
        for (String string : strings) {
            terms.add(new Term(string));
        }
        return terms;
    }
}
