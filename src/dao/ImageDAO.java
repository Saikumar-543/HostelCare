package dao;

import database.DBConnection;
import model.ComplaintImage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImageDAO {

    public int insert(ComplaintImage img) throws SQLException {
        String sql = "INSERT INTO complaint_images " +
            "(complaint_id, image_name, image_path, image_type, image_size, image_kind) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, img.getComplaintId());
            ps.setString(2, img.getImageName());
            ps.setString(3, img.getImagePath());
            ps.setString(4, img.getImageType());
            ps.setLong(5, img.getImageSize());
            ps.setString(6, img.getKind().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public List<ComplaintImage> findByComplaint(String complaintId, ComplaintImage.Kind kind) throws SQLException {
        String sql = "SELECT * FROM complaint_images WHERE complaint_id = ? AND image_kind = ? ORDER BY image_id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, complaintId);
            ps.setString(2, kind.name());
            try (ResultSet rs = ps.executeQuery()) {
                List<ComplaintImage> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    public int countByComplaint(String complaintId, ComplaintImage.Kind kind) throws SQLException {
        String sql = "SELECT COUNT(*) FROM complaint_images WHERE complaint_id = ? AND image_kind = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, complaintId);
            ps.setString(2, kind.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private ComplaintImage map(ResultSet rs) throws SQLException {
        ComplaintImage img = new ComplaintImage();
        img.setImageId(rs.getInt("image_id"));
        img.setComplaintId(rs.getString("complaint_id"));
        img.setImageName(rs.getString("image_name"));
        img.setImagePath(rs.getString("image_path"));
        img.setImageType(rs.getString("image_type"));
        img.setImageSize(rs.getLong("image_size"));
        img.setKind(ComplaintImage.Kind.valueOf(rs.getString("image_kind")));
        img.setUploadedAt(rs.getTimestamp("uploaded_at"));
        return img;
    }
}
