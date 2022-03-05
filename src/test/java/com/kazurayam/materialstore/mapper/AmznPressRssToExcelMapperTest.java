package com.kazurayam.materialstore.mapper;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AmznPressRssToExcelMapperTest {

    private static URL url;
    private static Path outputDir;
    private Store store;
    private JobName jobName;
    private JobTimestamp jobTimestamp;

    @BeforeAll
    public static void beforeAll() throws IOException {
        Path projectDir = Paths.get(System.getProperty("user.dir"));
        outputDir = projectDir.resolve("build/tmp/testOutput")
                .resolve(AmznPressRssToExcelMapperTest.class.getName());
        Files.createDirectories(outputDir);
    }

    @BeforeEach
    public void setup() {
        try {
            url = new URL("https://press.aboutamazon.com/rss/news-releases.xml");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Path root = outputDir.resolve("store");
        store = Stores.newInstance(root);
        jobName = new JobName("AmznPressRssToExcelMapperTest");
        jobTimestamp = JobTimestamp.now();
    }

    @Test
    void test_download_xml() throws IOException {
        Path tempFile = Files.createTempFile(null, null);
        URLDownloader.download(url, tempFile);
        
    }


}
