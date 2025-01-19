package mg.itu.utils;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.LineSegmentDetector;
import org.opencv.imgcodecs.Imgcodecs;
import java.util.*;

public class GoalCageUtils {
    
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static boolean isBallInsideGoal(Point ballPosition, GoalCageUtils.Goal goal) {
        double x1 = goal.post1.x1;
        double y1 = goal.post1.y1;
        double x2 = goal.post2.x1;
        double y2 = goal.post2.y1;
        
        double crossbarY = goal.crossbar.y1; // Assuming crossbar is horizontal
        
        boolean isBetweenPosts = ballPosition.x >= Math.min(x1, x2) && 
                                ballPosition.x <= Math.max(x1, x2);
        
        boolean isTopGoal = crossbarY > Math.max(y1, y2);
        
        // check if ball is at the right height, accounting for goal orientation
        boolean isAtRightHeight;
        if (isTopGoal) {
            // for top goal, ball should be below crossbar but above posts
            isAtRightHeight = ballPosition.y <= crossbarY && 
                             ballPosition.y >= Math.min(y1, y2);
        } 
        
        else {
            // for bottom goal, ball should be above crossbar but below posts
            isAtRightHeight = ballPosition.y >= crossbarY && 
                             ballPosition.y <= Math.max(y1, y2);
        }
        
        // Check if ball is within the goal depth
        boolean isWithinDepth = true; 
        return isBetweenPosts && isAtRightHeight && isWithinDepth;
    }

    public static List<Goal> detectGoal(String imagePath) {
        // Read and preprocess image
        Mat source = Imgcodecs.imread(imagePath);
        Mat gray = new Mat();
        Imgproc.cvtColor(source, gray, Imgproc.COLOR_BGR2GRAY);

        // Create LSD detector and detect lines
        Mat lines = new Mat();
        LineSegmentDetector detector = Imgproc.createLineSegmentDetector();
        detector.detect(gray, lines);

        // Process detected lines
        List<Line> verticalLines = new ArrayList<>();
        List<Line> horizontalLines = new ArrayList<>();

        // Convert detected lines to our Line objects
        for (int i = 0; i < lines.rows(); i++) {
            double[] line = lines.get(i, 0);
            Line detectedLine = new Line(line[0], line[1], line[2], line[3]);
            
            // Calculate line angle
            double angle = Math.abs(Math.toDegrees(detectedLine.getAngle()));
            
            // Classify lines with wider tolerance for perspective distortion
            if ((angle > 75 && angle < 105)) {
                verticalLines.add(detectedLine);
            } else if (angle < 15 || angle > 165) {
                horizontalLines.add(detectedLine);
            }
        }

        // Find goal candidates using geometric analysis
        List<Goal> detectedGoals = findGoalStructures(verticalLines, horizontalLines);

        // Draw detected goals
        drawGoals(source, detectedGoals);

        // Save result for debug
        Imgcodecs.imwrite("img/detected_goals.jpg", source);

        return detectedGoals;
    }

    private static List<Goal> findGoalStructures(List<Line> verticalLines, List<Line> horizontalLines) {
        List<Goal> goals = new ArrayList<>();
        double maxRatio = 2.5; // Maximum allowed ratio between post heights
        double minPostHeight = 50; // Minimum post height in pixels
        double minGoalSeparation = 100; // Minimum distance between different goals

        // Group nearby vertical lines that might be parts of the same post
        List<Line> mergedVerticals = mergeNearbyLines(verticalLines, 20);

        // Find potential goal post pairs
        for (int i = 0; i < mergedVerticals.size(); i++) {
            for (int j = i + 1; j < mergedVerticals.size(); j++) {
                Line post1 = mergedVerticals.get(i);
                Line post2 = mergedVerticals.get(j);

                // Check if posts have similar height and are roughly parallel
                if (isValidPostPair(post1, post2, maxRatio, minPostHeight)) {
                    // Find potential crossbar
                    Line crossbar = findCrossbar(post1, post2, horizontalLines);
                    if (crossbar != null) {
                        // Check if this goal is significantly different from previously detected goals
                        boolean isNewGoal = true;
                        for (Goal existingGoal : goals) {
                            if (isGoalDuplicate(existingGoal, new Goal(post1, post2, crossbar), minGoalSeparation)) {
                                isNewGoal = false;
                                break;
                            }
                        }
                        
                        if (isNewGoal) {
                            goals.add(new Goal(post1, post2, crossbar));
                        }
                    }
                }
            }
        }

        return goals;
    }

    private static boolean isGoalDuplicate(Goal g1, Goal g2, double minSeparation) {
        // Calculate centers of goals
        double center1X = (g1.post1.x1 + g1.post2.x1) / 2;
        double center1Y = (g1.post1.y1 + g1.post2.y1) / 2;
        double center2X = (g2.post1.x1 + g2.post2.x1) / 2;
        double center2Y = (g2.post1.y1 + g2.post2.y1) / 2;
        
        // Calculate distance between centers
        double distance = euclideanDistance(center1X, center1Y, center2X, center2Y);
        
        return distance < minSeparation;
    }

