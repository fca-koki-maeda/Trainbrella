package com.example.trainbrella.service;

import com.example.trainbrella.model.TrainInfo;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class TrainService {

    private static final String BASE_URL = "https://transit.yahoo.co.jp";

    public TrainInfo getTrainInfo(String line) throws Exception {
        String lineUrl = findLineUrl(line);
        if (lineUrl == null) {
            return new TrainInfo(line, "路線名が見つかりません", null);
        }

        Document doc = Jsoup.connect(lineUrl)
                .userAgent("Mozilla/5.0")
                .get();

        String lineName = line;
        Element title = doc.selectFirst(".labelLarge h1.title");
        if (title != null) {
            String t = title.text().trim();
            if (!t.isEmpty()) {
                lineName = t;
            }
        }

        Element statusWrap = doc.selectFirst("#mdServiceStatus");
        if (statusWrap != null) {
            Element statusDt = statusWrap.selectFirst("dt");
            Element statusP = statusWrap.selectFirst("dd p");

            String statusText = statusDt != null ? statusDt.text().trim() : "";
            String comment = statusP != null ? statusP.text().trim() : "";

            if (!statusText.isEmpty()) {
                return new TrainInfo(lineName, statusText + (comment.isEmpty() ? "" : "：" + comment), lineUrl);
            }
        }

        Element statusElement = doc.selectFirst(".elmTblLstLine, .elmTblLine, .trouble");
        if (statusElement == null) {
            return new TrainInfo(lineName, "運行情報を取得できません", lineUrl);
        }

        String statusText = statusElement.text();

        Element pElement = doc.selectFirst(".diainfoComment p");
        String comment = pElement != null ? pElement.text() : "";

        String result;
        if (statusText.contains("平常運転")) {
            result = "平常運転" + (comment.isEmpty() ? "" : "：" + comment);
        } else if (statusText.contains("運休") || statusText.contains("遅延") || statusText.contains("見合わせ")) {
            result = statusText + (comment.isEmpty() ? "" : "：" + comment);
        } else {
            result = statusText + (comment.isEmpty() ? "" : "：" + comment);
        }

        return new TrainInfo(lineName, result, lineUrl);
    }

    public String getTrainStatus(String line) throws Exception {
        return getTrainInfo(line).status;
    }

    private String findLineUrl(String line) throws Exception {
        // ① 正しい検索URLで検索
        Document searchDoc = Jsoup.connect(BASE_URL + "/diainfo/search")
                .userAgent("Mozilla/5.0")
                .header("Accept-Language", "ja")
                .data("q", line)
                .method(Connection.Method.GET)
                .followRedirects(true)
                .get();

        // 検索結果の一番上を採用
        Element first = searchDoc.selectFirst("#mdSearchResult a[href]");
        if (first != null) {
            String href = first.attr("href");
            return href.startsWith("http") ? href : BASE_URL + href;
        }

        // ② 旧検索(/diainfo?_qr=...)フォールバック
        Document searchDocOld = Jsoup.connect(BASE_URL + "/diainfo")
                .userAgent("Mozilla/5.0")
                .header("Accept-Language", "ja")
                .data("_qr", line)
                .method(Connection.Method.GET)
                .followRedirects(true)
                .get();

        String searchUrl = extractLineUrl(searchDocOld, line);
        if (searchUrl != null) {
            return searchUrl;
        }

        // ③ エリア走査にフォールバック
        Document top = Jsoup.connect(BASE_URL + "/diainfo/")
                .userAgent("Mozilla/5.0")
                .get();

        Elements areaLinks = top.select("a[href^=/diainfo/area/]");
        for (Element areaLink : areaLinks) {
            String areaUrl = BASE_URL + areaLink.attr("href");
            Document areaDoc = Jsoup.connect(areaUrl)
                    .userAgent("Mozilla/5.0")
                    .get();

            Element lineLink = areaDoc.selectFirst("a:contains(" + line + ")");
            if (lineLink != null) {
                String href = lineLink.attr("href");
                if (!href.startsWith("http")) {
                    href = BASE_URL + href;
                }
                if (href.contains("/diainfo/line/") || href.matches(".*/diainfo/\\d+/\\d+.*")) {
                    return href;
                }
            }
        }
        return null;
    }

    private String extractLineUrl(Document doc, String line) {
        String normalizedLine = normalizeLine(line);

        String location = doc.location();
        if (location != null && (location.contains("/diainfo/line/") || location.matches(".*/diainfo/\\d+/\\d+.*"))) {
            return location;
        }

        Elements lineLinks = doc.select("a[href^=/diainfo/line/], a[href*=/diainfo/line/], a[href^=/diainfo/][href*=/diainfo/]");
        for (Element link : lineLinks) {
            String text = normalizeLine(link.text());
            if (!text.isEmpty() && text.contains(normalizedLine)) {
                String href = link.absUrl("href");
                if (href == null || href.isEmpty()) {
                    href = link.attr("href");
                    href = href.startsWith("http") ? href : BASE_URL + href;
                }
                return href;
            }
        }
        return null;
    }

    private String normalizeLine(String line) {
        if (line == null) {
            return "";
        }
        return line.replaceAll("\\s+", "")
                .replace("（", "(")
                .replace("）", ")")
                .trim();
    }
}
