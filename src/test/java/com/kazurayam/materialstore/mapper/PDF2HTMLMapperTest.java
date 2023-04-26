package com.kazurayam.materialstore.mapper;

import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Metadata;
import com.kazurayam.materialstore.core.QueryOnMetadata;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import com.kazurayam.materialstore.map.MappedResultSerializer;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PDF2HTMLMapperTest {

    private static Path outputDir;
    private Store store;

    @BeforeAll
    public static void beforeAll() throws IOException {
        Path projectDir = Paths.get(System.getProperty("user.dir"));
        outputDir = projectDir.resolve("build/tmp/testOutput")
                .resolve(PDF2HTMLMapperTest.class.getName());
        Files.createDirectories(outputDir);
        //
        Path fixturesDir = projectDir.resolve("src/test/fixtures");
        Path fixtureDir = fixturesDir.resolve("mapper");
        FileUtils.copyDirectory(fixtureDir.toFile(), outputDir.toFile());
    }

    @BeforeEach
    public void setup() {
        Path root = outputDir.resolve("store");
        store = Stores.newInstance(root);
    }

    @Test
    public void test_smoke() throws MaterialstoreException {
        Metadata metadata =
                Metadata.builder().put("URL.host","www.fsa.go.jp").build();
        JobName jobName = new JobName("NISA");
        MaterialList materialList = store.select(jobName,
                new JobTimestamp("20220226_214458"),
                FileType.PDF,
                QueryOnMetadata.builder(metadata).build());
        assertEquals(1, materialList.size());
        //
        PDF2HTMLMapper mapper = new PDF2HTMLMapper();
        mapper.setStore(store);
        JobTimestamp newTimestamp = JobTimestamp.now();
        MappedResultSerializer serializer =
                new MappedResultSerializer(store, jobName, newTimestamp);
        mapper.setMappingListener(serializer);
        mapper.map(materialList.get(0));
        //
        MaterialList result = store.select(jobName, newTimestamp, QueryOnMetadata.ANY);
        assertTrue(result.size() > 0);
        assertEquals(FileType.HTML, result.get(0).getFileType());
    }


}
