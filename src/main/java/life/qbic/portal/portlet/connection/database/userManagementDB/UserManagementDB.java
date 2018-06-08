package life.qbic.portal.portlet.connection.database.userManagementDB;

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import life.qbic.portal.Styles;
import life.qbic.portal.Styles.NotificationType;
import life.qbic.portal.portlet.ProjectManagerUI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserManagementDB {

  private static final Logger LOG = LogManager.getLogger(UserManagementDB.class);
  private String portNumber = "3306";
  private String serverName = "portal-testing.am10.uni-tuebingen.de";
  private Connection conn = null;

  public UserManagementDB(String userName, String password) {
    Properties connectionProps = new Properties();
    connectionProps.put("user", userName);
    connectionProps.put("password", password);

    try {
      conn = DriverManager.getConnection(
          "jdbc:mysql://" + this.serverName + ":" + this.portNumber + "/",
          connectionProps);
      LOG.info("Connection to user management DB established.");
    } catch (SQLException e) {
      LOG.error("Connection to user management DB failed.");
      Notification notif = new Notification("Connection to user management db failed!", Type.ERROR_MESSAGE);
      notif.setDelayMsec(500000000);
      notif.show(Page.getCurrent());
      //e.printStackTrace();
    }
  }


  public int getProjectID(String projectCode) {
    Statement stmt = null;
    int projectID = -1;
    String query = "SELECT id " +
        "FROM " + "qbic_usermanagement_db" + ".projects" +
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
        "FROM " + "qbic_usermanagement_db" + ".projects_persons" +
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
    String query = "SELECT * " +
        "FROM " + "qbic_usermanagement_db" + ".persons" +
        " WHERE " + "id" + "=" + personID;
    try {
      stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      while (rs.next()) {
        String title = rs.getString("title");
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
        "FROM " + "qbic_usermanagement_db" + ".persons" +
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
    String offerID = "";
    String query = "SELECT id " +
        "FROM " + "facs_facility" + ".offers" +
        " WHERE " + "offer_project_reference" + " LIKE " + "'%" + projectCode + "%'";
    try {
      stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      while (rs.next()) {
        offerID = rs.getString("offer_number");
      }
    } catch (Exception e) {
      LOG.info("No offer found for project " + projectCode + " in the facs facility DB.");
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
    return offerID;
  }


}
