package com.github.typingtanuki.batt.scrapper;

public class NotePartsScrapper extends AbstractScrapper {
    public NotePartsScrapper() {
        super("https://www.noteparts.com/battery/");
    }

    @Override
    public String name() {
        return "Note Parts";
    }
}
