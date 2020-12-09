package com.github.typingtanuki.batt.scrapper;

import com.github.typingtanuki.batt.battery.Maker;
import com.github.typingtanuki.batt.battery.MakerComparator;
import com.github.typingtanuki.batt.battery.Source;
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

    public static List<Maker> extractMakers(Scrapper scrapper, String rootUrl) throws IOException {
        List<Maker> out = new LinkedList<>();
        Document index = http("maker", rootUrl);
        Elements makers = index.select("#manufacturerslistContent a.manufacturerName");
        if (makers.isEmpty()) {
            makers = index.select(".categoryListBoxContents > a");
        }
        if (makers.isEmpty()) {
            makers = index.select(".laptop-brand-list > li > a");
        }
        for (Element maker : makers) {
            String name = maker.text()
                    .toLowerCase(Locale.ENGLISH)
                    .replace("batteries", "")
                    .replace("laptop", "")
                    .replace("parts", "")
                    .strip();
            String url = maker.attr("href");
            out.add(new Maker(name, new Source(url, scrapper)));
        }
        out.sort(new MakerComparator());
        return out;
    }
}
