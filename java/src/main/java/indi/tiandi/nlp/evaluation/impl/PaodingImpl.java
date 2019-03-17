package indi.tiandi.nlp.evaluation.impl;
//
//import indi.nlp.Seg;
//import indi.nlp.Term;
//import org.apache.lucene.analysis.TokenStream;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class PaodingImpl implements Seg {
//    public static PaodingAnalyzer paodingAnalyzer = new PaodingAnalyzer();
//
//    @Override
//    public List<Term> segment(String sentence) {
//        List<Term> terms = new ArrayList<>();
//        try {
//            TokenStream tokenStream = paodingAnalyzer.tokenStream("", sentence);
//            System.out.println(tokenStream.toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return terms;
//    }
//
//    public static void main(String[] args) {
//new PaodingImpl().segment("我是中国人");
//    }
//}
