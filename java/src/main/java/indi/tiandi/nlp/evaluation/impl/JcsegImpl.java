package indi.tiandi.nlp.evaluation.impl;

import indi.tiandi.nlp.Seg;
import indi.tiandi.nlp.Term;
import org.lionsoul.jcseg.tokenizer.core.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class JcsegImpl extends Seg {

    private final static JcsegTaskConfig config = new JcsegTaskConfig(true);
    private final static ADictionary dic = DictionaryFactory.createSingletonDictionary(config);
    private static ISegment seg;

    static {
        try {
            seg = SegmentFactory.createJcseg(JcsegTaskConfig.COMPLEX_MODE, new Object[]{config, dic});
        }
        catch (JcsegException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Term> segment(String sentence) {
        List<Term> terms = new ArrayList<>();
        try {
            seg.reset(new StringReader(sentence));
            IWord word = null;
            while ((word = seg.next()) != null) {
                terms.add(new Term(word.getValue()));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return terms;
    }
}