    private static boolean isValidPostPair(Line post1, Line post2, double maxRatio, double minPostHeight) {
        double height1 = post1.length();
        double height2 = post2.length();
        
        // Check minimum height
        if (height1 < minPostHeight || height2 < minPostHeight) {
            return false;
        }

        // Check height ratio
        double ratio = Math.max(height1, height2) / Math.min(height1, height2);
        if (ratio > maxRatio) {
            return false;
        }

        // Check if posts are roughly parallel
        double angleDiff = Math.abs(post1.getAngle() - post2.getAngle());
        return angleDiff < Math.PI / 6; // 30 degrees tolerance
    }

    private static Line findCrossbar(Line post1, Line post2, List<Line> horizontalLines) {
        double tolerance = 20; // pixels
        double expectedWidth = euclideanDistance(post1.x1, post1.y1, post2.x1, post2.y1);
        Line bestCrossbar = null;
        double bestScore = Double.MAX_VALUE;

        for (Line line : horizontalLines) {
            // Check both top and bottom of posts
            if ((isNearPostEnd(line, post1, post2, tolerance, true) || // Check top
                 isNearPostEnd(line, post1, post2, tolerance, false))  // Check bottom
                && Math.abs(line.length() - expectedWidth) < tolerance * 2) {
                
                double widthDiff = Math.abs(line.length() - expectedWidth);
                if (widthDiff < bestScore) {
                    bestScore = widthDiff;
                    bestCrossbar = line;
                }
            }
        }
        
        return bestCrossbar;
    }

    private static boolean isNearPostEnd(Line horizontal, Line post1, Line post2, double tolerance, boolean checkTop) {
        // Get the Y coordinates of the posts' ends we want to check
        double post1Y = checkTop ? Math.min(post1.y1, post1.y2) : Math.max(post1.y1, post1.y2);
        double post2Y = checkTop ? Math.min(post2.y1, post2.y2) : Math.max(post2.y1, post2.y2);
        
        // Get horizontal line's Y coordinate (average of endpoints)
        double horizontalY = (horizontal.y1 + horizontal.y2) / 2;
        
        // Check if the horizontal line is near both posts' ends
        return Math.abs(horizontalY - post1Y) < tolerance && 
               Math.abs(horizontalY - post2Y) < tolerance;
    }

    private static List<Line> mergeNearbyLines(List<Line> lines, double tolerance) {
        List<Line> merged = new ArrayList<>();
        boolean[] used = new boolean[lines.size()];

        for (int i = 0; i < lines.size(); i++) {
            if (used[i]) continue;
            
            Line current = lines.get(i);
            List<Line> group = new ArrayList<>();
            group.add(current);
            
            for (int j = i + 1; j < lines.size(); j++) {
                if (!used[j] && shouldMergeLines(current, lines.get(j), tolerance)) {
                    group.add(lines.get(j));
                    used[j] = true;
                }
            }
            
            merged.add(mergeLine(group));
            used[i] = true;
        }

        return merged;
    }

    private static boolean shouldMergeLines(Line l1, Line l2, double tolerance) {
        // Check if lines are close and roughly parallel
        return euclideanDistance(l1.x1, l1.y1, l2.x1, l2.y1) < tolerance &&
               euclideanDistance(l1.x2, l1.y2, l2.x2, l2.y2) < tolerance &&
               Math.abs(l1.getAngle() - l2.getAngle()) < Math.PI / 12;
    }

    private static Line mergeLine(List<Line> lines) {
        if (lines.size() == 1) return lines.get(0);
        
        // Average the endpoints
        double x1 = 0, y1 = 0, x2 = 0, y2 = 0;
        for (Line l : lines) {
            x1 += l.x1; y1 += l.y1;
            x2 += l.x2; y2 += l.y2;
        }
        return new Line(x1/lines.size(), y1/lines.size(), x2/lines.size(), y2/lines.size());
    }

    private static void drawGoals(Mat image, List<Goal> goals) {        
        for (Goal goal : goals) {
            Scalar color = new Scalar(0, 255, 0); // Green
            int thickness = 2;
            // Draw posts
            Imgproc.line(image, 
                new Point(goal.post1.x1, goal.post1.y1),
                new Point(goal.post1.x2, goal.post1.y2),
                color, thickness);
            Imgproc.line(image, 
                new Point(goal.post2.x1, goal.post2.y1),
                new Point(goal.post2.x2, goal.post2.y2),
                color, thickness);
            
            // Draw crossbar
            Imgproc.line(image,
                new Point(goal.crossbar.x1, goal.crossbar.y1),
                new Point(goal.crossbar.x2, goal.crossbar.y2),
                color, thickness);            
        }
    }

    private static double euclideanDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    public static class Line {
        public double x1, y1, x2, y2;
        
        public Line(double x1, double y1, double x2, double y2) {
            this.x1 = x1; this.y1 = y1;
            this.x2 = x2; this.y2 = y2;
        }
        
        double getAngle() {
            return Math.atan2(y2 - y1, x2 - x1);
        }
        
        double length() {
            return euclideanDistance(x1, y1, x2, y2);
        }
    }

    public static class Goal {
        public Line post1, post2, crossbar;
        
        public Goal(Line post1, Line post2, Line crossbar) {
            this.post1 = post1;
            this.post2 = post2;
            this.crossbar = crossbar;
        }
    }
}