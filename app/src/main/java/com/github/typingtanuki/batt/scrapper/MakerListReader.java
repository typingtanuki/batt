package com.github.typingtanuki.batt.scrapper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static com.github.typingtanuki.batt.utils.CachedHttp.http;

public final class MakerListReader {
    private MakerListReader() {
        super();
    }

    public static List<String> extractMakers() throws IOException {
        List<String> out = new LinkedList<>();
        Document index = http("maker", "https://www.newlaptopaccessory.com/laptop-batteries-c-1.html");
        Elements makers = index.select(".categoryListBoxContents > a");
        for (Element maker : makers) {
            out.add(maker.attr("href"));
        }
        return out;
    }
}
