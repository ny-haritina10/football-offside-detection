package mg.itu;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import mg.itu.analyzer.FootballAnalyzer;

public class App {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            FootballAnalyzer app = new FootballAnalyzer();
            app.setVisible(true);
        });
    }
}