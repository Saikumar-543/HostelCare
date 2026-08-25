import ui.LoginFrame;

import javax.swing.*;

/**
 * HostelCare - Entry point.
 * Launches the login screen (Student / Admin / Staff).
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}
