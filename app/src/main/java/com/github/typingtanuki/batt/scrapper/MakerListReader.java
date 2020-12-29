package com.github.typingtanuki.batt.scrapper;

import com.github.typingtanuki.batt.battery.Maker;
import com.github.typingtanuki.batt.battery.MakerComparator;
import com.github.typingtanuki.batt.battery.Source;
import com.github.typingtanuki.batt.exceptions.PageUnavailableException;
import com.github.typingtanuki.batt.utils.PageType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.typingtanuki.batt.utils.CachedHttp.http;

public final class MakerListReader {
    private MakerListReader() {
        super();
    }

    public static List<Maker> extractMakers(Scrapper scrapper, String rootUrl) throws IOException, PageUnavailableException {
        List<Maker> out = new LinkedList<>();
        Document index = http(PageType.MAKER, rootUrl);
        Elements makers = index.select("#manufacturerslistContent a.manufacturerName");
        if (makers.isEmpty()) {
            makers = index.select(".categoryListBoxContents > a");
        }
        if (makers.isEmpty()) {
            makers = index.select(".laptop-brand-list > li > a");
        }
        if (makers.isEmpty()) {
            makers = index.select("td[width='147'] a");
        }
        for (Element maker : makers) {
            String name = maker.text()
                    .toLowerCase(Locale.ENGLISH)
                    .replaceAll(" バッテリー\\(\\d+\\)", "")
                    .replace("batteries", "")
                    .replace("laptop", "")
                    .replace("parts", "")
                    .strip();
            String url = maker.attr("href");
            if (!url.startsWith("http")) {
                Pattern a = Pattern.compile("^(https?://[^/]+)/.*$");
                Matcher matcher = a.matcher(rootUrl);
                if (!matcher.matches()) {
                    throw new IllegalStateException("Could not extract URL from: " + rootUrl);
                }
                url = matcher.group(1) + url;
            }
            out.add(new Maker(name, new Source(url, scrapper)));
        }
        out.sort(new MakerComparator());
        return out;
    }
}
