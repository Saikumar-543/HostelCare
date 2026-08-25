package ui;

import dao.ComplaintDAO;
import enums.ComplaintStatus;
import model.Complaint;
import model.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class StudentDashboard extends JFrame {

    private final Student student;
    private final ComplaintDAO complaintDAO = new ComplaintDAO();

    private JLabel totalCard, pendingCard, resolvedCard;
    private DefaultTableModel tableModel;
    private JTable table;

    public StudentDashboard(Student student) {
        this.student = student;
        setTitle("HostelCare - Student Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 640);
        setMinimumSize(new Dimension(800, 520));
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
        JLabel profile = new JLabel(student.getName() + "  |  Room " + student.getRoomNumber());
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

        JLabel welcome = new JLabel("Welcome, " + student.getName());
        welcome.setFont(UITheme.FONT_TITLE);
        welcome.setForeground(UITheme.TEXT_DARK);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(welcome, BorderLayout.WEST);

        JButton newComplaint = UITheme.primaryButton("+ New Complaint");
        newComplaint.addActionListener(e -> {
            ComplaintForm form = new ComplaintForm(this, student);
            form.setVisible(true);
            refresh();
        });
        JPanel newBtnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        newBtnWrap.setOpaque(false);
        newBtnWrap.add(newComplaint);
        top.add(newBtnWrap, BorderLayout.EAST);

        JPanel cards = new JPanel(new GridLayout(1, 3, 16, 0));
        cards.setOpaque(false);
        cards.setBorder(new EmptyBorder(16, 0, 16, 0));
        totalCard = new JLabel("0");
        pendingCard = new JLabel("0");
        resolvedCard = new JLabel("0");
        cards.add(wrapStat("TOTAL", totalCard, UITheme.PRIMARY));
        cards.add(wrapStat("PENDING", pendingCard, new Color(245, 124, 0)));
        cards.add(wrapStat("RESOLVED", resolvedCard, UITheme.SUCCESS));

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(top);
        north.add(cards);

        JLabel recent = new JLabel("Recent Complaints");
        recent.setFont(UITheme.FONT_HEADING);

        tableModel = new DefaultTableModel(
            new Object[]{"ID", "Category", "Priority", "Status", "Submitted", "Action"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 5; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setRowHeight(32);
        table.setFont(UITheme.FONT_BODY);
        table.getTableHeader().setFont(UITheme.FONT_BOLD_BODY);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        new TableButtonColumn(table, "View", modelRow -> {
            String id = (String) tableModel.getValueAt(modelRow, 0);
            new ComplaintDetails(this, id, ComplaintDetails.Viewer.STUDENT, null).setVisible(true);
            refresh();
        });

        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setOpaque(false);
        tableWrap.add(recent, BorderLayout.NORTH);
        tableWrap.add(new JScrollPane(table), BorderLayout.CENTER);

        body.add(north, BorderLayout.NORTH);
        body.add(tableWrap, BorderLayout.CENTER);
        return body;
    }

    private JPanel wrapStat(String label, JLabel valueLabel, Color color) {
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
            int total = complaintDAO.countByStudent(student.getStudentId(), null);
            int resolved = complaintDAO.countByStudent(student.getStudentId(), ComplaintStatus.RESOLVED)
                          + complaintDAO.countByStudent(student.getStudentId(), ComplaintStatus.CLOSED);
            int pending = total - resolved
                          - complaintDAO.countByStudent(student.getStudentId(), ComplaintStatus.IN_PROGRESS);
            totalCard.setText(String.valueOf(total));
            pendingCard.setText(String.valueOf(Math.max(pending, 0)));
            resolvedCard.setText(String.valueOf(resolved));

            List<Complaint> complaints = complaintDAO.findByStudent(student.getStudentId());
            tableModel.setRowCount(0);
            for (Complaint c : complaints) {
                tableModel.addRow(new Object[]{
                    c.getComplaintId(),
                    c.getCategory().getDisplayName(),
                    c.getPriority(),
                    c.getStatus(),
                    c.getCreatedAt(),
                    "View"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Could not load complaints: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
