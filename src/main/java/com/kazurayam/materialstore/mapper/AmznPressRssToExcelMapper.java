package com.kazurayam.materialstore.mapper;

import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.map.Mapper;
import com.kazurayam.materialstore.map.MappingListener;
import com.kazurayam.materialstore.metadata.Metadata;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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


public class AmznPressRssToExcelMapper implements Mapper {

    private static final Logger logger =
            LoggerFactory.getLogger(AmznPressRssToExcelMapper.class);

    public static final String AMZN_PRESS_URL =
            "https://press.aboutamazon.com/rss/news-releases.xml";

    private Store store;
    private MappingListener listener;

    public AmznPressRssToExcelMapper() {
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
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = createSheet(workbook);
        // create Header row, place it into the sheet
        Row header = createHeaderRow(sheet);
        logger.debug("header: " + header);
        // write the data rows with different style
        List<List<String>> dataGrid = getData();
        for (int i = 0; i < dataGrid.size(); i++) {
            Row data = createDataRow(sheet, i + 1, dataGrid.get(i));
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

    private Sheet createSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Sheet1");
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 4000);
        return sheet;
    }

    private Row createHeaderRow(Sheet sheet) {
        Row header = sheet.createRow(0);
        CellStyle headerCellStyle = createHeaderCellStyle(sheet.getWorkbook());
        Cell hc0 = createCell(header, 0, "Name", headerCellStyle);
        Cell hc1 = createCell(header, 1, "Age", headerCellStyle);
        logger.debug("hc0: " + hc0);
        logger.debug("hc1: " + hc1);
        return header;
    }

    private Row createDataRow(Sheet sheet, int rowIndex, List<String> rowData) {
        CellStyle style = createDataCellStyle(sheet.getWorkbook());
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < rowData.size(); i++) {
            Cell dc = createCell(row, i, rowData.get(i), style);
            logger.debug("dc0: " + dc);
        }
        return row;
    }

    private CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return cellStyle;
    }

    private CellStyle createDataCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        return style;
    }

    private Cell createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        return cell;
    }

    private List<List<String>> getData() {
        List<List<String>> grid = new ArrayList<>();
        grid.add(Arrays.asList("Hadrianus", "42"));
        grid.add(Arrays.asList("Antoninus Pius", "74"));
        grid.add(Arrays.asList("Marcus Aurelius", "58"));
        return grid;
    }

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

    public static void main(String[] args) throws IOException, FeedException {
        URL feedSource = new URL(AMZN_PRESS_URL);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedSource));
        System.out.println("[test_reading_an_external_feed]\n" + feed.toString());
    }


