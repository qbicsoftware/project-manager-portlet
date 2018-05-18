package life.qbic.portal.portlet.connection.database.projectInvestigatorDB;

import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.query.FreeformQuery;
import com.vaadin.data.util.sqlcontainer.query.TableQuery;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import life.qbic.portal.portlet.ProjectManagerUI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by sven on 12/11/16.
 */
public class ProjectDatabase implements ProjectDatabaseConnector {

  private String driverName = "com.mysql.jdbc.Driver";
  private String connectionURI = "jdbc:mysql://portal-testing.am10.uni-tuebingen.de:3306/project_investigator_db";
  private JDBCConnectionPool pool;
  private String user;
  private String password;
  private ProjectFilter filter;

  public ProjectDatabase(String user, String password, ProjectFilter filter) {
    this.user = user;
    this.password = password;
    this.filter = filter;
  }

  @Override
  public boolean connectToDatabase() throws SQLException {
    if (pool == null) {
        pool = new SimpleJDBCConnectionPool(driverName, connectionURI, user, password);
      return true;
    }
    return false;

  }

  @Override
  public SQLContainer loadCompleteTableData(String tableName, String primaryKey)
      throws RuntimeException, SQLException {
    TableQuery query = new TableQuery(tableName, pool);
    query.setVersionColumn(primaryKey);
    SQLContainer tableContent = new SQLContainer(query);
    tableContent.setAutoCommit(true);
    return tableContent;
  }

  @Override
  public FreeformQuery makeFreeFormQuery(QuerryType type, HashMap arguments, String primaryKey)
      throws SQLException, WrongArgumentSettingsException {
    FreeformQuery query = new FreeformQuery(
        SatusQuerryGenerator.getQuerryFromType(type, arguments, null), pool, primaryKey);
    return query;
  }

  @Override
  public FreeformQuery makeFreeFormQuery(QuerryType type, HashMap arguments, String primaryKey,
      List<String> followingProjects) throws SQLException, WrongArgumentSettingsException {
    FreeformQuery query = new FreeformQuery(
        SatusQuerryGenerator.getQuerryFromType(type, arguments, followingProjects), pool,
        primaryKey);
    return query;
  }

  @Override
  public JDBCConnectionPool getConnectionPool() {
    return this.pool;
  }

  @Override
  public ProjectFilter getProjectFilter() {
    return this.filter;
  }

  @Override
  public void setProjectFilter(ProjectFilter filter) {
    this.filter = filter;
  }

  @Override
  public SQLContainer loadSelectedTableData(String tableName, String primaryKey)
      throws SQLException, RuntimeException {

    TableQuery query = new TableQuery(tableName, pool);
    query.setVersionColumn(primaryKey);

    SQLContainer tableContent = new SQLContainer(query);
    tableContent.addContainerFilter(new Or(filter.getFilterList()));
    tableContent.setAutoCommit(true);
    return tableContent;
  }
}
