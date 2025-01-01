package bgu.spl.mics.application.objects;

/**
 * Represents the robot's pose (position and orientation) in the environment.
 * Includes x, y coordinates and the yaw angle relative to a global coordinate system.
 */
public class Pose {
    private float x;
    private float y;
    private float yaw;
    private int time;

    public Pose(float x, float y, float yaw, int time) {
        this.x = x;
        this.y = y;
        this.yaw = yaw;
        this.time = time;
    }
    public Pose(Pose other) {
        this.x = other.x;
        this.y = other.y;
        this.yaw = other.yaw;
        this.time = other.time;
    }

    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }

    public float getYaw() {
        return yaw;
    }

    public int getTime() {
        return time;
    }

    // Setters (optional, depending on mutability requirements)
    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Pose{" +
               "x=" + x +
               ", y=" + y +
               ", yaw=" + yaw +
               ", time=" + time +
               '}';
    }

}
