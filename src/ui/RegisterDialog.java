package ui;

import service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/** Modal dialog for new student self-registration. */
public class RegisterDialog extends JDialog {

    private final AuthService authService = new AuthService();

    public RegisterDialog(JFrame parent) {
        super(parent, "Create Student Account", true);
        setSize(440, 500);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(UITheme.BACKGROUND);

        JPanel form = new JPanel();
        form.setBackground(UITheme.BACKGROUND);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Register as a Student");
            title.setFont(UITheme.FONT_TITLE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField nameField = labeledField(form, "Full Name");
        JTextField emailField = labeledField(form, "Email");
        JTextField phoneField = labeledField(form, "Phone (10 digits)");
        JPasswordField passField = labeledPasswordField(form, "Password (min 6 characters)");
        JTextField roomField = labeledField(form, "Room Number (e.g. A-204)");
        JTextField blockField = labeledField(form, "Hostel Block (e.g. Block A)");

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setForeground(UITheme.DANGER);
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton registerBtn = UITheme.primaryButton("Create Account");
        registerBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        registerBtn.addActionListener(e -> {
            registerBtn.setEnabled(false);
            statusLabel.setForeground(UITheme.DANGER);
            try {
                authService.register(
                    nameField.getText(), emailField.getText(), phoneField.getText(),
                    new String(passField.getPassword()), roomField.getText(), blockField.getText()
                );
                JOptionPane.showMessageDialog(this,
                    "Account created! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (AuthService.AuthException ex) {
                statusLabel.setText(ex.getMessage());
                registerBtn.setEnabled(true);
            } catch (SQLException ex) {
                statusLabel.setText("Database error: " + ex.getMessage());
                registerBtn.setEnabled(true);
            }
        });

        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(14));
        form.add(registerBtn);
        form.add(Box.createVerticalStrut(8));
        form.add(statusLabel);

        setContentPane(new JScrollPane(form));
    }

    private JTextField labeledField(JPanel parent, String label) {
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BOLD_BODY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField f = UITheme.textField();
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        parent.add(l);
        parent.add(Box.createVerticalStrut(4));
        parent.add(f);
        parent.add(Box.createVerticalStrut(12));
        return f;
    }

    private JPasswordField labeledPasswordField(JPanel parent, String label) {
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BOLD_BODY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPasswordField f = UITheme.passwordField();
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        parent.add(l);
        parent.add(Box.createVerticalStrut(4));
        parent.add(f);
        parent.add(Box.createVerticalStrut(12));
        return f;
    }
}
