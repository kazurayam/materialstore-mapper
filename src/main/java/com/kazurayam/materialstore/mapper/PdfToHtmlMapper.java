package com.kazurayam.materialstore.mapper;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.Store;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.fit.pdfdom.PDFDomTree;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class PdfToHtmlMapper implements Mapper {

    private Store store;

    @Override
    public void setStore(Store store) {
        Objects.requireNonNull(store);
        this.store = store;
    }

    @Override
    public MapperResult map(Material pdfMaterial) throws IOException {
        Objects.requireNonNull(pdfMaterial);
        assert pdfMaterial.getFileType() == FileType.PDF;
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
        return new MapperResult(baos.toByteArray(), FileType.HTML);
    }
}
