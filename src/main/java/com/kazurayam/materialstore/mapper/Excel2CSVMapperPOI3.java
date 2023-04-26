package com.kazurayam.materialstore.mapper;

import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.Material;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Metadata;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.map.Mapper;
import com.kazurayam.materialstore.map.MappingListener;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Apache POI v3.17
 */
public final class Excel2CSVMapperPOI3 implements Mapper {

    private Store store;
    private MappingListener listener;

    private final String key_sheet_index = "sheet_index";
    private final String key_sheet_name = "sheet_name";

    public Excel2CSVMapperPOI3() {
        store = null;
        listener = null;
    }

    @Override
    public void setStore(Store store) {
        this.store = store;
    }

    @Override
    public void setMappingListener(MappingListener listener) {
        this.listener = listener;
    }

    @Override
    public void map(Material excelMaterial) throws MaterialstoreException {
        Objects.requireNonNull(excelMaterial);
        assert store != null;
        assert listener != null;
        assert excelMaterial.getFileType() == FileType.XLSX;
        //
        byte[] data = store.read(excelMaterial);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // do data format conversion
        Workbook workbook;
        try {
            workbook = new XSSFWorkbook(bais);
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
        int numberOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            List<List<String>> grid = readSheet(sheet);
            try {
                writeGrid(grid, baos);
            } catch (IOException e) {
                throw new MaterialstoreException(e);
            }
            Metadata metadata =
                    Metadata.builder(excelMaterial.getMetadata())
                            .put(key_sheet_index, Integer.toString(i))
                            .put(key_sheet_name, sheet.getSheetName())
                            .build();
            listener.onMapped(baos.toByteArray(), FileType.CSV,
                    metadata);
        }
    }

    private List<List<String>> readSheet(Sheet sheet) {
        List<List<String>> grid = new ArrayList<>();
        for (Row row : sheet) {
            List<String> cols = new ArrayList<>();
            for (Cell cell : row) {
                switch (cell.getCellTypeEnum()) {
                    case STRING:
                        cols.add(cell.getRichStringCellValue().getString());
                        break;
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            cols.add(cell.getDateCellValue() + "");
                        } else {
                            cols.add(cell.getNumericCellValue() + "");
                        }
                        break;
                    case BOOLEAN:
                        cols.add(cell.getBooleanCellValue() + "");
                        break;
                    case FORMULA:
                        cols.add(cell.getCellFormula() + "");
                        break;
                    default:
                        cols.add(" ");
                }
            }
            grid.add(cols);
        }
        return grid;
    }

    private void writeGrid(List<List<String>> grid,
                                   OutputStream os) throws IOException {
        Appendable out = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.EXCEL)) {
            for (List<String> cols : grid) {
                printer.printRecord(cols);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

