package model;

import enums.ComplaintCategory;

/** A maintenance staff member who resolves assigned complaints. */
public class MaintenanceStaff {
    private int staffId;
    private String name;
    private String email;
    private String phone;
    private String passwordHash;
    private ComplaintCategory specialization;
    private boolean available;

    // Not persisted directly; populated by DAO joins for dashboards.
    private int activeComplaints;

    public MaintenanceStaff() { }

    public MaintenanceStaff(int staffId, String name, String email, String phone,
                             String passwordHash, ComplaintCategory specialization, boolean available) {
        this.staffId = staffId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.specialization = specialization;
        this.available = available;
    }

    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public ComplaintCategory getSpecialization() { return specialization; }
    public void setSpecialization(ComplaintCategory specialization) { this.specialization = specialization; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public int getActiveComplaints() { return activeComplaints; }
    public void setActiveComplaints(int activeComplaints) { this.activeComplaints = activeComplaints; }

    @Override
    public String toString() {
        return name + " (" + specialization.getDisplayName() + ") - Active: " + activeComplaints;
    }
}