/* an example of SyndFeed object serialized:

SyndFeedImpl.copyright=null
SyndFeedImpl.styleSheet=null
SyndFeedImpl.link=https://press.aboutamazon.com/
SyndFeedImpl.icon=null
SyndFeedImpl.description=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.generator=null
SyndFeedImpl.foreignMarkup=[]
SyndFeedImpl.language=en
SyndFeedImpl.title=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.interface=interface com.rometools.rome.feed.synd.SyndFeed
SyndFeedImpl.preservingWireFeed=false
SyndFeedImpl.docs=null
SyndFeedImpl.links=[]
SyndFeedImpl.categories=[]
SyndFeedImpl.image=null
SyndFeedImpl.author=null
SyndFeedImpl.supportedFeedTypes[0]=rss_0.91U
SyndFeedImpl.supportedFeedTypes[1]=rss_1.0
SyndFeedImpl.supportedFeedTypes[2]=rss_2.0
SyndFeedImpl.supportedFeedTypes[3]=rss_0.93
SyndFeedImpl.supportedFeedTypes[4]=rss_0.92
SyndFeedImpl.supportedFeedTypes[5]=rss_0.94
SyndFeedImpl.supportedFeedTypes[6]=rss_0.91N
SyndFeedImpl.supportedFeedTypes[7]=rss_0.9
SyndFeedImpl.supportedFeedTypes[8]=atom_0.3
SyndFeedImpl.supportedFeedTypes[9]=atom_1.0
SyndFeedImpl.descriptionEx.mode=null
SyndFeedImpl.descriptionEx.type=null
SyndFeedImpl.descriptionEx.interface=interface com.rometools.rome.feed.synd.SyndContent
SyndFeedImpl.descriptionEx.value=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.managingEditor=null
SyndFeedImpl.encoding=null
SyndFeedImpl.webMaster=null
SyndFeedImpl.uri=null
SyndFeedImpl.modules[0].date=null
SyndFeedImpl.modules[0].formats=[]
SyndFeedImpl.modules[0].rightsList[0]=null
SyndFeedImpl.modules[0].sources=[]
SyndFeedImpl.modules[0].creators=[]
SyndFeedImpl.modules[0].subject=null
SyndFeedImpl.modules[0].description=null
SyndFeedImpl.modules[0].language=en
SyndFeedImpl.modules[0].source=null
SyndFeedImpl.modules[0].title=null
SyndFeedImpl.modules[0].type=null
SyndFeedImpl.modules[0].interface=interface com.rometools.rome.feed.module.DCModule
SyndFeedImpl.modules[0].descriptions=[]
SyndFeedImpl.modules[0].coverages=[]
SyndFeedImpl.modules[0].relation=null
SyndFeedImpl.modules[0].contributor=null
SyndFeedImpl.modules[0].rights=null
SyndFeedImpl.modules[0].publishers=[]
SyndFeedImpl.modules[0].coverage=null
SyndFeedImpl.modules[0].identifier=null
SyndFeedImpl.modules[0].creator=null
SyndFeedImpl.modules[0].types=[]
SyndFeedImpl.modules[0].languages[0]=en
SyndFeedImpl.modules[0].identifiers=[]
SyndFeedImpl.modules[0].subjects=[]
SyndFeedImpl.modules[0].format=null
SyndFeedImpl.modules[0].dates=[]
SyndFeedImpl.modules[0].titles=[]
SyndFeedImpl.modules[0].uri=http://purl.org/dc/elements/1.1/
SyndFeedImpl.modules[0].publisher=null
SyndFeedImpl.modules[0].contributors=[]
SyndFeedImpl.modules[0].relations=[]
SyndFeedImpl.entries[0].comments=null
SyndFeedImpl.entries[0].author=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[0].wireEntry=null
SyndFeedImpl.entries[0].link=https://press.aboutamazon.com/news-releases/news-release-details/amazon-travaille-en-collaboration-avec-des-ong-et-ses-employes
SyndFeedImpl.entries[0].description.mode=null
SyndFeedImpl.entries[0].description.type=text/html
SyndFeedImpl.entries[0].description.interface=interface com.rometools.rome.feed.synd.SyndContent
SyndFeedImpl.entries[0].description.value=Comme beaucoup d'entre vous à travers le monde, nous observons ce qui se passe en Ukraine avec horreur, inquiétude et cœur lourds. Bien que nous n’ayons pas d'activité commerciale directe en Ukraine, plusieurs de nos employés et partenaires sont originaires de ce pays ou entretiennent un lien
SyndFeedImpl.entries[0].foreignMarkup=[]
SyndFeedImpl.entries[0].updatedDate=null
SyndFeedImpl.entries[0].source=null
SyndFeedImpl.entries[0].title=Amazon travaille en collaboration avec des ONG et ses employés pour offrir un soutien immédiat au peuple ukrainien
SyndFeedImpl.entries[0].interface=interface com.rometools.rome.feed.synd.SyndEntry
SyndFeedImpl.entries[0].enclosures=[]
SyndFeedImpl.entries[0].uri=31591
SyndFeedImpl.entries[0].modules[0].date=Sat Mar 05 10:00:00 JST 2022
SyndFeedImpl.entries[0].modules[0].formats=[]
SyndFeedImpl.entries[0].modules[0].rightsList=[]
SyndFeedImpl.entries[0].modules[0].sources=[]
SyndFeedImpl.entries[0].modules[0].creators[0]=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[0].modules[0].subject=null
SyndFeedImpl.entries[0].modules[0].description=null
SyndFeedImpl.entries[0].modules[0].language=null
SyndFeedImpl.entries[0].modules[0].source=null
SyndFeedImpl.entries[0].modules[0].title=null
SyndFeedImpl.entries[0].modules[0].type=null
SyndFeedImpl.entries[0].modules[0].interface=interface com.rometools.rome.feed.module.DCModule
SyndFeedImpl.entries[0].modules[0].descriptions=[]
SyndFeedImpl.entries[0].modules[0].coverages=[]
SyndFeedImpl.entries[0].modules[0].relation=null
SyndFeedImpl.entries[0].modules[0].contributor=null
SyndFeedImpl.entries[0].modules[0].rights=null
SyndFeedImpl.entries[0].modules[0].publishers=[]
SyndFeedImpl.entries[0].modules[0].coverage=null
SyndFeedImpl.entries[0].modules[0].identifier=null
SyndFeedImpl.entries[0].modules[0].creator=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[0].modules[0].types=[]
SyndFeedImpl.entries[0].modules[0].languages=[]
SyndFeedImpl.entries[0].modules[0].identifiers=[]
SyndFeedImpl.entries[0].modules[0].subjects=[]
SyndFeedImpl.entries[0].modules[0].format=null
SyndFeedImpl.entries[0].modules[0].dates[0]=Sat Mar 05 10:00:00 JST 2022
SyndFeedImpl.entries[0].modules[0].titles=[]
SyndFeedImpl.entries[0].modules[0].uri=http://purl.org/dc/elements/1.1/
SyndFeedImpl.entries[0].modules[0].publisher=null
SyndFeedImpl.entries[0].modules[0].contributors=[]
SyndFeedImpl.entries[0].modules[0].relations=[]
SyndFeedImpl.entries[0].contents=[]
SyndFeedImpl.entries[0].links=[]
SyndFeedImpl.entries[0].publishedDate=Sat Mar 05 10:00:00 JST 2022
SyndFeedImpl.entries[0].contributors=[]
SyndFeedImpl.entries[0].categories=[]
SyndFeedImpl.entries[0].titleEx.mode=null
SyndFeedImpl.entries[0].titleEx.type=null
SyndFeedImpl.entries[0].titleEx.interface=interface com.rometools.rome.feed.synd.SyndContent
SyndFeedImpl.entries[0].titleEx.value=Amazon travaille en collaboration avec des ONG et ses employés pour offrir un soutien immédiat au peuple ukrainien
SyndFeedImpl.entries[0].authors=[]
SyndFeedImpl.entries[1].comments=null
SyndFeedImpl.entries[1].author=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[1].wireEntry=null
SyndFeedImpl.entries[1].link=https://press.aboutamazon.com/news-releases/news-release-details/amazon-announces-partnerships-universities-and-colleges-texas
SyndFeedImpl.entries[1].description.mode=null
SyndFeedImpl.entries[1].description.type=text/html
SyndFeedImpl.entries[1].description.interface=interface com.rometools.rome.feed.synd.SyndContent
SyndFeedImpl.entries[1].description.value=Amazon employees in the U.S. will benefit from new Career Choice partnerships with more than 140 Universities and Colleges including several colleges and universities in Texas as well as national non-profit online providers Southern New Hampshire University , Colorado State University – Global,
SyndFeedImpl.entries[1].foreignMarkup=[]
SyndFeedImpl.entries[1].updatedDate=null
SyndFeedImpl.entries[1].source=null
SyndFeedImpl.entries[1].title=Amazon Announces Partnerships with Universities and Colleges in Texas to Fully Fund Tuition for Local Hourly Employees
SyndFeedImpl.entries[1].interface=interface com.rometools.rome.feed.synd.SyndEntry
SyndFeedImpl.entries[1].enclosures=[]
SyndFeedImpl.entries[1].uri=31586
SyndFeedImpl.entries[1].modules[0].date=Fri Mar 04 02:45:00 JST 2022
SyndFeedImpl.entries[1].modules[0].formats=[]
SyndFeedImpl.entries[1].modules[0].rightsList=[]
SyndFeedImpl.entries[1].modules[0].sources=[]
SyndFeedImpl.entries[1].modules[0].creators[0]=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[1].modules[0].subject=null
SyndFeedImpl.entries[1].modules[0].description=null
SyndFeedImpl.entries[1].modules[0].language=null
SyndFeedImpl.entries[1].modules[0].source=null
SyndFeedImpl.entries[1].modules[0].title=null
SyndFeedImpl.entries[1].modules[0].type=null
SyndFeedImpl.entries[1].modules[0].interface=interface com.rometools.rome.feed.module.DCModule
SyndFeedImpl.entries[1].modules[0].descriptions=[]
SyndFeedImpl.entries[1].modules[0].coverages=[]
SyndFeedImpl.entries[1].modules[0].relation=null
SyndFeedImpl.entries[1].modules[0].contributor=null
SyndFeedImpl.entries[1].modules[0].rights=null
SyndFeedImpl.entries[1].modules[0].publishers=[]
SyndFeedImpl.entries[1].modules[0].coverage=null
SyndFeedImpl.entries[1].modules[0].identifier=null
SyndFeedImpl.entries[1].modules[0].creator=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[1].modules[0].types=[]
SyndFeedImpl.entries[1].modules[0].languages=[]
SyndFeedImpl.entries[1].modules[0].identifiers=[]
SyndFeedImpl.entries[1].modules[0].subjects=[]
SyndFeedImpl.entries[1].modules[0].format=null
SyndFeedImpl.entries[1].modules[0].dates[0]=Fri Mar 04 02:45:00 JST 2022
SyndFeedImpl.entries[1].modules[0].titles=[]
SyndFeedImpl.entries[1].modules[0].uri=http://purl.org/dc/elements/1.1/
SyndFeedImpl.entries[1].modules[0].publisher=null
SyndFeedImpl.entries[1].modules[0].contributors=[]
SyndFeedImpl.entries[1].modules[0].relations=[]
SyndFeedImpl.entries[1].contents=[]
SyndFeedImpl.entries[1].links=[]
SyndFeedImpl.entries[1].publishedDate=Fri Mar 04 02:45:00 JST 2022
SyndFeedImpl.entries[1].contributors=[]
SyndFeedImpl.entries[1].categories=[]
SyndFeedImpl.entries[1].titleEx.mode=null
SyndFeedImpl.entries[1].titleEx.type=null
SyndFeedImpl.entries[1].titleEx.interface=interface com.rometools.rome.feed.synd.SyndContent
SyndFeedImpl.entries[1].titleEx.value=Amazon Announces Partnerships with Universities and Colleges in Texas to Fully Fund Tuition for Local Hourly Employees
SyndFeedImpl.entries[1].authors=[]
SyndFeedImpl.entries[2].comments=null
SyndFeedImpl.entries[2].author=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[2].wireEntry=null
SyndFeedImpl.entries[2].link=https://press.aboutamazon.com/news-releases/news-release-details/amazon-boosts-upskilling-opportunities-hourly-employees
SyndFeedImpl.entries[2].description.mode=null
SyndFeedImpl.entries[2].description.type=text/html
SyndFeedImpl.entries[2].description.interface=interface com.rometools.rome.feed.synd.SyndContent
SyndFeedImpl.entries[2].description.value=Amazon employees in the U.S. will benefit from new Career Choice partnerships with Southern New Hampshire University , Colorado State University–Global, Western Governors University , National University , and numerous local universities Amazon also partners with GEDWorks and Smart Horizons to
SyndFeedImpl.entries[2].foreignMarkup=[]
SyndFeedImpl.entries[2].updatedDate=null
SyndFeedImpl.entries[2].source=null
SyndFeedImpl.entries[2].title=Amazon Boosts Upskilling Opportunities for Hourly Employees by Partnering with More Than 140 Universities and Colleges to Fully Fund Tuition
SyndFeedImpl.entries[2].interface=interface com.rometools.rome.feed.synd.SyndEntry
SyndFeedImpl.entries[2].enclosures=[]
SyndFeedImpl.entries[2].uri=31576
SyndFeedImpl.entries[2].modules[0].date=Thu Mar 03 22:00:00 JST 2022
SyndFeedImpl.entries[2].modules[0].formats=[]
SyndFeedImpl.entries[2].modules[0].rightsList=[]
SyndFeedImpl.entries[2].modules[0].sources=[]
SyndFeedImpl.entries[2].modules[0].creators[0]=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[2].modules[0].subject=null
SyndFeedImpl.entries[2].modules[0].description=null
SyndFeedImpl.entries[2].modules[0].language=null
SyndFeedImpl.entries[2].modules[0].source=null
SyndFeedImpl.entries[2].modules[0].title=null
SyndFeedImpl.entries[2].modules[0].type=null
SyndFeedImpl.entries[2].modules[0].interface=interface com.rometools.rome.feed.module.DCModule
SyndFeedImpl.entries[2].modules[0].descriptions=[]
SyndFeedImpl.entries[2].modules[0].coverages=[]
SyndFeedImpl.entries[2].modules[0].relation=null
SyndFeedImpl.entries[2].modules[0].contributor=null
SyndFeedImpl.entries[2].modules[0].rights=null
SyndFeedImpl.entries[2].modules[0].publishers=[]
SyndFeedImpl.entries[2].modules[0].coverage=null
SyndFeedImpl.entries[2].modules[0].identifier=null
SyndFeedImpl.entries[2].modules[0].creator=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[2].modules[0].types=[]
SyndFeedImpl.entries[2].modules[0].languages=[]
SyndFeedImpl.entries[2].modules[0].identifiers=[]
SyndFeedImpl.entries[2].modules[0].subjects=[]
SyndFeedImpl.entries[2].modules[0].format=null
SyndFeedImpl.entries[2].modules[0].dates[0]=Thu Mar 03 22:00:00 JST 2022
SyndFeedImpl.entries[2].modules[0].titles=[]
SyndFeedImpl.entries[2].modules[0].uri=http://purl.org/dc/elements/1.1/
SyndFeedImpl.entries[2].modules[0].publisher=null
SyndFeedImpl.entries[2].modules[0].contributors=[]
SyndFeedImpl.entries[2].modules[0].relations=[]
SyndFeedImpl.entries[2].contents=[]
SyndFeedImpl.entries[2].links=[]
SyndFeedImpl.entries[2].publishedDate=Thu Mar 03 22:00:00 JST 2022
SyndFeedImpl.entries[2].contributors=[]
SyndFeedImpl.entries[2].categories=[]
SyndFeedImpl.entries[2].titleEx.mode=null
SyndFeedImpl.entries[2].titleEx.type=null
SyndFeedImpl.entries[2].titleEx.interface=interface com.rometools.rome.feed.synd.SyndContent
SyndFeedImpl.entries[2].titleEx.value=Amazon Boosts Upskilling Opportunities for Hourly Employees by Partnering with More Than 140 Universities and Colleges to Fully Fund Tuition
SyndFeedImpl.entries[2].authors=[]
SyndFeedImpl.entries[3].comments=null
SyndFeedImpl.entries[3].author=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[3].wireEntry=null
SyndFeedImpl.entries[3].link=https://press.aboutamazon.com/news-releases/news-release-details/aws-and-bundesliga-debut-two-new-match-facts-giving-fans-insight
SyndFeedImpl.entries[3].description.mode=null
SyndFeedImpl.entries[3].description.type=text/html
SyndFeedImpl.entries[3].description.interface=interface com.rometools.rome.feed.synd.SyndContent
SyndFeedImpl.entries[3].description.value=The two new stats, “Set Piece Threat” and “Skill,” powered by AWS machine learning and analytics services, enhance the football fan experience and give invaluable strategic insights to teams SEATTLE --(BUSINESS WIRE)--Mar. 2, 2022-- Amazon Web Services (AWS), an Amazon.com , Inc.
SyndFeedImpl.entries[3].foreignMarkup=[]
SyndFeedImpl.entries[3].updatedDate=null
SyndFeedImpl.entries[3].source=null
SyndFeedImpl.entries[3].title=AWS and Bundesliga Debut Two New Match Facts Giving Fans Insight into Germany’s Top Football Players and Teams
SyndFeedImpl.entries[3].interface=interface com.rometools.rome.feed.synd.SyndEntry
SyndFeedImpl.entries[3].enclosures=[]
SyndFeedImpl.entries[3].uri=31571
SyndFeedImpl.entries[3].modules[0].date=Wed Mar 02 18:00:00 JST 2022
SyndFeedImpl.entries[3].modules[0].formats=[]
SyndFeedImpl.entries[3].modules[0].rightsList=[]
SyndFeedImpl.entries[3].modules[0].sources=[]
SyndFeedImpl.entries[3].modules[0].creators[0]=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[3].modules[0].subject=null
SyndFeedImpl.entries[3].modules[0].description=null
SyndFeedImpl.entries[3].modules[0].language=null
SyndFeedImpl.entries[3].modules[0].source=null
SyndFeedImpl.entries[3].modules[0].title=null
SyndFeedImpl.entries[3].modules[0].type=null
SyndFeedImpl.entries[3].modules[0].interface=interface com.rometools.rome.feed.module.DCModule
SyndFeedImpl.entries[3].modules[0].descriptions=[]
SyndFeedImpl.entries[3].modules[0].coverages=[]
SyndFeedImpl.entries[3].modules[0].relation=null
SyndFeedImpl.entries[3].modules[0].contributor=null
SyndFeedImpl.entries[3].modules[0].rights=null
SyndFeedImpl.entries[3].modules[0].publishers=[]
SyndFeedImpl.entries[3].modules[0].coverage=null
SyndFeedImpl.entries[3].modules[0].identifier=null
SyndFeedImpl.entries[3].modules[0].creator=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[3].modules[0].types=[]
SyndFeedImpl.entries[3].modules[0].languages=[]
SyndFeedImpl.entries[3].modules[0].identifiers=[]
SyndFeedImpl.entries[3].modules[0].subjects=[]
SyndFeedImpl.entries[3].modules[0].format=null
SyndFeedImpl.entries[3].modules[0].dates[0]=Wed Mar 02 18:00:00 JST 2022
SyndFeedImpl.entries[3].modules[0].titles=[]
SyndFeedImpl.entries[3].modules[0].uri=http://purl.org/dc/elements/1.1/
SyndFeedImpl.entries[3].modules[0].publisher=null
SyndFeedImpl.entries[3].modules[0].contributors=[]
SyndFeedImpl.entries[3].modules[0].relations=[]
SyndFeedImpl.entries[3].contents=[]
SyndFeedImpl.entries[3].links=[]
SyndFeedImpl.entries[3].publishedDate=Wed Mar 02 18:00:00 JST 2022
SyndFeedImpl.entries[3].contributors=[]
SyndFeedImpl.entries[3].categories=[]
SyndFeedImpl.entries[3].titleEx.mode=null
SyndFeedImpl.entries[3].titleEx.type=null
SyndFeedImpl.entries[3].titleEx.interface=interface com.rometools.rome.feed.synd.SyndContent
SyndFeedImpl.entries[3].titleEx.value=AWS and Bundesliga Debut Two New Match Facts Giving Fans Insight into Germany’s Top Football Players and Teams
SyndFeedImpl.entries[3].authors=[]
SyndFeedImpl.entries[4].comments=null
SyndFeedImpl.entries[4].author=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[4].wireEntry=null
SyndFeedImpl.entries[4].link=https://press.aboutamazon.com/news-releases/news-release-details/aws-et-la-lnh-devoilent-la-nouvelle-statistique-probabilite-de
SyndFeedImpl.entries[4].description.mode=null
SyndFeedImpl.entries[4].description.type=text/html
SyndFeedImpl.entries[4].description.interface=interface com.rometools.rome.feed.synd.SyndContent
SyndFeedImpl.entries[4].description.value=Une nouvelle statistique issue de l'apprentissage automatique permet de prédire quel joueur a le plus de chances de remporter une mise au jeu avant que la rondelle ne tombe SEATTLE – 1er mars 2022 – Amazon Web Services, Inc. (AWS), une entreprise Amazon.com, Inc.
SyndFeedImpl.entries[4].foreignMarkup=[]
SyndFeedImpl.entries[4].updatedDate=null
SyndFeedImpl.entries[4].source=null
SyndFeedImpl.entries[4].title=AWS et la LNH dévoilent la nouvelle statistique Probabilité de mise au jeu afin de rapprocher les amateurs de hockey de l'action sur la glace
SyndFeedImpl.entries[4].interface=interface com.rometools.rome.feed.synd.SyndEntry
SyndFeedImpl.entries[4].enclosures=[]
SyndFeedImpl.entries[4].uri=31551
SyndFeedImpl.entries[4].modules[0].date=Tue Mar 01 23:07:00 JST 2022
SyndFeedImpl.entries[4].modules[0].formats=[]
SyndFeedImpl.entries[4].modules[0].rightsList=[]
SyndFeedImpl.entries[4].modules[0].sources=[]
SyndFeedImpl.entries[4].modules[0].creators[0]=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[4].modules[0].subject=null
SyndFeedImpl.entries[4].modules[0].description=null
SyndFeedImpl.entries[4].modules[0].language=null
SyndFeedImpl.entries[4].modules[0].source=null
SyndFeedImpl.entries[4].modules[0].title=null
SyndFeedImpl.entries[4].modules[0].type=null
SyndFeedImpl.entries[4].modules[0].interface=interface com.rometools.rome.feed.module.DCModule
SyndFeedImpl.entries[4].modules[0].descriptions=[]
SyndFeedImpl.entries[4].modules[0].coverages=[]
SyndFeedImpl.entries[4].modules[0].relation=null
SyndFeedImpl.entries[4].modules[0].contributor=null
SyndFeedImpl.entries[4].modules[0].rights=null
SyndFeedImpl.entries[4].modules[0].publishers=[]
SyndFeedImpl.entries[4].modules[0].coverage=null
SyndFeedImpl.entries[4].modules[0].identifier=null
SyndFeedImpl.entries[4].modules[0].creator=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[4].modules[0].types=[]
SyndFeedImpl.entries[4].modules[0].languages=[]
SyndFeedImpl.entries[4].modules[0].identifiers=[]
SyndFeedImpl.entries[4].modules[0].subjects=[]
SyndFeedImpl.entries[4].modules[0].format=null
SyndFeedImpl.entries[4].modules[0].dates[0]=Tue Mar 01 23:07:00 JST 2022
SyndFeedImpl.entries[4].modules[0].titles=[]
SyndFeedImpl.entries[4].modules[0].uri=http://purl.org/dc/elements/1.1/
SyndFeedImpl.entries[4].modules[0].publisher=null
SyndFeedImpl.entries[4].modules[0].contributors=[]
SyndFeedImpl.entries[4].modules[0].relations=[]
SyndFeedImpl.entries[4].contents=[]
SyndFeedImpl.entries[4].links=[]
SyndFeedImpl.entries[4].publishedDate=Tue Mar 01 23:07:00 JST 2022
SyndFeedImpl.entries[4].contributors=[]
SyndFeedImpl.entries[4].categories=[]
SyndFeedImpl.entries[4].titleEx.mode=null
SyndFeedImpl.entries[4].titleEx.type=null
SyndFeedImpl.entries[4].titleEx.interface=interface com.rometools.rome.feed.synd.SyndContent
SyndFeedImpl.entries[4].titleEx.value=AWS et la LNH dévoilent la nouvelle statistique Probabilité de mise au jeu afin de rapprocher les amateurs de hockey de l'action sur la glace
SyndFeedImpl.entries[4].authors=[]
SyndFeedImpl.entries[5].comments=null
SyndFeedImpl.entries[5].author=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[5].wireEntry=null
SyndFeedImpl.entries[5].link=https://press.aboutamazon.com/news-releases/news-release-details/amazon-luna-cloud-gaming-service-now-available-everyone-mainland
SyndFeedImpl.entries[5].description.mode=null
SyndFeedImpl.entries[5].description.type=text/html
SyndFeedImpl.entries[5].description.interface=interface com.rometools.rome.feed.synd.SyndContent
SyndFeedImpl.entries[5].description.value=All customers in mainland United States can game with Amazon Luna on devices they already own Prime Members can try Luna for free: new Prime Gaming Channel offers rotating selection of free titles, including Immortals Fenyx Rising Introducing Retro Channel and Jackbox Games Channel, expanding
SyndFeedImpl.entries[5].foreignMarkup=[]
SyndFeedImpl.entries[5].updatedDate=null
SyndFeedImpl.entries[5].source=null
SyndFeedImpl.entries[5].title=Amazon Luna Cloud Gaming Service Now Available to Everyone in Mainland U.S. with Unique Offer for Amazon Prime Members
SyndFeedImpl.entries[5].interface=interface com.rometools.rome.feed.synd.SyndEntry
SyndFeedImpl.entries[5].enclosures=[]
SyndFeedImpl.entries[5].uri=31546
SyndFeedImpl.entries[5].modules[0].date=Tue Mar 01 23:00:00 JST 2022
SyndFeedImpl.entries[5].modules[0].formats=[]
SyndFeedImpl.entries[5].modules[0].rightsList=[]
SyndFeedImpl.entries[5].modules[0].sources=[]
SyndFeedImpl.entries[5].modules[0].creators[0]=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[5].modules[0].subject=null
SyndFeedImpl.entries[5].modules[0].description=null
SyndFeedImpl.entries[5].modules[0].language=null
SyndFeedImpl.entries[5].modules[0].source=null
SyndFeedImpl.entries[5].modules[0].title=null
SyndFeedImpl.entries[5].modules[0].type=null
SyndFeedImpl.entries[5].modules[0].interface=interface com.rometools.rome.feed.module.DCModule
SyndFeedImpl.entries[5].modules[0].descriptions=[]
SyndFeedImpl.entries[5].modules[0].coverages=[]
SyndFeedImpl.entries[5].modules[0].relation=null
SyndFeedImpl.entries[5].modules[0].contributor=null
SyndFeedImpl.entries[5].modules[0].rights=null
SyndFeedImpl.entries[5].modules[0].publishers=[]
SyndFeedImpl.entries[5].modules[0].coverage=null
SyndFeedImpl.entries[5].modules[0].identifier=null
SyndFeedImpl.entries[5].modules[0].creator=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[5].modules[0].types=[]
SyndFeedImpl.entries[5].modules[0].languages=[]
SyndFeedImpl.entries[5].modules[0].identifiers=[]
SyndFeedImpl.entries[5].modules[0].subjects=[]
SyndFeedImpl.entries[5].modules[0].format=null
SyndFeedImpl.entries[5].modules[0].dates[0]=Tue Mar 01 23:00:00 JST 2022
SyndFeedImpl.entries[5].modules[0].titles=[]
SyndFeedImpl.entries[5].modules[0].uri=http://purl.org/dc/elements/1.1/
SyndFeedImpl.entries[5].modules[0].publisher=null
SyndFeedImpl.entries[5].modules[0].contributors=[]
SyndFeedImpl.entries[5].modules[0].relations=[]
SyndFeedImpl.entries[5].contents=[]
SyndFeedImpl.entries[5].links=[]
SyndFeedImpl.entries[5].publishedDate=Tue Mar 01 23:00:00 JST 2022
SyndFeedImpl.entries[5].contributors=[]
SyndFeedImpl.entries[5].categories=[]
SyndFeedImpl.entries[5].titleEx.mode=null
SyndFeedImpl.entries[5].titleEx.type=null
SyndFeedImpl.entries[5].titleEx.interface=interface com.rometools.rome.feed.synd.SyndContent
SyndFeedImpl.entries[5].titleEx.value=Amazon Luna Cloud Gaming Service Now Available to Everyone in Mainland U.S. with Unique Offer for Amazon Prime Members
SyndFeedImpl.entries[5].authors=[]
SyndFeedImpl.entries[6].comments=null
SyndFeedImpl.entries[6].author=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[6].wireEntry=null
SyndFeedImpl.entries[6].link=https://press.aboutamazon.com/news-releases/news-release-details/aws-and-nhl-unveil-new-face-probability-stat-bring-hockey-fans
SyndFeedImpl.entries[6].description.mode=null
SyndFeedImpl.entries[6].description.type=text/html
SyndFeedImpl.entries[6].description.interface=interface com.rometools.rome.feed.synd.SyndContent
SyndFeedImpl.entries[6].description.value=New machine learning stat predicts which player is more likely to win a face-off before the puck is dropped SEATTLE --(BUSINESS WIRE)--Mar. 1, 2022-- Amazon Web Services, Inc. (AWS), an Amazon.com, Inc. company (NASDAQ: AMZN), and the National Hockey League (NHL) today announced Face-off
SyndFeedImpl.entries[6].foreignMarkup=[]
SyndFeedImpl.entries[6].updatedDate=null
SyndFeedImpl.entries[6].source=null
SyndFeedImpl.entries[6].title=AWS and the NHL Unveil New Face-off Probability Stat to Bring Hockey Fans Closer to the Action on the Ice
SyndFeedImpl.entries[6].interface=interface com.rometools.rome.feed.synd.SyndEntry
SyndFeedImpl.entries[6].enclosures=[]
SyndFeedImpl.entries[6].uri=31541
SyndFeedImpl.entries[6].modules[0].date=Tue Mar 01 17:01:00 JST 2022
SyndFeedImpl.entries[6].modules[0].formats=[]
SyndFeedImpl.entries[6].modules[0].rightsList=[]
SyndFeedImpl.entries[6].modules[0].sources=[]
SyndFeedImpl.entries[6].modules[0].creators[0]=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[6].modules[0].subject=null
SyndFeedImpl.entries[6].modules[0].description=null
SyndFeedImpl.entries[6].modules[0].language=null
SyndFeedImpl.entries[6].modules[0].source=null
SyndFeedImpl.entries[6].modules[0].title=null
SyndFeedImpl.entries[6].modules[0].type=null
SyndFeedImpl.entries[6].modules[0].interface=interface com.rometools.rome.feed.module.DCModule
SyndFeedImpl.entries[6].modules[0].descriptions=[]
SyndFeedImpl.entries[6].modules[0].coverages=[]
SyndFeedImpl.entries[6].modules[0].relation=null
SyndFeedImpl.entries[6].modules[0].contributor=null
SyndFeedImpl.entries[6].modules[0].rights=null
SyndFeedImpl.entries[6].modules[0].publishers=[]
SyndFeedImpl.entries[6].modules[0].coverage=null
SyndFeedImpl.entries[6].modules[0].identifier=null
SyndFeedImpl.entries[6].modules[0].creator=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[6].modules[0].types=[]
SyndFeedImpl.entries[6].modules[0].languages=[]
SyndFeedImpl.entries[6].modules[0].identifiers=[]
SyndFeedImpl.entries[6].modules[0].subjects=[]
SyndFeedImpl.entries[6].modules[0].format=null
SyndFeedImpl.entries[6].modules[0].dates[0]=Tue Mar 01 17:01:00 JST 2022
SyndFeedImpl.entries[6].modules[0].titles=[]
SyndFeedImpl.entries[6].modules[0].uri=http://purl.org/dc/elements/1.1/
SyndFeedImpl.entries[6].modules[0].publisher=null
SyndFeedImpl.entries[6].modules[0].contributors=[]
SyndFeedImpl.entries[6].modules[0].relations=[]
SyndFeedImpl.entries[6].contents=[]
SyndFeedImpl.entries[6].links=[]
SyndFeedImpl.entries[6].publishedDate=Tue Mar 01 17:01:00 JST 2022
SyndFeedImpl.entries[6].contributors=[]
SyndFeedImpl.entries[6].categories=[]
SyndFeedImpl.entries[6].titleEx.mode=null
SyndFeedImpl.entries[6].titleEx.type=null
SyndFeedImpl.entries[6].titleEx.interface=interface com.rometools.rome.feed.synd.SyndContent
SyndFeedImpl.entries[6].titleEx.value=AWS and the NHL Unveil New Face-off Probability Stat to Bring Hockey Fans Closer to the Action on the Ice
SyndFeedImpl.entries[6].authors=[]
SyndFeedImpl.entries[7].comments=null
SyndFeedImpl.entries[7].author=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[7].wireEntry=null
SyndFeedImpl.entries[7].link=https://press.aboutamazon.com/news-releases/news-release-details/amazon-canada-launches-new-program-teach-coding-through-music
SyndFeedImpl.entries[7].description.mode=null
SyndFeedImpl.entries[7].description.type=text/html
SyndFeedImpl.entries[7].description.interface=interface com.rometools.rome.feed.synd.SyndContent
SyndFeedImpl.entries[7].description.value=Amazon Canada partners with youth empowerment charity TakingITGlobal for a new program that builds technology skills while promoting social justice Students will remix music from Indigenous artists and enter for the chance to win $5,000 scholarships funded by Amazon Music Your Voice is Power aims
SyndFeedImpl.entries[7].foreignMarkup=[]
SyndFeedImpl.entries[7].updatedDate=null
SyndFeedImpl.entries[7].source=null
SyndFeedImpl.entries[7].title=Amazon Canada launches new program to teach coding through music from Indigenous artists
SyndFeedImpl.entries[7].interface=interface com.rometools.rome.feed.synd.SyndEntry
SyndFeedImpl.entries[7].enclosures=[]
SyndFeedImpl.entries[7].uri=31566
SyndFeedImpl.entries[7].modules[0].date=Wed Feb 23 21:00:00 JST 2022
SyndFeedImpl.entries[7].modules[0].formats=[]
SyndFeedImpl.entries[7].modules[0].rightsList=[]
SyndFeedImpl.entries[7].modules[0].sources=[]
SyndFeedImpl.entries[7].modules[0].creators[0]=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[7].modules[0].subject=null
SyndFeedImpl.entries[7].modules[0].description=null
SyndFeedImpl.entries[7].modules[0].language=null
SyndFeedImpl.entries[7].modules[0].source=null
SyndFeedImpl.entries[7].modules[0].title=null
SyndFeedImpl.entries[7].modules[0].type=null
SyndFeedImpl.entries[7].modules[0].interface=interface com.rometools.rome.feed.module.DCModule
SyndFeedImpl.entries[7].modules[0].descriptions=[]
SyndFeedImpl.entries[7].modules[0].coverages=[]
SyndFeedImpl.entries[7].modules[0].relation=null
SyndFeedImpl.entries[7].modules[0].contributor=null
SyndFeedImpl.entries[7].modules[0].rights=null
SyndFeedImpl.entries[7].modules[0].publishers=[]
SyndFeedImpl.entries[7].modules[0].coverage=null
SyndFeedImpl.entries[7].modules[0].identifier=null
SyndFeedImpl.entries[7].modules[0].creator=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[7].modules[0].types=[]
SyndFeedImpl.entries[7].modules[0].languages=[]
SyndFeedImpl.entries[7].modules[0].identifiers=[]
SyndFeedImpl.entries[7].modules[0].subjects=[]
SyndFeedImpl.entries[7].modules[0].format=null
SyndFeedImpl.entries[7].modules[0].dates[0]=Wed Feb 23 21:00:00 JST 2022
SyndFeedImpl.entries[7].modules[0].titles=[]
SyndFeedImpl.entries[7].modules[0].uri=http://purl.org/dc/elements/1.1/
SyndFeedImpl.entries[7].modules[0].publisher=null
SyndFeedImpl.entries[7].modules[0].contributors=[]
SyndFeedImpl.entries[7].modules[0].relations=[]
SyndFeedImpl.entries[7].contents=[]
SyndFeedImpl.entries[7].links=[]
SyndFeedImpl.entries[7].publishedDate=Wed Feb 23 21:00:00 JST 2022
SyndFeedImpl.entries[7].contributors=[]
SyndFeedImpl.entries[7].categories=[]
SyndFeedImpl.entries[7].titleEx.mode=null
SyndFeedImpl.entries[7].titleEx.type=null
SyndFeedImpl.entries[7].titleEx.interface=interface com.rometools.rome.feed.synd.SyndContent
SyndFeedImpl.entries[7].titleEx.value=Amazon Canada launches new program to teach coding through music from Indigenous artists
SyndFeedImpl.entries[7].authors=[]
SyndFeedImpl.entries[8].comments=null
SyndFeedImpl.entries[8].author=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[8].wireEntry=null
SyndFeedImpl.entries[8].link=https://press.aboutamazon.com/news-releases/news-release-details/amazon-canada-lance-un-nouveau-programme-qui-enseigne-le-codage
SyndFeedImpl.entries[8].description.mode=null
SyndFeedImpl.entries[8].description.type=text/html
SyndFeedImpl.entries[8].description.interface=interface com.rometools.rome.feed.synd.SyndContent
SyndFeedImpl.entries[8].description.value=Amazon Canada fait équipe avec TakingITGlobal, un organisme qui engage les jeunes activement, dans le cadre d'un nouveau programme qui permet d'apprendre des compétences technologiques tout en favorisant la justice sociale Les étudients remixeront des chansons d'artistes autochtones et courront la
SyndFeedImpl.entries[8].foreignMarkup=[]
SyndFeedImpl.entries[8].updatedDate=null
SyndFeedImpl.entries[8].source=null
SyndFeedImpl.entries[8].title=Amazon Canada lance un nouveau programme qui enseigne le codage par le biais de la musique d'artistes autochtones
SyndFeedImpl.entries[8].interface=interface com.rometools.rome.feed.synd.SyndEntry
SyndFeedImpl.entries[8].enclosures=[]
SyndFeedImpl.entries[8].uri=31531
SyndFeedImpl.entries[8].modules[0].date=Wed Feb 23 21:00:00 JST 2022
SyndFeedImpl.entries[8].modules[0].formats=[]
SyndFeedImpl.entries[8].modules[0].rightsList=[]
SyndFeedImpl.entries[8].modules[0].sources=[]
SyndFeedImpl.entries[8].modules[0].creators[0]=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[8].modules[0].subject=null
SyndFeedImpl.entries[8].modules[0].description=null
SyndFeedImpl.entries[8].modules[0].language=null
SyndFeedImpl.entries[8].modules[0].source=null
SyndFeedImpl.entries[8].modules[0].title=null
SyndFeedImpl.entries[8].modules[0].type=null
SyndFeedImpl.entries[8].modules[0].interface=interface com.rometools.rome.feed.module.DCModule
SyndFeedImpl.entries[8].modules[0].descriptions=[]
SyndFeedImpl.entries[8].modules[0].coverages=[]
SyndFeedImpl.entries[8].modules[0].relation=null
SyndFeedImpl.entries[8].modules[0].contributor=null
SyndFeedImpl.entries[8].modules[0].rights=null
SyndFeedImpl.entries[8].modules[0].publishers=[]
SyndFeedImpl.entries[8].modules[0].coverage=null
SyndFeedImpl.entries[8].modules[0].identifier=null
SyndFeedImpl.entries[8].modules[0].creator=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[8].modules[0].types=[]
SyndFeedImpl.entries[8].modules[0].languages=[]
SyndFeedImpl.entries[8].modules[0].identifiers=[]
SyndFeedImpl.entries[8].modules[0].subjects=[]
SyndFeedImpl.entries[8].modules[0].format=null
SyndFeedImpl.entries[8].modules[0].dates[0]=Wed Feb 23 21:00:00 JST 2022
SyndFeedImpl.entries[8].modules[0].titles=[]
SyndFeedImpl.entries[8].modules[0].uri=http://purl.org/dc/elements/1.1/
SyndFeedImpl.entries[8].modules[0].publisher=null
SyndFeedImpl.entries[8].modules[0].contributors=[]
SyndFeedImpl.entries[8].modules[0].relations=[]
SyndFeedImpl.entries[8].contents=[]
SyndFeedImpl.entries[8].links=[]
SyndFeedImpl.entries[8].publishedDate=Wed Feb 23 21:00:00 JST 2022
SyndFeedImpl.entries[8].contributors=[]
SyndFeedImpl.entries[8].categories=[]
SyndFeedImpl.entries[8].titleEx.mode=null
SyndFeedImpl.entries[8].titleEx.type=null
SyndFeedImpl.entries[8].titleEx.interface=interface com.rometools.rome.feed.synd.SyndContent
SyndFeedImpl.entries[8].titleEx.value=Amazon Canada lance un nouveau programme qui enseigne le codage par le biais de la musique d'artistes autochtones
SyndFeedImpl.entries[8].authors=[]
SyndFeedImpl.entries[9].comments=null
SyndFeedImpl.entries[9].author=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[9].wireEntry=null
SyndFeedImpl.entries[9].link=https://press.aboutamazon.com/news-releases/news-release-details/maple-leaf-sports-entertainment-and-aws-team-transform
SyndFeedImpl.entries[9].description.mode=null
SyndFeedImpl.entries[9].description.type=text/html
SyndFeedImpl.entries[9].description.interface=interface com.rometools.rome.feed.synd.SyndContent
SyndFeedImpl.entries[9].description.value=MLSE and AWS will innovate together to deliver more extraordinary moments for fans of Toronto’s premier hockey, basketball, soccer, and football teams, and aid scouting and coaching decisions SEATTLE --(BUSINESS WIRE)--Feb. 23, 2022-- Amazon Web Services, Inc. (AWS), an Amazon.com , Inc.
SyndFeedImpl.entries[9].foreignMarkup=[]
SyndFeedImpl.entries[9].updatedDate=null
SyndFeedImpl.entries[9].source=null
SyndFeedImpl.entries[9].title=Maple Leaf Sports & Entertainment and AWS Team Up to Transform Experiences for Canadian Sports Fans
SyndFeedImpl.entries[9].interface=interface com.rometools.rome.feed.synd.SyndEntry
SyndFeedImpl.entries[9].enclosures=[]
SyndFeedImpl.entries[9].uri=31521
SyndFeedImpl.entries[9].modules[0].date=Wed Feb 23 17:01:00 JST 2022
SyndFeedImpl.entries[9].modules[0].formats=[]
SyndFeedImpl.entries[9].modules[0].rightsList=[]
SyndFeedImpl.entries[9].modules[0].sources=[]
SyndFeedImpl.entries[9].modules[0].creators[0]=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[9].modules[0].subject=null
SyndFeedImpl.entries[9].modules[0].description=null
SyndFeedImpl.entries[9].modules[0].language=null
SyndFeedImpl.entries[9].modules[0].source=null
SyndFeedImpl.entries[9].modules[0].title=null
SyndFeedImpl.entries[9].modules[0].type=null
SyndFeedImpl.entries[9].modules[0].interface=interface com.rometools.rome.feed.module.DCModule
SyndFeedImpl.entries[9].modules[0].descriptions=[]
SyndFeedImpl.entries[9].modules[0].coverages=[]
SyndFeedImpl.entries[9].modules[0].relation=null
SyndFeedImpl.entries[9].modules[0].contributor=null
SyndFeedImpl.entries[9].modules[0].rights=null
SyndFeedImpl.entries[9].modules[0].publishers=[]
SyndFeedImpl.entries[9].modules[0].coverage=null
SyndFeedImpl.entries[9].modules[0].identifier=null
SyndFeedImpl.entries[9].modules[0].creator=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.entries[9].modules[0].types=[]
SyndFeedImpl.entries[9].modules[0].languages=[]
SyndFeedImpl.entries[9].modules[0].identifiers=[]
SyndFeedImpl.entries[9].modules[0].subjects=[]
SyndFeedImpl.entries[9].modules[0].format=null
SyndFeedImpl.entries[9].modules[0].dates[0]=Wed Feb 23 17:01:00 JST 2022
SyndFeedImpl.entries[9].modules[0].titles=[]
SyndFeedImpl.entries[9].modules[0].uri=http://purl.org/dc/elements/1.1/
SyndFeedImpl.entries[9].modules[0].publisher=null
SyndFeedImpl.entries[9].modules[0].contributors=[]
SyndFeedImpl.entries[9].modules[0].relations=[]
SyndFeedImpl.entries[9].contents=[]
SyndFeedImpl.entries[9].links=[]
SyndFeedImpl.entries[9].publishedDate=Wed Feb 23 17:01:00 JST 2022
SyndFeedImpl.entries[9].contributors=[]
SyndFeedImpl.entries[9].categories=[]
SyndFeedImpl.entries[9].titleEx.mode=null
SyndFeedImpl.entries[9].titleEx.type=null
SyndFeedImpl.entries[9].titleEx.interface=interface com.rometools.rome.feed.synd.SyndContent
SyndFeedImpl.entries[9].titleEx.value=Maple Leaf Sports & Entertainment and AWS Team Up to Transform Experiences for Canadian Sports Fans
SyndFeedImpl.entries[9].authors=[]
SyndFeedImpl.feedType=rss_2.0
SyndFeedImpl.publishedDate=null
SyndFeedImpl.contributors=[]
SyndFeedImpl.titleEx.mode=null
SyndFeedImpl.titleEx.type=null
SyndFeedImpl.titleEx.interface=interface com.rometools.rome.feed.synd.SyndContent
SyndFeedImpl.titleEx.value=Amazon.com, Inc. - Press Room News Releases
SyndFeedImpl.authors=[]

 */
}
