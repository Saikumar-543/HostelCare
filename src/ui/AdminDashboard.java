package ui;

import dao.ComplaintDAO;
import dao.StaffDAO;
import dao.StudentDAO;
import enums.ComplaintCategory;
import enums.ComplaintStatus;
import enums.Priority;
import model.Admin;
import model.Complaint;
import model.MaintenanceStaff;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboard extends JFrame {

    private final Admin admin;
    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private final StaffDAO staffDAO = new StaffDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    // Dashboard tab widgets
    private JLabel kpiTotal, kpiPending, kpiInProgress, kpiResolved, kpiHigh, kpiCritical;
    private BarChartPanel categoryChart, monthChart;

    // Complaints tab widgets
    private DefaultTableModel complaintsModel;
    private JTable complaintsTable;
    private JTextField searchField;
    private JComboBox<String> categoryFilter, priorityFilter, statusFilter;

    // Students / Staff tabs
    private DefaultTableModel studentsModel;
    private DefaultTableModel staffModel;

    // Reports tab
    private JLabel repTotal, repPending, repInProgress, repResolved, repCritical, repAvgTime, repTopCategory;
    private JLabel repFood, repWashing, repWifi, repCleaning;

    public AdminDashboard(Admin admin) {
        this.admin = admin;
        setTitle("HostelCare - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 720);
        setMinimumSize(new Dimension(980, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);

        contentPanel.setBackground(UITheme.BACKGROUND);
        contentPanel.add(buildDashboardPanel(), "Dashboard");
        contentPanel.add(buildComplaintsPanel(), "Complaints");
        contentPanel.add(buildStudentsPanel(), "Students");
        contentPanel.add(buildStaffPanel(), "Staff");
        contentPanel.add(buildReportsPanel(), "Reports");
        add(contentPanel, BorderLayout.CENTER);

        refreshDashboard();
        refreshComplaints();
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
        JLabel profile = new JLabel("Admin: " + admin.getName());
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

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(23, 44, 72));
        sidebar.setPreferredSize(new Dimension(180, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(16, 0, 0, 0));

        String[] items = {"Dashboard", "Complaints", "Students", "Staff", "Reports"};
        for (String item : items) {
            JButton b = new JButton(item);
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setBorder(new EmptyBorder(10, 20, 10, 20));
            b.setForeground(Color.WHITE);
            b.setBackground(new Color(23, 44, 72));
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setFont(UITheme.FONT_BOLD_BODY);
            b.addActionListener(e -> {
                cardLayout.show(contentPanel, item);
                if (item.equals("Dashboard")) refreshDashboard();
                if (item.equals("Complaints")) refreshComplaints();
                if (item.equals("Students")) refreshStudents();
                if (item.equals("Staff")) refreshStaff();
                if (item.equals("Reports")) refreshReports();
            });
            sidebar.add(b);
        }
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    // ==================== DASHBOARD TAB ====================

    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel heading = new JLabel("HOSTEL OVERVIEW");
        heading.setFont(UITheme.FONT_TITLE);

        JPanel kpiRow1 = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiRow1.setOpaque(false);
        kpiTotal = new JLabel("0"); kpiPending = new JLabel("0");
        kpiInProgress = new JLabel("0"); kpiResolved = new JLabel("0");
        kpiRow1.add(kpiCard("TOTAL", kpiTotal, UITheme.PRIMARY));
        kpiRow1.add(kpiCard("PENDING", kpiPending, new Color(245, 124, 0)));
        kpiRow1.add(kpiCard("IN PROGRESS", kpiInProgress, new Color(126, 87, 194)));
        kpiRow1.add(kpiCard("RESOLVED", kpiResolved, UITheme.SUCCESS));

        JPanel kpiRow2 = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiRow2.setOpaque(false);
        kpiHigh = new JLabel("0"); kpiCritical = new JLabel("0");
        kpiRow2.add(kpiCard("HIGH PRIORITY", kpiHigh, Priority.HIGH.getColor()));
        kpiRow2.add(kpiCard("CRITICAL", kpiCritical, Priority.CRITICAL.getColor()));
        kpiRow2.add(new JPanel() {{ setOpaque(false); }});
        kpiRow2.add(new JPanel() {{ setOpaque(false); }});

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiRow1.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiRow2.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(heading);
        top.add(Box.createVerticalStrut(14));
        top.add(kpiRow1);
        top.add(Box.createVerticalStrut(10));
        top.add(kpiRow2);

        JPanel charts = new JPanel(new GridLayout(1, 2, 16, 0));
        charts.setOpaque(false);
        charts.setBorder(new EmptyBorder(16, 0, 0, 0));

        JPanel catCard = UITheme.card();
        catCard.setLayout(new BorderLayout());
        JLabel catTitle = new JLabel("Complaints by Category");
        catTitle.setFont(UITheme.FONT_BOLD_BODY);
        categoryChart = new BarChartPanel();
        catCard.add(catTitle, BorderLayout.NORTH);
        catCard.add(new JScrollPane(categoryChart), BorderLayout.CENTER);

        JPanel monthCard = UITheme.card();
        monthCard.setLayout(new BorderLayout());
        JLabel monthTitle = new JLabel("Monthly Complaints (last 6 months)");
        monthTitle.setFont(UITheme.FONT_BOLD_BODY);
        monthChart = new BarChartPanel();
        monthCard.add(monthTitle, BorderLayout.NORTH);
        monthCard.add(new JScrollPane(monthChart), BorderLayout.CENTER);

        charts.add(catCard);
        charts.add(monthCard);

        panel.add(top, BorderLayout.NORTH);
        panel.add(charts, BorderLayout.CENTER);
        return panel;
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

    private void refreshDashboard() {
        try {
            kpiTotal.setText(String.valueOf(complaintDAO.countAll(null)));
            kpiPending.setText(String.valueOf(complaintDAO.countAll(ComplaintStatus.PENDING)));
            kpiInProgress.setText(String.valueOf(complaintDAO.countAll(ComplaintStatus.IN_PROGRESS)));
            kpiResolved.setText(String.valueOf(complaintDAO.countAll(ComplaintStatus.RESOLVED)));
            kpiHigh.setText(String.valueOf(complaintDAO.countByPriority(Priority.HIGH)));
            kpiCritical.setText(String.valueOf(complaintDAO.countByPriority(Priority.CRITICAL)));

            Map<String, Integer> catData = new LinkedHashMap<>();
            for (Map.Entry<ComplaintCategory, Integer> e : complaintDAO.countByCategory().entrySet()) {
                if (e.getValue() > 0) catData.put(e.getKey().getDisplayName(), e.getValue());
            }
            categoryChart.setData(catData, UITheme.PRIMARY);
            monthChart.setData(complaintDAO.countByMonth(), new Color(126, 87, 194));
        } catch (SQLException e) {
            showDbError(e);
        }
    }

    // ==================== COMPLAINTS TAB ====================

    private JPanel buildComplaintsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel heading = new JLabel("Complaints");
        heading.setFont(UITheme.FONT_TITLE);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filterRow.setOpaque(false);
        searchField = UITheme.textField();
        searchField.setPreferredSize(new Dimension(200, 32));

        categoryFilter = new JComboBox<>(catNames());
        priorityFilter = new JComboBox<>(prefixAll(Priority.values()));
        statusFilter = new JComboBox<>(prefixAll(ComplaintStatus.values()));

        JButton searchBtn = UITheme.primaryButton("Search");
        searchBtn.addActionListener(e -> refreshComplaints());

        filterRow.add(new JLabel("Search:"));
        filterRow.add(searchField);
        filterRow.add(new JLabel("Category:"));
        filterRow.add(categoryFilter);
        filterRow.add(new JLabel("Priority:"));
        filterRow.add(priorityFilter);
        filterRow.add(new JLabel("Status:"));
        filterRow.add(statusFilter);
        filterRow.add(searchBtn);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        top.add(filterRow, BorderLayout.SOUTH);

        complaintsModel = new DefaultTableModel(
            new Object[]{"ID", "Student", "Category", "Priority", "Status", "Action"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 5; }
        };
        complaintsTable = new JTable(complaintsModel);
        UITheme.styleTable(complaintsTable);
        complaintsTable.setRowHeight(30);
        complaintsTable.setFont(UITheme.FONT_BODY);
        complaintsTable.getTableHeader().setFont(UITheme.FONT_BOLD_BODY);
        new TableButtonColumn(complaintsTable, "View", modelRow -> {
            String id = (String) complaintsModel.getValueAt(modelRow, 0);
            new ComplaintDetails(this, id, ComplaintDetails.Viewer.ADMIN, null).setVisible(true);
            refreshComplaints();
            refreshDashboard();
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(complaintsTable), BorderLayout.CENTER);
        return panel;
    }

    private String[] catNames() {
        String[] arr = new String[ComplaintCategory.values().length + 1];
        arr[0] = "All";
        for (int i = 0; i < ComplaintCategory.values().length; i++) arr[i + 1] = ComplaintCategory.values()[i].getDisplayName();
        return arr;
    }

    private String[] prefixAll(Enum<?>[] values) {
        String[] arr = new String[values.length + 1];
        arr[0] = "All";
        for (int i = 0; i < values.length; i++) arr[i + 1] = values[i].name();
        return arr;
    }

    private void refreshComplaints() {
        try {
            String keyword = searchField != null ? searchField.getText() : null;
            ComplaintCategory cat = null;
            if (categoryFilter != null && categoryFilter.getSelectedIndex() > 0) {
                cat = ComplaintCategory.values()[categoryFilter.getSelectedIndex() - 1];
            }
            Priority pr = null;
            if (priorityFilter != null && priorityFilter.getSelectedIndex() > 0) {
                pr = Priority.values()[priorityFilter.getSelectedIndex() - 1];
            }
            ComplaintStatus st = null;
            if (statusFilter != null && statusFilter.getSelectedIndex() > 0) {
                st = ComplaintStatus.values()[statusFilter.getSelectedIndex() - 1];
            }

            List<Complaint> results = complaintDAO.search(keyword, cat, pr, st);
            complaintsModel.setRowCount(0);
            for (Complaint c : results) {
                complaintsModel.addRow(new Object[]{
                    c.getComplaintId(), c.getStudentName(), c.getCategory().getDisplayName(),
                    c.getPriority(), c.getStatus(), "View"
                });
            }
        } catch (SQLException e) {
            showDbError(e);
        }
    }

    // ==================== STUDENTS TAB ====================

    private JPanel buildStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel heading = new JLabel("Students");
        heading.setFont(UITheme.FONT_TITLE);

        studentsModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Email", "Phone", "Room", "Block"}, 0);
        JTable table = new JTable(studentsModel);
        table.setRowHeight(28);
        table.setFont(UITheme.FONT_BODY);
        table.getTableHeader().setFont(UITheme.FONT_BOLD_BODY);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void refreshStudents() {
        // Simple approach for a college project: query directly here via a lightweight DAO call pattern.
        try {
            studentsModel.setRowCount(0);
            for (var s : new dao.StudentDAO().findAllForAdmin()) {
                studentsModel.addRow(new Object[]{
                    s.getStudentId(), s.getName(), s.getEmail(), s.getPhone(), s.getRoomNumber(), s.getHostelBlock()
                });
            }
        } catch (SQLException e) {
            showDbError(e);
        }
    }

    // ==================== STAFF TAB ====================

    private JPanel buildStaffPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel heading = new JLabel("Maintenance Staff");
        heading.setFont(UITheme.FONT_TITLE);

        staffModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Phone", "Specialization", "Available", "Active Complaints"}, 0);
        JTable table = new JTable(staffModel);
        table.setRowHeight(28);
        table.setFont(UITheme.FONT_BODY);
        table.getTableHeader().setFont(UITheme.FONT_BOLD_BODY);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void refreshStaff() {
        try {
            staffModel.setRowCount(0);
            for (MaintenanceStaff s : staffDAO.findAll()) {
                int active = staffDAO.countActiveComplaints(s.getStaffId());
                staffModel.addRow(new Object[]{
                    s.getStaffId(), s.getName(), s.getPhone(),
                    s.getSpecialization().getDisplayName(),
                    s.isAvailable() ? "Yes" : "No", active
                });
            }
        } catch (SQLException e) {
            showDbError(e);
        }
    }

    // ==================== REPORTS TAB ====================

    private JPanel buildReportsPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(UITheme.BACKGROUND);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel heading = new JLabel("Reports");
        heading.setFont(UITheme.FONT_TITLE);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(heading);
        panel.add(Box.createVerticalStrut(16));

        JPanel grid1 = new JPanel(new GridLayout(1, 5, 12, 0));
        grid1.setOpaque(false);
        grid1.setAlignmentX(Component.LEFT_ALIGNMENT);
        repTotal = new JLabel("0"); repPending = new JLabel("0"); repInProgress = new JLabel("0");
        repResolved = new JLabel("0"); repCritical = new JLabel("0");
        grid1.add(kpiCard("Total", repTotal, UITheme.PRIMARY));
        grid1.add(kpiCard("Pending", repPending, new Color(245, 124, 0)));
        grid1.add(kpiCard("In Progress", repInProgress, new Color(126, 87, 194)));
        grid1.add(kpiCard("Resolved", repResolved, UITheme.SUCCESS));
        grid1.add(kpiCard("Critical", repCritical, Priority.CRITICAL.getColor()));
        panel.add(grid1);
        panel.add(Box.createVerticalStrut(16));

        JPanel infoCard = UITheme.card();
        infoCard.setLayout(new GridLayout(0, 2, 10, 8));
        infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        repAvgTime = new JLabel("-");
        repTopCategory = new JLabel("-");
        infoCard.add(boldLabel("Average Resolution Time:"));
        infoCard.add(repAvgTime);
        infoCard.add(boldLabel("Most Common Category:"));
        infoCard.add(repTopCategory);
        panel.add(infoCard);
        panel.add(Box.createVerticalStrut(16));

        JLabel catLabel = new JLabel("Category Spotlight");
        catLabel.setFont(UITheme.FONT_HEADING);
        catLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(catLabel);
        panel.add(Box.createVerticalStrut(8));

        JPanel grid2 = new JPanel(new GridLayout(1, 4, 12, 0));
        grid2.setOpaque(false);
        grid2.setAlignmentX(Component.LEFT_ALIGNMENT);
        repFood = new JLabel("0"); repWashing = new JLabel("0"); repWifi = new JLabel("0"); repCleaning = new JLabel("0");
        grid2.add(kpiCard("Food Complaints", repFood, UITheme.PRIMARY));
        grid2.add(kpiCard("Washing Machine", repWashing, UITheme.PRIMARY));
        grid2.add(kpiCard("Wi-Fi", repWifi, UITheme.PRIMARY));
        grid2.add(kpiCard("Cleaning", repCleaning, UITheme.PRIMARY));
        panel.add(grid2);

        return panel;
    }

    private JLabel boldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BOLD_BODY);
        return l;
    }

    private void refreshReports() {
        try {
            repTotal.setText(String.valueOf(complaintDAO.countAll(null)));
            repPending.setText(String.valueOf(complaintDAO.countAll(ComplaintStatus.PENDING)));
            repInProgress.setText(String.valueOf(complaintDAO.countAll(ComplaintStatus.IN_PROGRESS)));
            repResolved.setText(String.valueOf(complaintDAO.countAll(ComplaintStatus.RESOLVED)));
            repCritical.setText(String.valueOf(complaintDAO.countByPriority(Priority.CRITICAL)));

            double avgHrs = complaintDAO.averageResolutionHours();
            repAvgTime.setText(avgHrs > 0 ? String.format("%.1f hours", avgHrs) : "Not enough data yet");

            ComplaintCategory top = complaintDAO.mostCommonCategory();
            repTopCategory.setText(top != null ? top.getDisplayName() : "-");

            Map<ComplaintCategory, Integer> byCat = complaintDAO.countByCategory();
            int food = byCat.getOrDefault(ComplaintCategory.FOOD_QUALITY, 0)
                     + byCat.getOrDefault(ComplaintCategory.FOOD_HYGIENE, 0)
                     + byCat.getOrDefault(ComplaintCategory.DINING_HALL, 0);
            repFood.setText(String.valueOf(food));
            repWashing.setText(String.valueOf(byCat.getOrDefault(ComplaintCategory.WASHING_MACHINE, 0)));
            repWifi.setText(String.valueOf(byCat.getOrDefault(ComplaintCategory.WIFI, 0)));
            repCleaning.setText(String.valueOf(byCat.getOrDefault(ComplaintCategory.CLEANING, 0)));
        } catch (SQLException e) {
            showDbError(e);
        }
    }

    private void showDbError(SQLException e) {
        JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}
