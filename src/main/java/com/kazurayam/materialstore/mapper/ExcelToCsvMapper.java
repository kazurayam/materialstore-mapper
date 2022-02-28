package com.kazurayam.materialstore.mapper;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.Store;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ExcelToCsvMapper implements Mapper {

    private Store store;

    public ExcelToCsvMapper() {
        store = null;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public MapperResult map(Material excelMaterial) throws IOException {
        Objects.requireNonNull(excelMaterial);
        assert excelMaterial.getFileType() == FileType.XLSX;
        byte[] data = store.read(excelMaterial);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // do data format conversion
        Workbook workbook = new XSSFWorkbook(bais);
        Sheet sheet = workbook.getSheetAt(0);
        List<List<String>> grid = readSheet(sheet);
        writeGrid(grid, baos);
        //
        return new MapperResult(baos.toByteArray(), FileType.CSV);
    }

    private List<List<String>> readSheet(Sheet sheet) {
        List<List<String>> grid = new ArrayList<>();
        for (Row row : sheet) {
            List<String> cols = new ArrayList<>();
            for (Cell cell : row) {
                switch (cell.getCellType()) {
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

