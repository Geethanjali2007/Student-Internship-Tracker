
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

@WebServlet("/edit-student")
public class EditStudentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String sidText = request.getParameter("sid");

        if (sidText == null || sidText.trim().isEmpty()) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Student ID is required."
            );
            return;
        }

        int sid;

        try {
            sid = Integer.parseInt(sidText.trim());
        } catch (NumberFormatException e) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Invalid Student ID."
            );
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT SID, SNAME, SEMAIL, SADDR, PHNO " +
                 "FROM INTERNSHIP_STUDENTS WHERE SID = ?")) {

            ps.setInt(1, sid);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Student not found."
                    );
                    return;
                }

                String name = rs.getString("SNAME");
                String email = rs.getString("SEMAIL");
                String address = rs.getString("SADDR");
                String phone = rs.getString("PHNO");

                PrintWriter out = response.getWriter();
                String context = request.getContextPath();

                out.println("<!DOCTYPE html>");
                out.println("<html><head><meta charset='UTF-8'>");
                out.println("<title>Edit Student</title>");

                out.println("<style>");
                out.println("body{font-family:Arial,sans-serif;"
                    + "background:#f0f4f8;margin:0;padding:30px;}");
                out.println(".box{max-width:500px;margin:30px auto;"
                    + "background:white;padding:25px;border-radius:12px;"
                    + "box-shadow:0 3px 12px #ccc;}");
                out.println("h2{text-align:center;color:#243b55;}");
                out.println("label{display:block;margin-top:15px;"
                    + "font-weight:bold;}");
                out.println("input{box-sizing:border-box;width:100%;"
                    + "padding:10px;margin-top:6px;"
                    + "border:1px solid #bbb;border-radius:5px;}");
                out.println("button{width:100%;padding:12px;margin-top:22px;"
                    + "background:#1769aa;color:white;border:0;"
                    + "border-radius:5px;cursor:pointer;}");
                out.println("a{display:block;text-align:center;"
                    + "margin-top:15px;}");
                out.println("</style></head><body>");

                out.println("<div class='box'>");
                out.println("<h2>Edit Student Details</h2>");

                out.println("<form method='post' action='"
                    + context + "/edit-student'>");

                out.println("<input type='hidden' name='sid' value='"
                    + sid + "'>");

                out.println("<label>Student Name</label>");
                out.println("<input type='text' name='sname' required "
                    + "maxlength='100' value='"
                    + escapeHtml(name) + "'>");

                out.println("<label>Email</label>");
                out.println("<input type='email' name='semail' required "
                    + "maxlength='150' value='"
                    + escapeHtml(email) + "'>");

                out.println("<label>Address</label>");
                out.println("<input type='text' name='saddr' required "
                    + "maxlength='250' value='"
                    + escapeHtml(address) + "'>");

                out.println("<label>Phone Number</label>");
                out.println("<input type='text' name='phno' required "
                    + "maxlength='20' value='"
                    + escapeHtml(phone) + "'>");

                out.println("<button type='submit'>Update Student</button>");
                out.println("</form>");

                out.println("<a href='" + context
                    + "/display'>Back to Student List</a>");

                out.println("</div></body></html>");
            }

        } catch (SQLException e) {
            getServletContext().log("Error loading student details", e);
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Unable to load student details."
            );
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String sidText = request.getParameter("sid");
        String name = request.getParameter("sname");
        String email = request.getParameter("semail");
        String address = request.getParameter("saddr");
        String phone = request.getParameter("phno");

        if (sidText == null || name == null || email == null
                || address == null || phone == null
                || name.trim().isEmpty()
                || email.trim().isEmpty()
                || address.trim().isEmpty()
                || phone.trim().isEmpty()) {

            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "All fields are required."
            );
            return;
        }

        int sid;

        try {
            sid = Integer.parseInt(sidText.trim());
        } catch (NumberFormatException e) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Invalid Student ID."
            );
            return;
        }

        String sql = "UPDATE INTERNSHIP_STUDENTS "
            + "SET SNAME = ?, SEMAIL = ?, SADDR = ?, PHNO = ? "
            + "WHERE SID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name.trim());
            ps.setString(2, email.trim());
            ps.setString(3, address.trim());
            ps.setString(4, phone.trim());
            ps.setInt(5, sid);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                response.sendRedirect(
                    request.getContextPath() + "/display"
                );
            } else {
                response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Student not found."
                );
            }

        } catch (SQLException e) {
            getServletContext().log("Error updating student details", e);
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Unable to update student. Check the server console."
            );
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
                    .replace("'", "&#39;");
    }
}
