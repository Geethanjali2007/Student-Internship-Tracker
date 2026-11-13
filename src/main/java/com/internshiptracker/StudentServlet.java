package com.internshiptracker;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/register")
public class StudentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int ITERATIONS = 120000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private String hashPassword(String password) throws Exception {

        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] hash = factory.generateSecret(spec).getEncoded();

            return ITERATIONS + ":"
                    + Base64.getEncoder().encodeToString(salt) + ":"
                    + Base64.getEncoder().encodeToString(hash);

        } finally {
            spec.clearPassword();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.sendRedirect(
                request.getContextPath() + "/Register.html"
        );
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String sidText = request.getParameter("sid");
        String sname = request.getParameter("sname");
        String semail = request.getParameter("semail");
        String saddr = request.getParameter("saddr");
        String phno = request.getParameter("phno");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // Validate required fields
        if (isBlank(sidText) || isBlank(sname) || isBlank(semail)
                || isBlank(password) || isBlank(confirmPassword)) {

            redirectToRegister(request, response, "empty");
            return;
        }

        sidText = sidText.trim();
        sname = sname.trim();
        semail = semail.trim();

        // Validate student ID
        int sid;

        try {
            sid = Integer.parseInt(sidText);

            if (sid <= 0) {
                redirectToRegister(request, response, "invalidid");
                return;
            }

        } catch (NumberFormatException e) {
            redirectToRegister(request, response, "invalidid");
            return;
        }

        // Validate email format
        if (!semail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            redirectToRegister(request, response, "invalidemail");
            return;
        }

        // Validate password length
        if (password.length() < 8 || password.length() > 128) {
            redirectToRegister(request, response, "weakpassword");
            return;
        }

        // Check confirmation
        if (!password.equals(confirmPassword)) {
            redirectToRegister(request, response, "passwordmismatch");
            return;
        }

        String sql =
                "INSERT INTO INTERNSHIP_STUDENTS " +
                "(SID, SNAME, SEMAIL, SADDR, PHNO, PASSWORD_HASH) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            String passwordHash = hashPassword(password);

            try (Connection con = DBConnection.getConnection()) {

                // Prevent duplicate email addresses
                String emailCheckSql =
                        "SELECT SID FROM INTERNSHIP_STUDENTS " +
                        "WHERE LOWER(SEMAIL) = LOWER(?)";

                try (PreparedStatement emailCheck =
                             con.prepareStatement(emailCheckSql)) {

                    emailCheck.setString(1, semail);

                    try (java.sql.ResultSet rs = emailCheck.executeQuery()) {
                        if (rs.next()) {
                            redirectToRegister(
                                    request, response, "emailexists"
                            );
                            return;
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setInt(1, sid);
                    ps.setString(2, sname);
                    ps.setString(3, semail);
                    ps.setString(4, saddr == null ? "" : saddr.trim());
                    ps.setString(5, phno == null ? "" : phno.trim());
                    ps.setString(6, passwordHash);

                    int rows = ps.executeUpdate();

                    if (rows > 0) {
                        response.sendRedirect(
                                request.getContextPath()
                                + "/login.html?registered=success"
                        );
                    } else {
                        redirectToRegister(
                                request, response, "server"
                        );
                    }
                }
            }

        } catch (SQLException e) {

            getServletContext().log(
                    "Student registration database error.", e
            );

            // Oracle error code 1 means unique constraint violation.
            // ORA-00001 can occur if the Student ID already exists.
            if (e.getErrorCode() == 1) {
                redirectToRegister(request, response, "idexists");
            } else {
                redirectToRegister(request, response, "server");
            }

        } catch (Exception e) {

            getServletContext().log(
                    "Student password hashing failed.", e
            );

            redirectToRegister(request, response, "server");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void redirectToRegister(HttpServletRequest request,
                                    HttpServletResponse response,
                                    String error)
            throws IOException {

        response.sendRedirect(
                request.getContextPath()
                + "/Register.html?error=" + error
        );
    }
}