package lt.danielius.bakalauras.xml;

import java.util.HashMap;

import lt.danielius.bakalauras.xml.InfrastructureTable;

public interface InfrastructureTablesCallback {

    void onParsedTables(HashMap<String, InfrastructureTable> tables);

}
