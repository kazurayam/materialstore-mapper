package com.kazurayam.materialstore.mapper;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.map.Mapper;
import com.kazurayam.materialstore.map.MappingListener;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.fit.pdfdom.PDFDomTree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class PDF2HTMLMapper implements Mapper {

    private Store store;
    private MappingListener listener;

    @Override
    public void setStore(Store store) {
        Objects.requireNonNull(store);
        this.store = store;
    }

    @Override
    public void setMappingListener(MappingListener listener) {
        this.listener = listener;
    }

    @Override
    public void map(Material pdfMaterial) throws IOException {
        Objects.requireNonNull(pdfMaterial);
        assert store != null;
        assert listener != null;
        assert pdfMaterial.getFileType() == FileType.PDF;
        //
        byte[] data = store.read(pdfMaterial);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // do data format conversion
        PDDocument pdf = PDDocument.load(bais);
        Writer writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
        new PDFDomTree().writeText(pdf, writer);
        writer.flush();
        writer.close();
        //
        listener.onMapped(baos.toByteArray(), FileType.HTML,
                pdfMaterial.getMetadata());
    }
}
