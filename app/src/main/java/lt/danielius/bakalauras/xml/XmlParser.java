package lt.danielius.bakalauras.xml;

import com.google.android.gms.maps.model.LatLng;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import lt.danielius.bakalauras.files.FileManager;

public class XmlParser extends Thread {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private InfrastructureTablesCallback callback;

    public XmlParser(InfrastructureTablesCallback callback){
        this.callback = callback;
    }

    @Override
    public void run() {
        URL url = null;
        try {
            url = new URL("https://ev.lakd.lt/publicdata/EnergyInfrastructureTablePublication");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        Document document = null;
        try {
            document = parse(url);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

        HashMap<String, InfrastructureTable> tables = null;
        try {
            tables = getTables(document);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        File file = FileManager.getFile("./station_data.xml");
        if(!file.exists()) {
            try {
                writeXmlFile(FileManager.getCreatedFile("./station_data.xml"), document);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        HashMap<String, InfrastructureTable> oldTables = null;
        try {
            oldTables = getTables(parse(file));
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        if(compareTo(tables, oldTables) > 0) {
            System.out.println("Downloaded is newer!");
            try {
                writeXmlFile(file, document);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(callback != null){
                callback.onParsedTables(tables);
            }
        }
        else {
            System.out.println("Old is good!");
            if(callback != null){
                callback.onParsedTables(oldTables);
            }
        }
    }

    private HashMap<String, InfrastructureTable> getTables(Document document) throws ParseException {
        List<Node> nodes = document.selectNodes("//egi:energyInfrastructureTable");

        HashMap<String, InfrastructureTable> tables = new HashMap<>();

        for(Node node : nodes) {
            InfrastructureTable infrastructureTable = createInfrastructureTable(node);
            tables.put(infrastructureTable.getId(), infrastructureTable);
        }

        return tables;
    }

    private int compareTo(HashMap<String, InfrastructureTable> tables, HashMap<String, InfrastructureTable> otherTables){
        if(tables.size() != otherTables.size()){
            return 1;
        }

        for(InfrastructureTable table : tables.values()){
            if(otherTables.containsKey(table.getId())){
                InfrastructureTable otherTable = otherTables.get(table.getId());
                if(otherTable == null) {
                    return 1;
                }
                if(table.compareTo(otherTable) > 0){
                    return 1;
                }
            }
            else{
                return 1;
            }
        }
        return 0;
    }

    private InfrastructureTable createInfrastructureTable(Node node) throws ParseException {
        Element element = (Element) node;
        String id = element.attribute("id").getValue();
        List<InfrastructureSite> sites = new ArrayList<>();
        for (Iterator<Element> it = element.elementIterator(); it.hasNext();) {
            sites.add(createInfrastructureSite(it.next()));
        }
        return new InfrastructureTable(id, sites);
    }

    private InfrastructureSite createInfrastructureSite(Element element) throws ParseException {
        String id = element.attribute("id").getValue();
        List<InfrastructureStation> stations = new ArrayList<>();
        for (Iterator<Element> it = element.elementIterator(); it.hasNext();) {
            stations.add(createInfrastructureStation(it.next()));
        }
        return new InfrastructureSite(id, stations);
    }

    private InfrastructureStation createInfrastructureStation(Element element) throws ParseException {
        String id = element.attribute("id").getValue();
        String date = element.selectSingleNode("egi:lastUpdated").getStringValue();
        return new InfrastructureStation(id, sdf.parse(date), createSiteLocation(element), createSitePricingPolicy(element));
    }

    private SiteLocation createSiteLocation(Element element) {
        Node node = element.selectSingleNode("egi:siteLocation/loc:pointByCoordinates/loc:pointCoordinates/loc:latitude");
        String latitude = null;
        String longitude = null;
        if(node != null) {
            latitude = node.getStringValue();
        }
        node = element.selectSingleNode("egi:siteLocation/loc:pointByCoordinates/loc:pointCoordinates/loc:longitude");
        if(node != null) {
            longitude = node.getStringValue();
        }
        LatLng location = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        return new SiteLocation(location);
    }

    private SitePricingPolicy createSitePricingPolicy(Element element) {
        Node node = element.selectSingleNode("egi:electricEnergyMix/egi:rates/fac:energyPricingPolicy/egi:pricingPolicy");
        String pricingPolicy = null;
        if(node != null) {
            pricingPolicy = node.getStringValue();
        }
        return new SitePricingPolicy(pricingPolicy);
    }

    private void writeXmlFile(File file, Document document) throws IOException {
        if(!file.exists())
            file.createNewFile();

        FileWriter fileWriter = new FileWriter(file);
        document.write(fileWriter);
        fileWriter.flush();
        fileWriter.close();
    }

    private Document parse(File file) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(file);
        return document;
    }

    private Document parse(URL url) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(url);
        return document;
    }
}
