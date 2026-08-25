package ui;

import model.Admin;
import model.MaintenanceStaff;
import model.Student;
import service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/** The application's entry screen. Lets Student / Admin / Staff log in, and students register. */
public class LoginFrame extends JFrame {

    private final AuthService authService = new AuthService();
    private final JTabbedPane tabs = new JTabbedPane();

    public LoginFrame() {
        setTitle("HostelCare - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(430, 520);
        setMinimumSize(new Dimension(400, 480));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);

        tabs.setFont(UITheme.FONT_BOLD_BODY);
        tabs.addTab("Student", buildStudentPanel());
        tabs.addTab("Admin", buildRolePanel(Role.ADMIN));
        tabs.addTab("Staff", buildRolePanel(Role.STAFF));
        add(tabs, BorderLayout.CENTER);

        setVisible(true);
    }

    private enum Role { STUDENT, ADMIN, STAFF }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setBackground(UITheme.PRIMARY);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(24, 20, 20, 20));

        JLabel title = new JLabel("HOSTELCARE");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("Report. Track. Resolve.");
        tagline.setFont(UITheme.FONT_BODY);
        tagline.setForeground(new Color(200, 210, 225));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(tagline);
        return header;
    }

    /** Student tab has both a login form and a "Register" link. */
    private JPanel buildStudentPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.BACKGROUND);

        JPanel loginPanel = buildLoginPanel(Role.STUDENT);
        wrapper.add(loginPanel, BorderLayout.CENTER);

        JPanel registerRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        registerRow.setBackground(UITheme.BACKGROUND);
        JButton registerLink = new JButton("New here? Create a student account");
        registerLink.setBorderPainted(false);
        registerLink.setContentAreaFilled(false);
        registerLink.setForeground(UITheme.PRIMARY);
        registerLink.setFont(UITheme.FONT_BODY);
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLink.addActionListener(e -> new RegisterDialog(this).setVisible(true));
        registerRow.add(registerLink);
        wrapper.add(registerRow, BorderLayout.SOUTH);

        return wrapper;
    }

    private JPanel buildLoginPanel(Role role) {
        JPanel panel = new JPanel();
        panel.setBackground(UITheme.BACKGROUND);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(28, 30, 20, 30));

        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(UITheme.FONT_BOLD_BODY);
        JTextField emailField = UITheme.textField();
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(UITheme.FONT_BOLD_BODY);
        JPasswordField passField = UITheme.passwordField();
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setForeground(UITheme.DANGER);
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginBtn = UITheme.primaryButton("Login as " + roleName(role));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        loginBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passField.getPassword());
            if (email.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Please enter both email and password.");
                return;
            }
            loginBtn.setEnabled(false);
            statusLabel.setForeground(UITheme.DANGER);
            statusLabel.setText("Signing in...");
            try {
                switch (role) {
                    case STUDENT -> {
                        Student s = authService.loginStudent(email, password);
                        dispose();
                        new StudentDashboard(s).setVisible(true);
                    }
                    case ADMIN -> {
                        Admin a = authService.loginAdmin(email, password);
                        dispose();
                        new AdminDashboard(a).setVisible(true);
                    }
                    case STAFF -> {
                        MaintenanceStaff st = authService.loginStaff(email, password);
                        dispose();
                        new StaffDashboard(st).setVisible(true);
                    }
                }
            } catch (AuthService.AuthException ex) {
                statusLabel.setText(ex.getMessage());
                loginBtn.setEnabled(true);
            } catch (SQLException ex) {
                statusLabel.setText("Database error: " + ex.getMessage());
                loginBtn.setEnabled(true);
            }
        });

        for (Component c : new Component[]{emailLabel, emailField, passLabel, passField}) {
            ((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        panel.add(emailLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(emailField);
        panel.add(Box.createVerticalStrut(14));
        panel.add(passLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(passField);
        panel.add(Box.createVerticalStrut(18));
        panel.add(loginBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(statusLabel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel buildRolePanel(Role role) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.BACKGROUND);
        wrapper.add(buildLoginPanel(role), BorderLayout.CENTER);

        JPanel registerRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        registerRow.setBackground(UITheme.BACKGROUND);
        JButton registerLink = new JButton("Need an account? Create a " + roleName(role).toLowerCase() + " account");
        registerLink.setBorderPainted(false);
        registerLink.setContentAreaFilled(false);
        registerLink.setForeground(UITheme.PRIMARY);
        registerLink.setFont(UITheme.FONT_BODY);
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLink.addActionListener(e -> new RoleRegisterDialog(this,
            role == Role.ADMIN ? RoleRegisterDialog.Role.ADMIN : RoleRegisterDialog.Role.STAFF).setVisible(true));
        registerRow.add(registerLink);
        wrapper.add(registerRow, BorderLayout.SOUTH);
        return wrapper;
    }

    private String roleName(Role role) {
        return switch (role) {
            case STUDENT -> "Student";
            case ADMIN -> "Admin";
            case STAFF -> "Staff";
        };
    }
}
