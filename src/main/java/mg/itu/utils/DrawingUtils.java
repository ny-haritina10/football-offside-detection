package mg.itu.utils;

import java.util.List;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import mg.itu.algo.Algo;
import mg.itu.entity.Player;

public class DrawingUtils {

    // Color constants
    private static final Scalar OFFSIDE_LINE_COLOR = new Scalar(0, 165, 255); // Orange in BGR
    private static final Scalar OFFSIDE_PLAYER_COLOR = new Scalar(255, 0, 255);
    
    // Line constants
    private static final int LINE_THICKNESS = 2;
    
    // Offside line constants
    private static final int DASH_LENGTH = 10;
    private static final int DASH_SPACING = 10;
    
    // Player visualization constants
    private static final int X_SIZE = 60;
    private static final int X_THICKNESS = 6;
    private static final int PLAYER_CIRCLE_RADIUS = 25; // Adjusted radius
    private static final int ADDITIONAL_OFFSET = 1;    // Fine-tuning offset

    private static final Scalar ATTACK_ARROW_COLOR = new Scalar(0, 255, 0); // Green in BGR
    private static final int ARROW_LINE_THICKNESS = 2;
    private static final int ARROW_HEAD_LENGTH = 20;
    private static final double ARROW_HEAD_ANGLE = Math.PI / 6; // 30 degrees
    private static final int RECEIVER_CIRCLE_RADIUS = 35;
    private static final int RECEIVER_CIRCLE_THICKNESS = 3;
    private static final Scalar RECEIVER_MARK_COLOR = new Scalar(0, 255, 0);

    private static final Scalar GOAL_TEXT_COLOR = new Scalar(0, 255, 0); // Green in BGR
    private static final Scalar OFFSIDE_TEXT_COLOR = new Scalar(0, 0, 255); // Red in BGR
    private static final int GOAL_TEXT_THICKNESS = 3;
    private static final int GOAL_TEXT_FONT_SCALE = 4;
    private static final Scalar GOAL_BALL_COLOR = new Scalar(0, 165, 255); // Orange in BGR
    private static final int GOAL_BALL_RADIUS = 10;

    public static void drawGoalIndicator(Mat image, Point ballPosition) {
        // Draw the ball in orange
        Imgproc.circle(image, ballPosition, GOAL_BALL_RADIUS, GOAL_BALL_COLOR, -1);
        
        // Calculate text position (centered)
        Size textSize = Imgproc.getTextSize("GOAL", Imgproc.FONT_HERSHEY_SIMPLEX, 
                                          GOAL_TEXT_FONT_SCALE, GOAL_TEXT_THICKNESS, null);
        Point textPosition = new Point(
            (image.width() - textSize.width) / 2,
            (image.height() + textSize.height) / 2
        );
        
        // Draw "GOAL" text
        Imgproc.putText(image, "GOAL", textPosition, 
                       Imgproc.FONT_HERSHEY_SIMPLEX, GOAL_TEXT_FONT_SCALE, 
                       GOAL_TEXT_COLOR, GOAL_TEXT_THICKNESS);
    }

    public static void drawOffsideIndicator(Mat image, List<Player> offsidePlayers) {
        // Draw existing offside markings
        markOffsidePlayers(image, offsidePlayers);
        
        // Add "OFFSIDE" text if there are offside players
        if (!offsidePlayers.isEmpty()) {
            Size textSize = Imgproc.getTextSize("OFFSIDE", Imgproc.FONT_HERSHEY_SIMPLEX, 
                                              GOAL_TEXT_FONT_SCALE, GOAL_TEXT_THICKNESS, null);
            Point textPosition = new Point(
                (image.width() - textSize.width) / 2,
                (image.height() + textSize.height) / 2
            );
            
            Imgproc.putText(image, "OFFSIDE", textPosition, 
                           Imgproc.FONT_HERSHEY_SIMPLEX, GOAL_TEXT_FONT_SCALE, 
                           OFFSIDE_TEXT_COLOR, GOAL_TEXT_THICKNESS);
        }
    }
    
    public static void drawLastDefenderAndOffsideLine(Mat image, Size fieldSize, Player lastDefender, 
    Scalar color, String label, Algo.FieldOrientation orientation) {
        if (lastDefender == null) return;

        // Calculate the offset position with fine-tuning
        double offsetPosition;
        if (orientation == Algo.FieldOrientation.HORIZONTAL) {
            offsetPosition = lastDefender.position.x + 
                (lastDefender.isBlueTeam ? 
                    -(PLAYER_CIRCLE_RADIUS + ADDITIONAL_OFFSET) : 
                    (PLAYER_CIRCLE_RADIUS + ADDITIONAL_OFFSET));
        } else {
            offsetPosition = lastDefender.position.y + 
                (lastDefender.isBlueTeam ? 
                    -(PLAYER_CIRCLE_RADIUS + ADDITIONAL_OFFSET) : 
                    (PLAYER_CIRCLE_RADIUS + ADDITIONAL_OFFSET));
        }

        // Draw dotted offside line at the adjusted offset position
        if (orientation == Algo.FieldOrientation.HORIZONTAL) {
            for (double y = 0; y < fieldSize.height; y += DASH_LENGTH + DASH_SPACING) {
                Point start = new Point(offsetPosition, y);
                Point end = new Point(offsetPosition, Math.min(y + DASH_LENGTH, fieldSize.height));
                Imgproc.line(image, start, end, OFFSIDE_LINE_COLOR, LINE_THICKNESS);
            }
        } else {
            for (double x = 0; x < fieldSize.width; x += DASH_LENGTH + DASH_SPACING) {
                Point start = new Point(x, offsetPosition);
                Point end = new Point(Math.min(x + DASH_LENGTH, fieldSize.width), offsetPosition);
                Imgproc.line(image, start, end, OFFSIDE_LINE_COLOR, LINE_THICKNESS);
            }
        }
        
        // Label position adjusted for better visibility
        if (label != null && !label.isEmpty()) {
            Point labelPosition = new Point(
                orientation == Algo.FieldOrientation.HORIZONTAL ? 
                    offsetPosition - 10 : lastDefender.position.x,
                orientation == Algo.FieldOrientation.HORIZONTAL ? 
                    lastDefender.position.y : offsetPosition - 10
            );
            Imgproc.putText(image, label, labelPosition, 
                           Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, color, 2);
        }
    }

