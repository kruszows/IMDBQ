package org.kruszows.imdbq;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.kruszows.imdbq.bktree.BKNode;
import org.kruszows.imdbq.bktree.BKTree;
import org.kruszows.imdbq.util.Cache;

import java.io.IOException;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.TreeSet;


public class WebCrawler {

    private BKTree movieTermMap = new BKTree();
    private int moviesProcessed = 0;
    private static final HashSet<String> sectionsToExcludeByWidgetId = new HashSet<>();
    private final String BASE_URL = "https://www.imdb.com/search/title/?groups=top_1000&view=simple&sort=user_rating,desc";
    private final String USER_AGENT = "Mozilla/5.0";

    static {
        sectionsToExcludeByWidgetId.add("StaticFeature_MoreLikeThis");
        sectionsToExcludeByWidgetId.add("StaticFeature_UserReviews");
        sectionsToExcludeByWidgetId.add("StaticFeature_News");
        sectionsToExcludeByWidgetId.add("StaticFeature_Contribution");
        sectionsToExcludeByWidgetId.add("StaticFeature_BoxOffice");
        sectionsToExcludeByWidgetId.add("StaticFeature_TechSpecs");
    }

    public void parseSources() throws IOException {
        HashSet<Document> listingPages = getListingPages();
        listingPages.parallelStream().forEach(page -> {
            try {
                parseListPage(page);
            }
            catch (IOException ex) {
                System.err.println("could not parse " + page.location());
            }
        });
    }

    public HashSet<Document> getListingPages() {
        HashSet<Document> listingPages = new HashSet<>();
        try {
            Document page = Jsoup.connect(BASE_URL).userAgent(USER_AGENT).get();
            listingPages.add(page);
            while (true) {
                Element nextPageLink = page.selectFirst("a.next-page");
                if (nextPageLink != null && nextPageLink.hasAttr("href")) {
                    try {
                        page = Jsoup.connect(nextPageLink.absUrl("href")).userAgent(USER_AGENT).get();
                        listingPages.add(page);
                    }
                    catch (IOException ex) {
                        System.err.println("failed to connect: " + nextPageLink.absUrl("href"));
                    }
                }
                else {
                    break;
                }
            }
        }
        catch (IOException ex) {
            System.err.println("failed to connect: " + BASE_URL);
        }
        return listingPages;
    }

    public void parseListPage(Document page) throws IOException {
        HashSet<Element> linkElements = getLinkElements(page);
        linkElements.parallelStream().forEach(link -> {
            try {
                parseMovieEntry(link);
            }
            catch (IOException ex) {
                System.err.println("could not parse " + link.text());
            }
        });
    }

    public HashSet<Element> getLinkElements(Document page) {
        HashSet<Element> linkElements = new HashSet<>();
        Element listerElement = page.selectFirst("div.lister");
        if (listerElement != null) {
            Elements listElements = listerElement.getElementsByClass("lister-item-content");
            for (Element listElement : listElements) {
                Element titleElement = listElement.selectFirst("span.lister-item-header");
                if (titleElement != null) {
                    Element linkElement = titleElement.selectFirst("a");
                    if (linkElement != null) {
                        linkElements.add(linkElement);
                    }
                }
            }
        }
        return linkElements;
    }

    public void parseMovieEntry (Element linkElement) throws IOException {
        if (linkElement.hasAttr("href")) {
            String title = linkElement.text().trim();
            String movieDetailPageUrl = linkElement.absUrl("href");
            try {
                Document movieDetailDocument = Jsoup.connect(movieDetailPageUrl).userAgent(USER_AGENT).get();
                for (Element relevantPageElement : extractMainMovieDetailElements(movieDetailDocument)) {
                    getAllPlainTextAsWords(relevantPageElement, title);
                }
                moviesProcessed++;
            }
            catch (IOException ex) {
                System.err.println("failed to connect: " + movieDetailPageUrl);
            }
        }
    }

    public HashSet<Element> extractMainMovieDetailElements(Document document) {
        HashSet<Element> elements = new HashSet<>();
        Element summarySection = document.selectFirst("div.Hero__MetaContainer__Video-kvkd64-4");
        if (summarySection == null) {
            summarySection = document.selectFirst("div.Hero__MetaContainer__NoVideo-kvkd64-8");
        }
        elements.add(summarySection);
        Element mainDetailGroup = document.selectFirst("div.TitleMainBelowTheFoldGroup__TitleMainPrimaryGroup-sc-1vpywau-1");
        if (mainDetailGroup != null) {
            Elements mainDetailGroupSections = mainDetailGroup.getElementsByTag("section");
            for (Element section : mainDetailGroupSections) {
                if (!section.hasAttr("cel_widget_id") || !sectionsToExcludeByWidgetId.contains(section.attr("cel_widget_id"))) {
                    elements.add(section);
                }
            }
        }
        return elements;
    }

    public void getAllPlainTextAsWords(Node node, String title) {
        for (Node childNode : node.childNodes()) {
            if (childNode instanceof TextNode) {
                StringTokenizer stringTokenizer = new StringTokenizer(((TextNode) childNode).text());
                while (stringTokenizer.hasMoreTokens()) {
                    String word = normalize(stringTokenizer.nextToken());
                    if (word.length() > 2) {
                        movieTermMap.add(word, title);
                    }
                }
            }
            else {
                getAllPlainTextAsWords(childNode, title);
            }
        }
    }

    public static String normalize(String input) {
        return input.replaceAll("\\p{Punct}", "").toLowerCase();
    }

    public TreeSet<String> search(String query) {
        StringTokenizer queryTokenizer = new StringTokenizer(query);
        TreeSet<String> resultSet = new TreeSet<>();
        while (queryTokenizer.hasMoreTokens()) {
            String word = normalize(queryTokenizer.nextToken());
            if (word.length() > 2) {
                if (resultSet.isEmpty()) {
                    resultSet.addAll(searchWord(word).getAssociatedTerms());
                }
                else {
                    resultSet.retainAll(searchWord(word).getAssociatedTerms());
                }
            }
        }
        return resultSet;
    }

    public BKNode searchWord(String query) {
        if (!Cache.hasQuery(query)) {
            BKNode closestMatch = movieTermMap.closestMatchSearch(query, 2);
            Cache.addQuery(query, closestMatch);
        }
        return Cache.getQuery(query);
    }

    public void printTree() {
        System.out.printf("%d movies assigned over %d terms\n", moviesProcessed, movieTermMap.size());
        System.out.println(movieTermMap.toString());
    }

}
