package ui;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** Lightweight horizontal bar chart drawn with Graphics2D - no external charting library needed. */
public class BarChartPanel extends JPanel {

    private Map<String, Integer> data = new LinkedHashMap<>();
    private Color barColor = UITheme.PRIMARY;

    public BarChartPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(400, 220));
    }

    public void setData(Map<String, Integer> data, Color barColor) {
        this.data = data;
        if (barColor != null) this.barColor = barColor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty()) {
            g.setColor(UITheme.TEXT_MUTED);
            g.drawString("No data yet.", 10, 20);
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(UITheme.FONT_SMALL);

        int max = data.values().stream().max(Integer::compareTo).orElse(1);
        if (max == 0) max = 1;

        int rowHeight = Math.max(18, Math.min(28, getHeight() / Math.max(1, data.size())));
        int labelWidth = 130;
        int chartWidth = Math.max(60, getWidth() - labelWidth - 50);
        int y = 6;

        for (Map.Entry<String, Integer> e : data.entrySet()) {
            String label = e.getKey();
            int value = e.getValue();
            int barLength = (int) ((value / (double) max) * chartWidth);

            g2.setColor(UITheme.TEXT_DARK);
            g2.drawString(truncate(label, 18), 4, y + rowHeight / 2 + 4);

            g2.setColor(new Color(230, 233, 238));
            g2.fillRoundRect(labelWidth, y + 2, chartWidth, rowHeight - 6, 6, 6);

            g2.setColor(barColor);
            g2.fillRoundRect(labelWidth, y + 2, Math.max(barLength, value > 0 ? 4 : 0), rowHeight - 6, 6, 6);

            g2.setColor(UITheme.TEXT_DARK);
            g2.drawString(String.valueOf(value), labelWidth + chartWidth + 8, y + rowHeight / 2 + 4);

            y += rowHeight;
        }
        g2.dispose();
        setPreferredSize(new Dimension(getWidth(), y + 10));
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
