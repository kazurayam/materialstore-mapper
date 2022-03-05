package com.kazurayam.materialstore.mapper;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.map.MappedResultSerializer;
import com.kazurayam.materialstore.map.MappingListener;
import com.kazurayam.materialstore.materialize.URLDownloader;
import com.kazurayam.materialstore.materialize.URLMaterializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RSSAmznPress2ExcelMapperTest {

    private static URL url;
    private static Path outputDir;
    private static Store store;
    private JobName jobName;
    private JobTimestamp jobTimestamp;

    @BeforeAll
    public static void beforeAll() throws IOException {
        Path projectDir = Paths.get(System.getProperty("user.dir"));
        outputDir = projectDir.resolve("build/tmp/testOutput")
                .resolve(RSSAmznPress2ExcelMapperTest.class.getName());
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

    @Disabled
    @Test
    void test_download_xml() throws IOException {
        Path tempFile = Files.createTempFile(null, null);
        URLDownloader.download(url, tempFile);
        assertTrue(Files.exists(tempFile));
        assertTrue(tempFile.toFile().length() > 0);
    }


    @Disabled
    @Test
    void test_materialize_xml() throws IOException {
        jobName = new JobName("test_materialize_xml");
        jobTimestamp = JobTimestamp.now();
        URLMaterializer materializer = new URLMaterializer(store);
        Material material = materializer.materialize(url, jobName, jobTimestamp, FileType.XML);
        assertNotNull(material);
        Path f = material.toPath(store.getRoot());
        assertTrue(Files.exists(f));
        assertTrue(f.toFile().length() > 0);
    }

    @Test
    void test_map() throws IOException {
        // prepare the XML file as the source material
        jobName = new JobName("test_map");
        jobTimestamp = JobTimestamp.now();
        URLMaterializer materializer = new URLMaterializer(store);
        Material rssXml = materializer.materialize(url, jobName, jobTimestamp, FileType.XML);
        //
        RSSAmznPress2ExcelMapper mapper = new RSSAmznPress2ExcelMapper();
        mapper.setStore(store);
        MappingListener serializer =
                new MappedResultSerializer(store, jobName, jobTimestamp);
        mapper.setMappingListener(serializer);
        mapper.map(rssXml);
        //
        MaterialList materialList = store.select(jobName, jobTimestamp, FileType.XLSX);
        assertEquals(1, materialList.size());
    }
}
