package ui;

import enums.ComplaintCategory;
import model.Complaint;
import model.ComplaintImage;
import model.Student;
import service.ComplaintService;
import service.ImageService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Dialog for a student to submit a new complaint, including up to 3 evidence images. */
public class ComplaintForm extends JDialog {

    private static final int MAX_IMAGES = 3;

    private final Student student;
    private final ComplaintService complaintService = new ComplaintService();
    private final ImageService imageService = new ImageService();

    private final JComboBox<ComplaintCategory> categoryBox = new JComboBox<>(ComplaintCategory.values());
    private final JTextField roomField = UITheme.textField();
    private final JTextArea descArea = UITheme.textArea(5);

    // Optional category-specific fields
    private final JComboBox<String> mealBox = new JComboBox<>(new String[]{"", "Breakfast", "Lunch", "Dinner"});
    private final JTextField foodItemField = UITheme.textField();
    private final JTextField machineNumberField = UITheme.textField();
    private final JTextField machineLocationField = UITheme.textField();
    private final JPanel foodPanel = new JPanel();
    private final JPanel washingPanel = new JPanel();

    private final List<File> selectedImages = new ArrayList<>();
    private final JPanel previewPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    private final JLabel statusLabel = new JLabel(" ");

    public ComplaintForm(JFrame parent, Student student) {
        super(parent, "New Complaint", true);
        this.student = student;
        setSize(560, 700);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(UITheme.BACKGROUND);
        buildUI();
    }

    private void buildUI() {
        JPanel form = new JPanel();
        form.setBackground(UITheme.BACKGROUND);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("CREATE COMPLAINT");
        title.setFont(UITheme.FONT_HEADING);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        roomField.setText(student.getRoomNumber());
        roomField.setEditable(false);

        categoryBox.setFont(UITheme.FONT_BODY);
        categoryBox.addActionListener(e -> updateConditionalFields());

        foodPanel.setLayout(new BoxLayout(foodPanel, BoxLayout.Y_AXIS));
        foodPanel.setOpaque(false);
        foodPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        addLabeled(foodPanel, "Meal (optional)", mealBox);
        addLabeled(foodPanel, "Food Item (optional)", foodItemField);

        washingPanel.setLayout(new BoxLayout(washingPanel, BoxLayout.Y_AXIS));
        washingPanel.setOpaque(false);
        washingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        addLabeled(washingPanel, "Machine Number (optional, e.g. WM-03)", machineNumberField);
        addLabeled(washingPanel, "Location (optional, e.g. Laundry Block A)", machineLocationField);

        JButton chooseImageBtn = UITheme.secondaryButton("Choose Image");
        chooseImageBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        chooseImageBtn.addActionListener(e -> chooseImage());

        previewPanel.setOpaque(false);
        previewPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        previewPanel.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

        statusLabel.setForeground(UITheme.DANGER);
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton submitBtn = UITheme.primaryButton("Submit Complaint");
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        submitBtn.addActionListener(e -> submit(submitBtn));

        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(14));
        addLabeled(form, "Room Number", roomField);
        addLabeled(form, "Category", categoryBox);
        form.add(foodPanel);
        form.add(washingPanel);
        addLabeled(form, "Description", new JScrollPane(descArea));
        form.add(Box.createVerticalStrut(8));

        JLabel imgLabel = new JLabel("Upload Images (up to 3, JPG/PNG, max 5 MB each)");
        imgLabel.setFont(UITheme.FONT_BOLD_BODY);
        imgLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(imgLabel);
        form.add(Box.createVerticalStrut(4));
        form.add(chooseImageBtn);
        form.add(Box.createVerticalStrut(6));
        form.add(previewPanel);

        form.add(Box.createVerticalStrut(16));
        form.add(submitBtn);
        form.add(Box.createVerticalStrut(6));
        form.add(statusLabel);

