package com.kazurayam.materialstore.mapper;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.map.Mapper;
import com.kazurayam.materialstore.map.MappingListener;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class RSS2ExcelMapper implements Mapper {

    private static final Logger logger =
            LoggerFactory.getLogger(RSS2ExcelMapper.class);

    protected Store store;
    protected MappingListener listener;

    public RSS2ExcelMapper() {
        store = Store.NULL_OBJECT;
        listener = MappingListener.NULL_OBJECT;
    }

    @Override
    public void setStore(Store store) {
        Objects.requireNonNull(store);
        this.store = store;
    }

    @Override
    public void setMappingListener(MappingListener listener) {
        Objects.requireNonNull(listener);
        this.listener = listener;
    }

    @Override
    public void map(Material material) throws IOException {
        Objects.requireNonNull(material);
        SyndFeed feed = getFeed(material);
        logger.debug(feed.toString());
        //
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = createSheet(workbook, getSheetName(), getColumns());
        // create Header row, place it into the sheet
        //Row header = createHeaderRow(sheet, getColumnsOfDemo());
        Row header = createHeaderRow(sheet, getColumns());
        logger.debug("header: " + header);
        // write the data rows with different style
        //List<Map<String, String>> dataGrid = getDataOfDemo();
        List<Map<String, String>> dataGrid = getData(feed);
        for (int i = 0; i < dataGrid.size(); i++) {
            Row data = createDataRow(sheet,
                    getColumns(),
                    i + 1, dataGrid.get(i));
            logger.debug("data: " + data.toString());
        }
        // let's write the content into byte[]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        // let's store the byte[] into the materialstore
        Metadata metadata =
                Metadata.builder(material.getMetadata())
                        .put("foo", "bar")
                        .build();
        assert listener != MappingListener.NULL_OBJECT;
        listener.onMapped(baos.toByteArray(), FileType.XLSX, metadata);
    }

    abstract String getSheetName();

    abstract List<Map<String, String>> getData(SyndFeed feed);

    abstract List<Column> getColumns();

    private SyndFeed getFeed(Material material) throws IOException {
        URL feedSource = material.toURL(store.getRoot());
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = null;
        try {
            feed = input.build(new XmlReader(feedSource));
        } catch (FeedException e) {
            e.printStackTrace();
        }
        return feed;
    }

    private Sheet createSheet(Workbook workbook, String sheetName, List<Column> columns) {
        Sheet sheet = workbook.createSheet(sheetName);
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            sheet.setColumnWidth(i, column.width());
        }
        return sheet;
    }

    private Row createHeaderRow(Sheet sheet, List<Column> columns) {
        Row header = sheet.createRow(0);
        CellStyle headerCellStyle = createHeaderCellStyle(sheet.getWorkbook());
        for (int i = 0; i < columns.size(); i++) {
            Cell headerCell = createCell(header, i, columns.get(i).name(), headerCellStyle);
            logger.debug("headerCell: " + headerCell);
        }
        return header;
    }

    private Cell createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        return cell;
    }


    private CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return cellStyle;
    }

    private Row createDataRow(Sheet sheet, List<Column> columns,
                              int rowIndex, Map<String, String> rowData) {
        CellStyle style = createDataCellStyle(sheet.getWorkbook());
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < rowData.size(); i++) {
            String key = columns.get(i).name();
            Cell dc = createCell(row, i, rowData.get(key), style);
            logger.debug("dc0: " + dc);
        }
        return row;
    }

    private CellStyle createDataCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        return style;
    }


    /**
     *
     */
    protected class Column {
        private String name;
        private int width;
        Column(String name, int width) {
            this.name = name;
            this.width = width;
        }
        String name() {
            return name;
        }
        int width() {
            return width;
        }
    }

}
