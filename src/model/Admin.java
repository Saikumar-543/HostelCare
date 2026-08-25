package model;

/** A warden/administrator account. */
public class Admin {
    private int adminId;
    private String name;
    private String email;
    private String passwordHash;

    public Admin() { }

    public Admin(int adminId, String name, String email, String passwordHash) {
        this.adminId = adminId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    @Override
    public String toString() { return name; }
}
