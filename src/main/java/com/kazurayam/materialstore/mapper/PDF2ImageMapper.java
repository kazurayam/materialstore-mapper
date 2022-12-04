package com.kazurayam.materialstore.mapper;

import com.kazurayam.materialstore.core.filesystem.FileType;
import com.kazurayam.materialstore.core.filesystem.Material;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.Metadata;
import com.kazurayam.materialstore.core.filesystem.Store;
import com.kazurayam.materialstore.core.map.Mapper;
import com.kazurayam.materialstore.core.map.MappingListener;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

public final class PDF2ImageMapper implements Mapper {

    private Store store = null;
    private MappingListener listener = null;

    @Override
    public void setStore(Store store) {
        Objects.requireNonNull(store);
        this.store = store;
    }

    @Override
    public void setMappingListener(MappingListener listener) {
        this.listener = listener;
    }

    /**
     * https://www.baeldung.com/pdf-conversions-java
     */
    @Override
    public void map(Material pdfMaterial) throws MaterialstoreException {
        Objects.requireNonNull(pdfMaterial);
        assert store != null;
        assert listener != null;
        assert pdfMaterial.getFileType() == FileType.PDF;
        //
        byte[] data = store.read(pdfMaterial);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        // do data format conversion
        try {
            PDDocument document = PDDocument.load(bais);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(
                        page, 300, ImageType.RGB);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIOUtil.writeImage(
                        bim, "png", baos, 300);
                //
                Metadata metadataWithPage =
                        Metadata.builder(pdfMaterial.getMetadata())
                                .put("page", Integer.toString(page + 1))
                                .build();
                listener.onMapped(baos.toByteArray(), FileType.PNG, metadataWithPage);
            }
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }
}
