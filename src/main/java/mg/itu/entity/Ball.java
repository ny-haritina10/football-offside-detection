package mg.itu.entity;

import org.opencv.core.Point;

public class Ball {
    
    public Point position;

    public Ball(Point position, double confidence) 
    { this.position = position; }
}