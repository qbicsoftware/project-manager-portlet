package life.qbic.portal.portlet.project;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import life.qbic.portal.portlet.connection.database.projectInvestigatorDB.ProjectDatabaseConnector;
import life.qbic.portal.portlet.connection.database.userManagementDB.UserManagementDB;
import life.qbic.portal.portlet.connection.openbis.OpenBisConnection;

/**
 * Created by sven on 11/13/16. This class contains the business logic and is connected with the
 * MySQL database which contains all the information of QBiC projects.
 */
public class ProjectContentModel {

  private final ProjectDatabaseConnector projectDatabaseConnector;
  private HashMap<String, String> queryArguments = new HashMap<>();
  private String primaryKey = "projectID";
  private int unregisteredProjects, inTimeProjects, overdueProjects;
  private SQLContainer tableContent;
  private List<String> followingProjects;
  private OpenBisConnection openBisConnection;
  private UserManagementDB userManagementDB;
  private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  {
    queryArguments.put("table", "projectsoverview");
  }

  public ProjectContentModel(ProjectDatabaseConnector projectDatabaseConnector,
      UserManagementDB userManagementDB,
      List followingProjects, OpenBisConnection openBisConnection) {
    this.projectDatabaseConnector = projectDatabaseConnector;
    this.followingProjects = followingProjects;
    this.openBisConnection = openBisConnection;
    this.userManagementDB = userManagementDB;
  }

  public List<String> getFollowingProjects() {
    return this.followingProjects;
  }

  public final void init()
      throws SQLException, IllegalArgumentException {
    projectDatabaseConnector.connectToDatabase();
    this.tableContent = projectDatabaseConnector
        .loadSelectedTableData(queryArguments.get("table"), primaryKey);
    if (getFollowingProjects().size() > 0) {
      update();
    }
  }

  /**
   * Getter for the table content
   *
   * @return The table content
   */
  public final SQLContainer getTableContent() {
    return this.tableContent;
  }


  public void update() {
    try {
      if (getFollowingProjects().size() > 0) {
        writeInfos();
        writeProjectStatus();
      }
    } catch (Exception exp) {
      exp.printStackTrace();
    }
  }

