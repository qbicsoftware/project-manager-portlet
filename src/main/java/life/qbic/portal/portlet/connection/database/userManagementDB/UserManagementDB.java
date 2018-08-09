package life.qbic.portal.portlet.connection.database.userManagementDB;

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import life.qbic.portal.Styles;
import life.qbic.portal.Styles.NotificationType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserManagementDB {

  private static final Logger LOG = LogManager.getLogger(UserManagementDB.class);
  private Connection conn = null;
  private String database = "qbic_usermanagement_db";

  public UserManagementDB(String userName, String password, String hostname, String port) {
    String connectionURI = "jdbc:mysql://" + hostname + ":" + port + "/" + database;

    try {
      Class.forName("com.mysql.jdbc.Driver");
      conn = DriverManager.getConnection(connectionURI, userName, password);
      LOG.info("Connection to user management DB established.");
    } catch (SQLException e) {
      LOG.error("Connection to user management DB failed. [SQLException]");
      e.printStackTrace();
      Notification notification = new Notification("Connection could not be established.");
      notification.show(Page.getCurrent());
    } catch (ClassNotFoundException e) {
      LOG.error("Connection to user management DB failed. [ClassNotFoundException]");
      Notification notification = new Notification("Connection could not be established.");
      notification.show(Page.getCurrent());
      e.printStackTrace();
    }
  }


  public int getProjectID(String projectCode) {
    Statement stmt = null;
    int projectID = -1;
    String query = "SELECT id " +
        "FROM " + database + ".projects" +
        " WHERE " + "openbis_project_identifier" + " LIKE " + "'%" + projectCode + "%'";
    try {
      stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      while (rs.next()) {
        projectID = rs.getInt("id");
      }
    } catch (Exception e) {
      Styles
          .notification("Project not found.",
              "The project with the ID " + projectID + " could not have been found.",
              NotificationType.SUCCESS);
      LOG.error("Project " + projectID + " could not been found in the user management DB.");
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
    return projectID;
  }

  public int getPIID(int projectID) {
    Statement stmt = null;
    int personID = -1;
    String query = "SELECT * " +
        "FROM " + database + ".projects_persons" +
        " WHERE " + "project_id" + "=" + projectID + " AND " + "project_role" + " LIKE " + "'%PI%'";
    try {
      stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      while (rs.next()) {
        personID = rs.getInt("person_id");
      }
    } catch (SQLException e) {
      LOG.error("PI ID " + personID + " could not been found in the user management DB.");
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
    return personID;
  }

  public String getPI(int personID) {
    Statement stmt = null;
    String pi = null;
    String title = "";
    String query = "SELECT * " +
        "FROM " + database + ".persons" +
        " WHERE " + "id" + "=" + personID;
    try {
      stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      while (rs.next()) {
        if (rs.getString("title") != null) {
          title = rs.getString("title");
        }
        String first = rs.getString("first_name");
        String family = rs.getString("family_name");
        pi = title + " " + first + " " + family;
      }
    } catch (SQLException e) {
      LOG.error("PI " + pi + " could not been found in the user management DB.");
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
    return pi;
  }

  public String getEmail(String personID) {

    Statement stmt = null;
    String email = null;
    String query = "SELECT email " +
        "FROM " + database + ".persons" +
        " WHERE " + "id" + "=" + personID;
    try {
      stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      while (rs.next()) {
        email = rs.getString("email");
      }
    } catch (SQLException e) {
      LOG.error("E-Mail of " + personID + " could not been found in the user management DB.");
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
    return email;
  }

  public String getProjectPI(String projectCode) {
    String pi = getPI(getPIID(getProjectID(projectCode)));
    return pi;
  }

  public String getOfferID(String projectCode) {
    Statement stmt = null;
    String offerNumber= "";
    String query = "SELECT offer_number " +
        "FROM " + "facs_facility" + ".offers" +
        " WHERE " + "offer_project_reference" + " LIKE " + "'%" + projectCode + "%'";
    try {
      stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      while (rs.next()) {
        offerNumber = rs.getString("offer_number");
      }
    } catch (NullPointerException e) {
      //nothing
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    return offerNumber;
  }


}
