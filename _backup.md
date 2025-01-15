// draw utils
public static void drawGoalkeeper(Player goalkeeper, Mat image, boolean isBlueTeam) {
    if (goalkeeper == null || image == null) return;

    Scalar color = isBlueTeam ? BLUE_TEAM_COLOR : RED_TEAM_COLOR;
    String text = isBlueTeam ? "GK Blue" : "GK Red";

    Imgproc.circle(image, goalkeeper.position, GOALKEEPER_CIRCLE_RADIUS, color, GOALKEEPER_CIRCLE_THICKNESS);
    Imgproc.putText(
        image,
        text,
        new Point(goalkeeper.position.x + 15, goalkeeper.position.y - 10),
        Imgproc.FONT_HERSHEY_SIMPLEX,
        TEXT_SIZE,
        color,
        TEXT_THICKNESS
    );
}

public static void markBallAndPlayer(Mat image, Point ballCenter, Player playerWithBall, Algo.FieldOrientation orientation) {
    Imgproc.circle(image, ballCenter, BALL_OUTER_RADIUS, BLACK_COLOR, PLAYER_CIRCLE_THICKNESS);
    Imgproc.circle(image, ballCenter, BALL_INNER_RADIUS, BALL_COLOR, -1);

    if (playerWithBall != null) {
        Imgproc.line(image, ballCenter, playerWithBall.position, BALL_COLOR, LINE_THICKNESS);
        Point attackPosition = ImageProcessingUtils.calculateAttackPosition(playerWithBall.position, ballCenter, 
                playerWithBall.isBlueTeam, orientation);
        Imgproc.arrowedLine(image, playerWithBall.position, attackPosition, 
                ATTACK_ARROW_COLOR, LINE_THICKNESS, ARROW_LINE_TYPE, 0, ARROW_TIP_SIZE);
    }
}