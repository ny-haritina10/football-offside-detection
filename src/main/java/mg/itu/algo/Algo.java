package mg.itu.algo;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import mg.itu.entity.Player;
import mg.itu.utils.DrawingUtils;
import mg.itu.utils.GoalCageUtils;
import mg.itu.utils.GoalCageUtils.Goal;
import mg.itu.utils.ImageProcessingUtils;
import mg.itu.utils.PlayerUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Algo {

    // load open cv native lib once 
    static 
    { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    // CONSTANTS
    public static final int ATTACK_ARROW_OFFSET = 100;
    public static final String IMG_FILE_PATH = "img/receive.jpg";
    public static final String IMG_FILE_PATH_2 = "img/shoot.jpg";


    private static int blueTeamScore = 0;
    private static int redTeamScore = 0;

    public enum FieldOrientation {
        HORIZONTAL,
        VERTICAL
    }

    public static class AnalysisResult {
        public boolean isOffside;
        public boolean isGoal;
        public String message;
        
        public AnalysisResult(boolean isOffside, boolean isGoal, String message) {
            this.isOffside = isOffside;
            this.isGoal = isGoal;
            this.message = message;
        }
    }

    public static boolean analyze(String imagePath, boolean isReversedOrientation, FieldOrientation imageOrientation) 
        throws Exception 
    {
        Mat image = Imgcodecs.imread(imagePath);
        if (image.empty()) 
            throw new Exception("Failed to read input image: " + imagePath);
        
        // Process image
        FieldOrientation orientation = imageOrientation;
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
        PlayerUtils.detectGoalkeepers(players, fieldSize, image, orientation);

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
        
        // Reset offside status for all players
        for (Player player : players) {
            player.setOffside(false);
        }

        List<Player> offsidePlayers = PlayerUtils.findOffsidePlayers(
            players, 
            lastDefenderPlayer, 
            playerWithBall.isBlueTeam, 
            playerWithBall, 
            orientation
        );

        // Mark players as offside
        for (Player offside : offsidePlayers) {
            offside.setOffside(true);
        }

        // draw analyse
        DrawingUtils.markOffsidePlayers(image, offsidePlayers);
        DrawingUtils.drawAttackArrowsAndMarkReceivers(image, playerWithBall, players, orientation);   
        DrawingUtils.drawAttackArrows(image, playerWithBall, players, orientation);
        
        // Write the result image and verify
        boolean writeSuccess = Imgcodecs.imwrite(IMG_FILE_PATH, image);

        if (!writeSuccess) 
        { throw new Exception("Failed to write result image"); }
        
        File resultFile = new File(IMG_FILE_PATH);
        if (!resultFile.exists() || resultFile.length() == 0) 
        { throw new Exception("Result file was not created properly"); }

        if (offsidePlayers.size() == 0)
        { return false; }

        else 
        { return true; }
    }
    
    public static AnalysisResult analyzePlay(String receiveImage, String shootImage, 
    boolean isReversedOrientation, FieldOrientation orientation) 
        throws Exception 
    {
        // analyse image-1
        boolean isOffside = analyze(receiveImage, isReversedOrientation, orientation);
        String message;

        Mat receiveFrame = Imgcodecs.imread(receiveImage);
        if (receiveFrame.empty()) 
        { throw new Exception("Failed to read receive image: " + receiveImage); }

        List<Player> players = ImageProcessingUtils.detectPlayers(receiveFrame);
        Point ballCenter = ImageProcessingUtils.detectBall(receiveFrame);
        Player receivingPlayer = ImageProcessingUtils.findClosestPlayer(players, ballCenter);

        if (isReversedOrientation) {
            for (Player player : players) 
            { player.isBlueTeam = !player.isBlueTeam; }
        }

        if (receivingPlayer == null) 
        { throw new Exception("Could not determine receiving player"); }

        boolean isGoal = false;        

        // no offside 
        if (!isOffside) {
            // analyse image-2
            Mat shootFrame = Imgcodecs.imread(shootImage);
            if (shootFrame.empty()) 
            { throw new Exception("Failed to read shoot image: " + shootImage); }

            Point finalBallPosition = ImageProcessingUtils.detectBall(shootFrame);
            if (finalBallPosition == null) 
            { throw new Exception("No ball detected in the shoot image"); }

            try {            
                List<Goal> cages = GoalCageUtils.detectGoal(shootImage);
                
                if (finalBallPosition != null && !cages.isEmpty()) {
                    for (GoalCageUtils.Goal goal : cages) {
                        if (GoalCageUtils.isBallInsideGoal(finalBallPosition, goal)) {
                            isGoal = true;
                            DrawingUtils.drawGoalIndicator(shootFrame, finalBallPosition);
                            break;
                        }
                    }
                }  
            } 
            catch (Exception e) { 
                System.err.println("Error during goal detection: " + e.getMessage()); 
            }
            
            // Always save the shoot frame, regardless of goal detection outcome
            boolean writeSuccess = Imgcodecs.imwrite(IMG_FILE_PATH_2, shootFrame);
            if (!writeSuccess) {
                throw new Exception("Failed to write shoot image result");
            }

            if (isGoal) {

                // bleu and not reversed
                if (receivingPlayer.isBlueTeam && !isReversedOrientation) {
                    blueTeamScore++;
                    message = "Goal for Blue team! Score: Blue " + blueTeamScore + " - Red " + redTeamScore;
                } 

                // not bleu and not reversed
                else if (!receivingPlayer.isBlueTeam && !isReversedOrientation){
                    redTeamScore++;
                    message = "Goal for Red team! Score: Blue " + blueTeamScore + " - Red " + redTeamScore;
                }

                // bleu but reversed (red)
                else if (receivingPlayer.isBlueTeam && isReversedOrientation) {
                    redTeamScore++;
                    message = "Goal for Blue team! Score: Blue " + blueTeamScore + " - Red " + redTeamScore;
                }

                // red but reversed (bleu)
                else if (!receivingPlayer.isBlueTeam && isReversedOrientation) {
                    blueTeamScore++;
                    message = "Goal for Blue team! Score: Blue " + blueTeamScore + " - Red " + redTeamScore;
                }

                else {
                    message = "Goal";
                }
            } 
            
            else {
                message = "No goal.";
            }

            return new AnalysisResult(isOffside, isGoal, message);
        }

        else {
            message = "Offside! Action disallowed.";
            
            Mat shootFrame = Imgcodecs.imread(shootImage);
            boolean writeSuccess = Imgcodecs.imwrite(IMG_FILE_PATH_2, shootFrame);
            if (!writeSuccess) {
                throw new Exception("Failed to write shoot image result");
            }
            return new AnalysisResult(isOffside, isGoal, message);
        }
    }
}