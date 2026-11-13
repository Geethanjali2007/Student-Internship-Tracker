package com.internshiptracker;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control",
                "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("sid") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        Object sidObject = session.getAttribute("sid");
        Object nameObject = session.getAttribute("sname");
        Object emailObject = session.getAttribute("semail");

        if (!(sidObject instanceof Number)) {
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        int studentId = ((Number) sidObject).intValue();

        String studentIdText = escapeHtml(String.valueOf(studentId));
        String name = escapeHtml(
                nameObject == null ? "Student" : nameObject.toString());
        String email = escapeHtml(
                emailObject == null ? "" : emailObject.toString());
        String contextPath = request.getContextPath();

        int totalApplications = 0;
        int pendingApplications = 0;
        int selectedApplications = 0;
        int rejectedApplications = 0;

        String sql =
                "SELECT COUNT(*) AS TOTAL_COUNT, " +
                "SUM(CASE WHEN UPPER(TRIM(APPLICATION_STATUS)) = 'PENDING' " +
                "THEN 1 ELSE 0 END) AS PENDING_COUNT, " +
                "SUM(CASE WHEN UPPER(TRIM(APPLICATION_STATUS)) = 'SELECTED' " +
                "THEN 1 ELSE 0 END) AS SELECTED_COUNT, " +
                "SUM(CASE WHEN UPPER(TRIM(APPLICATION_STATUS)) = 'REJECTED' " +
                "THEN 1 ELSE 0 END) AS REJECTED_COUNT " +
                "FROM INTERNSHIP_APPLICATIONS WHERE SID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalApplications = rs.getInt("TOTAL_COUNT");
                    pendingApplications = rs.getInt("PENDING_COUNT");
                    selectedApplications = rs.getInt("SELECTED_COUNT");
                    rejectedApplications = rs.getInt("REJECTED_COUNT");
                }
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load internship statistics.", e);
        }

        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Dashboard | Internship Tracker</title>");

        out.println("<link rel='preconnect' href='https://fonts.googleapis.com'>");
        out.println("<link rel='preconnect' href='https://fonts.gstatic.com' crossorigin>");
        out.println("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap' rel='stylesheet'>");

        out.println("<style>");

        out.println("*{box-sizing:border-box;margin:0;padding:0}");
        out.println(":root{--navy:#172554;--navy2:#1e1b4b;--purple:#7c3aed;--bg:#f5f7fb;--text:#1e293b;--muted:#64748b;--white:#ffffff}");
        out.println("body{font-family:'Inter',Arial,sans-serif;background:var(--bg);color:var(--text);min-height:100vh}");
        out.println(".layout{display:flex;min-height:100vh}");

        /* Sidebar */

        out.println(".sidebar{width:255px;background:linear-gradient(180deg,#172554,#312e81);color:white;padding:30px 20px;display:flex;flex-direction:column;flex-shrink:0}");
        out.println(".brand{font-size:20px;font-weight:800;line-height:1.5;margin-bottom:8px}");
        out.println(".brand span{color:#c4b5fd}");
        out.println(".brand-sub{font-size:12px;color:#cbd5e1;line-height:1.6;margin-bottom:35px}");
        out.println(".nav-label{font-size:11px;text-transform:uppercase;letter-spacing:1.5px;color:#c4b5fd;margin:15px 10px}");
        out.println(".nav-link{display:block;text-decoration:none;color:#e2e8f0;padding:13px 14px;border-radius:10px;margin-bottom:7px;font-size:14px;transition:.2s}");
        out.println(".nav-link:hover,.nav-link.active{background:rgba(255,255,255,.15);color:white}");
        out.println(".sidebar-bottom{margin-top:auto;padding-top:30px}");
        out.println(".profile-mini{border-top:1px solid rgba(255,255,255,.18);padding-top:20px;font-size:13px;line-height:1.8;overflow-wrap:anywhere}");
        out.println(".profile-mini .email{color:#cbd5e1;font-size:12px}");

        /* Main */

        out.println(".main{flex:1;min-width:0;padding:32px 38px}");
        out.println(".topbar{display:flex;justify-content:space-between;align-items:center;gap:20px;margin-bottom:32px}");
        out.println(".eyebrow{font-size:12px;color:var(--purple);font-weight:700;text-transform:uppercase;letter-spacing:1.4px;margin-bottom:8px}");
        out.println(".page-title{font-size:30px;font-weight:800;color:var(--navy);letter-spacing:-.8px}");
        out.println(".subtitle{color:var(--muted);font-size:14px;margin-top:8px;line-height:1.6}");
        out.println(".avatar{width:48px;height:48px;border-radius:15px;background:#ede9fe;color:var(--purple);display:flex;align-items:center;justify-content:center;font-size:20px;font-weight:800;flex-shrink:0}");

        /* Welcome banner */

        out.println(".welcome-banner{background:linear-gradient(115deg,#312e81,#7c3aed);border-radius:18px;padding:28px 30px;color:white;display:flex;justify-content:space-between;align-items:center;gap:20px;margin-bottom:30px;box-shadow:0 10px 28px rgba(91,33,182,.15)}");
        out.println(".welcome-banner h2{font-size:23px;margin-bottom:10px}");
        out.println(".welcome-banner p{font-size:14px;color:#ede9fe;line-height:1.7}");
        out.println(".welcome-icon{font-size:43px;flex-shrink:0}");

        /* Statistics */

        out.println(".section-heading{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-bottom:18px}");
        out.println(".section-heading h2{font-size:19px;color:var(--navy);font-weight:800}");
        out.println(".section-heading p{font-size:12px;color:var(--muted);margin-top:6px}");
        out.println(".stats{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:18px;margin-bottom:32px}");
        out.println(".stat-card{background:white;border:1px solid #e8eaf3;border-radius:15px;padding:22px;box-shadow:0 4px 15px rgba(15,23,42,.025);transition:transform .2s,box-shadow .2s}");
        out.println(".stat-card:hover{transform:translateY(-4px);box-shadow:0 12px 25px rgba(15,23,42,.08)}");
        out.println(".stat-top{display:flex;align-items:center;justify-content:space-between;gap:8px;margin-bottom:20px}");
        out.println(".stat-label{font-size:13px;color:var(--muted);font-weight:600;line-height:1.5}");
        out.println(".stat-icon{width:40px;height:40px;border-radius:12px;display:flex;align-items:center;justify-content:center;font-size:19px}");
        out.println(".stat-number{font-size:32px;font-weight:800;color:var(--navy);letter-spacing:-1px}");
        out.println(".stat-note{font-size:12px;color:var(--muted);margin-top:9px}");
        out.println(".icon-total{background:#ede9fe;color:#6d28d9}");
        out.println(".icon-pending{background:#fef3c7;color:#b45309}");
        out.println(".icon-selected{background:#dcfce7;color:#15803d}");
        out.println(".icon-rejected{background:#fee2e2;color:#b91c1c}");

        /* Content cards */

        out.println(".content-grid{display:grid;grid-template-columns:1fr 1fr;gap:22px}");
        out.println(".panel{background:white;border:1px solid #e8eaf3;border-radius:16px;padding:25px;box-shadow:0 4px 15px rgba(15,23,42,.025);min-width:0}");
        out.println(".panel-heading{display:flex;align-items:center;gap:13px;margin-bottom:14px}");
        out.println(".panel-icon{width:44px;height:44px;border-radius:12px;background:#ede9fe;color:#6d28d9;display:flex;align-items:center;justify-content:center;font-size:21px;flex-shrink:0}");
        out.println(".panel h2{font-size:17px;color:var(--navy);font-weight:800}");
        out.println(".panel-description{font-size:13px;line-height:1.8;color:var(--muted);margin-bottom:22px}");
        out.println(".detail-row{display:flex;gap:10px;align-items:flex-start;margin:13px 0;font-size:13px;line-height:1.6;overflow-wrap:anywhere}");
        out.println(".detail-label{color:var(--muted);min-width:90px}");
        out.println(".detail-value{font-weight:600;color:var(--text)}");
        out.println(".button-list{display:flex;flex-wrap:wrap;gap:10px;margin-top:20px}");
        out.println(".btn{display:inline-flex;align-items:center;justify-content:center;gap:8px;text-decoration:none;border:0;border-radius:9px;padding:12px 15px;font-family:inherit;font-size:13px;font-weight:700;background:var(--purple);color:white;transition:.2s;cursor:pointer}");
        out.println(".btn:hover{background:#6d28d9;transform:translateY(-1px)}");
        out.println(".btn-outline{background:white;color:var(--purple);border:1px solid #ddd6fe}");
        out.println(".btn-outline:hover{background:#f5f3ff}");
        out.println(".btn-logout{background:#fff1f2;color:#be123c;border:1px solid #ffe4e6}");
        out.println(".btn-logout:hover{background:#ffe4e6}");
        out.println(".footer{text-align:center;color:#94a3b8;font-size:12px;margin-top:35px;padding:20px 0}");

        /* Responsive */

        out.println("@media(max-width:1100px){.main{padding:25px}.stats{grid-template-columns:repeat(2,minmax(0,1fr))}.content-grid{grid-template-columns:1fr}}");
        out.println("@media(max-width:700px){.layout{flex-direction:column}.sidebar{width:100%;padding:20px}.brand-sub{margin-bottom:18px}.nav-label{margin-top:10px}.nav-link{display:inline-block;margin-right:4px;padding:10px}.sidebar-bottom{margin-top:15px}.profile-mini{padding-top:12px}.main{padding:20px 15px}.page-title{font-size:24px}.topbar{margin-bottom:22px}.welcome-banner{padding:22px}.welcome-banner h2{font-size:20px}.welcome-icon{font-size:30px}}");
        out.println("@media(max-width:420px){.stats{grid-template-columns:1fr}.stat-card{padding:18px}.welcome-icon{display:none}.panel{padding:20px}.button-list{flex-direction:column}.btn{width:100%}.topbar{align-items:flex-start}}");

        out.println("</style>");
        out.println("</head>");

        out.println("<body>");
        out.println("<div class='layout'>");

        // Sidebar
        out.println("<aside class='sidebar'>");
        out.println("<div class='brand'>Internship<span>Track</span></div>");
        out.println("<div class='brand-sub'>Internship &amp; Package<br>Tracking Management</div>");
        out.println("<div class='nav-label'>Workspace</div>");
        out.println("<a class='nav-link active' href='" + contextPath + "/dashboard'>▦ &nbsp; Dashboard</a>");
        out.println("<a class='nav-link' href='" + contextPath + "/internship.html'>＋ &nbsp; Add Internship</a>");
        out.println("<a class='nav-link' href='" + contextPath + "/internship-display'>▤ &nbsp; My Internships</a>");
        out.println("<a class='nav-link' href='" + contextPath + "/offer-tracking'>◇ &nbsp; Offers &amp; Packages</a>");
        out.println("<a class='nav-link' href='" + contextPath + "/display'>♙ &nbsp; View Students</a>");

        out.println("<div class='sidebar-bottom'>");
        out.println("<div class='profile-mini'>");
        out.println("<div>👤 <strong>" + name + "</strong></div>");
        out.println("<div class='email'>" + email + "</div>");
        out.println("<div class='email'>Student ID: " + studentIdText + "</div>");
        out.println("</div>");
        out.println("<div class='button-list'>");
        out.println("<a class='btn btn-logout' href='" + contextPath + "/logout'>↪ Logout</a>");
        out.println("</div>");
        out.println("</div>");
        out.println("</aside>");

        // Main content
        out.println("<main class='main'>");

        out.println("<header class='topbar'>");
        out.println("<div>");
        out.println("<div class='eyebrow'>Student workspace</div>");
        out.println("<h1 class='page-title'>Dashboard</h1>");
        out.println("<p class='subtitle'>Track your internship journey, applications and career opportunities.</p>");
        out.println("</div>");
        out.println("<div class='avatar'>👤</div>");
        out.println("</header>");

        // Welcome banner
        out.println("<section class='welcome-banner'>");
        out.println("<div>");
        out.println("<h2>Welcome back, " + name + "! 👋</h2>");
        out.println("<p>Here is an overview of your internship applications.<br>Keep tracking your progress toward your career goals.</p>");
        out.println("</div>");
        out.println("<div class='welcome-icon'>🚀</div>");
        out.println("</section>");

        // Statistics
        out.println("<div class='section-heading'>");
        out.println("<div><h2>Application Overview</h2>");
        out.println("<p>Your application statistics at a glance</p></div>");
        out.println("</div>");

        out.println("<section class='stats'>");

        out.println("<div class='stat-card'>");
        out.println("<div class='stat-top'><span class='stat-label'>Total Applications</span><span class='stat-icon icon-total'>▤</span></div>");
        out.println("<div class='stat-number'>" + totalApplications + "</div>");
        out.println("<div class='stat-note'>All your applications</div>");
        out.println("</div>");

        out.println("<div class='stat-card'>");
        out.println("<div class='stat-top'><span class='stat-label'>Pending</span><span class='stat-icon icon-pending'>◷</span></div>");
        out.println("<div class='stat-number'>" + pendingApplications + "</div>");
        out.println("<div class='stat-note'>Awaiting a decision</div>");
        out.println("</div>");

        out.println("<div class='stat-card'>");
        out.println("<div class='stat-top'><span class='stat-label'>Selected</span><span class='stat-icon icon-selected'>✓</span></div>");
        out.println("<div class='stat-number'>" + selectedApplications + "</div>");
        out.println("<div class='stat-note'>Successful applications</div>");
        out.println("</div>");

        out.println("<div class='stat-card'>");
        out.println("<div class='stat-top'><span class='stat-label'>Rejected</span><span class='stat-icon icon-rejected'>×</span></div>");
        out.println("<div class='stat-number'>" + rejectedApplications + "</div>");
        out.println("<div class='stat-note'>Applications not selected</div>");
        out.println("</div>");

        out.println("</section>");

        // Profile and management panels
        out.println("<section class='content-grid'>");

        out.println("<div class='panel'>");
        out.println("<div class='panel-heading'><div class='panel-icon'>♙</div><h2>Student Profile</h2></div>");
        out.println("<p class='panel-description'>Your account information and student details.</p>");
        out.println("<div class='detail-row'><span class='detail-label'>Student ID</span><span class='detail-value'>" + studentIdText + "</span></div>");
        out.println("<div class='detail-row'><span class='detail-label'>Full Name</span><span class='detail-value'>" + name + "</span></div>");
        out.println("<div class='detail-row'><span class='detail-label'>Email</span><span class='detail-value'>" + email + "</span></div>");
        out.println("<div class='button-list'>");
        out.println("<a class='btn btn-outline' href='" + contextPath + "/display'>View Students</a>");
        out.println("</div>");
        out.println("</div>");

        out.println("<div class='panel'>");
        out.println("<div class='panel-heading'><div class='panel-icon'>▤</div><h2>Internship Management</h2></div>");
        out.println("<p class='panel-description'>Manage your applications and monitor internship offers, stipends and packages.</p>");
        out.println("<div class='button-list'>");
        out.println("<a class='btn' href='" + contextPath + "/internship.html'>＋ Add Internship</a>");
        out.println("<a class='btn btn-outline' href='" + contextPath + "/internship-display'>View Internships</a>");
        out.println("<a class='btn btn-outline' href='" + contextPath + "/offer-tracking'>Offers &amp; Packages</a>");
        out.println("</div>");
        out.println("</div>");

        out.println("</section>");

        out.println("<div class='footer'>");
        out.println("Internship &amp; Package Tracking Management System &nbsp;•&nbsp; Student Workspace");
        out.println("</div>");

        out.println("</main>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");

        out.flush();
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }
}
