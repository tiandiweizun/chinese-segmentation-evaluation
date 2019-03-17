package indi.tiandi.nlp.evaluation.impl;

import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.Word;
import indi.tiandi.nlp.Seg;
import indi.tiandi.nlp.Term;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class MMSeg4jImpl extends Seg {
    private static final Dictionary dic = Dictionary.getInstance();
    private static final ComplexSeg seg = new ComplexSeg(dic);
    private static final MMSeg mmSeg = new MMSeg(new StringReader(""), seg);

    @Override
    public List<Term> segment(String sentence) {
        mmSeg.reset(new StringReader(sentence));
        Word word = null;
        List<Term> terms = new ArrayList<>();
        try {
            while ((word = mmSeg.next()) != null) {
                if (word != null) {
                    terms.add(new Term(word.getString()));
                }
            }
        }
        catch (IOException e) {
            System.out.println(sentence);
            e.printStackTrace();
        }
        return terms;
    }
}
