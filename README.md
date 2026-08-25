# HostelCare

**Report. Track. Resolve.**

A complete Hostel Complaint and Maintenance Management System built with
Java Swing, JDBC, and MySQL. Three roles - Student, Admin/Warden, and
Maintenance Staff - each get their own dashboard, wired to a real MySQL
database, with image evidence upload and a full complaint lifecycle.

## What's implemented

- **Student**: register, login, dashboard with live stats, submit a complaint
  (category-specific fields for food & washing-machine issues, up to 3 image
  uploads with live preview), track status, view resolution photos.
- **Admin**: dashboard with KPI cards and two bar charts (by category, by
  month), searchable/filterable complaint table, assign staff (sorted by
  matching specialization + lowest workload), change priority, close resolved
  complaints, Students list, Staff list, Reports tab (avg. resolution time,
  most common category, category spotlight).
- **Staff**: dashboard with assigned-complaint stats, start work, add a
  timeline update, upload a resolution ("after") photo and mark resolved.
- **Shared**: auto-generated complaint IDs (CMP1001, CMP1002, ...),
  auto-suggested priority via simple keyword rules (no AI), an enforced
  status workflow (SUBMITTED → PENDING → ASSIGNED → IN_PROGRESS → RESOLVED →
  CLOSED), and a per-complaint timeline.

## Project Structure

```
HostelCare/
├── src/
│   ├── model/       Student, Admin, MaintenanceStaff, Complaint, ComplaintImage, ComplaintUpdate
│   ├── enums/       ComplaintCategory, Priority, ComplaintStatus
│   ├── dao/         StudentDAO, AdminDAO, StaffDAO, ComplaintDAO, ImageDAO, ComplaintUpdateDAO
│   ├── service/     AuthService, ComplaintService, ImageService, PriorityService, PasswordUtil
│   ├── database/    DBConnection
│   ├── ui/          LoginFrame, RegisterDialog, StudentDashboard, AdminDashboard,
│   │                StaffDashboard, ComplaintForm, ComplaintDetails, UITheme,
│   │                BarChartPanel, TableButtonColumn
│   └── Main.java
├── database/
│   └── schema.sql    Full MySQL schema + sample data (run this first)
├── uploads/           Where complaint/resolution images are actually stored
└── README.md
```

## 1. Set up MySQL

1. Make sure MySQL Server is installed and running.
2. Run the schema file - it creates the `hostel_maintenance` database, all
   7 tables, and a few sample accounts:
   ```bash
   mysql -u root -p < database/schema.sql
   ```
   (Or open `database/schema.sql` in MySQL Workbench and click Execute.)

3. Configure the database connection without putting credentials in source:
   ```powershell
   $env:HOSTEL_DB_USER = "root"
   $env:HOSTEL_DB_PASSWORD = "your-password"
   ```
   You can also pass `-DHOSTEL_DB_USER=...` and `-DHOSTEL_DB_PASSWORD=...` to
   Java. Change `HOSTEL_DB_URL` when MySQL is running on another host.

### Demo accounts (created by schema.sql)

| Role    | Email               | Password    |
|---------|---------------------|-------------|
| Student | sai@hostel.com      | password123 |
| Admin   | admin@hostel.com    | admin123    |
| Staff   | ravi@hostel.com     | staff123    |

## 2. Get the MySQL JDBC driver

The JDK does **not** include a MySQL driver, so you need to add one:

1. Download **MySQL Connector/J** (a `.jar` file) from
   https://dev.mysql.com/downloads/connector/j/ (choose "Platform Independent").
2. Unzip it and note the path to `mysql-connector-j-<version>.jar`.

## 3. Run it

### Option A — VS Code (recommended)

1. Open the `HostelCare` folder in VS Code.
2. Install the **Extension Pack for Java** (Microsoft) if you haven't already.
3. Add the driver: `Java Projects` panel → right-click **Referenced Libraries**
   → **Add Jar** → select the `mysql-connector-j-<version>.jar` you downloaded.
4. Open `src/Main.java` and click **Run** above `public static void main`.

### Option B — Command line

```bash
cd HostelCare/src
javac -d ../out $(find . -name "*.java")
cd ..
java -cp "out:/path/to/mysql-connector-j-9.x.x.jar" Main
```//
(On Windows, use `;` instead of `:` in the classpath, e.g. `-cp "out;C:\libs\mysql-connector-j-9.x.x.jar"`.)

## 4. Try the workflow

1. Log in as the **student** (or register a new one) → **+ New Complaint** →
   pick a category, write a description, attach a photo → Submit. Note the
   generated complaint ID and auto-suggested priority.
2. Log out, log in as **admin** → **Complaints** tab → find the complaint →
   **View** → assign it to a staff member (matching specialization is listed
   first).
3. Log out, log in as **staff** → **View** the complaint → **Start Work** →
   add a timeline update → **Upload Resolution Image & Mark Resolved**.
4. Log back in as the student to see it marked RESOLVED with the after photo.
5. Log back in as admin → close the complaint, and check the **Reports** tab
   for updated stats.

## Notes on scope

- Images are never stored in MySQL - only their path/name/size/type are
  (see `complaint_images` table); the actual files live in `uploads/`.
- Passwords are hashed with salted SHA-256 (`service/PasswordUtil.java`) -
  no plain-text passwords are ever stored or compared.
- All user input goes through `PreparedStatement` - no string-concatenated SQL.
- This is intentionally a plain Java Swing + JDBC + MySQL desktop app (no
  Spring Boot, no web framework, no AI/ML) so it stays approachable as a
  college/resume project.

## 5. Web dashboard preview

The `web/` folder contains a responsive browser implementation of the
HostelCare workflow. It includes role-based login, separate Student/Admin/
Staff modes, complaint search and filters, complaint details with a timeline,
admin assignment and closing actions, staff start/resolve actions, student
read-only tracking, before-image upload previews, and resolution-photo upload
controls. It is currently frontend-only, so data is held in browser memory.

For the frontend-only preview, run it from the project root:

```powershell
cd web
python -m http.server 4173
```

Then open http://localhost:4173.

### Run the integrated Java web app

The project also includes `web.WebServer`, a lightweight same-origin API that
uses the existing `AuthService`, `ComplaintService`, DAOs, MySQL schema, and
role sessions. First run the schema and configure the database variables as
described above, then from the project root run:

```powershell
$sourceFiles = Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { $_.FullName }
javac -cp "lib\mysql-connector-j-9.4.0.jar" -d out $sourceFiles
java -cp "out;lib\mysql-connector-j-9.4.0.jar" web.WebServer
```

Open http://localhost:8080. The browser client uses the API automatically for
login and complaint lists; the 4173 preview remains available as a demo
fallback when the database server is not running.
