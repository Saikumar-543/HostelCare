package model;

/** A hostel student who can submit and track complaints. */
public class Student {
    private int studentId;
    private String name;
    private String email;
    private String phone;
    private String passwordHash;
    private String roomNumber;
    private String hostelBlock;

    public Student() { }

    public Student(int studentId, String name, String email, String phone,
                    String passwordHash, String roomNumber, String hostelBlock) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.roomNumber = roomNumber;
        this.hostelBlock = hostelBlock;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getHostelBlock() { return hostelBlock; }
    public void setHostelBlock(String hostelBlock) { this.hostelBlock = hostelBlock; }

    @Override
    public String toString() { return name + " (Room " + roomNumber + ")"; }
}
