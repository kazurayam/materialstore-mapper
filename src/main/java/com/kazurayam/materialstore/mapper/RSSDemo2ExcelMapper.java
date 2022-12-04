package com.kazurayam.materialstore.mapper;

import com.kazurayam.materialstore.core.map.Mapper;
import com.rometools.rome.feed.synd.SyndFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RSSDemo2ExcelMapper extends RSS2ExcelMapper implements Mapper {

    private static final Logger logger =
            LoggerFactory.getLogger(RSSDemo2ExcelMapper.class);

    public RSSDemo2ExcelMapper() {
        super();
    }

    @Override
    String getSheetName() {
        return "Sheet1";
    }

    @Override
    List<Map<String, String>> getData(SyndFeed feed) {
        // feed argument is not used
        List<Column> columnNames = this.getColumns();
        List<Map<String, String>> grid = new ArrayList<>();
        grid.add(new HashMap<String, String>() {{
            put(columnNames.get(0).name(), "Hadrianus");
            put(columnNames.get(1).name(), "42");
        }});
        grid.add(new HashMap<String, String>() {{
            put(columnNames.get(0).name(), "Antoninus Pius");
            put(columnNames.get(1).name(), "74");
        }});
        grid.add(new HashMap<String, String>() {{
            put(columnNames.get(0).name(), "Marcus Aurelius");
            put(columnNames.get(1).name(), "58");
        }});
        return grid;
    }

    @Override
    List<Column> getColumns() {
        return Arrays.asList(
                new Column("Name", 6000),
                new Column("Age", 4000)
        );
    }

}
