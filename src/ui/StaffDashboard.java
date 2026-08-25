package ui;

import dao.ComplaintDAO;
import enums.ComplaintStatus;
import model.Complaint;
import model.MaintenanceStaff;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class StaffDashboard extends JFrame {

    private final MaintenanceStaff staff;
    private final ComplaintDAO complaintDAO = new ComplaintDAO();

    private JLabel assignedCard, pendingCard, inProgressCard, completedCard;
    private DefaultTableModel tableModel;
    private JTable table;

    public StaffDashboard(MaintenanceStaff staff) {
        this.staff = staff;
        setTitle("HostelCare - Staff Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 640);
        setMinimumSize(new Dimension(820, 520));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("HOSTELCARE");
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(Color.WHITE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        JLabel profile = new JLabel(staff.getName() + "  |  " + staff.getSpecialization().getDisplayName());
        profile.setForeground(Color.WHITE);
        profile.setFont(UITheme.FONT_BODY);
        JButton logout = new JButton("Logout");
        logout.setFocusPainted(false);
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });
        right.add(profile);
        right.add(logout);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(UITheme.BACKGROUND);
        body.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel welcome = new JLabel("Welcome, " + staff.getName());
        welcome.setFont(UITheme.FONT_TITLE);

        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setOpaque(false);
        assignedCard = new JLabel("0");
        pendingCard = new JLabel("0");
        inProgressCard = new JLabel("0");
        completedCard = new JLabel("0");
        cards.add(kpiCard("ASSIGNED", assignedCard, UITheme.PRIMARY));
        cards.add(kpiCard("PENDING START", pendingCard, new Color(245, 124, 0)));
        cards.add(kpiCard("IN PROGRESS", inProgressCard, new Color(126, 87, 194)));
        cards.add(kpiCard("COMPLETED", completedCard, UITheme.SUCCESS));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(welcome);
        top.add(Box.createVerticalStrut(14));
        top.add(cards);

        JLabel listLabel = new JLabel("My Assigned Complaints");
        listLabel.setFont(UITheme.FONT_HEADING);

        tableModel = new DefaultTableModel(
            new Object[]{"ID", "Room", "Category", "Priority", "Status", "Action"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 5; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setRowHeight(30);
        table.setFont(UITheme.FONT_BODY);
        table.getTableHeader().setFont(UITheme.FONT_BOLD_BODY);
        new TableButtonColumn(table, "View", modelRow -> {
            String id = (String) tableModel.getValueAt(modelRow, 0);
            new ComplaintDetails(this, id, ComplaintDetails.Viewer.STAFF, staff).setVisible(true);
            refresh();
        });

        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setOpaque(false);
        tableWrap.add(listLabel, BorderLayout.NORTH);
        tableWrap.add(new JScrollPane(table), BorderLayout.CENTER);

        body.add(top, BorderLayout.NORTH);
        body.add(tableWrap, BorderLayout.CENTER);
        return body;
    }

    private JPanel kpiCard(String label, JLabel valueLabel, Color color) {
        JPanel p = UITheme.card();
        p.setLayout(new BorderLayout());
        valueLabel.setFont(UITheme.FONT_KPI);
        valueLabel.setForeground(color);
        JLabel textLabel = new JLabel(label);
        textLabel.setFont(UITheme.FONT_SMALL);
        textLabel.setForeground(UITheme.TEXT_MUTED);
        p.add(valueLabel, BorderLayout.CENTER);
        p.add(textLabel, BorderLayout.SOUTH);
        return p;
    }

    public void refresh() {
        try {
            int total = complaintDAO.countByStaffAndStatus(staff.getStaffId(), null);
            int assigned = complaintDAO.countByStaffAndStatus(staff.getStaffId(), ComplaintStatus.ASSIGNED);
            int inProgress = complaintDAO.countByStaffAndStatus(staff.getStaffId(), ComplaintStatus.IN_PROGRESS);
            int resolved = complaintDAO.countByStaffAndStatus(staff.getStaffId(), ComplaintStatus.RESOLVED)
                          + complaintDAO.countByStaffAndStatus(staff.getStaffId(), ComplaintStatus.CLOSED);

            assignedCard.setText(String.valueOf(total));
            pendingCard.setText(String.valueOf(assigned));
            inProgressCard.setText(String.valueOf(inProgress));
            completedCard.setText(String.valueOf(resolved));

            List<Complaint> complaints = complaintDAO.findByStaff(staff.getStaffId());
            tableModel.setRowCount(0);
            for (Complaint c : complaints) {
                tableModel.addRow(new Object[]{
                    c.getComplaintId(), c.getRoomNumber(), c.getCategory().getDisplayName(),
                    c.getPriority(), c.getStatus(), "View"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Could not load complaints: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
