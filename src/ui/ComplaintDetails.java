package ui;

import dao.ComplaintDAO;
import dao.ComplaintUpdateDAO;
import dao.ImageDAO;
import dao.StaffDAO;
import enums.ComplaintStatus;
import enums.Priority;
import model.*;
import service.ComplaintService;
import service.ImageService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Full complaint detail view shared by all three roles. Which action buttons
 * appear depends on {@link Viewer}.
 */
public class ComplaintDetails extends JDialog {

    public enum Viewer { STUDENT, ADMIN, STAFF }

    private final String complaintId;
    private final Viewer viewer;
    private final MaintenanceStaff currentStaff; // only set when viewer == STAFF

    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private final ImageDAO imageDAO = new ImageDAO();
    private final ComplaintUpdateDAO updateDAO = new ComplaintUpdateDAO();
    private final StaffDAO staffDAO = new StaffDAO();
    private final ComplaintService complaintService = new ComplaintService();
    private final ImageService imageService = new ImageService();

    private final JPanel contentPanel = new JPanel();
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");

    public ComplaintDetails(JFrame parent, String complaintId, Viewer viewer, MaintenanceStaff currentStaff) {
        super(parent, "Complaint " + complaintId, true);
        this.complaintId = complaintId;
        this.viewer = viewer;
        this.currentStaff = currentStaff;
        setSize(640, 760);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(UITheme.BACKGROUND);

        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UITheme.BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(20, 24, 20, 24));
        setContentPane(new JScrollPane(contentPanel));

