package model;

import enums.ComplaintCategory;
import enums.ComplaintStatus;
import enums.Priority;

import java.sql.Timestamp;

/** A single complaint submitted by a student. */
public class Complaint {
    private String complaintId;      // e.g. CMP1001
    private int studentId;
    private String studentName;      // populated by DAO joins for display
    private String roomNumber;
    private ComplaintCategory category;
    private String description;
    private Priority priority;
    private ComplaintStatus status;
    private Integer assignedStaffId; // null until assigned
    private String assignedStaffName; // populated by DAO joins for display
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp resolvedAt;

    // Optional, category-specific fields (nullable)
    private String meal;          // FOOD_* : Breakfast / Lunch / Dinner
    private String foodItem;      // FOOD_*
    private String machineNumber; // WASHING_MACHINE
    private String machineLocation; // WASHING_MACHINE

    public Complaint() { }

    public String getComplaintId() { return complaintId; }
    public void setComplaintId(String complaintId) { this.complaintId = complaintId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public ComplaintCategory getCategory() { return category; }
    public void setCategory(ComplaintCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; }

    public Integer getAssignedStaffId() { return assignedStaffId; }
    public void setAssignedStaffId(Integer assignedStaffId) { this.assignedStaffId = assignedStaffId; }

    public String getAssignedStaffName() { return assignedStaffName; }
    public void setAssignedStaffName(String assignedStaffName) { this.assignedStaffName = assignedStaffName; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public Timestamp getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Timestamp resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getMeal() { return meal; }
    public void setMeal(String meal) { this.meal = meal; }

    public String getFoodItem() { return foodItem; }
    public void setFoodItem(String foodItem) { this.foodItem = foodItem; }

    public String getMachineNumber() { return machineNumber; }
    public void setMachineNumber(String machineNumber) { this.machineNumber = machineNumber; }

    public String getMachineLocation() { return machineLocation; }
    public void setMachineLocation(String machineLocation) { this.machineLocation = machineLocation; }

    @Override
    public String toString() {
        return complaintId + " - " + category.getDisplayName() + " - " + status;
    }
}
