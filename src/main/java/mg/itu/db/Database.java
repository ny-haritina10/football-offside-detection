package mg.itu.db;

import java.sql.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

public class Database {

    private static final String CONFIG_FILE = "D:\\Studies\\ITU\\S5\\INF301_Architechture-Logiciel\\projet\\offside-v3\\football-offside-detection\\src\\main\\java\\mg\\itu\\db\\db.properties";
    private static Database instance;
    private Connection connection;
    
    private Database() {
        try {
            Properties props = loadProperties();

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");
            String driver = props.getProperty("db.driver");

            Class.forName(driver);

            connection = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }
    
    private Properties loadProperties() throws IOException {
        Properties props = new Properties();
        FileInputStream input = new FileInputStream(CONFIG_FILE);
        props.load(input);
        return props;
    }
    
    public static synchronized Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }
    
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                instance = new Database();
            }
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }
    
    // Match-related database operations
    public int createNewMatch() throws SQLException {
        String sql = "INSERT INTO matches (start_time) VALUES (CURRENT_TIMESTAMP) RETURNING match_id";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("match_id");
            }
            throw new SQLException("Failed to create new match");
        }
    }
    
    public void endMatch(int matchId) throws SQLException {
        String sql = "UPDATE matches SET end_time = CURRENT_TIMESTAMP WHERE match_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, matchId);
            stmt.executeUpdate();
        }
    }
    
    public void saveMatchAction(int matchId, String receiveImagePath, String shootImagePath,
                              boolean isOffside, boolean isGoal, String scoringTeam, 
                              String actionResult) throws SQLException {
        String sql = "INSERT INTO match_actions (match_id, receive_image_path, shoot_image_path, " +
                    "is_offside, is_goal, scoring_team, action_result) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, matchId);
            stmt.setString(2, receiveImagePath);
            stmt.setString(3, shootImagePath);
            stmt.setBoolean(4, isOffside);
            stmt.setBoolean(5, isGoal);
            stmt.setString(6, scoringTeam);
            stmt.setString(7, actionResult);
            stmt.executeUpdate();
        }
    }
    
    public void updateMatchScore(int matchId, int blueScore, int redScore) throws SQLException {
        String sql = "UPDATE matches SET blue_team_score = ?, red_team_score = ? WHERE match_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, blueScore);
            stmt.setInt(2, redScore);
            stmt.setInt(3, matchId);
            stmt.executeUpdate();
        }
    }
}