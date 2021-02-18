package indi.tiandi.nlp.evaluation.impl;

import indi.tiandi.nlp.Seg;
import indi.tiandi.nlp.Term;
import org.ansj.domain.Result;
import org.ansj.library.AmbiguityLibrary;
import org.ansj.library.DicLibrary;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;

import java.util.ArrayList;
import java.util.List;
/**
 * Ansj分词
 *
 * @date 2019/3/4
 * @author tiandi
 */
public class AnsjImpl extends Seg {
    static {
//         设置后速度会慢25% 左右
//        MyStaticValue.ENV.put(DicLibrary.DEFAULT, AnsjImpl.class.getClassLoader().getResource("ansj/library/default.dic").getPath());
//        MyStaticValue.ENV.put(AmbiguityLibrary.DEFAULT,  AnsjImpl.class.getClassLoader().getResource("ansj/library/ambiguity.dic").getPath());
    }

    @Override
    public List<Term> segment(String sentence) {
        Result result = ToAnalysis.parse(sentence);
        List<Term> terms = new ArrayList<>();
        for (org.ansj.domain.Term term : result) {
            terms.add(new Term(term.getName()));
        }
        return terms;
    }
}
