package service;

import dao.ComplaintDAO;
import dao.ComplaintUpdateDAO;
import enums.ComplaintCategory;
import enums.ComplaintStatus;
import enums.Priority;
import model.Complaint;

import java.sql.SQLException;

/**
 * Business logic for the complaint lifecycle. UI classes call this instead
 * of touching ComplaintDAO directly, so validation rules live in one place.
 */
public class ComplaintService {

    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private final ComplaintUpdateDAO updateDAO = new ComplaintUpdateDAO();

    public static class ComplaintException extends Exception {
        public ComplaintException(String message) { super(message); }
    }

    public Complaint submit(int studentId, String roomNumber, ComplaintCategory category,
                             String description, String meal, String foodItem,
                             String machineNumber, String machineLocation) throws SQLException, ComplaintException {
        if (description == null || description.trim().length() < 5) {
            throw new ComplaintException("Please enter a more detailed description (at least 5 characters).");
        }
        if (roomNumber == null || roomNumber.isBlank()) {
            throw new ComplaintException("Room number is required.");
        }

        Complaint c = new Complaint();
        c.setStudentId(studentId);
        c.setRoomNumber(roomNumber.trim());
        c.setCategory(category);
        c.setDescription(description.trim());
        c.setPriority(PriorityService.suggest(category, description));
        c.setMeal(meal);
        c.setFoodItem(foodItem);
        c.setMachineNumber(machineNumber);
        c.setMachineLocation(machineLocation);

        Complaint saved = complaintDAO.insert(c);
        updateDAO.insert(saved.getComplaintId(), null, null, ComplaintStatus.SUBMITTED,
                "Complaint submitted by student.");
        // Auto-advance SUBMITTED -> PENDING immediately; it now awaits admin triage.
        complaintDAO.updateStatus(saved.getComplaintId(), ComplaintStatus.PENDING);
        updateDAO.insert(saved.getComplaintId(), null, ComplaintStatus.SUBMITTED, ComplaintStatus.PENDING,
                "Awaiting admin review.");
        saved.setStatus(ComplaintStatus.PENDING);
        return saved;
    }

    public void assignStaff(String complaintId, int staffId, String staffName) throws SQLException, ComplaintException {
        Complaint c = complaintDAO.findById(complaintId);
        if (c == null) throw new ComplaintException("Complaint not found.");
        if (c.getStatus() != ComplaintStatus.PENDING && c.getStatus() != ComplaintStatus.ASSIGNED) {
            throw new ComplaintException("Only PENDING complaints can be assigned to staff.");
        }
        ComplaintStatus old = c.getStatus();
        complaintDAO.assignStaff(complaintId, staffId);
        updateDAO.insert(complaintId, null, old, ComplaintStatus.ASSIGNED, "Assigned to " + staffName + ".");
    }

    public void changePriority(String complaintId, Priority priority) throws SQLException {
        complaintDAO.updatePriority(complaintId, priority);
    }

    public void startWork(String complaintId, int staffId) throws SQLException, ComplaintException {
        Complaint c = complaintDAO.findById(complaintId);
        if (c == null) throw new ComplaintException("Complaint not found.");
        requireAssignedStaff(c, staffId);
        transition(c, ComplaintStatus.IN_PROGRESS, staffId, "Work started by staff.");
    }

    public void addUpdate(String complaintId, int staffId, String comment) throws SQLException, ComplaintException {
        if (comment == null || comment.isBlank()) {
            throw new ComplaintException("Please enter an update comment.");
        }
        Complaint c = complaintDAO.findById(complaintId);
        if (c == null) throw new ComplaintException("Complaint not found.");
        updateDAO.insert(complaintId, staffId, c.getStatus(), c.getStatus(), comment.trim());
    }

    public void resolve(String complaintId, int staffId) throws SQLException, ComplaintException {
        Complaint c = complaintDAO.findById(complaintId);
        if (c == null) throw new ComplaintException("Complaint not found.");
        requireAssignedStaff(c, staffId);
        transition(c, ComplaintStatus.RESOLVED, staffId, "Marked resolved by staff.");
    }

    public void close(String complaintId) throws SQLException, ComplaintException {
        Complaint c = complaintDAO.findById(complaintId);
        if (c == null) throw new ComplaintException("Complaint not found.");
        transition(c, ComplaintStatus.CLOSED, null, "Closed by admin.");
    }

    private void requireAssignedStaff(Complaint complaint, int staffId) throws ComplaintException {
        if (complaint.getAssignedStaffId() == null || complaint.getAssignedStaffId() != staffId) {
            throw new ComplaintException("This complaint is assigned to another staff member.");
        }
    }

    private void transition(Complaint c, ComplaintStatus target, Integer staffId, String comment)
            throws SQLException, ComplaintException {
        if (!c.getStatus().canTransitionTo(target)) {
            throw new ComplaintException(
                "Cannot move complaint from " + c.getStatus() + " to " + target + ".");
        }
        ComplaintStatus old = c.getStatus();
        complaintDAO.updateStatus(c.getComplaintId(), target);
        updateDAO.insert(c.getComplaintId(), staffId, old, target, comment);
    }
}