  public void refresh() {
    try {
      this.tableContent = projectDatabaseConnector
          .loadSelectedTableData(queryArguments.get("table"), primaryKey);
      if (getFollowingProjects().size() > 0) {
        update();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * Request project timeline statistics
   *
   * @return A map containing values for different categories
   */
  public Map<String, Integer> getProjectsTimeLineStats() {
    LinkedHashMap<String, Integer> projectsStats = new LinkedHashMap<>();

    if (tableContent == null) {
      return projectsStats;
    }

    projectsStats = writeNumberProjectsPerTimeIntervalFromStart();

    return projectsStats;

  }

  private void writeInfos() {
    if (getFollowingProjects().size() > 0) {
      Collection<?> itemIds = tableContent.getItemIds();
      for (Object itemId : itemIds) {
        String projectID = tableContent.getContainerProperty(itemId, "projectID").getValue()
            .toString();
        Project project = openBisConnection.getProjectByCode(projectID);
        writeProjectDate(itemId, project);
        writeAnalyzedDate(itemId, project);
        writeRawDate(itemId, project);
        writeProjectPI(itemId, project);
        writeSamples(itemId, project);
        writeSpecies(itemId, project);
        writeSampleTypes(itemId, project);
        writeOfferID(itemId, project);
      }
    }
  }

  private void writeProjectDate(Object itemId, Project project) {
    tableContent.getContainerProperty(itemId, "projectRegisteredDate")
        .setValue(openBisConnection.getProjectRegistrationDate(project));
  }


  public void writeSamples(Object itemId, Project project) {
    tableContent.getContainerProperty(itemId, "samples")
        .setValue(openBisConnection.getSamplesOfProject(project).size());
  }

  public void writeSpecies(Object itemId, Project project) {
    tableContent.getContainerProperty(itemId, "species")
        .setValue(openBisConnection.getSpeciesOfProject(project));
  }


  private void writeRawDate(Object itemId, Project project) {
    tableContent.getContainerProperty(itemId, "rawDataRegistered")
        .setValue(openBisConnection.getFirstRegisteredDate(project));
  }

  private void writeAnalyzedDate(Object itemId, Project project) {
    tableContent.getContainerProperty(itemId, "dataAnalyzedDate")
        .setValue(openBisConnection.getFirstAnalyzedDate(project));
  }

  private void writeSampleTypes(Object itemId, Project project) {
    String sampleTypes = String.join(",", openBisConnection.getSampleTypesOfProject(project));
    tableContent.getContainerProperty(itemId, "sampleTypes")
        .setValue(sampleTypes);
  }

  public void writeProjectStatus() {
    if (getFollowingProjects().size() > 0) {
      Collection<?> itemIds = tableContent.getItemIds();
      for (Object itemId : itemIds) {
        if (tableContent.getContainerProperty(itemId, "rawDataRegistered").getValue() == null) {
          tableContent.getContainerProperty(itemId, "projectTime").setValue("unregistered");
        } else {
          try {
            Date currentDate = new Date();
            Date registration = dateFormat.parse(
                tableContent.getContainerProperty(itemId, "rawDataRegistered").getValue()
                    .toString());

            Date analyzed = null;
            long daysFromRegToAnalisis = 1000;
            if (tableContent.getContainerProperty(itemId, "dataAnalyzedDate").getValue() != null) {
              analyzed = dateFormat.parse(
                  tableContent.getContainerProperty(itemId, "dataAnalyzedDate").getValue()
                      .toString());
              daysFromRegToAnalisis = TimeUnit.DAYS
                  .convert(analyzed.getTime() - registration.getTime(), TimeUnit.MILLISECONDS);
            }
            long daysPassed = TimeUnit.DAYS
                .convert(currentDate.getTime() - registration.getTime(), TimeUnit.MILLISECONDS);
            if ((daysPassed / 7 < 6) || (daysFromRegToAnalisis / 7 < 6)) {
              tableContent.getContainerProperty(itemId, "projectTime").setValue("in time");
            } else {
              tableContent.getContainerProperty(itemId, "projectTime").setValue("overdue");
            }
          } catch (ParseException e) {
            e.printStackTrace();
          }
        }
      }
    }

  }

  private void writeProjectPI(Object itemId, Project project) {
    String projectPI = userManagementDB.getProjectPI(project.getCode());
    if (projectPI == null || projectPI.equals("")) {
      projectPI = "unknown";
    }
    tableContent.getContainerProperty(itemId, "investigatorName").setValue(projectPI);
  }

  private void writeOfferID(Object itemId, Project project) {
    String offerID = userManagementDB.getOfferID(project.getCode());
    if (offerID == null || offerID.equals("")) {
      offerID = "";
    }
    tableContent.getContainerProperty(itemId, "offerID").setValue(offerID);

  }

  public LinkedHashMap<String, Integer> writeNumberProjectsPerTimeIntervalFromStart() {

    LinkedHashMap<String, Integer> container = new LinkedHashMap<>();
    unregisteredProjects = 0;
    inTimeProjects = 0;
    overdueProjects = 0;
    for (Object itemId : tableContent.getItemIds()) {
      if (tableContent.getContainerProperty(itemId, "projectTime").getValue()
          .equals("unregistered")) {
        unregisteredProjects = unregisteredProjects + 1;
      } else if (tableContent.getContainerProperty(itemId, "projectTime").getValue()
          .equals("in time")) {
        inTimeProjects = inTimeProjects + 1;
      } else if (tableContent.getContainerProperty(itemId, "projectTime").getValue()
          .equals("overdue")) {
        overdueProjects = overdueProjects + 1;
      }
    }

    container.put("unregistered", unregisteredProjects);
    container.put("in time", inTimeProjects);
    container.put("overdue", overdueProjects);

    return container;
  }


  public int getUnregisteredProjects() {
    return unregisteredProjects;
  }

  public int getInTimeProjects() {
    return inTimeProjects;
  }

  public int getOverdueProjects() {
    return overdueProjects;
  }

  public Button exportProjects() {
    Button exportButton = new Button();
    try {
      Collection<?> itemIds = tableContent.getItemIds();
      String fileName = "project_overview";
      try {
        File projectFile = File.createTempFile(fileName, ".csv");
        FileWriter fw = new FileWriter(projectFile);
        BufferedWriter bw = new BufferedWriter(fw);
        String header = "Project,Status,Progress,PI,Species,Samples,Sample Types,Project Registered,Raw Data Registered,Data Analyzed,Offer ID,Invoice\n";
        bw.write(header);
        for (Object itemId : itemIds) {
          String projectName = tableContent.getContainerProperty(itemId, "projectID").getValue()
              .toString();
          String projectTime = tableContent.getContainerProperty(itemId, "projectTime").getValue()
              .toString();
          String projectStatus = tableContent.getContainerProperty(itemId, "projectStatus")
              .getValue()
              .toString();
          String projectPI = tableContent.getContainerProperty(itemId, "investigatorName")
              .getValue()
              .toString();
          String species = tableContent.getContainerProperty(itemId, "species").getValue()
              .toString();
          String samples = tableContent.getContainerProperty(itemId, "samples").getValue()
              .toString();
          String sampleType = tableContent.getContainerProperty(itemId, "sampleTypes").getValue()
              .toString();
          String projectRegisteredDate = tableContent
              .getContainerProperty(itemId, "projectRegisteredDate").getValue().toString();

          String rawDataRegisteredDate = "";
          try {
            rawDataRegisteredDate = tableContent
                .getContainerProperty(itemId, "rawDataRegistered").getValue().toString();
          } catch (NullPointerException e) {
            rawDataRegisteredDate = "";
          }
          String dataAnalyzedDate = "";
          try {
            dataAnalyzedDate = tableContent.getContainerProperty(itemId, "dataAnalyzedDate")
                .getValue().toString();
          } catch (NullPointerException e) {
            dataAnalyzedDate = "";
          }
          String offerID = "";
          try {
            offerID = tableContent.getContainerProperty(itemId, "offerID")
                .getValue().toString();
          } catch (NullPointerException e) {
            offerID = "";
          }
          String invoice = "";
          try {
            invoice = tableContent.getContainerProperty(itemId, "invoice")
                .getValue().toString();
          } catch (NullPointerException e) {
            invoice = "";
          }
          bw.write(
              projectName + "," + projectTime + "," + projectStatus + "," + projectPI + ","
                  + species
                  + "," + samples + "," + sampleType + "," + projectRegisteredDate + ","
                  + rawDataRegisteredDate + "," + dataAnalyzedDate + "," + offerID + "," + invoice
                  + "\n");
        }
        bw.close();
        fw.close();
        FileResource res = new FileResource(projectFile);
        FileDownloader fd = new FileDownloader(res);
        exportButton = new Button("Summary");
        exportButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
        fd.extend(exportButton);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (StringIndexOutOfBoundsException e) {
      exportButton = new Button("Summary");
    }

    return exportButton;
  }
}
