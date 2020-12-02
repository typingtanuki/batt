package com.github.typingtanuki.batt.scrapper;

import com.github.typingtanuki.batt.battery.Maker;
import com.github.typingtanuki.batt.battery.MakerComparator;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static com.github.typingtanuki.batt.utils.CachedHttp.http;

public final class MakerListReader {
    private MakerListReader() {
        super();
    }

    public static List<Maker> extractMakers(String rootUrl) throws IOException {
        List<Maker> out = new LinkedList<>();
        Document index = http("maker", rootUrl);
        Elements makers = index.select(".categoryListBoxContents > a");
        for (Element maker : makers) {
            String name = maker.text()
                    .toLowerCase(Locale.ENGLISH)
                    .replace("batteries", "")
                    .replace("laptop", "")
                    .strip();
            String url = maker.attr("href");
            out.add(new Maker(name, url));
        }
        out.sort(new MakerComparator());
        return out;
    }
}
