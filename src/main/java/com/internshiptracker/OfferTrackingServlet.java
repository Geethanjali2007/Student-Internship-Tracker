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

@WebServlet("/offer-tracking")
public class OfferTrackingServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("sid") == null) {
            response.sendRedirect(
                request.getContextPath() + "/login.html"
            );
            return;
        }

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            out.println("<!DOCTYPE html>");
            out.println("<html><head><meta charset='UTF-8'>");
            out.println("<title>Offer Tracking</title>");
            out.println("<style>");
            out.println("body{font-family:Arial;background:#f3f6fb;margin:0;padding:25px;color:#222}");
            out.println(".container{max-width:1100px;margin:auto}");
            out.println(".card{background:white;padding:25px;margin-bottom:25px;border-radius:12px;box-shadow:0 3px 12px #ddd}");
            out.println("h1,h2{color:#263b70}");
            out.println("form{display:grid;grid-template-columns:1fr 1fr;gap:15px}");
            out.println("label{font-weight:bold;display:block;margin-bottom:6px}");
            out.println("input,select,textarea{width:100%;box-sizing:border-box;padding:10px;border:1px solid #ccc;border-radius:6px}");
            out.println("textarea{resize:vertical}");
            out.println(".full{grid-column:1/-1}");
            out.println("button{background:#263b70;color:white;padding:12px 20px;border:0;border-radius:6px;cursor:pointer}");
            out.println("table{width:100%;border-collapse:collapse;background:white}");
            out.println("th,td{padding:10px;border:1px solid #ddd;text-align:left}");
            out.println("th{background:#263b70;color:white}");
            out.println(".table-wrap{overflow-x:auto}");
            out.println("@media(max-width:650px){form{grid-template-columns:1fr}.full{grid-column:auto}}");
            out.println("</style></head><body><div class='container'>");

            out.println("<h1>Offer, Stipend & Package Tracking</h1>");

            String message = request.getParameter("message");
            if (message != null) {
                out.println("<p style='background:#e4f8e8;padding:12px;border-radius:6px'>");
                out.println(escapeHtml(message));
                out.println("</p>");
            }

            out.println("<div class='card'><h2>Add Offer Details</h2>");
            out.println("<form method='post' action='" +
                request.getContextPath() + "/offer-tracking'>");

            out.println("<div><label>Application ID</label>");
            out.println("<input type='number' name='applicationId' min='1' required></div>");

            out.println("<div><label>Company Name</label>");
            out.println("<input type='text' name='companyName' maxlength='150' required></div>");

            out.println("<div><label>Job Role</label>");
            out.println("<input type='text' name='jobRole' maxlength='150'></div>");

            out.println("<div><label>Offer Type</label><select name='offerType'>");
            out.println("<option value='Internship'>Internship</option>");
            out.println("<option value='Full-Time Job'>Full-Time Job</option>");
            out.println("</select></div>");

            out.println("<div><label>Monthly Stipend (₹)</label>");
            out.println("<input type='number' name='monthlyStipend' min='0' step='0.01' value='0' required></div>");

            out.println("<div><label>Annual CTC (₹)</label>");
            out.println("<input type='number' name='annualCtc' min='0' step='0.01' value='0' required></div>");

            out.println("<div><label>Offer Date</label>");
            out.println("<input type='date' name='offerDate'></div>");

            out.println("<div><label>Joining Date</label>");
            out.println("<input type='date' name='joiningDate'></div>");

            out.println("<div><label>Offer Status</label><select name='offerStatus'>");
            out.println("<option>Offer Received</option>");
            out.println("<option>Accepted</option>");
            out.println("<option>Rejected</option>");
            out.println("<option>On Hold</option>");
            out.println("<option>Joined</option>");
            out.println("</select></div>");

            out.println("<div><label>Notes</label>");
            out.println("<input type='text' name='notes' maxlength='500'></div>");

            out.println("<div class='full'><button type='submit'>Save Offer</button></div>");
            out.println("</form></div>");

            out.println("<div class='card'><h2>My Offers</h2><div class='table-wrap'>");
            out.println("<table><tr><th>Application ID</th><th>Company</th><th>Role</th>");
            out.println("<th>Type</th><th>Monthly Stipend</th><th>Annual CTC</th>");
            out.println("<th>Offer Date</th><th>Joining Date</th><th>Status</th><th>Notes</th></tr>");

            String sql =
                "SELECT O.APPLICATION_ID, O.COMPANY_NAME, O.JOB_ROLE, " +
                "O.OFFER_TYPE, O.MONTHLY_STIPEND, O.ANNUAL_CTC, " +
                "O.OFFER_DATE, O.JOINING_DATE, O.OFFER_STATUS, O.NOTES " +
                "FROM INTERNSHIP_OFFERS O " +
                "JOIN INTERNSHIP_APPLICATIONS A " +
                "ON O.APPLICATION_ID = A.APPLICATION_ID " +
                "WHERE O.SID = ? AND A.SID = ? " +
                "ORDER BY O.OFFER_ID DESC";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                int sid = Integer.parseInt(session.getAttribute("sid").toString());
                ps.setInt(1, sid);
                ps.setInt(2, sid);

                try (ResultSet rs = ps.executeQuery()) {
                    boolean found = false;

                    while (rs.next()) {
                        found = true;
                        out.println("<tr>");
                        out.println("<td>" + rs.getInt("APPLICATION_ID") + "</td>");
                        out.println("<td>" + escapeHtml(rs.getString("COMPANY_NAME")) + "</td>");
                        out.println("<td>" + escapeHtml(rs.getString("JOB_ROLE")) + "</td>");
                        out.println("<td>" + escapeHtml(rs.getString("OFFER_TYPE")) + "</td>");
                        out.println("<td>₹" + rs.getBigDecimal("MONTHLY_STIPEND") + "</td>");
                        out.println("<td>₹" + rs.getBigDecimal("ANNUAL_CTC") + "</td>");
                        out.println("<td>" + rs.getDate("OFFER_DATE") + "</td>");
                        out.println("<td>" + rs.getDate("JOINING_DATE") + "</td>");
                        out.println("<td>" + escapeHtml(rs.getString("OFFER_STATUS")) + "</td>");
                        out.println("<td>" + escapeHtml(rs.getString("NOTES")) + "</td>");
                        out.println("</tr>");
                    }

                    if (!found) {
                        out.println("<tr><td colspan='10'>No offers found for your account.</td></tr>");
                    }
                }

            } catch (SQLException | NumberFormatException e) {
                getServletContext().log("Unable to load offers", e);
                out.println("<tr><td colspan='10'>Unable to load offers. Check server logs.</td></tr>");
            }

            out.println("</table></div></div>");
            out.println("<p><a href='" + request.getContextPath() +
                "/dashboard'>Back to Dashboard</a></p>");
            out.println("</div></body></html>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("sid") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            int sid = Integer.parseInt(session.getAttribute("sid").toString());
            int applicationId = Integer.parseInt(request.getParameter("applicationId"));

            String companyName = request.getParameter("companyName");
            String jobRole = request.getParameter("jobRole");
            String offerType = request.getParameter("offerType");
            String offerStatus = request.getParameter("offerStatus");
            String notes = request.getParameter("notes");

            java.math.BigDecimal stipend =
                new java.math.BigDecimal(request.getParameter("monthlyStipend"));
            java.math.BigDecimal ctc =
                new java.math.BigDecimal(request.getParameter("annualCtc"));

            if (companyName == null || companyName.trim().isEmpty()
                    || stipend.signum() < 0 || ctc.signum() < 0) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Enter a valid company name, stipend, and CTC.");
                return;
            }

            String sql =
                "INSERT INTO INTERNSHIP_OFFERS (" +
                "OFFER_ID, APPLICATION_ID, SID, COMPANY_NAME, JOB_ROLE, " +
                "OFFER_TYPE, MONTHLY_STIPEND, ANNUAL_CTC, OFFER_DATE, " +
                "JOINING_DATE, OFFER_STATUS, NOTES) " +
                "SELECT INTERNSHIP_OFFER_SEQ.NEXTVAL, A.APPLICATION_ID, A.SID, " +
                "?, ?, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), " +
                "TO_DATE(?, 'YYYY-MM-DD'), ?, ? " +
                "FROM INTERNSHIP_APPLICATIONS A " +
                "WHERE A.APPLICATION_ID = ? AND A.SID = ?";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, companyName.trim());
                ps.setString(2, jobRole == null ? null : jobRole.trim());
                ps.setString(3, offerType);
                ps.setBigDecimal(4, stipend);
                ps.setBigDecimal(5, ctc);

                setDateParameter(ps, 6, request.getParameter("offerDate"));
                setDateParameter(ps, 7, request.getParameter("joiningDate"));

                ps.setString(8, offerStatus);
                ps.setString(9, notes == null ? null : notes.trim());
                ps.setInt(10, applicationId);
                ps.setInt(11, sid);

                int rows = ps.executeUpdate();

                if (rows == 0) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Application not found for your account. Check Application ID.");
                    return;
                }
            }

            response.sendRedirect(request.getContextPath() +
                "/offer-tracking?message=Offer+saved+successfully");

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "Invalid application ID, stipend, or CTC.");
        } catch (SQLException e) {
            getServletContext().log("Unable to save offer", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Could not save offer. Check server logs and database constraints.");
        }
    }

    private void setDateParameter(PreparedStatement ps, int index, String value)
            throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            ps.setNull(index, java.sql.Types.DATE);
        } else {
            ps.setString(index, value);
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
