package mg.itu.entity;

import org.opencv.core.Point;

public class Player {
    
    public Point position;
    public boolean isBlueTeam;
    public boolean isOffside;  // New field

    public Player(Point position, boolean isBlueTeam) {
        this.position = position;
        this.isBlueTeam = isBlueTeam;
        this.isOffside = false;  // Initialize as not offside
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public boolean isBlueTeam() {
        return isBlueTeam;
    }

    public void setBlueTeam(boolean blueTeam) {
        isBlueTeam = blueTeam;
    }

    public boolean isOffside() {
        return isOffside;
    }

    public void setOffside(boolean offside) {
        this.isOffside = offside;
    }
}