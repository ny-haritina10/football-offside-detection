package mg.itu.algo;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import mg.itu.entity.Player;

import mg.itu.utils.DrawingUtils;
import mg.itu.utils.ImageProcessingUtils;
import mg.itu.utils.PlayerUtils;

import java.io.File;
import java.util.List;

public class Algo {

    // load open cv native lib once 
    static 
    { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    // CONSTANTS
    public static final int ATTACK_ARROW_OFFSET = 100;
    public static final String IMG_FILE_PATH = "img/picture.jpg";

    public enum FieldOrientation {
        HORIZONTAL,
        VERTICAL
    }

    public static void analyze(String imagePath, boolean isReversedOrientation) 
        throws Exception 
    {
        Mat image = Imgcodecs.imread(imagePath);
        if (image.empty()) 
            throw new Exception("Failed to read input image: " + imagePath);
        
        // Process image
        FieldOrientation orientation = determineFieldOrientation(image);
        List<Player> players = ImageProcessingUtils.detectPlayers(image);
        
        Point ballCenter = ImageProcessingUtils.detectBall(image);
        
        if (players.isEmpty()) 
            throw new Exception("No players detected in the image");
        
        if (ballCenter == null) 
            throw new Exception("No ball detected in the image");
        
        // Adjust player team assignments if orientation is reversed
        if (isReversedOrientation) {
            for (Player player : players) {
                player.isBlueTeam = !player.isBlueTeam;
            }
        }
        
        Size fieldSize = new Size(image.width(), image.height());
        detectGoalkeepers(players, fieldSize, image, orientation);
        
        Player playerWithBall = ImageProcessingUtils.findClosestPlayer(players, ballCenter);
        if (playerWithBall == null) 
        { throw new Exception("Could not determine player with ball"); }
        
        Player lastDefenderBlue = PlayerUtils.findLastDefender(players, true, fieldSize, orientation);

        Player lastDefenderRed = PlayerUtils.findLastDefender(players, false, fieldSize, orientation);

        Player lastDefenderPlayer = PlayerUtils.findLastDefender(
            players, 
            playerWithBall, 
            playerWithBall.isBlueTeam, 
            fieldSize, 
            orientation
        );

        if (playerWithBall.isBlueTeam) {
            DrawingUtils.drawLastDefenderAndOffsideLine(
                image, 
                fieldSize, 
                lastDefenderRed, 
                new Scalar(255, 255, 0), 
                "", 
                orientation
            );  
        }

        else {
            DrawingUtils.drawLastDefenderAndOffsideLine(
                image, 
                fieldSize, 
                lastDefenderBlue, 
                new Scalar(255, 0, 255), 
                "", 
                orientation
            ); 
        }
        
        List<Player> offsidePlayers = PlayerUtils.findOffsidePlayers(
            players, 
            lastDefenderPlayer, 
            playerWithBall.isBlueTeam, 
            playerWithBall, 
            orientation
        );

        DrawingUtils.markOffsidePlayers(image, offsidePlayers);
        
        // Write the result image and verify
        boolean writeSuccess = Imgcodecs.imwrite(IMG_FILE_PATH, image);

        if (!writeSuccess) 
        { throw new Exception("Failed to write result image"); }
        
        File resultFile = new File(IMG_FILE_PATH);
        if (!resultFile.exists() || resultFile.length() == 0) 
        { throw new Exception("Result file was not created properly"); }
    }

    private static FieldOrientation determineFieldOrientation(Mat image) 
    { return image.width() > image.height() ? FieldOrientation.HORIZONTAL : FieldOrientation.VERTICAL; }
    
    private static void detectGoalkeepers(List<Player> players, Size fieldSize, Mat image, FieldOrientation orientation) {
        if (players.isEmpty()) return;

        Point blueGoal = PlayerUtils.getGoalPosition(fieldSize, true, orientation);
        Point redGoal = PlayerUtils.getGoalPosition(fieldSize, false, orientation);

        Player blueGoalkeeper = PlayerUtils.findGoalkeeper(players, true, blueGoal);
        Player redGoalkeeper = PlayerUtils.findGoalkeeper(players, false, redGoal);

        // do something with GK if required
    }
}