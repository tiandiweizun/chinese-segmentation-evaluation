package indi.tiandi.nlp;

import java.util.List;

public abstract class Seg {
    private String name;

    public abstract List<Term> segment(String sentence);

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Seg(String name) {
        this.name = name;
    }

    public Seg() {
        this.name = this.getClass().getSimpleName();
    }
}
