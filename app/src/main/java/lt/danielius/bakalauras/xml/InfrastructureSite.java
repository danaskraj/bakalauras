package lt.danielius.bakalauras.xml;

import java.util.List;

public class InfrastructureSite implements Comparable<InfrastructureSite> {

    private String id;
    private List<InfrastructureStation> stations;

    public InfrastructureSite(String id, List<InfrastructureStation> stations) {
        this.id = id;
        this.stations = stations;
    }

    public String getId() {
        return id;
    }

    public List<InfrastructureStation> getStations() {
        return stations;
    }

    public InfrastructureStation getStationById(String id){
        for(InfrastructureStation station : stations){
            if(station.getId().equals(id)){
                return station;
            }
        }
        return null;
    }

    @Override
    public int compareTo(InfrastructureSite o) {
        if(o == null)
            return 1;

        for(InfrastructureStation station : stations) {
            InfrastructureStation otherStation = o.getStationById(station.getId());
            if(otherStation == null)
                return 1;
            if(station.compareTo(otherStation) > 0){
                return 1;
            }
        }
        return 0;
    }
}
