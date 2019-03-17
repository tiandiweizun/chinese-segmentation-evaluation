package indi.tiandi.nlp;

public class Term {
    private String word;
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
