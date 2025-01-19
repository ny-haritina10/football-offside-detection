package mg.itu.utils;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;

import mg.itu.algo.Algo;
import mg.itu.algo.Algo.FieldOrientation;
import mg.itu.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerUtils {
    
    public static Player findGoalkeeper(List<Player> players, boolean isBlueTeam, Point goalPosition) {
        return players.stream()
                .filter(player -> player.isBlueTeam == isBlueTeam)
                .min((p1, p2) -> Double.compare(
                    ImageProcessingUtils.calculateDistance(p1.position, goalPosition),
                    ImageProcessingUtils.calculateDistance(p2.position, goalPosition)))
                .orElse(null);
    }

    public static Player findLastDefender(
            List<Player> players, 
            Player playerWithBall, 
            boolean isBlueTeam, 
            Size fieldSize, 
            Algo.FieldOrientation orientation
    ) {
        Player lastDefender = null;
        double reference = isBlueTeam ? Double.MIN_VALUE : Double.MAX_VALUE;
    
        Point goalPosition = getGoalPosition(fieldSize, !isBlueTeam, orientation);
        Player goalkeeper = findGoalkeeper(players, !isBlueTeam, goalPosition);
    
        for (Player player : players) {
            if (player.isBlueTeam != isBlueTeam && player != goalkeeper) {
                double positionReference = orientation == Algo.FieldOrientation.HORIZONTAL
                    ? player.position.x
                    : player.position.y;
    
                if (isBlueTeam) {
                    if (positionReference > reference) {
                        reference = positionReference;
                        lastDefender = player;
                    }
                } else {
                    if (positionReference < reference) {
                        reference = positionReference;
                        lastDefender = player;
                    }
                }
            }
        }
    
        return lastDefender;
    }
    
    public static Player findLastDefender(
            List<Player> players, 
            boolean isBlueTeam, 
            Size fieldSize, 
            Algo.FieldOrientation orientation
    ) 
    {
        Player lastDefender = null;
        double reference = isBlueTeam ? Double.MAX_VALUE : Double.MIN_VALUE;
    
        Point goalPosition = getGoalPosition(fieldSize, isBlueTeam, orientation);
        Player goalkeeper = findGoalkeeper(players, isBlueTeam, goalPosition);
    
        for (Player player : players) {
            if (player.isBlueTeam == isBlueTeam && player != goalkeeper) {
                double positionReference = orientation == Algo.FieldOrientation.HORIZONTAL
                    ? player.position.x
                    : player.position.y;
    
                if (isBlueTeam) {
                    if (positionReference < reference) {
                        reference = positionReference;
                        lastDefender = player;
                    }
                } else {
                    if (positionReference > reference) {
                        reference = positionReference;
                        lastDefender = player;
                    }
                }
            }
        }
    
        return lastDefender;
    }

    public static List<Player> findOffsidePlayers(
            List<Player> players, 
            Player lastDefender, 
            boolean isBlueTeam, 
            Player playerWithBall, 
            Algo.FieldOrientation orientation
    ) {
        if (lastDefender == null || playerWithBall == null) {
            return new ArrayList<>();
        }

        List<Player> offsidePlayers = new ArrayList<>();
        double offsideLine = orientation == Algo.FieldOrientation.HORIZONTAL ? 
                lastDefender.position.x : lastDefender.position.y;

        for (Player player : players) {
            // offside rule adaptation
            if (player.isBlueTeam == isBlueTeam /* && player != playerWithBall */ ) {
                double playerPosition = orientation == Algo.FieldOrientation.HORIZONTAL ? 
                        player.position.x : player.position.y;
                double ballPosition = orientation == Algo.FieldOrientation.HORIZONTAL ? 
                        playerWithBall.position.x : playerWithBall.position.y;

                boolean isAheadOfOffsideLine = isBlueTeam ? 
                        playerPosition > offsideLine : playerPosition < offsideLine;
                boolean isAheadOfBall = isBlueTeam ? 
                        playerPosition > ballPosition : playerPosition < ballPosition;

                // offside rules adapation 
                if (isAheadOfOffsideLine /*&& isAheadOfBall */) {
                    offsidePlayers.add(player);
                }
            }
        }

        return offsidePlayers;
    }

    public static Point getGoalPosition(Size fieldSize, boolean isBlueTeam, Algo.FieldOrientation orientation) {
        if (orientation == Algo.FieldOrientation.HORIZONTAL) {
            return isBlueTeam 
                ? new Point(0, fieldSize.height / 2)
                : new Point(fieldSize.width, fieldSize.height / 2);
        } else {
            return isBlueTeam
                ? new Point(fieldSize.width / 2, 0)
                : new Point(fieldSize.width / 2, fieldSize.height);
        }
    }
    
    public static void detectGoalkeepers(List<Player> players, Size fieldSize, Mat image, FieldOrientation orientation) {
        if (players.isEmpty()) return;

        Point blueGoal = PlayerUtils.getGoalPosition(fieldSize, true, orientation);
        Point redGoal = PlayerUtils.getGoalPosition(fieldSize, false, orientation);

        Player blueGoalkeeper = PlayerUtils.findGoalkeeper(players, true, blueGoal);
        Player redGoalkeeper = PlayerUtils.findGoalkeeper(players, false, redGoal);

        // do something with GK if required
    }
}