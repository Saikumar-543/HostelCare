package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;

/** Shared colors, fonts and small reusable Swing component builders. */
public final class UITheme {
    public static final Color PRIMARY = new Color(20, 83, 82);
    public static final Color PRIMARY_LIGHT = new Color(31, 112, 108);
    public static final Color BACKGROUND = new Color(246, 248, 247);
    public static final Color CARD = Color.WHITE;
    public static final Color BORDER = new Color(218, 226, 223);
    public static final Color TEXT_DARK = new Color(28, 39, 38);
    public static final Color TEXT_MUTED = new Color(89, 105, 101);
    public static final Color SUCCESS = new Color(25, 126, 79);
    public static final Color WARNING = new Color(190, 116, 18);
    public static final Color DANGER = new Color(184, 54, 54);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD_BODY = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_KPI = new Font("Segoe UI", Font.BOLD, 30);

    private UITheme() { }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            new EmptyBorder(16, 18, 16, 18)));
        return p;
    }

    /** A KPI stat card like the ones in the student/admin dashboards. */
    public static JPanel statCard(String label, String value, Color accent) {
        JPanel p = card();
        p.setLayout(new BorderLayout(4, 4));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(FONT_KPI);
        valueLabel.setForeground(accent != null ? accent : PRIMARY);

        JLabel textLabel = new JLabel(label.toUpperCase());
        textLabel.setFont(FONT_SMALL);
        textLabel.setForeground(TEXT_MUTED);

        p.add(valueLabel, BorderLayout.CENTER);
        p.add(textLabel, BorderLayout.SOUTH);
        return p;
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, PRIMARY, Color.WHITE);
        return b;
    }

    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, new Color(233, 236, 240), TEXT_DARK);
        return b;
    }

    public static JButton dangerButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, DANGER, Color.WHITE);
        return b;
    }

    private static void styleButton(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setFont(FONT_BOLD_BODY);
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setRolloverEnabled(true);
        b.setFocusPainted(true);
        b.setRolloverIcon(null);
        b.addChangeListener(e -> {
            if (b.getModel().isPressed()) b.setBackground(bg.darker());
            else if (b.getModel().isRollover()) b.setBackground(bg.brighter());
            else b.setBackground(bg);
        });
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setForeground(TEXT_DARK);
        table.setBackground(CARD);
        table.setSelectionBackground(new Color(218, 239, 236));
        table.setSelectionForeground(TEXT_DARK);
        table.setRowHeight(36);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD_BODY);
        header.setForeground(TEXT_DARK);
        header.setBackground(new Color(232, 239, 237));
        header.setReorderingAllowed(false);
    }

    /** Small rounded, colored label used for status/priority badges. */
    public static JLabel badge(String text, Color color) {
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        label.setOpaque(false);
        label.setBackground(color);
        label.setForeground(Color.WHITE);
        label.setFont(FONT_SMALL);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(new EmptyBorder(4, 10, 4, 10));
        return label;
    }

    public static JTextField textField() {
        JTextField f = new JTextField();
        f.setFont(FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(8, 10, 8, 10)));
        return f;
    }

    public static JPasswordField passwordField() {
        JPasswordField f = new JPasswordField();
        f.setFont(FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(8, 10, 8, 10)));
        return f;
    }

    public static JTextArea textArea(int rows) {
        JTextArea a = new JTextArea(rows, 20);
        a.setFont(FONT_BODY);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(new EmptyBorder(8, 10, 8, 10));
        return a;
    }

    public static void sectionLabel(JPanel parent, String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BOLD_BODY);
        l.setForeground(TEXT_MUTED);
        parent.add(l);
    }
}
