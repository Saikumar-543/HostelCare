package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dao.ComplaintDAO;
import enums.ComplaintCategory;
import model.Admin;
import model.Complaint;
import model.MaintenanceStaff;
import model.Student;
import service.AuthService;
import service.ComplaintService;
import service.ImageService;
import model.ComplaintImage;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import enums.ComplaintStatus;

/** Small same-origin HTTP API for the web client. Uses the existing services and DAOs. */
public final class WebServer {
    private static final int PORT = Integer.parseInt(System.getProperty("HOSTEL_WEB_PORT", "8080"));
    private static final AuthService auth = new AuthService();
    private static final ComplaintDAO complaints = new ComplaintDAO();
    private static final ComplaintService complaintService = new ComplaintService();
    private static final ImageService imageService = new ImageService();
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();

    private WebServer() { }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/auth/login", WebServer::login);
        server.createContext("/api/auth/register", WebServer::register);
        server.createContext("/api/auth/logout", WebServer::logout);
        server.createContext("/api/complaints", WebServer::complaintRoutes);
        server.createContext("/api/dashboard", WebServer::dashboard);
        server.createContext("/api/health", exchange -> send(exchange, 200, "{\"ok\":true}"));
        server.createContext("/", WebServer::staticFile);
        server.setExecutor(null);
        server.start();
        System.out.println("HostelCare web app: http://localhost:" + PORT);
    }

    private static void login(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) { send(exchange, 405, "{}"); return; }
        String body = read(exchange);
        String role = value(body, "role");
        String email = value(body, "email");
        String password = value(body, "password");
        try {
            String token = UUID.randomUUID().toString();
            String userJson;
            if ("student".equals(role)) {
                Student user = auth.loginStudent(email, password);
                sessions.put(token, new Session(role, user.getStudentId()));
                userJson = userJson(user.getStudentId(), user.getName(), user.getEmail(), role, user.getRoomNumber());
            } else if ("staff".equals(role)) {
                MaintenanceStaff user = auth.loginStaff(email, password);
                sessions.put(token, new Session(role, user.getStaffId()));
                userJson = userJson(user.getStaffId(), user.getName(), user.getEmail(), role, null);
            } else {
                Admin user = auth.loginAdmin(email, password);
                sessions.put(token, new Session("admin", user.getAdminId()));
                userJson = userJson(user.getAdminId(), user.getName(), user.getEmail(), "admin", null);
            }
            exchange.getResponseHeaders().add("Set-Cookie", "HOSTEL_SESSION=" + token + "; Path=/; HttpOnly; SameSite=Lax");
            send(exchange, 200, userJson);
        } catch (Exception e) {
            send(exchange, 401, "{\"error\":\"Invalid email or password\"}");
        }
    }

    private static void register(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) { send(exchange, 405, "{}"); return; }
        String body = read(exchange);
        try {
            Student user = auth.register(value(body, "name"), value(body, "email"), value(body, "phone"),
                    value(body, "password"), value(body, "roomNumber"), value(body, "hostelBlock"));
            send(exchange, 201, userJson(user.getStudentId(), user.getName(), user.getEmail(), "student", user.getRoomNumber()));
        } catch (Exception e) {
            send(exchange, 400, "{\"error\":\"" + escape(e.getMessage() == null ? "Registration failed" : e.getMessage()) + "\"}");
        }
    }

    private static void logout(HttpExchange exchange) throws IOException {
        Session session = session(exchange);
        if (session != null) {
            String cookie = exchange.getRequestHeaders().getFirst("Cookie");
            for (String part : cookie.split(";")) if (part.trim().startsWith("HOSTEL_SESSION=")) sessions.remove(part.trim().substring(15));
        }
        exchange.getResponseHeaders().add("Set-Cookie", "HOSTEL_SESSION=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
        send(exchange, 200, "{\"ok\":true}");
    }

    private static void complaintRoutes(HttpExchange exchange) throws IOException {
        Session session = session(exchange);
        if (session == null) { send(exchange, 401, "{\"error\":\"Sign in required\"}"); return; }
        try {
            String[] path = exchange.getRequestURI().getPath().split("/");
            if ("POST".equals(exchange.getRequestMethod()) && path.length >= 5) {
                String complaintId = path[3];
                String action = path[4];
                if ("staff".equals(session.role) && "start".equals(action)) complaintService.startWork(complaintId, session.userId);
                else if ("staff".equals(session.role) && "resolve".equals(action)) complaintService.resolve(complaintId, session.userId);
                else if ("admin".equals(session.role) && "close".equals(action)) complaintService.close(complaintId);
                else if ("admin".equals(session.role) && "assign".equals(action)) {
                    String body = read(exchange);
                    int staffId = Integer.parseInt(value(body, "staffId"));
                    complaintService.assignStaff(complaintId, staffId, value(body, "staffName"));
                } else if ("student".equals(session.role) && "images".equals(action)) {
                    String body = read(exchange);
                    storeImage(body, complaintId, ComplaintImage.Kind.BEFORE);
                } else if ("staff".equals(session.role) && "resolution-image".equals(action)) {
                    String body = read(exchange);
                    storeImage(body, complaintId, ComplaintImage.Kind.RESOLUTION);
                } else { send(exchange, 403, "{\"error\":\"Action not allowed\"}"); return; }
                send(exchange, 200, "{\"ok\":true}");
                return;
            }
            if ("GET".equals(exchange.getRequestMethod())) {
                List<Complaint> list = "student".equals(session.role)
                        ? complaints.findByStudent(session.userId)
                        : "staff".equals(session.role) ? complaints.findByStaff(session.userId)
                        : complaints.search(null, null, null, null);
                send(exchange, 200, complaintListJson(list));
                return;
            }
            if ("POST".equals(exchange.getRequestMethod()) && "student".equals(session.role)) {
                String body = read(exchange);
                Complaint saved = complaintService.submit(session.userId, value(body, "roomNumber"),
                        ComplaintCategory.valueOf(value(body, "category")), value(body, "description"),
                        value(body, "meal"), value(body, "foodItem"), value(body, "machineNumber"), value(body, "machineLocation"));
                send(exchange, 201, complaintJson(saved));
                return;
            }
            send(exchange, 405, "{}");
        } catch (Exception e) {
            send(exchange, 400, "{\"error\":\"" + escape(e.getMessage() == null ? "Request failed" : e.getMessage()) + "\"}");
        }
    }

    private static void dashboard(HttpExchange exchange) throws IOException {
        Session session = session(exchange);
        if (session == null) { send(exchange, 401, "{\"error\":\"Sign in required\"}"); return; }
        try {
            int total = "student".equals(session.role) ? complaints.countByStudent(session.userId, null) : "staff".equals(session.role) ? complaints.countByStaffAndStatus(session.userId, null) : complaints.countAll(null);
            int pending = "student".equals(session.role) ? complaints.countByStudent(session.userId, ComplaintStatus.PENDING) : "staff".equals(session.role) ? complaints.countByStaffAndStatus(session.userId, ComplaintStatus.ASSIGNED) : complaints.countAll(ComplaintStatus.PENDING);
            int progress = "student".equals(session.role) ? complaints.countByStudent(session.userId, ComplaintStatus.IN_PROGRESS) : "staff".equals(session.role) ? complaints.countByStaffAndStatus(session.userId, ComplaintStatus.IN_PROGRESS) : complaints.countAll(ComplaintStatus.IN_PROGRESS);
            int resolved = "student".equals(session.role) ? complaints.countByStudent(session.userId, ComplaintStatus.RESOLVED) + complaints.countByStudent(session.userId, ComplaintStatus.CLOSED) : "staff".equals(session.role) ? complaints.countByStaffAndStatus(session.userId, ComplaintStatus.RESOLVED) + complaints.countByStaffAndStatus(session.userId, ComplaintStatus.CLOSED) : complaints.countAll(ComplaintStatus.RESOLVED) + complaints.countAll(ComplaintStatus.CLOSED);
            send(exchange, 200, "{\"total\":" + total + ",\"pending\":" + pending + ",\"inProgress\":" + progress + ",\"resolved\":" + resolved + "}");
        } catch (Exception e) { send(exchange, 500, "{\"error\":\"Could not load dashboard\"}"); }
    }

    private static void staticFile(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("/".equals(path)) path = "/index.html";
        java.nio.file.Path webRoot = java.nio.file.Paths.get("web").toAbsolutePath().normalize();
        java.nio.file.Path file = webRoot.resolve(path.substring(1)).normalize();
        if (!file.startsWith(webRoot) || !java.nio.file.Files.isRegularFile(file)) {
            send(exchange, 404, "Not found"); return;
        }
        byte[] data = java.nio.file.Files.readAllBytes(file);
        exchange.getResponseHeaders().set("Content-Type", path.endsWith(".css") ? "text/css" : path.endsWith(".js") ? "text/javascript" : "text/html");
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream out = exchange.getResponseBody()) { out.write(data); }
    }

    private static Session session(HttpExchange exchange) {
        String cookie = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookie == null) return null;
        for (String part : cookie.split(";")) if (part.trim().startsWith("HOSTEL_SESSION=")) return sessions.get(part.trim().substring(15));
        return null;
    }

    private static String read(HttpExchange exchange) throws IOException { return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); }
    private static void storeImage(String body, String complaintId, ComplaintImage.Kind kind) throws IOException, Exception {
        String encoded = value(body, "data");
        String name = value(body, "name");
        if (!encoded.startsWith("data:") || name.isBlank()) throw new IllegalArgumentException("Invalid image data");
        String payload = encoded.substring(encoded.indexOf(',') + 1);
        String suffix = name.toLowerCase().endsWith(".png") ? ".png" : ".jpg";
        Path temporary = Files.createTempFile("hostelcare-", suffix);
        try {
            Files.write(temporary, Base64.getDecoder().decode(payload));
            imageService.store(temporary, complaintId, kind);
        } finally { Files.deleteIfExists(temporary); }
    }
    private static String value(String json, String key) { String marker = "\"" + key + "\""; int start = json.indexOf(marker); if (start < 0) return ""; start = json.indexOf(':', start) + 1; while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++; if (start < json.length() && json.charAt(start) == '"') { int end = json.indexOf('"', start + 1); return end < 0 ? "" : json.substring(start + 1, end); } return ""; }
    private static String userJson(int id, String name, String email, String role, String room) { return "{\"id\":" + id + ",\"name\":\"" + escape(name) + "\",\"email\":\"" + escape(email) + "\",\"role\":\"" + role + "\",\"roomNumber\":\"" + escape(room) + "\"}"; }
    private static String complaintListJson(List<Complaint> list) { StringBuilder json = new StringBuilder("["); for (int i = 0; i < list.size(); i++) { if (i > 0) json.append(','); json.append(complaintJson(list.get(i))); } return json.append(']').toString(); }
    private static String complaintJson(Complaint c) { return "{\"id\":\"" + escape(c.getComplaintId()) + "\",\"room\":\"" + escape(c.getRoomNumber()) + "\",\"category\":\"" + c.getCategory().name() + "\",\"description\":\"" + escape(c.getDescription()) + "\",\"priority\":\"" + c.getPriority().name() + "\",\"status\":\"" + c.getStatus().name() + "\",\"staff\":\"" + escape(c.getAssignedStaffName()) + "\"}"; }
    private static String escape(String text) { if (text == null) return ""; return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n"); }
    private static void send(HttpExchange exchange, int status, String body) throws IOException { byte[] data = body.getBytes(StandardCharsets.UTF_8); exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8"); exchange.sendResponseHeaders(status, data.length); try (OutputStream out = exchange.getResponseBody()) { out.write(data); } }
    private record Session(String role, int userId) { }
}