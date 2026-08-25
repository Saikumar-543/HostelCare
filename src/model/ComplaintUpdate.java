package model;

import enums.ComplaintStatus;

import java.sql.Timestamp;

/** One entry in a complaint's timeline (a status change or a work note). */
public class ComplaintUpdate {
    private int updateId;
    private String complaintId;
    private Integer staffId;
    private String staffName; // populated by DAO join
    private ComplaintStatus oldStatus;
    private ComplaintStatus newStatus;
    private String comment;
    private Timestamp updatedAt;

    public ComplaintUpdate() { }

    public int getUpdateId() { return updateId; }
    public void setUpdateId(int updateId) { this.updateId = updateId; }

    public String getComplaintId() { return complaintId; }
    public void setComplaintId(String complaintId) { this.complaintId = complaintId; }

    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }

    public ComplaintStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(ComplaintStatus oldStatus) { this.oldStatus = oldStatus; }

    public ComplaintStatus getNewStatus() { return newStatus; }
    public void setNewStatus(ComplaintStatus newStatus) { this.newStatus = newStatus; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
