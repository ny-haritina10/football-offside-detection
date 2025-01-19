package mg.itu.analyzer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import mg.itu.algo.Algo;
import mg.itu.algo.Algo.FieldOrientation;
import mg.itu.algo.Algo.AnalysisResult;

public class FootballAnalyzer extends JFrame {

    private static final String RECEIVE_IMG_PATH = "img/receive.jpg";
    private static final String SHOOT_IMG_PATH = "img/shoot.jpg";

    private JButton selectReceiveImageButton, selectShootImageButton, analyzeButton;
    private JLabel receiveImageLabel, shootImageLabel, statusLabel, scoreLabel;
    private String selectedReceiveImagePath, selectedShootImagePath;
    private Font gameFont;
    private JComboBox<String> orientationComboBox;
    private boolean isReversedOrientation = false;
    private FieldOrientation imageOrientation;

    public FootballAnalyzer() {
        setTitle("Video Assistance Referee (VAR)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        loadCustomFont();
        setupTheme();
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createContentPanel(), BorderLayout.CENTER);
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        
        controlPanel.add(createOrientationPanel());
        controlPanel.add(addFieldOrientationPanel());
        controlPanel.add(createButtonPanel());
        controlPanel.add(createScorePanel());
        
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        setSize(1200, 900);
        setLocationRelativeTo(null);
        
        verifyOpenCV();
    }  

