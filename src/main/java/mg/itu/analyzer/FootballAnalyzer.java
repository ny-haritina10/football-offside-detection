package mg.itu.analyzer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import mg.itu.algo.Algo;

public class FootballAnalyzer extends JFrame {

    private static final String IMG_FILE_PATH = "img/picture.jpg";
    private JButton selectImageButton, analyzeButton;
    private JLabel imageLabel, statusLabel;
    private String selectedImagePath;
    private Font gameFont;
    private JRadioButton standardOrientationBtn, reversedOrientationBtn;
    private boolean isReversedOrientation = false;

    public FootballAnalyzer() {
        setTitle("Football Offside Analyzer Pro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        loadCustomFont();
        setupTheme();
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createContentPanel(), BorderLayout.CENTER);
        
        // Create a panel for buttons and orientation
        JPanel buttonOrientationPanel = new JPanel();
        buttonOrientationPanel.setLayout(new BoxLayout(buttonOrientationPanel, BoxLayout.Y_AXIS));
        
        buttonOrientationPanel.add(createButtonPanel());
        buttonOrientationPanel.add(createOrientationPanel());
        
        mainPanel.add(buttonOrientationPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        setSize(1000, 800);
        setLocationRelativeTo(null);
        
        verifyOpenCV();
    }    

    private void loadCustomFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/fonts/Gameplay.ttf");
            gameFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(gameFont);
        } catch (Exception e) {
            gameFont = new Font("Arial", Font.BOLD, 14);
        }
    }

    private void setupTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Button.background", new Color(69, 173, 168));
            UIManager.put("Button.foreground", Color.BLACK);
            UIManager.put("Label.foreground", Color.BLACK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("Football Offside Analyzer Pro");
        titleLabel.setFont(gameFont.deriveFont(24f));
        headerPanel.add(titleLabel);
        return headerPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        selectImageButton = new JButton("Select Image");
        analyzeButton = new JButton("Analyze");
        analyzeButton.setEnabled(false);
        
        selectImageButton.addActionListener(e -> selectImage());
        analyzeButton.addActionListener(e -> analyzeImage());
        
        buttonPanel.add(selectImageButton);
        buttonPanel.add(analyzeButton);
        
        return buttonPanel;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        imageLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel = new JLabel("Please select an image", SwingConstants.CENTER);
        
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(statusLabel, BorderLayout.SOUTH);
        
        return contentPanel;
    }

    private JPanel createOrientationPanel() {
        JPanel orientationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        standardOrientationBtn = new JRadioButton("Standard", true);
        reversedOrientationBtn = new JRadioButton("Reversed");
        
        ButtonGroup orientationGroup = new ButtonGroup();
        
        orientationGroup.add(standardOrientationBtn);
        orientationGroup.add(reversedOrientationBtn);
        
        standardOrientationBtn.addActionListener(e -> isReversedOrientation = false);
        reversedOrientationBtn.addActionListener(e -> isReversedOrientation = true);
        
        orientationPanel.add(new JLabel("Team Orientation: "));
        orientationPanel.add(standardOrientationBtn);
        orientationPanel.add(reversedOrientationBtn);
        
        return orientationPanel;
    }

    private void verifyOpenCV() {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            statusLabel.setText("OpenCV loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            JOptionPane.showMessageDialog(this, "Failed to load OpenCV library.", "OpenCV Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("OpenCV not loaded - functionality will be limited");
        }
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) { return f.isDirectory() || f.getName().toLowerCase().matches(".*\\.(jpg|jpeg|png|bmp)$"); }
            public String getDescription() { return "Image files (*.jpg, *.jpeg, *.png, *.bmp)"; }
        });
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedImagePath = selectedFile.getAbsolutePath();
            Mat testImage = Imgcodecs.imread(selectedImagePath);
            if (testImage.empty()) {
                JOptionPane.showMessageDialog(this,"Unable to read the selected image.","Image Error",JOptionPane.ERROR_MESSAGE);
                return;
            }
            displayImage(selectedImagePath);
            analyzeButton.setEnabled(true);
            statusLabel.setText("Image selected: " + selectedFile.getName());
        }
    }

    private void displayImage(String imagePath) {
        try {
            ImageIcon imageIcon = new ImageIcon(imagePath);
            Image image = imageIcon.getImage();
            int maxWidth = imageLabel.getParent().getSize().width - 20;
            int maxHeight = imageLabel.getParent().getSize().height - 20;
            double scale = Math.min((double) maxWidth / imageIcon.getIconWidth(), (double) maxHeight / imageIcon.getIconHeight());
            Image scaledImage = image.getScaledInstance((int)(imageIcon.getIconWidth() * scale), (int)(imageIcon.getIconHeight() * scale), Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
            imageLabel.revalidate();
            imageLabel.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,"Error displaying image: " + e.getMessage(),"Display Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void analyzeImage() {
        if (selectedImagePath == null) return;
        
        selectImageButton.setEnabled(false);
        analyzeButton.setEnabled(false);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    Mat inputImage = Imgcodecs.imread(selectedImagePath);
                    if (inputImage.empty()) throw new Exception("Unable to read input image");
                    
                    // Pass the orientation flag to the analyze method
                    Algo.analyze(selectedImagePath, isReversedOrientation);
                    
                    File resultFile = new File(IMG_FILE_PATH);
                    if (!resultFile.exists() || resultFile.length() == 0) 
                        throw new Exception("Result not created properly");
                    
                    return true;
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(FootballAnalyzer.this,
                            "Error analyzing image: " + e.getMessage(),
                            "Analysis Error",
                            JOptionPane.ERROR_MESSAGE));
                    return false;
                }
            }
            
            @Override
            protected void done() {
                try {
                    if (get()) displayImage(IMG_FILE_PATH);
                    else statusLabel.setText("Analysis failed.");
                } catch (Exception e) {
                    statusLabel.setText("Error during analysis: " + e.getMessage());
                }
                selectImageButton.setEnabled(true);
                analyzeButton.setEnabled(true);
            }
        };
        
        worker.execute();
    }
}