package com.kazurayam.materialstore.mapper;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.Store;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;


import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class PdfToImageMapper implements Mapper {

    private Store store;

    private static final FileType imageType = FileType.PNG;

    @Override
    public void setStore(Store store) {
        Objects.requireNonNull(store);
        this.store = store;
    }

    /**
     * https://www.baeldung.com/pdf-conversions-java
     */
    @Override
    public MapperResult map(Material pdfMaterial) throws IOException {
        Objects.requireNonNull(pdfMaterial);
        assert pdfMaterial.getFileType() == FileType.PDF;
        byte[] data = store.read(pdfMaterial);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // do data format conversion
        PDDocument document = PDDocument.load(bais);
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        for (int page = 0; page < document.getNumberOfPages(); ++page) {
            BufferedImage bim = pdfRenderer.renderImageWithDPI(
                    page, 300, ImageType.RGB);
            ByteArrayOutputStream baos_ = new ByteArrayOutputStream();
            ImageIOUtil.writeImage(
                    bim, "png", baos_, 300);
            //
            baos = baos_;
        }

        //
        return new MapperResult(baos.toByteArray(), imageType);
    }
}