    private void loadCustomFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/fonts/Gameplay.ttf");
            gameFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(gameFont);
        } 
        
        catch (Exception e) {
            gameFont = new Font("Arial Bold Italic", Font.BOLD, 14);
        }
    }

    private void setupTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            UIManager.put("Panel.background", new Color(173, 216, 230)); 
            UIManager.put("Button.background", new Color(69, 173, 168)); 
            UIManager.put("Button.foreground", Color.BLACK); 
            UIManager.put("Label.foreground", Color.BLACK); 
        } 
        
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("Video Assistance Referee (VAR)");
        titleLabel.setFont(gameFont.deriveFont(24f));
        headerPanel.add(titleLabel);
        
        return headerPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        selectReceiveImageButton = new JButton("Select Receive Image");
        selectShootImageButton = new JButton("Select Shot Image");
        analyzeButton = new JButton("Analyze Play");
        
        analyzeButton.setEnabled(false);
        
        selectReceiveImageButton.addActionListener(e -> selectImage(true));
        selectShootImageButton.addActionListener(e -> selectImage(false));
        analyzeButton.addActionListener(e -> analyzePlay());
        
        buttonPanel.add(selectReceiveImageButton);
        buttonPanel.add(selectShootImageButton);
        buttonPanel.add(analyzeButton);
        
        return buttonPanel;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        
        // Left panel for receive image
        JPanel receivePanel = new JPanel(new BorderLayout());
        receiveImageLabel = new JLabel("Receive Image", SwingConstants.CENTER);

        JScrollPane receiveScrollPane = new JScrollPane(receiveImageLabel);
        receivePanel.add(new JLabel("", SwingConstants.CENTER), BorderLayout.NORTH);
        receivePanel.add(receiveScrollPane, BorderLayout.CENTER);
        
        // Right panel for shoot image
        JPanel shootPanel = new JPanel(new BorderLayout());
        shootImageLabel = new JLabel("Shoot Image", SwingConstants.CENTER);
        JScrollPane shootScrollPane = new JScrollPane(shootImageLabel);
        shootPanel.add(new JLabel("", SwingConstants.CENTER), BorderLayout.NORTH);
        shootPanel.add(shootScrollPane, BorderLayout.CENTER);
        
        contentPanel.add(receivePanel);
        contentPanel.add(shootPanel);
        
        // Status panel at the bottom
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Please select both images", SwingConstants.CENTER);
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        
        JPanel combinedPanel = new JPanel(new BorderLayout());
        combinedPanel.add(contentPanel, BorderLayout.CENTER);
        combinedPanel.add(statusPanel, BorderLayout.SOUTH);
        
        return combinedPanel;
    }

    private JPanel createOrientationPanel() {
        JPanel orientationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        // Create a JComboBox for orientation selection
        String[] orientations = { "Standard", "Reversed" };
        orientationComboBox = new JComboBox<>(orientations);
        
        orientationComboBox.addActionListener(e -> {
            isReversedOrientation = orientationComboBox.getSelectedItem().equals("Reversed");
        });
        
        orientationPanel.add(new JLabel("Side: "));
        orientationPanel.add(orientationComboBox); 
        
        return orientationPanel;
    }

    private JPanel createScorePanel() {
        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        scoreLabel = new JLabel("Score: Blue 0 - Red 0");
        scoreLabel.setFont(gameFont.deriveFont(18f));
        scorePanel.add(scoreLabel);
        return scorePanel;
    }

    private JPanel addFieldOrientationPanel() {
        JPanel fieldOrientationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    
        JRadioButton verticalButton = new JRadioButton("Vertical");
        JRadioButton horizontalButton = new JRadioButton("Horizontal");
    
        ButtonGroup orientationGroup = new ButtonGroup();
        orientationGroup.add(verticalButton);
        orientationGroup.add(horizontalButton);
    
        horizontalButton.setSelected(true);
    
        verticalButton.addActionListener(e -> imageOrientation = FieldOrientation.VERTICAL);
        horizontalButton.addActionListener(e -> imageOrientation = FieldOrientation.HORIZONTAL);

    
        fieldOrientationPanel.add(new JLabel("Field Orientation: "));

        fieldOrientationPanel.add(verticalButton);
        fieldOrientationPanel.add(horizontalButton);
    
        return fieldOrientationPanel;
    }

    private void verifyOpenCV() {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            statusLabel.setText("Welcome !");
        } catch (UnsatisfiedLinkError e) {
            JOptionPane.showMessageDialog(this, "Failed to load OpenCV library.", "OpenCV Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("OpenCV not loaded - functionality will be limited");
        }
    }

    private void selectImage(boolean isReceiveImage) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().matches(".*\\.(jpg|jpeg|png|bmp)$");
            }
            public String getDescription() {
                return "Image files (*.jpg, *.jpeg, *.png, *.bmp)";
            }
        });
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String imagePath = selectedFile.getAbsolutePath();
            
            Mat testImage = Imgcodecs.imread(imagePath);
            if (testImage.empty()) {
                JOptionPane.showMessageDialog(this, 
                    "Unable to read the selected image.", 
                    "Image Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (isReceiveImage) {
                selectedReceiveImagePath = imagePath;
                displayImage(selectedReceiveImagePath, receiveImageLabel);
                statusLabel.setText("Receive image selected: " + selectedFile.getName());
            } else {
                selectedShootImagePath = imagePath;
                displayImage(selectedShootImagePath, shootImageLabel);
                statusLabel.setText("Shot image selected: " + selectedFile.getName());
            }
            
            // Enable analyze button only when both images are selected
            analyzeButton.setEnabled(selectedReceiveImagePath != null && selectedShootImagePath != null);
        }
    }

    private void displayImage(String imagePath, JLabel targetLabel) {
        try {
            ImageIcon imageIcon = new ImageIcon(imagePath);
            Image image = imageIcon.getImage();
            
            // Calculate scaling to fit the label while maintaining aspect ratio
            int maxWidth = getWidth() / 2 - 40;  // Half the frame width minus margins
            int maxHeight = getHeight() - 200;   // Frame height minus space for controls
            
            double scale = Math.min(
                (double) maxWidth / imageIcon.getIconWidth(),
                (double) maxHeight / imageIcon.getIconHeight()
            );
            
            Image scaledImage = image.getScaledInstance(
                (int)(imageIcon.getIconWidth() * scale),
                (int)(imageIcon.getIconHeight() * scale),
                Image.SCALE_SMOOTH
            );
            
            targetLabel.setIcon(new ImageIcon(scaledImage));
            targetLabel.revalidate();
            targetLabel.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error displaying image: " + e.getMessage(),
                "Display Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void analyzePlay() {
        if (selectedReceiveImagePath == null || selectedShootImagePath == null) return;
        
        selectReceiveImageButton.setEnabled(false);
        selectShootImageButton.setEnabled(false);
        analyzeButton.setEnabled(false);
        statusLabel.setText("Analyzing play...");
        
        SwingWorker<AnalysisResult, Void> worker = new SwingWorker<>() {
            @Override
            protected AnalysisResult doInBackground() throws Exception {
                return Algo.analyzePlay(
                    selectedReceiveImagePath,
                    selectedShootImagePath,
                    isReversedOrientation,
                    imageOrientation
                );
            }
            
            @Override
            protected void done() {
                try {
                    AnalysisResult result = get();
                    
                    // Display the analysis results
                    statusLabel.setText(result.message);
                    
                    // Show visual results
                    displayImage(RECEIVE_IMG_PATH, receiveImageLabel);
                    displayImage(SHOOT_IMG_PATH, shootImageLabel);
                    
                    selectReceiveImageButton.setEnabled(true);
                    selectShootImageButton.setEnabled(true);
                    analyzeButton.setEnabled(true);
                    
                } catch (Exception e) {
                    statusLabel.setText("Analysis failed: " + e.getMessage());
                    JOptionPane.showMessageDialog(FootballAnalyzer.this,
                        "Error analyzing play: " + e.getMessage(),
                        "Analysis Error",
                        JOptionPane.ERROR_MESSAGE);
                    
                    selectReceiveImageButton.setEnabled(true);
                    selectShootImageButton.setEnabled(true);
                    analyzeButton.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
}