package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    private final int id;
    private String description;
    private List<CloudPoint> Coordinates;

    public LandMark(int id, String description, List<CloudPoint> Coordinates) {
        this.id = id;
        this.description = description;
        this.Coordinates = Coordinates;
    }

    public int getId() { return id; }
    public String getDescription() { return description; }
    public List<CloudPoint> getCoordinates() { return Coordinates; }

    public void setDescription(String description) { this.description = description; }
    public void setCoordinates(List<CloudPoint> Coordinates) { this.Coordinates = Coordinates; }
    public void addCoordinate(CloudPoint coordinate) { Coordinates.add(coordinate); }
}
