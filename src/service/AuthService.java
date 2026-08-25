package service;

import dao.AdminDAO;
import dao.StaffDAO;
import dao.StudentDAO;
import enums.ComplaintCategory;
import model.Admin;
import model.MaintenanceStaff;
import model.Student;

import java.sql.SQLException;

/** Handles login and account registration for all three roles. */
public class AuthService {

    private final StudentDAO studentDAO = new StudentDAO();
    private final AdminDAO adminDAO = new AdminDAO();
    private final StaffDAO staffDAO = new StaffDAO();

    public static class AuthException extends Exception {
        public AuthException(String message) { super(message); }
    }

    public Student loginStudent(String email, String password) throws SQLException, AuthException {
        Student s = studentDAO.findByEmail(email);
        if (s == null || !PasswordUtil.verify(password, s.getPasswordHash())) {
            throw new AuthException("Invalid email or password.");
        }
        return s;
    }

    public Admin loginAdmin(String email, String password) throws SQLException, AuthException {
        Admin a = adminDAO.findByEmail(email);
        if (a == null || !PasswordUtil.verify(password, a.getPasswordHash())) {
            throw new AuthException("Invalid email or password.");
        }
        return a;
    }

    public MaintenanceStaff loginStaff(String email, String password) throws SQLException, AuthException {
        MaintenanceStaff st = staffDAO.findByEmail(email);
        if (st == null || !PasswordUtil.verify(password, st.getPasswordHash())) {
            throw new AuthException("Invalid email or password.");
        }
        return st;
    }

    public Student register(String name, String email, String phone, String password,
                             String roomNumber, String hostelBlock) throws SQLException, AuthException {
        if (name == null || name.isBlank()) throw new AuthException("Name is required.");
        if (email == null || !email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            throw new AuthException("Please enter a valid email address.");
        }
        if (phone == null || !phone.matches("^[0-9]{10}$")) {
            throw new AuthException("Phone number must be 10 digits.");
        }
        if (password == null || password.length() < 6) {
            throw new AuthException("Password must be at least 6 characters.");
        }
        if (roomNumber == null || roomNumber.isBlank()) {
            throw new AuthException("Room number is required.");
        }
        if (studentDAO.emailExists(email)) {
            throw new AuthException("An account with this email already exists.");
        }

        Student s = new Student();
        s.setName(name.trim());
        s.setEmail(email.trim());
        s.setPhone(phone.trim());
        s.setPasswordHash(PasswordUtil.hash(password));
        s.setRoomNumber(roomNumber.trim());
        s.setHostelBlock(hostelBlock == null ? "" : hostelBlock.trim());

        int id = studentDAO.register(s);
        s.setStudentId(id);
        return s;
    }

    public Admin registerAdmin(String name, String email, String password)
            throws SQLException, AuthException {
        validateIdentity(name, email, password);
        if (adminDAO.emailExists(email.trim())) {
            throw new AuthException("An admin account with this email already exists.");
        }
        Admin admin = new Admin();
        admin.setName(name.trim());
        admin.setEmail(email.trim());
        admin.setPasswordHash(PasswordUtil.hash(password));
        admin.setAdminId(adminDAO.register(admin));
        return admin;
    }

    public MaintenanceStaff registerStaff(String name, String email, String phone,
                                          String password, ComplaintCategory specialization)
            throws SQLException, AuthException {
        validateIdentity(name, email, password);
        if (phone == null || !phone.matches("^[0-9]{10}$")) {
            throw new AuthException("Phone number must be 10 digits.");
        }
        if (specialization == null) {
            throw new AuthException("Please select a specialization.");
        }
        if (staffDAO.emailExists(email.trim())) {
            throw new AuthException("A staff account with this email already exists.");
        }
        MaintenanceStaff staff = new MaintenanceStaff();
        staff.setName(name.trim());
        staff.setEmail(email.trim());
        staff.setPhone(phone.trim());
        staff.setPasswordHash(PasswordUtil.hash(password));
        staff.setSpecialization(specialization);
        staff.setAvailable(true);
        staff.setStaffId(staffDAO.register(staff));
        return staff;
    }

    private void validateIdentity(String name, String email, String password) throws AuthException {
        if (name == null || name.isBlank()) throw new AuthException("Name is required.");
        if (email == null || !email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            throw new AuthException("Please enter a valid email address.");
        }
        if (password == null || password.length() < 6) {
            throw new AuthException("Password must be at least 6 characters.");
        }
    }
}
