package lt.danielius.bakalauras.xml;

import java.util.Date;

public class InfrastructureStation implements Comparable<InfrastructureStation> {

    private String id;
    private Date lastUpdated;
    private SiteLocation location;
    private SitePricingPolicy pricingPolicy;

    public InfrastructureStation(String id, Date lastUpdated, SiteLocation location, SitePricingPolicy sitePricingPolicy) {
        this.id = id;
        this.lastUpdated = lastUpdated;
        this.location = location;
        this.pricingPolicy = sitePricingPolicy;
    }

    public String getId() {
        return id;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public SiteLocation getSiteLocation() {
        return location;
    }

    public SitePricingPolicy getSitePricingPolicy() {
        return pricingPolicy;
    }

    public int compareTo(InfrastructureStation o) {
        if(o == null)
            return 1;

        return lastUpdated.compareTo(o.lastUpdated);
    }
}
