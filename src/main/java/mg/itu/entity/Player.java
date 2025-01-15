package mg.itu.entity;

import org.opencv.core.Point;

public class Player {
    
    public Point position;
    public boolean isBlueTeam;

    public Player(Point position, boolean isBlueTeam) {
        this.position = position;
        this.isBlueTeam = isBlueTeam;
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
}
