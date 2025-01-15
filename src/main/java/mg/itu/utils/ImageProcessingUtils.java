package mg.itu.utils;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import mg.itu.algo.Algo;
import mg.itu.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ImageProcessingUtils {

    // HSV Color ranges
    private static final Scalar BLUE_HSV_MIN = new Scalar(100, 50, 50);
    private static final Scalar BLUE_HSV_MAX = new Scalar(130, 255, 255);
    private static final Scalar RED_HSV_MIN1 = new Scalar(0, 50, 50);
    private static final Scalar RED_HSV_MAX1 = new Scalar(10, 255, 255);
    private static final Scalar RED_HSV_MIN2 = new Scalar(170, 50, 50);
    private static final Scalar RED_HSV_MAX2 = new Scalar(180, 255, 255);
    private static final Scalar BALL_HSV_MIN = new Scalar(0, 0, 0);
    private static final Scalar BALL_HSV_MAX = new Scalar(180, 255, 30);
    
    private static final int MIN_PLAYER_AREA = 100;

    public static List<Player> detectPlayers(Mat image) {
        List<Player> players = new ArrayList<>();
        Mat hsv = new Mat();
        Imgproc.cvtColor(image, hsv, Imgproc.COLOR_BGR2HSV);

        Mat blueMask = new Mat();
        Core.inRange(hsv, BLUE_HSV_MIN, BLUE_HSV_MAX, blueMask);
        detectTeamPlayers(blueMask, true, players);

        Mat redMask1 = new Mat();
        Mat redMask2 = new Mat();

        Core.inRange(hsv, RED_HSV_MIN1, RED_HSV_MAX1, redMask1);
        Core.inRange(hsv, RED_HSV_MIN2, RED_HSV_MAX2, redMask2);
        
        Mat redMask = new Mat();
        Core.add(redMask1, redMask2, redMask);
        detectTeamPlayers(redMask, false, players);

        return players;
    }

    private static void detectTeamPlayers(Mat mask, boolean isBlueTeam, List<Player> players) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint contour : contours) {
            Moments moments = Imgproc.moments(contour);
            double area = Imgproc.contourArea(contour);

            if (area > MIN_PLAYER_AREA) {
                int x = (int) (moments.m10 / moments.m00);
                int y = (int) (moments.m01 / moments.m00);
                players.add(new Player(new Point(x, y), isBlueTeam));
            }
        }
    }

    public static Point detectBall(Mat image) {
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);

        Mat ballMask = new Mat();
        Core.inRange(hsvImage, BALL_HSV_MIN, BALL_HSV_MAX, ballMask);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(ballMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        if (!contours.isEmpty()) {
            Moments moments = Imgproc.moments(contours.get(0));
            int ballX = (int) (moments.m10 / moments.m00);
            int ballY = (int) (moments.m01 / moments.m00);
            return new Point(ballX, ballY);
        }
        return null;
    }

    public static Player findClosestPlayer(List<Player> players, Point ballCenter) {
        if (ballCenter == null || players.isEmpty()) return null;

        Player closestPlayer = null;
        double minDistance = Double.MAX_VALUE;

        for (Player player : players) {
            double distance = calculateDistance(ballCenter, player.position);
            if (distance < minDistance) {
                minDistance = distance;
                closestPlayer = player;
            }
        }
        
        return closestPlayer;
    }

    public static double calculateDistance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
    }

    public static Point calculateAttackPosition(Point playerPosition, Point ballPosition, 
            boolean isBlueTeam, Algo.FieldOrientation orientation) {
        double attackOffset = isBlueTeam ? Algo.ATTACK_ARROW_OFFSET : -Algo.ATTACK_ARROW_OFFSET;
        
        if (orientation == Algo.FieldOrientation.HORIZONTAL) {
            return new Point(playerPosition.x + attackOffset, playerPosition.y);
        } else {
            return new Point(playerPosition.x, playerPosition.y + attackOffset);
        }
    }
}