
package com.internshiptracker;

import java.io.IOException;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * Verify the entered password against the stored password hash.
     *
     * Expected storage format:
     * iterations:Base64Salt:Base64PasswordHash
     */
    private boolean verifyPassword(String password, String storedHash)
            throws Exception {

        String[] parts = storedHash.split(":", -1);

        if (parts.length != 3) {
            return false;
        }

        int iterations;

        try {
            iterations = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            return false;
        }

        if (iterations <= 0
                || parts[1].isEmpty()
                || parts[2].isEmpty()) {
            return false;
        }

        byte[] salt;
        byte[] expectedHash;

        try {
            salt = Base64.getDecoder().decode(parts[1]);
            expectedHash = Base64.getDecoder().decode(parts[2]);
        } catch (IllegalArgumentException e) {
            return false;
        }

        if (salt.length == 0 || expectedHash.length == 0) {
            return false;
        }

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                iterations,
                expectedHash.length * 8
        );

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] actualHash =
                    factory.generateSecret(spec).getEncoded();

            return MessageDigest.isEqual(expectedHash, actualHash);

        } finally {
            spec.clearPassword();
        }
    }

    /**
     * GET /login
     * Open the login page.
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.sendRedirect(
                request.getContextPath() + "/login.html"
        );
    }

    /**
     * POST /login
     * Authenticate the student and open the dashboard.
     */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("semail");
        String password = request.getParameter("password");

        // Validate required fields.
        if (email == null
                || email.trim().isEmpty()
                || password == null
                || password.isEmpty()) {

            redirectToLogin(request, response, "empty");
            return;
        }

        String sql =
                "SELECT SID, SNAME, SEMAIL, PASSWORD_HASH "
                + "FROM INTERNSHIP_STUDENTS "
                + "WHERE LOWER(SEMAIL) = LOWER(?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email.trim());

            try (ResultSet rs = ps.executeQuery()) {

                // Email was not found.
                if (!rs.next()) {
                    redirectToLogin(request, response, "invalid");
                    return;
                }

                int sid = rs.getInt("SID");
                String sname = rs.getString("SNAME");
                String semail = rs.getString("SEMAIL");
                String storedHash = rs.getString("PASSWORD_HASH");

                // The account has no stored password hash.
                if (storedHash == null || storedHash.trim().isEmpty()) {
                    redirectToLogin(request, response, "passwordnotset");
                    return;
                }

                boolean validPassword;

                try {
                    validPassword = verifyPassword(password, storedHash);

                } catch (Exception e) {

                    getServletContext().log(
                            "Password verification failed.", e
                    );

                    redirectToLogin(request, response, "server");
                    return;
                }

                // Password does not match.
                if (!validPassword) {
                    redirectToLogin(request, response, "invalid");
                    return;
                }

                // Invalidate any old session after successful authentication.
                HttpSession oldSession = request.getSession(false);

                if (oldSession != null) {
                    oldSession.invalidate();
                }

                // Create a new session.
                HttpSession session = request.getSession(true);

                session.setAttribute("sid", sid);
                session.setAttribute("sname", sname);
                session.setAttribute("semail", semail);

                // Session expires after 30 minutes of inactivity.
                session.setMaxInactiveInterval(30 * 60);

                // Login successful: redirect to the dashboard.
                response.sendRedirect(
                        request.getContextPath() + "/dashboard"
                );
            }

        } catch (Exception e) {

            getServletContext().log(
                    "Login failed due to a server error.", e
            );

            redirectToLogin(request, response, "server");
        }
    }

    /**
     * Redirect to login.html with an error code.
     */
    private void redirectToLogin(HttpServletRequest request,
                                 HttpServletResponse response,
                                 String error)
            throws IOException {

        response.sendRedirect(
                request.getContextPath()
                + "/login.html?error=" + error
        );
    }
}