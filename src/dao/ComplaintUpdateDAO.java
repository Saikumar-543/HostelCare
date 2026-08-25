package dao;

import database.DBConnection;
import enums.ComplaintStatus;
import model.ComplaintUpdate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComplaintUpdateDAO {

    public void insert(String complaintId, Integer staffId, ComplaintStatus oldStatus,
                        ComplaintStatus newStatus, String comment) throws SQLException {
        String sql = "INSERT INTO complaint_updates (complaint_id, staff_id, old_status, new_status, comment) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, complaintId);
            if (staffId != null) ps.setInt(2, staffId); else ps.setNull(2, Types.INTEGER);
            ps.setString(3, oldStatus != null ? oldStatus.name() : null);
            ps.setString(4, newStatus != null ? newStatus.name() : null);
            ps.setString(5, comment);
            ps.executeUpdate();
        }
    }

    public List<ComplaintUpdate> findByComplaint(String complaintId) throws SQLException {
        String sql = "SELECT cu.*, ms.name AS staff_name FROM complaint_updates cu " +
                     "LEFT JOIN maintenance_staff ms ON ms.staff_id = cu.staff_id " +
                     "WHERE cu.complaint_id = ? ORDER BY cu.updated_at ASC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, complaintId);
            try (ResultSet rs = ps.executeQuery()) {
                List<ComplaintUpdate> list = new ArrayList<>();
                while (rs.next()) {
                    ComplaintUpdate u = new ComplaintUpdate();
                    u.setUpdateId(rs.getInt("update_id"));
                    u.setComplaintId(rs.getString("complaint_id"));
                    int staffId = rs.getInt("staff_id");
                    u.setStaffId(rs.wasNull() ? null : staffId);
                    u.setStaffName(rs.getString("staff_name"));
                    String oldS = rs.getString("old_status");
                    String newS = rs.getString("new_status");
                    u.setOldStatus(oldS != null ? ComplaintStatus.valueOf(oldS) : null);
                    u.setNewStatus(newS != null ? ComplaintStatus.valueOf(newS) : null);
                    u.setComment(rs.getString("comment"));
                    u.setUpdatedAt(rs.getTimestamp("updated_at"));
                    list.add(u);
                }
                return list;
            }
        }
    }
}
