package mg.itu.utils;

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
}