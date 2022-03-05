package com.kazurayam.materialstore.mapper;

import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndCategoryImpl;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.SyndFeedOutput;
import com.rometools.rome.io.XmlReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * https://www.baeldung.com/rome-rss
 */
public class RomeTest {
    private SyndFeed feed;

    @BeforeEach
    void setup() {
        // 3. Creating a new RSS feed
        feed = new SyndFeedImpl();
        feed.setFeedType("rss_2.0");
        feed.setTitle("Test title");
        feed.setLink("http://www.somelink.com");
        feed.setDescription("Basic description");

        // 4. Adding an Entry
        SyndEntry entry = new SyndEntryImpl();
        entry.setTitle("Entry title");
        entry.setLink("http://www.somelink.com/entry1");
        feed.setEntries(Arrays.asList(entry));
        //System.out.println(entry.toString());

        // 5. Adding a Description
        SyndContent description = new SyndContentImpl();
        description.setType("text/html");
        description.setValue("First entry");
        entry.setDescription(description);

        // 6. Adding a Category
        List<SyndCategory> categories = new ArrayList<>();
        SyndCategory category = new SyndCategoryImpl();
        category.setName("Sophisticated category");
        entry.setCategories(categories);
    }

    @Test
    void test_publishing_the_feed()
            throws FeedException, IOException {
        Writer writer = new StringWriter();
        SyndFeedOutput syndFeedOutput = new SyndFeedOutput();
        syndFeedOutput.output(feed, writer);
        System.out.println("[test_publishing_the_feed]\n" +
                feed.toString());
    }

    @Test
    void test_reading_an_external_feed() throws IOException, FeedException {
        URL feedSource = new URL("https://press.aboutamazon.com/rss/news-releases.xml");
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedSource));
        System.out.println("[test_reading_an_external_feed]\n" +
                feed.toString());
    }
}
