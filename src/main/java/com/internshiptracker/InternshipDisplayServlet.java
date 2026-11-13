package com.internshiptracker;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/internship-display")
public class InternshipDisplayServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final List<String> ALLOWED_STATUSES = Arrays.asList(
            "Applied",
            "Assessment",
            "Interview",
            "Selected",
            "Rejected",
            "Offer Received",
            "Completed"
    );

    // ==================== DISPLAY INTERNSHIPS ====================

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("sid") instanceof Number)) {

            response.sendRedirect(
                    request.getContextPath() + "/login.html");
            return;
        }

        int studentId =
                ((Number) session.getAttribute("sid")).intValue();

        response.setContentType("text/html;charset=UTF-8");

        response.setHeader(
                "Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        String sql =
                "SELECT APPLICATION_ID, SID, COMPANY_NAME, ROLE_NAME, "
              + "APPLY_DATE, JOB_LOCATION, WORK_MODE, STIPEND, "
              + "APPLICATION_STATUS, APPLICATION_LINK, NOTES "
              + "FROM INTERNSHIP_APPLICATIONS "
              + "WHERE SID = ? "
              + "ORDER BY APPLY_DATE DESC, APPLICATION_ID DESC";

        try (PrintWriter out = response.getWriter()) {

            out.println("<!DOCTYPE html>");
            out.println("<html><head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' "
                    + "content='width=device-width, initial-scale=1.0'>");
            out.println("<title>My Internship Tracker</title>");

            out.println("<style>");
            out.println("*{box-sizing:border-box;}");
            out.println("body{font-family:Arial,sans-serif;"
                    + "background:#f1f5f9;margin:0;padding:25px;"
                    + "color:#1f2937;}");
            out.println(".container{max-width:1500px;margin:auto;}");
            out.println("h2{text-align:center;color:#1e3a8a;}");
            out.println(".top-link{display:inline-block;margin:12px 8px 16px 0;"
                    + "padding:11px 16px;background:#2563eb;color:white;"
                    + "text-decoration:none;border-radius:6px;}");
            out.println(".top-link:hover{background:#1d4ed8;}");
            out.println(".table-box{overflow-x:auto;background:white;"
                    + "border-radius:8px;box-shadow:0 2px 8px #00000012;}");
            out.println("table{width:100%;border-collapse:collapse;"
                    + "min-width:1250px;}");
            out.println("th,td{padding:12px;border:1px solid #ddd;"
                    + "text-align:left;vertical-align:middle;}");
            out.println("th{background:#1e3a8a;color:white;}");
            out.println("tr:nth-child(even){background:#f8fafc;}");
            out.println(".status-form{display:flex;gap:6px;align-items:center;}");
            out.println("select{padding:7px;border:1px solid #cbd5e1;"
                    + "border-radius:5px;}");
            out.println(".update-btn{padding:8px 10px;background:#15803d;"
                    + "color:white;border:0;border-radius:5px;cursor:pointer;}");
            out.println(".update-btn:hover{background:#166534;}");
            out.println(".message{padding:12px;margin:12px 0;border-radius:6px;}");
            out.println(".success{background:#dcfce7;color:#166534;}");
            out.println(".error{background:#fee2e2;color:#991b1b;}");
            out.println(".status{font-weight:bold;}");
            out.println("</style></head><body>");

            out.println("<div class='container'>");
            out.println("<h2>📋 My Internship Applications</h2>");

            out.println("<a class='top-link' "
                    + "href='internship.html'>+ Add Internship</a>");

            out.println("<a class='top-link' "
                    + "href='dashboard'>← Dashboard</a>");

            String message = request.getParameter("message");

            if ("updated".equals(message)) {

                out.println("<div class='message success'>"
                        + "Application status updated successfully!"
                        + "</div>");

            } else if ("invalid".equals(message)) {

                out.println("<div class='message error'>"
                        + "Invalid application or status. "
                        + "Please try again.</div>");

            } else if ("error".equals(message)) {

                out.println("<div class='message error'>"
                        + "Unable to update status. "
                        + "Check the Eclipse Console for the exact error."
                        + "</div>");
            }

            out.println("<div class='table-box'><table>");
            out.println("<tr>");
            out.println("<th>Application ID</th>");
            out.println("<th>Student ID</th>");
            out.println("<th>Company</th>");
            out.println("<th>Role</th>");
            out.println("<th>Apply Date</th>");
            out.println("<th>Location</th>");
            out.println("<th>Work Mode</th>");
            out.println("<th>Stipend (₹)</th>");
            out.println("<th>Current Status</th>");
            out.println("<th>Update Status</th>");
            out.println("<th>Application Link</th>");
            out.println("<th>Notes</th>");
            out.println("</tr>");

            boolean found = false;
            int count = 0;

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, studentId);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        found = true;
                        count++;

                        int applicationId =
                                rs.getInt("APPLICATION_ID");

                        String currentStatus =
                                rs.getString("APPLICATION_STATUS");

                        if (currentStatus == null
                                || currentStatus.trim().isEmpty()) {

                            currentStatus = "Applied";
                        }

                        out.println("<tr>");

                        out.println("<td>" + applicationId + "</td>");
                        out.println("<td>" + rs.getInt("SID") + "</td>");

                        out.println("<td>"
                                + escapeHtml(rs.getString("COMPANY_NAME"))
                                + "</td>");

                        out.println("<td>"
                                + escapeHtml(rs.getString("ROLE_NAME"))
                                + "</td>");

                        java.sql.Date date =
                                rs.getDate("APPLY_DATE");

                        out.println("<td>"
                                + (date == null ? "-" : date.toString())
                                + "</td>");

                        out.println("<td>"
                                + escapeHtml(rs.getString("JOB_LOCATION"))
                                + "</td>");

                        out.println("<td>"
                                + escapeHtml(rs.getString("WORK_MODE"))
                                + "</td>");

                        BigDecimal stipend =
                                rs.getBigDecimal("STIPEND");

                        out.println("<td>"
                                + (stipend == null
                                        ? "-"
                                        : stipend.toPlainString())
                                + "</td>");

                        out.println("<td class='status'>"
                                + escapeHtml(currentStatus)
                                + "</td>");

                        out.println("<td>");

                        out.println("<form class='status-form' "
                                + "method='post' action='internship-display'>");

                        out.println("<input type='hidden' "
                                + "name='applicationId' value='"
                                + applicationId + "'>");

                        out.println("<select name='applicationStatus' required>");

                        for (String status : ALLOWED_STATUSES) {

                            out.println("<option value='"
                                    + escapeHtml(status) + "'"
                                    + (status.equalsIgnoreCase(currentStatus)
                                            ? " selected"
                                            : "")
                                    + ">"
                                    + escapeHtml(status)
                                    + "</option>");
                        }

                        if (!ALLOWED_STATUSES.contains(currentStatus)) {

                            out.println("<option value='"
                                    + escapeHtml(currentStatus)
                                    + "' selected>"
                                    + escapeHtml(currentStatus)
                                    + "</option>");
                        }

                        out.println("</select>");

                        out.println("<button class='update-btn' "
                                + "type='submit'>Update</button>");

                        out.println("</form>");
                        out.println("</td>");

                        String link =
                                rs.getString("APPLICATION_LINK");

                        if (link != null
                                && (link.startsWith("https://")
                                || link.startsWith("http://"))) {

                            out.println("<td><a href='"
                                    + escapeHtml(link)
                                    + "' target='_blank' "
                                    + "rel='noopener noreferrer'>"
                                    + "Open Link</a></td>");

                        } else {

                            out.println("<td>-</td>");
                        }

                        out.println("<td>"
                                + escapeHtml(rs.getString("NOTES"))
                                + "</td>");

                        out.println("</tr>");
                    }
                }

            } catch (SQLException e) {

                getServletContext().log(
                        "Unable to load internship applications.", e);

                e.printStackTrace();

                response.setStatus(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                out.println("<tr><td colspan='12'>"
                        + "Unable to load applications. "
                        + "Check Eclipse Console."
                        + "</td></tr>");

                out.println("</table></div></div></body></html>");
                return;
            }

            if (!found) {

                out.println("<tr><td colspan='12' "
                        + "style='text-align:center;'>"
                        + "No internship applications found. "
                        + "Add your first application!"
                        + "</td></tr>");
            }

            out.println("</table></div>");

            out.println("<p>Total applications displayed: "
                    + count + "</p>");

            out.println("</div></body></html>");
        }
    }

    // ==================== UPDATE APPLICATION STATUS ====================

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("sid") instanceof Number)) {

            response.sendRedirect(
                    request.getContextPath() + "/login.html");
            return;
        }

        int studentId =
                ((Number) session.getAttribute("sid")).intValue();

        String applicationIdText =
                request.getParameter("applicationId");

        String newStatus =
                request.getParameter("applicationStatus");

        int applicationId;

        try {

            applicationId = Integer.parseInt(applicationIdText);

        } catch (NumberFormatException | NullPointerException e) {

            response.sendRedirect(request.getContextPath()
                    + "/internship-display?message=invalid");
            return;
        }

        if (applicationId <= 0
                || newStatus == null
                || !ALLOWED_STATUSES.contains(newStatus)) {

            response.sendRedirect(request.getContextPath()
                    + "/internship-display?message=invalid");
            return;
        }

        String sql =
                "UPDATE INTERNSHIP_APPLICATIONS "
              + "SET APPLICATION_STATUS = ? "
              + "WHERE APPLICATION_ID = ? AND SID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, applicationId);
            ps.setInt(3, studentId);

            int updatedRows = ps.executeUpdate();

            if (updatedRows == 1) {

                response.sendRedirect(request.getContextPath()
                        + "/internship-display?message=updated");

            } else {

                response.sendRedirect(request.getContextPath()
                        + "/internship-display?message=invalid");
            }

        } catch (SQLException e) {

            // Print the actual Oracle/JDBC error in Eclipse Console.
            System.err.println(
                    "========== STATUS UPDATE ERROR ==========");

            System.err.println(
                    "Application ID: " + applicationId);

            System.err.println(
                    "Student ID: " + studentId);

            System.err.println(
                    "New Status: " + newStatus);

            System.err.println(
                    "Oracle Error Code: " + e.getErrorCode());

            System.err.println(
                    "SQL State: " + e.getSQLState());

            System.err.println(
                    "Error Message: " + e.getMessage());

            e.printStackTrace();

            System.err.println(
                    "=========================================");

            getServletContext().log(
                    "Unable to update internship application status.", e);

            response.sendRedirect(request.getContextPath()
                    + "/internship-display?message=error");
        }
    }

    // ==================== HTML ESCAPING ====================

    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
