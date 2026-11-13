package com.internshiptracker;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Use javax.servlet imports instead of jakarta.servlet if your project
// is configured for Tomcat 9.

@WebServlet("/delete-student")
public class DeleteStudentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String sidText = request.getParameter("sid");

        if (sidText == null || sidText.trim().isEmpty()) {
            response.getWriter().println(
                "<h3>Student ID is missing.</h3>"
            );
            return;
        }

        int sid;

        try {
            sid = Integer.parseInt(sidText.trim());
        } catch (NumberFormatException e) {
            response.getWriter().println(
                "<h3>Invalid Student ID.</h3>"
            );
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            // Check whether internship applications exist.
            String checkSql =
                "SELECT COUNT(*) FROM INTERNSHIP_APPLICATIONS WHERE SID = ?";

            try (PreparedStatement checkPs =
                     con.prepareStatement(checkSql)) {

                checkPs.setInt(1, sid);

                try (ResultSet rs = checkPs.executeQuery()) {
                    rs.next();

                    int applicationCount = rs.getInt(1);

                    if (applicationCount > 0) {
                        response.getWriter().println(
                            "<!DOCTYPE html><html><head>" +
                            "<meta charset='UTF-8'>" +
                            "<title>Cannot Delete Student</title>" +
                            "</head><body>" +
                            "<h2>Student Cannot Be Deleted</h2>" +
                            "<p>This student has " + applicationCount +
                            " internship application(s).</p>" +
                            "<p>The internship history has been preserved.</p>" +
                            "<a href='" + request.getContextPath() +
                            "/display'>Back to Student List</a>" +
                            "</body></html>"
                        );
                        return;
                    }
                }
            }

            // Delete only if no internship applications are linked.
            String deleteSql =
                "DELETE FROM INTERNSHIP_STUDENTS WHERE SID = ?";

            try (PreparedStatement deletePs =
                     con.prepareStatement(deleteSql)) {

                deletePs.setInt(1, sid);

                int rows = deletePs.executeUpdate();

                if (rows > 0) {
                    response.sendRedirect(
                        request.getContextPath() + "/display"
                    );
                } else {
                    response.getWriter().println(
                        "<h3>Student not found.</h3>" +
                        "<a href='" + request.getContextPath() +
                        "/display'>Back to Student List</a>"
                    );
                }
            }

        } catch (SQLException e) {
            getServletContext().log(
                "Error deleting student with SID: " + sid, e
            );

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().println(
                "<h3>Unable to delete student.</h3>" +
                "<p>Please check the server console for details.</p>" +
                "<a href='" + request.getContextPath() +
                "/display'>Back to Student List</a>"
            );
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.sendError(
            HttpServletResponse.SC_METHOD_NOT_ALLOWED,
            "Please use the Delete button."
        );
    }
}