    public static void markOffsidePlayers(Mat image, java.util.List<Player> offsidePlayers) {
        for (Player player : offsidePlayers) {
            // Draw X marker
            Point center = player.position;
            Point topLeft = new Point(center.x - X_SIZE/2, center.y - X_SIZE/2);
            Point topRight = new Point(center.x + X_SIZE/2, center.y - X_SIZE/2);
            Point bottomLeft = new Point(center.x - X_SIZE/2, center.y + X_SIZE/2);
            Point bottomRight = new Point(center.x + X_SIZE/2, center.y + X_SIZE/2);
            
            // Draw the X
            Imgproc.line(image, topLeft, bottomRight, OFFSIDE_PLAYER_COLOR, X_THICKNESS);
            Imgproc.line(image, topRight, bottomLeft, OFFSIDE_PLAYER_COLOR, X_THICKNESS);
        }
    }    

    public static void drawAttackArrows(Mat image, Player playerWithBall, List<Player> players, Algo.FieldOrientation orientation) {
        if (playerWithBall == null) return;
        
        for (Player player : players) {
            // Only draw arrows to teammates who are not offside and are in front of the ball
            if (player != playerWithBall && player.isBlueTeam == playerWithBall.isBlueTeam) {
                boolean isAheadOfBall = isPlayerAheadOfBall(playerWithBall, player, orientation);
                
                if (isAheadOfBall && !isPlayerOffside(player)) {
                    drawArrow(image, playerWithBall.position, player.position);
                }
            }
        }
    }

    private static boolean isPlayerOffside(Player player) 
    { return player.isOffside; }

    public static void drawAttackArrowsAndMarkReceivers(Mat image, Player playerWithBall, List<Player> players, Algo.FieldOrientation orientation) {
        if (playerWithBall == null) return;
        
        for (Player player : players) {
            // Only consider teammates who are not offside and are in front of the ball
            if (player != playerWithBall && player.isBlueTeam == playerWithBall.isBlueTeam) {
                boolean isAheadOfBall = isPlayerAheadOfBall(playerWithBall, player, orientation);
                
                if (isAheadOfBall && !player.isOffside) {
                    // Draw arrow to the player
                    drawArrow(image, playerWithBall.position, player.position);
                    // Mark the player as potential receiver
                    markReceiver(image, player);
                }
            }
        }
    }

    private static void drawArrow(Mat image, Point start, Point end) {
        // Draw the main line
        Imgproc.line(image, start, end, ATTACK_ARROW_COLOR, ARROW_LINE_THICKNESS);
        
        // Calculate arrow head points
        double angle = Math.atan2(end.y - start.y, end.x - start.x);
        
        Point arrowHead1 = new Point(
            end.x - ARROW_HEAD_LENGTH * Math.cos(angle + ARROW_HEAD_ANGLE),
            end.y - ARROW_HEAD_LENGTH * Math.sin(angle + ARROW_HEAD_ANGLE)
        );
        
        Point arrowHead2 = new Point(
            end.x - ARROW_HEAD_LENGTH * Math.cos(angle - ARROW_HEAD_ANGLE),
            end.y - ARROW_HEAD_LENGTH * Math.sin(angle - ARROW_HEAD_ANGLE)
        );
        
        // Draw arrow head
        Imgproc.line(image, end, arrowHead1, ATTACK_ARROW_COLOR, ARROW_LINE_THICKNESS);
        Imgproc.line(image, end, arrowHead2, ATTACK_ARROW_COLOR, ARROW_LINE_THICKNESS);
    }

    private static void markReceiver(Mat image, Player player) {
        // Draw a circle around the potential receiver
        Imgproc.circle(
            image, 
            player.position, 
            RECEIVER_CIRCLE_RADIUS, 
            RECEIVER_MARK_COLOR, 
            RECEIVER_CIRCLE_THICKNESS
        );
        
        // Draw a pulsing effect with a larger, thinner circle
        Imgproc.circle(
            image, 
            player.position, 
            RECEIVER_CIRCLE_RADIUS + 10, 
            RECEIVER_MARK_COLOR, 
            1
        );
    }

    private static boolean isPlayerAheadOfBall(Player playerWithBall, Player targetPlayer, Algo.FieldOrientation orientation) {
        if (orientation == Algo.FieldOrientation.HORIZONTAL) {
            return playerWithBall.isBlueTeam ? 
                targetPlayer.position.x > playerWithBall.position.x :
                targetPlayer.position.x < playerWithBall.position.x;
        } else {
            return playerWithBall.isBlueTeam ? 
                targetPlayer.position.y > playerWithBall.position.y :
                targetPlayer.position.y < playerWithBall.position.y;
        }
    }
}