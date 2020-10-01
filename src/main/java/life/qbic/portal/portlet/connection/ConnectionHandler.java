package life.qbic.portal.portlet.connection;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import com.liferay.util.portlet.PortletProps;
import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import life.qbic.portal.portlet.connection.database.projectInvestigatorDB.ProjectDatabase;
import life.qbic.portal.portlet.connection.database.projectInvestigatorDB.ProjectFilter;
import life.qbic.portal.portlet.connection.database.userManagementDB.UserManagementDB;
import life.qbic.portal.portlet.connection.openbis.OpenBisConnection;
import life.qbic.portal.utils.ConfigurationManager;
import life.qbic.portal.utils.ConfigurationManagerFactory;
import life.qbic.portal.utils.PortalUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConnectionHandler {

  private static final Logger LOG = LogManager.getLogger(ConnectionHandler.class);
  private String mysqlUser, mysqlPW, openBisPw, openBisUser, openBisUrl, userID, hostname, port;
  private ConfigurationManager conf = ConfigurationManagerFactory.getInstance();
  private UserManagementDB userManagementDB;
  private ProjectDatabase projectDatabase;
  private OpenBisConnection openBisConnection;

  private String propertyFilePath = "developer.properties";

  public ConnectionHandler(ProjectFilter projectFilter) {
    setCredentials();
    userManagementDB = new UserManagementDB(mysqlUser, mysqlPW, hostname, port);
    projectDatabase = new ProjectDatabase(mysqlUser, mysqlPW, hostname, port, projectFilter);
    try {
      projectDatabase.connectToDatabase();
      LOG.info("Connection to project DB established.");
    } catch (Exception e) {
      LOG.info("Connection to project DB failed.");
      Notification notif = new Notification("Connection to projectDB failed!", Type.ERROR_MESSAGE);
      notif.setDelayMsec(500000000);
      notif.show(Page.getCurrent());
    }

    openBisConnection = connectToOpenBis();
  }

  public OpenBisConnection connectToOpenBis() {
    try {

      // get a reference to AS API
      IApplicationServerApi app = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class,
          openBisUrl + IApplicationServerApi.SERVICE_URL,
          10000);

      // TODO: login/loginAs? loginAs seems to return a null sessionToken
      String sessionToken = app.login(openBisUser, openBisPw);
      openBisConnection = new OpenBisConnection(app, sessionToken);
      LOG.info("Connection to openBIS established.");

    } catch (Exception e) {
      LOG.error("Connection to openBIS failed.");
      e.printStackTrace();
    }

    return openBisConnection;
  }

  public void setCredentials() {
    LOG.info("Set credentials");
    LOG.info("Using configuration manager {}", conf.getClass().getName());

    mysqlUser = conf.getMysqlUser();
    LOG.info("mysql user = {}", mysqlUser);
    mysqlPW = conf.getMysqlPass();
    hostname = conf.getMysqlHost();
    LOG.info("mysql host = {}", hostname);
    port = conf.getMysqlPort();
    openBisUser = conf.getDataSourceUser();
    LOG.info("openBIS user = {}", openBisUser);
    openBisPw = conf.getDataSourcePassword();
    openBisUrl = conf.getDataSourceUrl() + "/openbis/openbis" ;
    userID = PortalUtils.getNonNullScreenName();
    if (mysqlUser == null || openBisUser == null) {
      LOG.info("No Liferay Portlet found. Getting user and passwords from local file {}", propertyFilePath);
      getCredentials(propertyFilePath);
    }
  }


  private void getCredentials(String propertyFilePath) {
    Properties prop = new Properties();
    try (final InputStream input = new FileInputStream(propertyFilePath)) {
      // load a properties file
      prop.load(input);

      // get the property value and print it out
      openBisPw = prop.getProperty("datasource.password");
      openBisUser = prop.getProperty("datasource.user");
      userID = prop.getProperty("portal.user");
      openBisUrl = prop.getProperty("datasource.url");
      mysqlPW = prop.getProperty("mysql.pass");
      mysqlUser = prop.getProperty("mysql.user");
      hostname = prop.getProperty("mysql.host");
      port = prop.getProperty("mysql.port");

    } catch (IOException ex) {
      throw new RuntimeException("Could not find the property file.", ex);
    }
  }


  public String getOpenBisUser() {
    return openBisUser;
  }

  public UserManagementDB getUserManagementDB() {
    return userManagementDB;
  }

  public ProjectDatabase getProjectDatabase() {
    return projectDatabase;
  }

  public OpenBisConnection getOpenBisConnection() {
    return openBisConnection;
  }

  public String getUserID() {
    return userID;
  }
}
