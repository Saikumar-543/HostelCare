package dao;

import database.DBConnection;
import enums.ComplaintCategory;
import enums.ComplaintStatus;
import enums.Priority;
import model.Complaint;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ComplaintDAO {

    /** Generates the next sequential ID in the form CMP1001, CMP1002, ... */
    public String generateNextComplaintId(Connection con) throws SQLException {
        String sql = "SELECT complaint_id FROM complaints ORDER BY complaint_id DESC LIMIT 1";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                String last = rs.getString(1);              // e.g. "CMP1007"
                int num = Integer.parseInt(last.substring(3));
                return "CMP" + (num + 1);
            }
        }
        return "CMP1001";
    }

    public Complaint insert(Complaint c) throws SQLException {
        String sql = "INSERT INTO complaints " +
            "(complaint_id, student_id, room_number, category, description, priority, status, " +
            " meal, food_item, machine_number, machine_location) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                String id = generateNextComplaintId(con);
                c.setComplaintId(id);
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, id);
                    ps.setInt(2, c.getStudentId());
                    ps.setString(3, c.getRoomNumber());
                    ps.setString(4, c.getCategory().name());
                    ps.setString(5, c.getDescription());
                    ps.setString(6, c.getPriority().name());
                    ps.setString(7, ComplaintStatus.SUBMITTED.name());
                    ps.setString(8, c.getMeal());
                    ps.setString(9, c.getFoodItem());
                    ps.setString(10, c.getMachineNumber());
                    ps.setString(11, c.getMachineLocation());
                    ps.executeUpdate();
                }
                con.commit();
                c.setStatus(ComplaintStatus.SUBMITTED);
                return c;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public Complaint findById(String complaintId) throws SQLException {
        String sql = baseSelect() + " WHERE co.complaint_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, complaintId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Complaint> findByStudent(int studentId) throws SQLException {
        String sql = baseSelect() + " WHERE co.student_id = ? ORDER BY co.created_at DESC";
        return queryList(sql, studentId);
    }

    public List<Complaint> findByStaff(int staffId) throws SQLException {
        String sql = baseSelect() + " WHERE co.assigned_staff_id = ? ORDER BY co.created_at DESC";
        return queryList(sql, staffId);
    }

    /** Flexible search for the admin complaint table. Any parameter may be null/blank to mean "no filter". */
    public List<Complaint> search(String keyword, ComplaintCategory category,
                                   Priority priority, ComplaintStatus status) throws SQLException {
        StringBuilder sql = new StringBuilder(baseSelect() + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (co.complaint_id LIKE ? OR s.name LIKE ? OR co.room_number LIKE ? OR co.description LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like); params.add(like); params.add(like); params.add(like);
        }
        if (category != null) {
            sql.append(" AND co.category = ?");
            params.add(category.name());
        }
        if (priority != null) {
            sql.append(" AND co.priority = ?");
            params.add(priority.name());
        }
        if (status != null) {
            sql.append(" AND co.status = ?");
            params.add(status.name());
        }
        sql.append(" ORDER BY co.created_at DESC");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Complaint> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    public void updateStatus(String complaintId, ComplaintStatus newStatus) throws SQLException {
        String sql = "UPDATE complaints SET status = ?, updated_at = CURRENT_TIMESTAMP" +
                     (newStatus == ComplaintStatus.RESOLVED ? ", resolved_at = CURRENT_TIMESTAMP" : "") +
                     " WHERE complaint_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setString(2, complaintId);
            ps.executeUpdate();
        }
    }

    public void updatePriority(String complaintId, Priority priority) throws SQLException {
        String sql = "UPDATE complaints SET priority = ?, updated_at = CURRENT_TIMESTAMP WHERE complaint_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, priority.name());
            ps.setString(2, complaintId);
            ps.executeUpdate();
        }
    }

    public void assignStaff(String complaintId, int staffId) throws SQLException {
        String sql = "UPDATE complaints SET assigned_staff_id = ?, status = ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE complaint_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            ps.setString(2, ComplaintStatus.ASSIGNED.name());
            ps.setString(3, complaintId);
            ps.executeUpdate();
        }
    }

    // ---------- Dashboard stat helpers ----------

    public int countByStudent(int studentId, ComplaintStatus status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM complaints WHERE student_id = ?" +
                     (status != null ? " AND status = ?" : "");
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            if (status != null) ps.setString(2, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int countAll(ComplaintStatus status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM complaints" + (status != null ? " WHERE status = ?" : "");
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (status != null) ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int countByPriority(Priority priority) throws SQLException {
        String sql = "SELECT COUNT(*) FROM complaints WHERE priority = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, priority.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int countByStaffAndStatus(int staffId, ComplaintStatus status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM complaints WHERE assigned_staff_id = ?" +
                     (status != null ? " AND status = ?" : "");
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            if (status != null) ps.setString(2, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Complaint count per category, for the admin bar chart. Preserves category enum order. */
    public Map<ComplaintCategory, Integer> countByCategory() throws SQLException {
        Map<ComplaintCategory, Integer> result = new LinkedHashMap<>();
        for (ComplaintCategory cat : ComplaintCategory.values()) result.put(cat, 0);

        String sql = "SELECT category, COUNT(*) AS cnt FROM complaints GROUP BY category";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(ComplaintCategory.valueOf(rs.getString("category")), rs.getInt("cnt"));
            }
        }
        return result;
    }

    /** Complaint count per calendar month (last 6 months), for the monthly trend view. */
    public Map<String, Integer> countByMonth() throws SQLException {
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(created_at, '%b %Y') AS ym, COUNT(*) AS cnt " +
                     "FROM complaints " +
                     "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " +
                     "GROUP BY DATE_FORMAT(created_at, '%Y-%m'), ym " +
                     "ORDER BY DATE_FORMAT(created_at, '%Y-%m')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.put(rs.getString("ym"), rs.getInt("cnt"));
        }
        return result;
    }

    public double averageResolutionHours() throws SQLException {
        String sql = "SELECT AVG(TIMESTAMPDIFF(HOUR, created_at, resolved_at)) AS avg_hrs " +
                     "FROM complaints WHERE resolved_at IS NOT NULL";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble("avg_hrs");
        }
        return 0;
    }

    public ComplaintCategory mostCommonCategory() throws SQLException {
        Map<ComplaintCategory, Integer> counts = countByCategory();
        ComplaintCategory best = null;
        int max = -1;
        for (Map.Entry<ComplaintCategory, Integer> e : counts.entrySet()) {
            if (e.getValue() > max) { max = e.getValue(); best = e.getKey(); }
        }
        return best;
    }

    /** How many active (non-resolved/closed) complaints reference a given washing machine number. */
    public Map<String, Integer> countByMachine() throws SQLException {
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT machine_number, COUNT(*) AS cnt FROM complaints " +
                     "WHERE category = 'WASHING_MACHINE' AND machine_number IS NOT NULL " +
                     "GROUP BY machine_number ORDER BY cnt DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.put(rs.getString("machine_number"), rs.getInt("cnt"));
        }
        return result;
    }

    // ---------- internal helpers ----------

    private List<Complaint> queryList(String sql, int param) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                List<Complaint> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    private String baseSelect() {
        return "SELECT co.*, s.name AS student_name, ms.name AS staff_name " +
               "FROM complaints co " +
               "JOIN students s ON s.student_id = co.student_id " +
               "LEFT JOIN maintenance_staff ms ON ms.staff_id = co.assigned_staff_id";
    }

    private Complaint map(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setComplaintId(rs.getString("complaint_id"));
        c.setStudentId(rs.getInt("student_id"));
        c.setStudentName(rs.getString("student_name"));
        c.setRoomNumber(rs.getString("room_number"));
        c.setCategory(ComplaintCategory.valueOf(rs.getString("category")));
        c.setDescription(rs.getString("description"));
        c.setPriority(Priority.valueOf(rs.getString("priority")));
        c.setStatus(ComplaintStatus.valueOf(rs.getString("status")));
        int staffId = rs.getInt("assigned_staff_id");
        c.setAssignedStaffId(rs.wasNull() ? null : staffId);
        c.setAssignedStaffName(rs.getString("staff_name"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        c.setUpdatedAt(rs.getTimestamp("updated_at"));
        c.setResolvedAt(rs.getTimestamp("resolved_at"));
        c.setMeal(rs.getString("meal"));
        c.setFoodItem(rs.getString("food_item"));
        c.setMachineNumber(rs.getString("machine_number"));
        c.setMachineLocation(rs.getString("machine_location"));
        return c;
    }
}
