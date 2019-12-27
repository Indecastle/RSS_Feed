package lol.kek.lab4;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.widget.ArrayAdapter;

import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class RssItem {
    private int id;
    private final String imageUrl;
    private String title;
    private String description;
    private Date pubDate;
    private String link;

    public RssItem(int id, String title, String description, Date pubDate, String link, String imageUrl) {
        this(title, description, pubDate, link, imageUrl);
        this.id = id;
    }

    public RssItem(String title, String description, Date pubDate, String link, String imageUrl) {
        this.title = title;
        this.description = description;
        this.pubDate = pubDate;
        this.link = link;
        this.imageUrl = imageUrl;
    }

    public String getTitle()
    {
        return this.title;
    }

    public String getLink()
    {
        return this.link;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public String getDescription()
    {
        return this.description;
    }

    public Date getPubDate()
    {
        return this.pubDate;
    }

    @Override
    public String toString() {

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd - hh:mm:ss");

        String result = getTitle() + "  ( " + sdf.format(this.getPubDate()) + " )";
        return result;
    }


    public static Pair<String, ArrayList<RssItem>> getRssItems(InputStream stream) {

        ArrayList<RssItem> rssItems = new ArrayList<RssItem>();
        String title = "";

        try {
            //DocumentBuilderFactory, DocumentBuilder are used for
            //xml parsing
            DocumentBuilderFactory dbf = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            //using db (Document Builder) parse xml data and assign
            //it to Element
            Document document = db.parse(stream);
            Element element = document.getDocumentElement();

            NodeList nodeChannel = element.getElementsByTagName("channel");
            Element elementChannel = (Element)nodeChannel.item(0);
            Element elementTitle = (Element)elementChannel.getElementsByTagName("title").item(0);
            title = elementTitle.getFirstChild().getNodeValue();

            //take rss nodes to NodeList
            NodeList nodeList = element.getElementsByTagName("item");

            if (nodeList.getLength() > 0) {
                for (int i = 0; i < nodeList.getLength(); i++) {

                    //take each entry (corresponds to <item></item> tags in
                    //xml data

                    Element entry = (Element) nodeList.item(i);

                    Element _titleE = (Element) entry.getElementsByTagName(
                            "title").item(0);
                    Element _descriptionE = (Element) entry
                            .getElementsByTagName("description").item(0);
                    Element _pubDateE = (Element) entry
                            .getElementsByTagName("pubDate").item(0);
                    Element _linkE = (Element) entry.getElementsByTagName(
                            "link").item(0);

                    String _title = _titleE.getFirstChild().getNodeValue();
                    Date _pubDate = new Date(_pubDateE.getFirstChild().getNodeValue());
                    String _link = _linkE.getFirstChild().getNodeValue();
                    String _description = _descriptionE.getFirstChild().getNodeValue();
                    String imageUrl = extractImageUrl(_description);
                    _description = extractOnlyText(_description);
                    //create RssItemObject and add it to the ArrayList
                    RssItem rssItem = new RssItem(_title, _description, _pubDate, _link, imageUrl);

                    rssItems.add(rssItem);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return new Pair<String, ArrayList<RssItem>>(title, rssItems);
    }


    static private String extractImageUrl(String description) {
        org.jsoup.nodes.Document document = Jsoup.parse(description);
        org.jsoup.select.Elements imgs = document.select("img");

        for (org.jsoup.nodes.Element img : imgs) {
            if (img.hasAttr("src")) {
                return img.attr("src");
            }
        }

        // no image URL
        return null;
    }

    static private String extractOnlyText(String description) {
        org.jsoup.nodes.Document document = Jsoup.parse(description);
        return document.text();
    }

    public int getId() {
        return id;
    }
}
