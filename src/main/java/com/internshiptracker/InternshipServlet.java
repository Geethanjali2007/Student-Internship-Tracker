package com.internshiptracker;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/internship")
public class InternshipServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("sid") == null) {
            response.sendRedirect(
                request.getContextPath() + "/login.html");
            return;
        }

        response.sendRedirect(
            request.getContextPath() + "/internship.html");
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // Check whether the student is logged in.
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("sid") == null) {
            response.sendRedirect(
                request.getContextPath() + "/login.html");
            return;
        }

        // Use the logged-in student's ID instead of accepting an ID
        // from the form.
        Object sidObject = session.getAttribute("sid");

        if (!(sidObject instanceof Number)) {
            session.invalidate();
            response.sendRedirect(
                request.getContextPath() + "/login.html");
            return;
        }

        int sid = ((Number) sidObject).intValue();

        String companyName = request.getParameter("companyName");
        String roleName = request.getParameter("roleName");
        String applyDate = request.getParameter("applyDate");
        String jobLocation = request.getParameter("jobLocation");
        String workMode = request.getParameter("workMode");
        String stipendText = request.getParameter("stipend");
        String applicationStatus =
            request.getParameter("applicationStatus");
        String applicationLink = request.getParameter("applicationLink");
        String notes = request.getParameter("notes");

        try (PrintWriter out = response.getWriter()) {

            // Validate required fields.
            if (companyName == null || companyName.trim().isEmpty()
                    || roleName == null || roleName.trim().isEmpty()) {

                response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);

                out.println("<h3>Company Name and Internship Role are required.</h3>");
                out.println("<a href='internship.html'>Go Back</a>");
                return;
            }

            // Validate stipend if provided.
            BigDecimal stipend = null;

            if (stipendText != null && !stipendText.trim().isEmpty()) {
                try {
                    stipend = new BigDecimal(stipendText.trim());

                    if (stipend.compareTo(BigDecimal.ZERO) < 0) {
                        response.setStatus(
                            HttpServletResponse.SC_BAD_REQUEST);
                        out.println("<h3>Stipend cannot be negative.</h3>");
                        out.println("<a href='internship.html'>Go Back</a>");
                        return;
                    }

                } catch (NumberFormatException e) {
                    response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST);
                    out.println("<h3>Please enter a valid stipend amount.</h3>");
                    out.println("<a href='internship.html'>Go Back</a>");
                    return;
                }
            }

            String sql =
                "INSERT INTO INTERNSHIP_APPLICATIONS " +
                "(APPLICATION_ID, SID, COMPANY_NAME, ROLE_NAME, APPLY_DATE, " +
                "JOB_LOCATION, WORK_MODE, STIPEND, APPLICATION_STATUS, " +
                "APPLICATION_LINK, NOTES) " +
                "VALUES (INTERNSHIP_APP_SEQ.NEXTVAL, ?, ?, ?, " +
                "NVL(TO_DATE(?, 'YYYY-MM-DD'), SYSDATE), ?, ?, ?, ?, ?, ?)";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, sid);
                ps.setString(2, companyName.trim());
                ps.setString(3, roleName.trim());

                if (applyDate == null || applyDate.trim().isEmpty()) {
                    ps.setNull(4, java.sql.Types.VARCHAR);
                } else {
                    ps.setString(4, applyDate.trim());
                }

                ps.setString(5, jobLocation);
                ps.setString(6, workMode);

                if (stipend == null) {
                    ps.setNull(7, java.sql.Types.NUMERIC);
                } else {
                    ps.setBigDecimal(7, stipend);
                }

                ps.setString(8, applicationStatus);
                ps.setString(9, applicationLink);
                ps.setString(10, notes);

                int rows = ps.executeUpdate();

                if (rows > 0) {
                    response.sendRedirect(
                        request.getContextPath() + "/internship-display");
                } else {
                    response.setStatus(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                    out.println("<h3>Application was not saved.</h3>");
                    out.println("<a href='internship.html'>Try Again</a>");
                }

            } catch (SQLException e) {
                getServletContext().log(
                    "Error saving internship application", e);

                response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                out.println("<h2>Unable to save internship application.</h2>");
                out.println("<p>Please check the Eclipse Console for the database error.</p>");
                out.println("<a href='internship.html'>Go Back</a>");
            }
        }
    }
}