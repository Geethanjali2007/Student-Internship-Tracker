# Student Internship Tracker

A web-based application developed to help students manage and track their internship applications and placement offers in one place.

## Overview

The Student Internship Tracker provides a centralized platform where students can create accounts, manage internship application details, monitor application statuses, and track received offers.

The application uses Java Servlets and JDBC for backend processing and Oracle Database XE for data storage.

## Features

- **Student Registration:** Create a student account with personal and login details.
- **Student Login and Logout:** Authenticate users and manage sessions.
- **Dashboard:** View internship application statistics.
- **Internship Application Management:** Add, view, edit, and delete internship application records.
- **Application Status Tracking:** Monitor the status of internship applications.
- **Offer Tracking:** Manage internship and placement offer information.
- **Database Integration:** Store and retrieve student and application information using Oracle Database.

## Technology Stack

| Category              | Technologies            |
| --------------------- | ----------------------- |
| Frontend              | HTML5, CSS3, JavaScript |
| Backend               | Java, Java Servlets     |
| Database              | Oracle Database XE      |
| Database Connectivity | JDBC                    |
| Server                | Apache Tomcat 9         |
| Build Tool            | Apache Maven            |
| IDE                   | Eclipse IDE             |
| Version Control       | Git and GitHub          |

## Project Structure

```text
StudentInternshipTracker/
│
├── database/
│   └── schema.sql
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── internshiptracker/
│       │           ├── DBConnection.java
│       │           ├── StudentServlet.java
│       │           ├── LoginServlet.java
│       │           ├── LogoutServlet.java
│       │           ├── DashboardServlet.java
│       │           ├── InternshipServlet.java
│       │           ├── InternshipDisplayServlet.java
│       │           ├── EditStudentServlet.java
│       │           ├── DeleteStudentServlet.java
│       │           ├── DisplayServlet.java
│       │           └── OfferTrackingServlet.java
│       │
│       └── webapp/
│           ├── Register.html
│           ├── login.html
│           ├── internship.html
│           ├── WEB-INF/
│           │   └── web.xml
│           └── META-INF/
│
├── pom.xml
├── .gitignore
└── README.md
```

## Prerequisites

Install the following software before running the application:

- JDK 17
- Eclipse IDE with Enterprise Java and Web Developer Tools
- Apache Tomcat 9
- Oracle Database XE
- Apache Maven
- Oracle SQL Developer (recommended for database setup)

## Installation and Setup

### 1. Clone the Repository

```bash
git clone https://github.com/Geethanjali2007/Student-Internship-Tracker.git
```

### 2. Import the Project into Eclipse

1. Open Eclipse IDE.
2. Select **File → Import**.
3. Choose **Existing Maven Projects**.
4. Browse to the cloned project folder.
5. Select the project and click **Finish**.

### 3. Configure the Oracle Database

1. Start Oracle Database XE.
2. Open Oracle SQL Developer.
3. Connect to your Oracle database using your database account.
4. Open the `database/schema.sql` file.
5. Execute the SQL script using **F5 (Run Script)**.
6. Verify that the required tables and sequences have been created.

The schema script creates the following database objects:

**Tables**

- `INTERNSHIP_STUDENTS`
- `INTERNSHIP_APPLICATIONS`
- `INTERNSHIP_OFFERS`

**Sequences**

- `INTERNSHIP_APP_SEQ`
- `INTERNSHIP_OFFER_SEQ`

### 4. Configure Database Connectivity

Update the database connection configuration in `DBConnection.java`.

Default Oracle XE connection URL:

```text
jdbc:oracle:thin:@localhost:1521:XE
```

Configure your own database username and password. Do not commit actual database credentials to GitHub.

### 5. Configure Apache Tomcat

1. Add Apache Tomcat 9 to Eclipse.
2. Right-click the project.
3. Select **Run As → Run on Server**.
4. Select Tomcat 9.
5. Finish the configuration and start the server.

### 6. Access the Application

Open your browser and navigate to:

```text
http://localhost:8080/StudentInternshipTracker/
```

The application starting page depends on the configured welcome page.

## Database Design

The application uses Oracle Database XE with JDBC connectivity.

| Database Object         | Description                           |
| ----------------------- | ------------------------------------- |
| INTERNSHIP_STUDENTS     | Stores student information            |
| INTERNSHIP_APPLICATIONS | Stores internship application details |
| INTERNSHIP_OFFERS       | Stores offer information              |
| INTERNSHIP_APP_SEQ      | Generates application IDs             |
| INTERNSHIP_OFFER_SEQ    | Generates offer IDs                   |

The SQL schema is available in the `database/schema.sql` file.

## Learning Outcomes

Through this project, we gained practical experience in:

- Java web application development.
- Client-server architecture.
- Java Servlet request and response handling.
- JDBC-based database connectivity.
- SQL operations and relational database management.
- User authentication and session management.
- Frontend and backend integration.
- Web application deployment using Apache Tomcat.

## Future Enhancements

- Advanced internship search and filtering.
- Interview scheduling and reminders.
- Application deadline notifications.
- Exporting internship application reports.
- Improved security and password management.

## Contributors

1. **Reddem Geethanjali** – [https://github.com/Geethanjali2007](https://github.com/Geethanjali2007)
2. **Puli Naga Pradeepthi Reddy**- [https://github.com/pulinagapradeepthireddy-sketch](https://github.com/pulinagapradeepthireddy-sketch)
3. **Shaik Thoushya** – [https://github.com/shaikthoushya-oss](https://github.com/shaikthoushya-oss)
4. **Seelam Prakash Reddy –&#x20;**[https://github.com/SeelamPrakashReddy](https://github.com/SeelamPrakashReddy)

## License

This project was developed for academic and educational purposes.
