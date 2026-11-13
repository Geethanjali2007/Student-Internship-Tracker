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

@WebServlet("/display")
public class DisplayServlet extends HttpServlet {

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

        // Check whether the student is logged in
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("sid") == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login.html");
            return;
        }

        String contextPath = request.getContextPath();

        String sql =
                "SELECT SID, SNAME, SEMAIL, SADDR, PHNO " +
                "FROM INTERNSHIP_STUDENTS ORDER BY SID";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            PrintWriter out = response.getWriter();

            out.println("<!DOCTYPE html>");
            out.println("<html lang='en'>");

            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("<title>Student Records</title>");

            out.println("<style>");

            out.println("* { box-sizing: border-box; }");

            out.println("body {");
            out.println("font-family: Arial, sans-serif;");
            out.println("background: #eef2f7;");
            out.println("margin: 0;");
            out.println("padding: 30px 15px;");
            out.println("color: #1e293b;");
            out.println("}");

            out.println(".container {");
            out.println("max-width: 1100px;");
            out.println("margin: auto;");
            out.println("background: white;");
            out.println("padding: 25px;");
            out.println("border-radius: 12px;");
            out.println("box-shadow: 0 4px 15px rgba(0,0,0,0.1);");
            out.println("}");

            out.println("h1 {");
            out.println("text-align: center;");
            out.println("color: #2563eb;");
            out.println("}");

            out.println(".table-container {");
            out.println("width: 100%;");
            out.println("overflow-x: auto;");
            out.println("margin-top: 25px;");
            out.println("}");

            out.println("table {");
            out.println("width: 100%;");
            out.println("border-collapse: collapse;");
            out.println("min-width: 650px;");
            out.println("}");

            out.println("th, td {");
            out.println("padding: 13px;");
            out.println("border: 1px solid #e2e8f0;");
            out.println("text-align: left;");
            out.println("}");

            out.println("th {");
            out.println("background: #2563eb;");
            out.println("color: white;");
            out.println("}");

            out.println("tr:nth-child(even) {");
            out.println("background: #f8fafc;");
            out.println("}");

            out.println(".btn {");
            out.println("display: inline-block;");
            out.println("background: #2563eb;");
            out.println("color: white;");
            out.println("padding: 11px 18px;");
            out.println("border-radius: 6px;");
            out.println("text-decoration: none;");
            out.println("margin-top: 20px;");
            out.println("}");

            out.println(".btn:hover {");
            out.println("background: #1d4ed8;");
            out.println("}");

            out.println(".footer {");
            out.println("text-align: center;");
            out.println("margin-top: 25px;");
            out.println("color: #64748b;");
            out.println("font-size: 13px;");
            out.println("}");

            out.println("</style>");
            out.println("</head>");

            out.println("<body>");
            out.println("<div class='container'>");

            out.println("<h1>Student Records</h1>");

            out.println("<p>Registered student details</p>");

            out.println("<div class='table-container'>");

            out.println("<table>");
            out.println("<tr>");
            out.println("<th>Student ID</th>");
            out.println("<th>Name</th>");
            out.println("<th>Email</th>");
            out.println("<th>Address</th>");
            out.println("<th>Phone Number</th>");
            out.println("</tr>");

            boolean hasRecords = false;

            while (rs.next()) {

                hasRecords = true;

                out.println("<tr>");

                out.println("<td>"
                        + escapeHtml(rs.getString("SID")) + "</td>");

                out.println("<td>"
                        + escapeHtml(rs.getString("SNAME")) + "</td>");

                out.println("<td>"
                        + escapeHtml(rs.getString("SEMAIL")) + "</td>");

                out.println("<td>"
                        + escapeHtml(rs.getString("SADDR")) + "</td>");

                out.println("<td>"
                        + escapeHtml(rs.getString("PHNO")) + "</td>");

                out.println("</tr>");
            }

            if (!hasRecords) {
                out.println("<tr>");
                out.println("<td colspan='5'>No student records found.</td>");
                out.println("</tr>");
            }

            out.println("</table>");
            out.println("</div>");

            out.println("<a class='btn' href='" + contextPath
                    + "/dashboard'>Back to Dashboard</a>");

            out.println("<div class='footer'>");
            out.println("Internship &amp; Package Tracking Management System");
            out.println("</div>");

            out.println("</div>");
            out.println("</body>");
            out.println("</html>");

            out.flush();

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to retrieve student records.", e);
        }
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
