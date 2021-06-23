package application;

import java.util.ArrayList;
import java.util.List;

public class Signalement {

    public int id;
    public String idGlobal;
    public String scientificName;

    public List<Double> coordinates = new ArrayList<Double>(10);
    public String geohash;

    public Double longitude;
    public Double latitude;

    public String order;
    public String superclass;
    public String recordedBy;
    public String species;

    public int date;

    public Signalement(int id, String scientificName, List<Double> coordinates)
    {
        this.id = id;
        this.scientificName = scientificName;
        this.coordinates = coordinates;
        this.longitude = this.coordinates.get(0);
        this.latitude = this.coordinates.get(1);
    }

    public Signalement(String id, String scientificName, int date, Double longitude, Double latitude)
    {
        this.idGlobal = id;
        this.scientificName = scientificName;
        this.date = date;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Signalement(String id, String scientificName, int date, Double longitude, Double latitude, String order, String superclass, String recordedBy, String species)
    {
        this.idGlobal = id;
        this.scientificName = scientificName;
        this.date = date;
        this.longitude = longitude;
        this.latitude = latitude;
        this.order = order;
        this.superclass = superclass;
        this.recordedBy = recordedBy;
        this.species = species;
    }
}
