package com.kazurayam.materialstore.mapper;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.materialize.URLDownloader;
import com.kazurayam.materialstore.materialize.URLMaterializer;
import com.kazurayam.materialstore.metadata.Metadata;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AmznPressRssToExcelMapperTest {

    private static URL url;
    private static Path outputDir;
    private static Store store;
    private JobName jobName;
    private JobTimestamp jobTimestamp;

    @BeforeAll
    public static void beforeAll() throws IOException {
        Path projectDir = Paths.get(System.getProperty("user.dir"));
        outputDir = projectDir.resolve("build/tmp/testOutput")
                .resolve(AmznPressRssToExcelMapperTest.class.getName());
        Files.createDirectories(outputDir);
        Path root = outputDir.resolve("store");
        store = Stores.newInstance(root);
    }

    @BeforeEach
    public void setup() {
        try {
            url = new URL("https://press.aboutamazon.com/rss/news-releases.xml");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void test_download_xml() throws IOException {
        Path tempFile = Files.createTempFile(null, null);
        URLDownloader.download(url, tempFile);
        assertTrue(Files.exists(tempFile));
        assertTrue(tempFile.toFile().length() > 0);
    }


    @Test
    void test_materialize_xml() throws IOException {
        jobName = new JobName("AmznPressRssToExcelMapperTest");
        jobTimestamp = JobTimestamp.now();
        URLMaterializer materializer = new URLMaterializer(store);
        Material material = materializer.materialize(url, jobName, jobTimestamp, FileType.XML);
        assertNotNull(material);
        Path f = material.toPath(store.getRoot());
        assertTrue(Files.exists(f));
        assertTrue(f.toFile().length() > 0);
    }


}