        setContentPane(new JScrollPane(form));
        updateConditionalFields();
    }

    private void addLabeled(JPanel parent, String labelText, JComponent field) {
        JLabel l = new JLabel(labelText);
        l.setFont(UITheme.FONT_BOLD_BODY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (field instanceof JScrollPane) {
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        } else {
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            field.setPreferredSize(new Dimension(field.getPreferredSize().width, 36));
        }
        parent.add(l);
        parent.add(Box.createVerticalStrut(4));
        parent.add(field);
        parent.add(Box.createVerticalStrut(12));
    }

    private void updateConditionalFields() {
        ComplaintCategory cat = (ComplaintCategory) categoryBox.getSelectedItem();
        boolean isFood = cat == ComplaintCategory.FOOD_QUALITY || cat == ComplaintCategory.FOOD_HYGIENE
                          || cat == ComplaintCategory.DINING_HALL;
        boolean isWashing = cat == ComplaintCategory.WASHING_MACHINE;
        foodPanel.setVisible(isFood);
        washingPanel.setVisible(isWashing);
        revalidate();
        repaint();
    }

    private void chooseImage() {
        if (selectedImages.size() >= MAX_IMAGES) {
            statusLabel.setText("You can upload a maximum of 3 images.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Images (jpg, jpeg, png)", "jpg", "jpeg", "png"));
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try {
            imageService.validate(file.toPath());
            selectedImages.add(file);
            addPreviewThumbnail(file);
            statusLabel.setText(" ");
        } catch (ImageService.ValidationException ex) {
            statusLabel.setText(ex.getMessage());
        } catch (IOException ex) {
            statusLabel.setText("Could not read the selected file.");
        }
    }

    private void addPreviewThumbnail(File file) {
        try {
            BufferedImage img = javax.imageio.ImageIO.read(file);
            Image scaled = img.getScaledInstance(90, 90, Image.SCALE_SMOOTH);
            JLabel thumb = new JLabel(new ImageIcon(scaled));
            thumb.setToolTipText(file.getName() + " (" + (file.length() / 1024) + " KB)");
            thumb.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
            previewPanel.add(thumb);
            previewPanel.revalidate();
            previewPanel.repaint();
        } catch (IOException ex) {
            statusLabel.setText("Could not preview image: " + ex.getMessage());
        }
    }

    private void submit(JButton submitBtn) {
        submitBtn.setEnabled(false);
        statusLabel.setForeground(UITheme.DANGER);
        ComplaintCategory category = (ComplaintCategory) categoryBox.getSelectedItem();
        String meal = (String) mealBox.getSelectedItem();
        try {
            Complaint saved = complaintService.submit(
                student.getStudentId(),
                student.getRoomNumber(),
                category,
                descArea.getText(),
                (meal == null || meal.isBlank()) ? null : meal,
                foodItemField.getText().isBlank() ? null : foodItemField.getText().trim(),
                machineNumberField.getText().isBlank() ? null : machineNumberField.getText().trim(),
                machineLocationField.getText().isBlank() ? null : machineLocationField.getText().trim()
            );

            for (File f : selectedImages) {
                Path p = f.toPath();
                imageService.store(p, saved.getComplaintId(), ComplaintImage.Kind.BEFORE);
            }

            JOptionPane.showMessageDialog(this,
                "Complaint submitted successfully!\n\n" +
                "Complaint ID: " + saved.getComplaintId() + "\n" +
                "Priority: " + saved.getPriority() + "\n" +
                "Status: " + saved.getStatus(),
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (ComplaintService.ComplaintException ex) {
            statusLabel.setText(ex.getMessage());
            submitBtn.setEnabled(true);
        } catch (SQLException ex) {
            statusLabel.setText("Could not save the complaint. Please try again.");
            submitBtn.setEnabled(true);
        } catch (ImageService.ValidationException ex) {
            statusLabel.setText(ex.getMessage());
            submitBtn.setEnabled(true);
        } catch (IOException ex) {
            statusLabel.setText("An image could not be uploaded. Please try again.");
            submitBtn.setEnabled(true);
        }
    }
}
