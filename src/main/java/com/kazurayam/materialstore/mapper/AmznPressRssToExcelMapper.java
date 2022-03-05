package com.kazurayam.materialstore.mapper;

import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.map.Mapper;
import com.kazurayam.materialstore.map.MappingListener;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AmznPressRssToExcelMapper implements Mapper {

    private static final Logger logger =
            LoggerFactory.getLogger(AmznPressRssToExcelMapper.class);

    public static final String AMZN_PRESS_URL =
            "https://press.aboutamazon.com/rss/news-releases.xml";

    private Store store;
    private MappingListener listener;

    public AmznPressRssToExcelMapper() {
        store = Store.NULL_OBJECT;
        listener = MappingListener.NULL_OBJECT;
    }

    @Override
    public void setStore(Store store) {
        Objects.requireNonNull(store);
        this.store = store;
    }

    @Override
    public void setMappingListener(MappingListener listener) {
        Objects.requireNonNull(listener);
        this.listener = listener;
    }

    @Override
    public void map(Material material) throws IOException {
        Objects.requireNonNull(material);
        SyndFeed feed = getFeed(material);
        logger.info(feed.toString());
        // TODO: convert the SyndFeed object into an Excel workbook
    }

    private SyndFeed getFeed(Material material) throws IOException {
        URL feedSource = material.toURL(store.getRoot());
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = null;
        try {
            feed = input.build(new XmlReader(feedSource));
        } catch (FeedException e) {
            e.printStackTrace();
        }
        return feed;
    }

    public static void main(String[] args) throws IOException, FeedException {
        URL feedSource = new URL(AMZN_PRESS_URL);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedSource));
        System.out.println("[test_reading_an_external_feed]\n" +
                feed.toString());
    }

}
