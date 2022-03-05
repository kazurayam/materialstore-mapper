package com.kazurayam.materialstore.mapper;

import com.kazurayam.materialstore.filesystem.*;
import com.kazurayam.materialstore.map.MappedResultSerializer;
import com.kazurayam.materialstore.map.MappingListener;
import com.kazurayam.materialstore.metadata.Metadata;
import com.kazurayam.materialstore.metadata.QueryOnMetadata;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
public class Excel2CSVMapperPOI5Test {

    private static Path outputDir;
    private static Path fixtureDir;
    private Store store;

    @BeforeAll
    public static void beforeAll() throws IOException {
        Path projectDir = Paths.get(System.getProperty("user.dir"));
        outputDir = projectDir.resolve("build/tmp/testOutput")
                .resolve(Excel2CSVMapperPOI5Test.class.getName());
        Files.createDirectories(outputDir);
        //
        fixtureDir = projectDir.resolve("src/test/fixture");
        FileUtils.copyDirectory(fixtureDir.toFile(), outputDir.toFile());
    }

    @BeforeEach
    public void setup() {
        Path root = outputDir.resolve("store");
        store = Stores.newInstance(root);
    }

    @Test
    public void test_smoke() throws IOException {
        Metadata metadata =
                Metadata.builder().put("URL.host","www.fsa.go.jp").build();
        JobName jobName = new JobName("NISA");
        MaterialList materialList = store.select(jobName,
                new JobTimestamp("20220226_214458"),
                QueryOnMetadata.builder(metadata).build(),
                FileType.XLSX);
        assertEquals(1, materialList.size());
        //
        Excel2CSVMapperPOI5 mapper = new Excel2CSVMapperPOI5();
        mapper.setStore(store);
        JobTimestamp newTimestamp = JobTimestamp.now();
        MappingListener serializer =
                new MappedResultSerializer(store, jobName, newTimestamp);
        mapper.setMappingListener(serializer);
        mapper.map(materialList.get(0));
        //
        MaterialList result = store.select(jobName, newTimestamp, QueryOnMetadata.ANY);
        assertTrue(result.size() > 0);
        assertEquals(FileType.CSV, result.get(0).getFileType());
    }

}
