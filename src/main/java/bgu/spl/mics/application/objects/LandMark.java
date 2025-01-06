package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    private final String id;
    private String description;
    private List<CloudPoint> Coordinates;

    public LandMark(String id, String description) {
        this.id = id;
        this.description = description;
        this.Coordinates = new LinkedList<CloudPoint>();
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public List<CloudPoint> getCoordinates() { return Coordinates; }

    public void setDescription(String description) { this.description = description; }
    public void addCoordinate(CloudPoint coordinate) { Coordinates.add(coordinate); }
    public void setCoordinate(int index, CloudPoint coordinate) { Coordinates.set(index, coordinate); }
}
