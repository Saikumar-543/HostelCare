package ui;

import enums.ComplaintCategory;
import service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/** Account creation form for administrative and maintenance roles. */
public class RoleRegisterDialog extends JDialog {
    public enum Role { ADMIN, STAFF }

    private final AuthService authService = new AuthService();

    public RoleRegisterDialog(JFrame parent, Role role) {
        super(parent, "Create " + roleName(role) + " Account", true);
        setSize(440, role == Role.STAFF ? 570 : 470);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(UITheme.BACKGROUND);

        JPanel form = new JPanel();
        form.setBackground(UITheme.BACKGROUND);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(22, 26, 22, 26));

        JLabel title = new JLabel("Create " + roleName(role) + " Account");
        title.setFont(UITheme.FONT_TITLE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(6));

        JLabel subtitle = new JLabel(role == Role.STAFF
            ? "Set up a maintenance profile for assigned work."
            : "Set up an administrative account for hostel operations.");
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(subtitle);
        form.add(Box.createVerticalStrut(18));

        JTextField nameField = labeledField(form, "Full Name");
        JTextField emailField = labeledField(form, "Email");
        JTextField phoneField = role == Role.STAFF ? labeledField(form, "Phone (10 digits)") : null;
        JPasswordField passwordField = labeledPasswordField(form, "Password (min 6 characters)");
        JComboBox<ComplaintCategory> specialization = null;
        if (role == Role.STAFF) {
            specialization = new JComboBox<>(ComplaintCategory.values());
            addLabeledComponent(form, "Specialization", specialization);
        }

        JLabel status = new JLabel(" ");
        status.setFont(UITheme.FONT_SMALL);
        status.setForeground(UITheme.DANGER);
        status.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton create = UITheme.primaryButton("Create " + roleName(role) + " Account");
        create.setAlignmentX(Component.LEFT_ALIGNMENT);
        create.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        JComboBox<ComplaintCategory> finalSpecialization = specialization;
        create.addActionListener(e -> {
            create.setEnabled(false);
            try {
                if (role == Role.ADMIN) {
                    authService.registerAdmin(nameField.getText(), emailField.getText(),
                        new String(passwordField.getPassword()));
                } else {
                    authService.registerStaff(nameField.getText(), emailField.getText(), phoneField.getText(),
                        new String(passwordField.getPassword()),
                        (ComplaintCategory) finalSpecialization.getSelectedItem());
                }
                JOptionPane.showMessageDialog(this, "Account created. You can now log in.",
                    "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (AuthService.AuthException ex) {
                status.setText(ex.getMessage());
                create.setEnabled(true);
            } catch (SQLException ex) {
                status.setText("Database error: " + ex.getMessage());
                create.setEnabled(true);
            }
        });

        form.add(create);
        form.add(Box.createVerticalStrut(8));
        form.add(status);
        setContentPane(new JScrollPane(form));
    }

    private static String roleName(Role role) {
        return role == Role.ADMIN ? "Admin" : "Staff";
    }

    private JTextField labeledField(JPanel parent, String label) {
        JTextField field = UITheme.textField();
        addLabeledComponent(parent, label, field);
        return field;
    }

    private JPasswordField labeledPasswordField(JPanel parent, String label) {
        JPasswordField field = UITheme.passwordField();
        addLabeledComponent(parent, label, field);
        return field;
    }

    private void addLabeledComponent(JPanel parent, String label, JComponent component) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(UITheme.FONT_BOLD_BODY);
        labelComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        parent.add(labelComponent);
        parent.add(Box.createVerticalStrut(4));
        parent.add(component);
        parent.add(Box.createVerticalStrut(12));
    }
}
