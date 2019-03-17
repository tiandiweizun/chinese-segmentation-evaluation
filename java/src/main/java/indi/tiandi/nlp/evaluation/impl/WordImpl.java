package indi.tiandi.nlp.evaluation.impl;

import indi.tiandi.nlp.Seg;
import indi.tiandi.nlp.Term;
import org.apdplat.word.segmentation.Segmentation;
import org.apdplat.word.segmentation.SegmentationAlgorithm;
import org.apdplat.word.segmentation.SegmentationFactory;
import org.apdplat.word.segmentation.Word;

import java.util.ArrayList;
import java.util.List;

public class WordImpl extends Seg {
    public static final Segmentation segmentation = SegmentationFactory.getSegmentation(SegmentationAlgorithm.MaxNgramScore);

    @Override
    public List<Term> segment(String sentence) {
        List<Word> words = segmentation.seg(sentence);
        List<Term> terms = new ArrayList<>();
        for (Word word : words) {
            terms.add(new Term(word.getText()));
        }

        return terms;
    }
}
