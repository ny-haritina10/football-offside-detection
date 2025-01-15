package mg.itu.entity;

public class TeamsConfiguration {
    
    private final boolean isBlueTeamLeft; // For horizontal orientation
    private final boolean isBlueTeamTop;  // For vertical orientation

    public TeamsConfiguration(boolean isBlueTeamLeft, boolean isBlueTeamTop) {
        this.isBlueTeamLeft = isBlueTeamLeft;
        this.isBlueTeamTop = isBlueTeamTop;
    }

    public boolean isBlueTeamLeft() {
        return isBlueTeamLeft;
    }

    public boolean isBlueTeamTop() {
        return isBlueTeamTop;
    }
}