        loadAndRender();
    }

    private void loadAndRender() {
        contentPanel.removeAll();
        try {
            Complaint c = complaintDAO.findById(complaintId);
            if (c == null) {
                contentPanel.add(new JLabel("Complaint not found."));
                refreshDialog();
                return;
            }

            addTitleRow(c);
            contentPanel.add(Box.createVerticalStrut(12));
            addInfoGrid(c);
            contentPanel.add(Box.createVerticalStrut(16));
            addImagesSection(c);
            contentPanel.add(Box.createVerticalStrut(16));
            addTimelineSection();
            contentPanel.add(Box.createVerticalStrut(16));
            addActionsSection(c);

        } catch (SQLException e) {
            contentPanel.add(new JLabel("Database error: " + e.getMessage()));
        }
        refreshDialog();
    }

    private void refreshDialog() {
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void addTitleRow(Complaint c) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = new JLabel("COMPLAINT " + c.getComplaintId());
        title.setFont(UITheme.FONT_TITLE);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        badges.setOpaque(false);
        badges.add(UITheme.badge(c.getPriority().toString(), c.getPriority().getColor()));
        badges.add(UITheme.badge(c.getStatus().toString(), c.getStatus().getColor()));

        row.add(title, BorderLayout.WEST);
        row.add(badges, BorderLayout.EAST);
        contentPanel.add(row);
    }

    private void addInfoGrid(Complaint c) {
        JPanel card = UITheme.card();
        card.setLayout(new GridLayout(0, 2, 10, 8));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        addField(card, "Student", c.getStudentName());
        addField(card, "Room", c.getRoomNumber());
        addField(card, "Category", c.getCategory().getDisplayName());
        addField(card, "Priority", c.getPriority().toString());
        addField(card, "Status", c.getStatus().toString());
        addField(card, "Assigned Staff", c.getAssignedStaffName() != null ? c.getAssignedStaffName() : "Not yet assigned");
        addField(card, "Submitted", c.getCreatedAt() != null ? fmt.format(c.getCreatedAt()) : "-");
        addField(card, "Last Updated", c.getUpdatedAt() != null ? fmt.format(c.getUpdatedAt()) : "-");

        if (c.getMeal() != null) addField(card, "Meal", c.getMeal());
        if (c.getFoodItem() != null) addField(card, "Food Item", c.getFoodItem());
        if (c.getMachineNumber() != null) addField(card, "Machine Number", c.getMachineNumber());
        if (c.getMachineLocation() != null) addField(card, "Machine Location", c.getMachineLocation());

        contentPanel.add(card);

        JLabel descLabel = new JLabel("Description");
        descLabel.setFont(UITheme.FONT_BOLD_BODY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextArea desc = new JTextArea(c.getDescription());
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setFont(UITheme.FONT_BODY);
        desc.setBackground(UITheme.CARD);
        desc.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER), new EmptyBorder(8, 8, 8, 8)));
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(descLabel);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(desc);
    }

    private void addField(JPanel card, String label, String value) {
        JLabel l = new JLabel("<html><b>" + label + ":</b></html>");
        l.setFont(UITheme.FONT_BODY);
        JLabel v = new JLabel(value == null ? "-" : value);
        v.setFont(UITheme.FONT_BODY);
        card.add(l);
        card.add(v);
    }

    private void addImagesSection(Complaint c) throws SQLException {
        JLabel beforeLabel = new JLabel("Uploaded Images (Before)");
        beforeLabel.setFont(UITheme.FONT_BOLD_BODY);
        beforeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(beforeLabel);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(imageRow(imageDAO.findByComplaint(c.getComplaintId(), ComplaintImage.Kind.BEFORE)));

        contentPanel.add(Box.createVerticalStrut(12));
        JLabel afterLabel = new JLabel("Resolution Image (After)");
        afterLabel.setFont(UITheme.FONT_BOLD_BODY);
        afterLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(afterLabel);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(imageRow(imageDAO.findByComplaint(c.getComplaintId(), ComplaintImage.Kind.RESOLUTION)));
    }

    private JPanel imageRow(List<ComplaintImage> images) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (images.isEmpty()) {
            JLabel none = new JLabel("No images uploaded.");
            none.setForeground(UITheme.TEXT_MUTED);
            none.setFont(UITheme.FONT_SMALL);
            row.add(none);
            return row;
        }
        for (ComplaintImage img : images) {
            JButton thumb = new JButton();
            thumb.setPreferredSize(new Dimension(90, 90));
            thumb.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
            try {
                BufferedImage bi = javax.imageio.ImageIO.read(new File(img.getImagePath()));
                if (bi != null) {
                    thumb.setIcon(new ImageIcon(bi.getScaledInstance(84, 84, Image.SCALE_SMOOTH)));
                }
            } catch (IOException ignored) { }
            thumb.setToolTipText("View Full Image");
            thumb.addActionListener(e -> showFullImage(img));
            row.add(thumb);
        }
        return row;
    }

    private void showFullImage(ComplaintImage img) {
        try {
            BufferedImage bi = javax.imageio.ImageIO.read(new File(img.getImagePath()));
            if (bi == null) throw new IOException("Unreadable image");
            int w = Math.min(bi.getWidth(), 600);
            int h = (int) (bi.getHeight() * (w / (double) bi.getWidth()));
            JLabel label = new JLabel(new ImageIcon(bi.getScaledInstance(w, h, Image.SCALE_SMOOTH)));
            JOptionPane.showMessageDialog(this, label, img.getImageName(), JOptionPane.PLAIN_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not open image: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addTimelineSection() throws SQLException {
        JLabel label = new JLabel("Complaint Timeline");
        label.setFont(UITheme.FONT_BOLD_BODY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(label);
        contentPanel.add(Box.createVerticalStrut(4));

        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        List<ComplaintUpdate> updates = updateDAO.findByComplaint(complaintId);
        if (updates.isEmpty()) {
            card.add(new JLabel("No timeline entries yet."));
        }
        for (ComplaintUpdate u : updates) {
            String who = u.getStaffName() != null ? u.getStaffName() : "System";
            String statusText = u.getNewStatus() != null ? u.getNewStatus().toString() : "Update";
            JLabel entry = new JLabel(
                "<html>&#10003; <b>" + statusText + "</b> - " + (u.getComment() != null ? u.getComment() : "") +
                "<br><span style='color:gray;font-size:90%;'>" + fmt.format(u.getUpdatedAt()) + " · " + who +
                "</span></html>");
            entry.setFont(UITheme.FONT_BODY);
            entry.setBorder(new EmptyBorder(4, 0, 8, 0));
            card.add(entry);
        }
        contentPanel.add(card);
    }

    private void addActionsSection(Complaint c) {
        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.setBorder(new EmptyBorder(12, 0, 0, 0));

        switch (viewer) {
            case ADMIN -> buildAdminActions(actions, c);
            case STAFF -> buildStaffActions(actions, c);
            case STUDENT -> { /* read-only */ }
        }
        contentPanel.add(actions);
    }

    // -------------------- Admin actions --------------------

    private void buildAdminActions(JPanel actions, Complaint c) {
        JPanel priorityRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        priorityRow.setOpaque(false);
        JLabel pLabel = new JLabel("Change Priority:");
        pLabel.setFont(UITheme.FONT_BOLD_BODY);
        JComboBox<Priority> priorityBox = new JComboBox<>(Priority.values());
        priorityBox.setSelectedItem(c.getPriority());
        JButton applyPriority = UITheme.secondaryButton("Apply");
        applyPriority.addActionListener(e -> {
            try {
                complaintService.changePriority(c.getComplaintId(), (Priority) priorityBox.getSelectedItem());
                loadAndRender();
            } catch (SQLException ex) {
                showError(ex);
            }
        });
        priorityRow.add(pLabel);
        priorityRow.add(priorityBox);
        priorityRow.add(applyPriority);
        actions.add(priorityRow);

        if (c.getStatus() == ComplaintStatus.PENDING) {
            JPanel assignRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            assignRow.setOpaque(false);
            JLabel aLabel = new JLabel("Assign Staff:");
            aLabel.setFont(UITheme.FONT_BOLD_BODY);
            JComboBox<MaintenanceStaff> staffBox = new JComboBox<>();
            try {
                for (MaintenanceStaff s : staffDAO.findCandidatesForCategory(c.getCategory())) {
                    staffBox.addItem(s);
                }
            } catch (SQLException ex) { showError(ex); }
            JButton assignBtn = UITheme.primaryButton("Assign");
            assignBtn.addActionListener(e -> {
                MaintenanceStaff sel = (MaintenanceStaff) staffBox.getSelectedItem();
                if (sel == null) return;
                try {
                    complaintService.assignStaff(c.getComplaintId(), sel.getStaffId(), sel.getName());
                    loadAndRender();
                } catch (SQLException | ComplaintService.ComplaintException ex) {
                    showError(ex);
                }
            });
            assignRow.add(aLabel);
            assignRow.add(staffBox);
            assignRow.add(assignBtn);
            actions.add(assignRow);
        }

        if (c.getStatus() == ComplaintStatus.RESOLVED) {
            JButton closeBtn = UITheme.secondaryButton("Close Complaint");
            closeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            closeBtn.addActionListener(e -> {
                try {
                    complaintService.close(c.getComplaintId());
                    loadAndRender();
                } catch (SQLException | ComplaintService.ComplaintException ex) {
                    showError(ex);
                }
            });
            actions.add(Box.createVerticalStrut(8));
            actions.add(closeBtn);
        }
    }

    // -------------------- Staff actions --------------------

    private void buildStaffActions(JPanel actions, Complaint c) {
        if (currentStaff == null) return;

        if (c.getStatus() == ComplaintStatus.ASSIGNED) {
            JButton startBtn = UITheme.primaryButton("Start Work");
            startBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            startBtn.addActionListener(e -> {
                try {
                    complaintService.startWork(c.getComplaintId(), currentStaff.getStaffId());
                    loadAndRender();
                } catch (SQLException | ComplaintService.ComplaintException ex) {
                    showError(ex);
                }
            });
            actions.add(startBtn);
            actions.add(Box.createVerticalStrut(10));
        }

        if (c.getStatus() == ComplaintStatus.IN_PROGRESS || c.getStatus() == ComplaintStatus.ASSIGNED) {
            JPanel updateRow = new JPanel(new BorderLayout(6, 0));
            updateRow.setOpaque(false);
            updateRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            JTextField updateField = UITheme.textField();
            JButton addUpdateBtn = UITheme.secondaryButton("Add Update");
            addUpdateBtn.addActionListener(e -> {
                try {
                    complaintService.addUpdate(c.getComplaintId(), currentStaff.getStaffId(), updateField.getText());
                    loadAndRender();
                } catch (SQLException | ComplaintService.ComplaintException ex) {
                    showError(ex);
                }
            });
            updateRow.add(updateField, BorderLayout.CENTER);
            updateRow.add(addUpdateBtn, BorderLayout.EAST);
            actions.add(updateRow);
            actions.add(Box.createVerticalStrut(10));
        }

        if (c.getStatus() == ComplaintStatus.IN_PROGRESS) {
            JButton resolveBtn = UITheme.primaryButton("Upload Resolution Image & Mark Resolved");
            resolveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            resolveBtn.addActionListener(e -> resolveWithImage(c));
            actions.add(resolveBtn);
        }
    }

    private void resolveWithImage(Complaint c) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Images (jpg, jpeg, png)", "jpg", "jpeg", "png"));
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            int proceed = JOptionPane.showConfirmDialog(this,
                "No resolution image selected. Mark resolved anyway?", "Confirm",
                JOptionPane.YES_NO_OPTION);
            if (proceed != JOptionPane.YES_OPTION) return;
        } else {
            File file = chooser.getSelectedFile();
            try {
                imageService.validate(file.toPath());
                imageService.store(file.toPath(), c.getComplaintId(), ComplaintImage.Kind.RESOLUTION);
            } catch (ImageService.ValidationException | IOException | SQLException ex) {
                JOptionPane.showMessageDialog(this, "Image upload failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        try {
            complaintService.resolve(c.getComplaintId(), currentStaff.getStaffId());
            loadAndRender();
        } catch (SQLException | ComplaintService.ComplaintException ex) {
            showError(ex);
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
