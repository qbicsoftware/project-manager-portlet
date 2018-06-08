package life.qbic.portal.portlet.connection;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;
import life.qbic.portal.portlet.ProjectManagerUI;
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
  private String mysqlUser, mysqlPW, openBisPw, openBisUser;
  private ConfigurationManager conf = ConfigurationManagerFactory.getInstance();
  private UserManagementDB userManagementDB;
  private ProjectDatabase projectDatabase;
  private OpenBisConnection openBisConnection;

  private String propertyFilePath = "/Users/spaethju/qbic-ext.properties";

  public ConnectionHandler(ProjectFilter projectFilter) {
    setCredentials();
    userManagementDB = new UserManagementDB(mysqlUser, mysqlPW);
    projectDatabase = new ProjectDatabase(mysqlUser, mysqlPW, projectFilter);
    try {
      projectDatabase.connectToDatabase();
      LOG.info("Connection to project DB established.");
    } catch (Exception e) {
      LOG.info("Connection to project DB failed.");
      Notification notif = new Notification("Connection to projectDB failed!", Type.ERROR_MESSAGE);
      notif.setDelayMsec(500000000);
      notif.show(Page.getCurrent());
      //e.printStackTrace();
    }

    openBisConnection = connectToOpenBis();
  }

  public OpenBisConnection connectToOpenBis() {
    try {

      // Connect to openbis
      IDataStoreServerApi dss =
          HttpInvokerUtils.createStreamSupportingServiceStub(IDataStoreServerApi.class,
              "https://qbis.qbic.uni-tuebingen.de:444/datastore_server"
                  + IDataStoreServerApi.SERVICE_URL, 10000);

      // get a reference to AS API
      IApplicationServerApi app = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class,
          "https://qbis.qbic.uni-tuebingen.de/openbis/openbis" + IApplicationServerApi.SERVICE_URL,
          10000);

      String sessionToken = "";

      LOG.info("Connect user " + openBisUser + " to openbis.");
      sessionToken = app.login(openBisUser, openBisPw);
      openBisConnection = new OpenBisConnection(app, dss, sessionToken);
      LOG.info("Connection to openBIS established.");

    } catch (Exception e) {
        LOG.error("Connection to openBIS failed.");
      Notification notif = new Notification("Connection to openbis failed!", Type.ERROR_MESSAGE);
      notif.setDelayMsec(500000000);
      notif.show(Page.getCurrent());
        //e.printStackTrace();
      }

    return openBisConnection;
  }

  public void setCredentials() {
    LOG.info("Set credentials");
    try {
      mysqlUser = conf.getMysqlUser();
      mysqlPW = conf.getMysqlPass();
      openBisUser = conf.getDataSourceUser();
      openBisPw = conf.getDataSourcePassword();
      LOG.info(mysqlUser, openBisUser);
    } catch (Exception e) {
      LOG.info("No Liferay Portlet found. Get user and passwords from local file.");

      getCredentials(propertyFilePath);
    }
  }

  private void getCredentials(String propertyFilePath) {
    Properties prop = new Properties();
    InputStream input = null;

    try {

      input = new FileInputStream(propertyFilePath);

      // load a properties file
      prop.load(input);

      // get the property value and print it out
      openBisPw = prop.getProperty("datasource.password");
      openBisUser = prop.getProperty("datasource.user");
      mysqlPW = prop.getProperty("mysql.pass");
      mysqlUser = prop.getProperty("mysql.user");

    } catch (IOException ex) {
      LOG.error("Could not find the property file. ");
      //ex.printStackTrace();
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
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
}
