package dao;

import database.DBConnection;
import enums.ComplaintCategory;
import model.MaintenanceStaff;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT staff_id FROM maintenance_staff WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int register(MaintenanceStaff staff) throws SQLException {
        String sql = "INSERT INTO maintenance_staff " +
                     "(name, email, phone, password, specialization, availability) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, staff.getName());
            ps.setString(2, staff.getEmail());
            ps.setString(3, staff.getPhone());
            ps.setString(4, staff.getPasswordHash());
            ps.setString(5, staff.getSpecialization().name());
            ps.setBoolean(6, staff.isAvailable());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public MaintenanceStaff findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM maintenance_staff WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public MaintenanceStaff findById(int staffId) throws SQLException {
        String sql = "SELECT * FROM maintenance_staff WHERE staff_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<MaintenanceStaff> findAll() throws SQLException {
        List<MaintenanceStaff> list = new ArrayList<>();
        String sql = "SELECT * FROM maintenance_staff ORDER BY name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    /** Staff list for the "assign" picker, sorted so matching-specialization staff appear first. */
    public List<MaintenanceStaff> findCandidatesForCategory(ComplaintCategory category) throws SQLException {
        List<MaintenanceStaff> all = findAll();
        for (MaintenanceStaff s : all) {
            s.setActiveComplaints(countActiveComplaints(s.getStaffId()));
        }
        all.sort((a, b) -> {
            boolean aMatch = a.getSpecialization() == category;
            boolean bMatch = b.getSpecialization() == category;
            if (aMatch != bMatch) return aMatch ? -1 : 1;
            return Integer.compare(a.getActiveComplaints(), b.getActiveComplaints());
        });
        return all;
    }

    public int countActiveComplaints(int staffId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM complaints WHERE assigned_staff_id = ? " +
                     "AND status NOT IN ('RESOLVED','CLOSED')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private MaintenanceStaff map(ResultSet rs) throws SQLException {
        return new MaintenanceStaff(
            rs.getInt("staff_id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("password"),
            ComplaintCategory.valueOf(rs.getString("specialization")),
            rs.getBoolean("availability")
        );
    }
}
