package lt.danielius.bakalauras.xml;

import java.util.List;

public class InfrastructureTable implements Comparable<InfrastructureTable> {

    private String id;
    private List<InfrastructureSite> sites;

    public InfrastructureTable(String id, List<InfrastructureSite> sites) {
        this.id = id;
        this.sites = sites;
    }

    public String getId() {
        return id;
    }

    public List<InfrastructureSite> getSites() {
        return sites;
    }

    public InfrastructureSite getSiteById(String id){
        for(InfrastructureSite site : sites){
            if(site.getId().equals(id)){
                return site;
            }
        }
        return null;
    }

    @Override
    public int compareTo(InfrastructureTable o) {
        if(o == null)
            return 1;

        for(InfrastructureSite site : sites) {
            InfrastructureSite otherSite = o.getSiteById(site.getId());
            if(otherSite == null)
                return 1;
            if(site.compareTo(otherSite) > 0){
                return 1;
            }
        }
        return 0;
    }
}
