package indi.tiandi.nlp;

/**
 * 词和词性
 *
 * @date 2019/3/4
 * @author tiandi
 */
public class Term {
    /**
     * 词
     */
    private String word;
    /**
     * 词性
     */
    private String pos;

    public Term(String word) {
        this.word = word;
    }

    public Term(String word, String pos) {
        this.word = word;
        this.pos = pos;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    @Override
    public String toString() {
        return word;
    }
}
