package life.qbic.portal.portlet.module.projectOverviewModule;

import com.vaadin.addon.charts.PointClickListener;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Field;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import life.qbic.portal.portlet.ProjectManagerUI;
import life.qbic.portal.portlet.connection.database.projectInvestigatorDB.ColumnTypes;
import life.qbic.portal.portlet.connection.database.projectInvestigatorDB.ProjectDatabaseConnector;
import life.qbic.portal.portlet.connection.database.projectInvestigatorDB.TableColumns;
import life.qbic.portal.portlet.connection.openbis.OpenBisConnection;
import life.qbic.portal.portlet.module.overviewChartModule.OverviewChartPresenter;
import life.qbic.portal.portlet.project.ProjectContentModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vaadin.gridutil.cell.GridCellFilter;

/**
 * This presenter class will connect the UI with the underlying logic. As this module will display
 * the projectmanager database content, the presenter will request data from the model, which
 * handles the SQL connection and contains the information to be shown. Issues and Errors will be
 * directed to the user via a notification message.
 */
public class ProjectOVPresenter {

  private static final Logger LOG = LogManager.getLogger(ProjectOVPresenter.class);
  private ProjectContentModel contentModel;
  private ProjectOverviewModule overViewModule;
  private String overviewTable = "projectsoverview";
  private ObjectProperty<Boolean> overviewModuleChanged = new ObjectProperty<>(true);
  private ObjectProperty<String> selectedProject = new ObjectProperty<>("");
  private ProjectDatabaseConnector connection;

  private Button unfollowButton = new Button("Unfollow");
  private Button detailsButton = new Button("Details");
  private String portalURL = "https://portal.qbic.uni-tuebingen.de/portal/web/qbic/qnavigator#!project/";
  private ColumnFieldTypes columnFieldTypes;
  private OverviewChartPresenter overviewChartPresenter;
  private OpenBisConnection openbis;
  private Item selectedProjectItem = null;

  public ProjectOVPresenter(ProjectContentModel model,
      ProjectOverviewModule overViewModule,
      OverviewChartPresenter overviewChartPresenter,
      OpenBisConnection openbis,
      ProjectDatabaseConnector connection) {
    this.contentModel = model;
    this.overviewChartPresenter = overviewChartPresenter;
    this.openbis = openbis;
    this.overViewModule = overViewModule;
    this.connection = connection;
    columnFieldTypes = new ColumnFieldTypes();

    unfollowButton.setIcon(FontAwesome.MINUS_CIRCLE);
    unfollowButton.setStyleName(ValoTheme.BUTTON_DANGER);
    unfollowButton.setEnabled(false);

    detailsButton.setIcon(FontAwesome.INFO_CIRCLE);
    detailsButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
    detailsButton.setEnabled(false);
  }

  /**
   * Call and validate database connection of the business logic.
   */
  public void init() {
    if (contentModel == null) {
      return;
    }
    try {
      contentModel.init();
    } catch (SQLException exp) {
      return;
    }

    if (contentModel.getFollowingProjects().size() == 0) {
      this.overViewModule.noProjectMessage();
    } else {
      this.overViewModule.getOverviewGrid()
          .setContainerDataSource(this.contentModel.getTableContent());
      this.overViewModule.showGrid();
      overViewModule.getOverviewGrid().isChanged
          .addValueChangeListener(this::triggerViewPropertyChanged);

      overViewModule.getOverviewGrid().addItemClickListener(event -> {
        this.selectedProjectItem = event.getItem();
        this.selectedProject.setValue((String) event.getItem().getItemProperty(TableColumns.PROJECTOVERVIEWTABLE.get(ColumnTypes.PROJECTID)).getValue());
      });
    }

    selectedProject.addValueChangeListener((ValueChangeListener) event -> {
      if (selectedProject.getValue() != null) {
        unfollowButton.setEnabled(true);
        detailsButton.setEnabled(true);
      } else {
        unfollowButton.setEnabled(false);
        detailsButton.setEnabled(false);
      }
    });

    renderTable();
    overviewChartPresenter.getChart().addPointClickListener((PointClickListener) event -> {
      setFilter("projectTime", overviewChartPresenter.getChart().getDataSeriesObject(event));
    });

  }

  /**
   * Beautify the grid
   */
  private void renderTable() {
    overViewModule.getOverviewGrid().setSizeFull();
    overViewModule.columnList = overViewModule.getOverviewGrid().getColumns();
    overViewModule.getOverviewGrid().setResponsive(true);
    overViewModule.getOverviewGrid().removeAllColumns();
    overViewModule.getOverviewGrid().addColumn("projectID").setHeaderCaption("Project");
    overViewModule.getOverviewGrid().addColumn("projectTime").setHeaderCaption("Status");
    overViewModule.getOverviewGrid().addColumn("projectStatus").setHeaderCaption("Progress");
    overViewModule.getOverviewGrid().addColumn("investigatorName")
        .setHeaderCaption("Principal Investigator");
    overViewModule.getOverviewGrid().addColumn("species");
    overViewModule.getOverviewGrid().addColumn("samples");
    overViewModule.getOverviewGrid().addColumn("sampleTypes");
    overViewModule.getOverviewGrid().addColumn("projectRegisteredDate")
        .setHeaderCaption("Project Registered");
    overViewModule.getOverviewGrid().addColumn("rawDataRegistered")
        .setHeaderCaption("Raw Data Registered");
    overViewModule.getOverviewGrid().addColumn("dataAnalyzedDate")
        .setHeaderCaption("Data Analyzed");
    overViewModule.getOverviewGrid().addColumn("offerID").setHeaderCaption("Offer");
    overViewModule.getOverviewGrid().addColumn("invoice").setHeaderCaption("Invoice");

    overViewModule.getOverviewGrid().getColumn("projectID").setEditable(false);
    overViewModule.getOverviewGrid().getColumn("investigatorName").setEditable(false);
    overViewModule.getOverviewGrid().getColumn("species").setEditable(false);
    overViewModule.getOverviewGrid().getColumn("samples").setEditable(false);
    overViewModule.getOverviewGrid().getColumn("sampleTypes").setEditable(false);
    overViewModule.getOverviewGrid().getColumn("projectRegisteredDate").setEditable(false);
    overViewModule.getOverviewGrid().getColumn("rawDataRegistered").setEditable(false);
    overViewModule.getOverviewGrid().getColumn("dataAnalyzedDate").setEditable(false);
    overViewModule.getOverviewGrid().getColumn("projectTime").setEditable(false);
    overViewModule.getOverviewGrid().getColumn("offerID").setEditable(false);
    overViewModule.getOverviewGrid().getColumn("invoice").setEditable(true);

//    overViewModule.getOverviewGrid().setRowStyleGenerator(rowRef -> {// Java 8
//      if (rowRef.getItem().getItemProperty("projectTime").getValue().equals("projectTime")) {
//        return "overdue";
//      }
//      else if (rowRef.getItem().getItemProperty("projectTime").getValue().equals("in time")) {
//        return "intime";
//      }
//      else if (rowRef.getItem().getItemProperty("projectTime").getValue().equals("unregistered")) {
//        return "unregistered";
//      } else {
//        return null;
//      }
//    });

    overViewModule.getOverviewGrid().setCellStyleGenerator(cellRef -> {// Java 8
      if (cellRef.getPropertyId().equals("projectTime") && cellRef.getItem()
          .getItemProperty("projectTime").getValue().toString().equals("overdue")) {
        return "ov";
      } else if (cellRef.getPropertyId().equals("projectTime") && cellRef.getItem()
          .getItemProperty("projectTime").getValue().toString().equals("unregistered")) {
        return "un";
      } else if (cellRef.getPropertyId().equals("projectTime") && cellRef.getItem()
          .getItemProperty("projectTime").getValue().toString().equals("in time")) {
        return "in";
      } else if (cellRef.getPropertyId().equals("projectStatus") && cellRef.getItem()
          .getItemProperty("projectStatus").getValue().toString().equals("completed")) {
        return "in";
      } else if (cellRef.getPropertyId().equals("projectStatus") && cellRef.getItem()
          .getItemProperty("projectStatus").getValue().toString().equals("open")) {
        return "un";
      } else {
        return null;
      }
    });

    columnFieldTypes.clearFromParents();    // Clear from parent nodes (when reloading page)
    setFieldType("projectStatus", columnFieldTypes.getPROJECTSTATUS());

    final Column projectID = overViewModule.getOverviewGrid().
        getColumn(TableColumns.PROJECTOVERVIEWTABLE.get(ColumnTypes.PROJECTID));

    projectID.setRenderer(new HtmlRenderer(), new Converter<String, String>() {

      @Override
      public String convertToModel(String s, Class<? extends String> aClass, Locale locale)
          throws ConversionException {
        return "not implemented";
      }

      @Override
      public String convertToPresentation(String project, Class<? extends String> aClass,
          Locale locale) throws ConversionException {
        String space = openbis.getSpaceOfProject(project);
        return String.format(
            "<a href='%s/%s/%s' target='_blank' style='color:black; font-weight:bold'>%s</a>",
            portalURL, space, project, project);
      }

      @Override
      public Class<String> getModelType() {
        return String.class;
      }

      @Override
      public Class<String> getPresentationType() {
        return String.class;
      }
    });

    final GridCellFilter filter = new GridCellFilter(overViewModule.getOverviewGrid());
    configureFilter(filter);

    overViewModule.getOverviewGrid().getColumn("rawDataRegistered").
        setRenderer(new DateRenderer(new SimpleDateFormat("yyyy-MM-dd")));
    overViewModule.getOverviewGrid().getColumn("projectRegisteredDate").
        setRenderer(new DateRenderer(new SimpleDateFormat("yyyy-MM-dd")));
    overViewModule.getOverviewGrid().getColumn("dataAnalyzedDate").
        setRenderer(new DateRenderer(new SimpleDateFormat("yyyy-MM-dd")));

    for (Column column : overViewModule.getOverviewGrid().getColumns()) {
      if (column.getHeaderCaption().equals("Principal Investigator") ||
          column.getHeaderCaption().equals("Offer") ||
          column.getHeaderCaption().equals("Invoice")) {
        column.setWidth(230);
      } else if (column.getHeaderCaption().equals("Project") || column.getHeaderCaption()
          .equals("Samples")) {
        column.setWidth(110);
      } else {
        column.setWidth(180);
      }
    }
    overViewModule.getOverviewGrid().setFrozenColumnCount(2);
  }

  /**
   * Configures the filter header in the grid
   */
  private void configureFilter(GridCellFilter filter) {
    initExtraHeaderRow(overViewModule.getOverviewGrid(), filter);
    filter.setTextFilter("projectID", true, false);
    final List<String> projectTimeStatus = new ArrayList<>();
    projectTimeStatus.add("overdue");
    projectTimeStatus.add("in time");
    projectTimeStatus.add("unregistered");

    final List<String> projectStatus = new ArrayList<>();
    projectStatus.add("open");
    projectStatus.add("completed");
    filter.setComboBoxFilter("projectTime", projectTimeStatus);
    filter.setComboBoxFilter("projectStatus", projectStatus);
    filter.setDateFilter("rawDataRegistered", new SimpleDateFormat("yyyy-MM-dd"), true);
    filter.setDateFilter("projectRegisteredDate", new SimpleDateFormat("yyyy-MM-dd"), true);
    filter.setDateFilter("dataAnalyzedDate", new SimpleDateFormat("yyyy-MM-dd"), true);
    filter.setTextFilter("offerID", true, false);
    filter.setTextFilter("investigatorName", true, false);
    filter.setTextFilter("species", true, false);
    filter.setTextFilter("sampleTypes", true, false);
    filter.setNumberFilter("samples");
    filter.setTextFilter("invoice", true, false);

  }

  /**
   * Implement the filter row in the header of the grid
   *
   * @param grid The overview Grid reference
   * @param filter The GridCellFilter reference
   */
  private void initExtraHeaderRow(final Grid grid, final GridCellFilter filter) {
    Grid.HeaderRow firstHeaderRow = grid.prependHeaderRow();
    // "projectStatus removed (#25)
    firstHeaderRow
        .join("projectID", "projectTime", "projectStatus", "investigatorName", "species", "samples",
            "sampleTypes",
            "projectRegisteredDate",
            "rawDataRegistered", "dataAnalyzedDate", "offerID", "invoice");
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setSpacing(true);
    firstHeaderRow.getCell("projectID").setComponent(buttonLayout);
    Button clearAllFilters = new Button("", (Button.ClickListener) clickEvent ->
        filter.clearAllFilters());
    clearAllFilters.setDescription("Clear all filters.");
    clearAllFilters.setIcon(FontAwesome.TIMES);
    clearAllFilters.addStyleName(ValoTheme.BUTTON_QUIET);
    buttonLayout.addComponents(clearAllFilters, unfollowButton, detailsButton);
    buttonLayout.setComponentAlignment(unfollowButton, Alignment.MIDDLE_RIGHT);
    buttonLayout.setComponentAlignment(detailsButton, Alignment.MIDDLE_LEFT);
  }


  private void setFieldType(String columnID, Field fieldType) {
    try {
      overViewModule.getOverviewGrid().getColumn(columnID).setEditorField(fieldType);
    } catch (Exception exp) {
    }
  }

  public void setFilter(String column, String filter) {
    Container.Filter tmpFilter = new Like(column, filter);
    if (!contentModel.getTableContent().getContainerFilters().contains(tmpFilter)) {
      //contentModel.getTableContent().removeContainerFilters("projectStatus");
      contentModel.getTableContent().addContainerFilter(new Like(column, filter));
    } else {
      contentModel.getTableContent().removeContainerFilter(tmpFilter);
    }

  }

  public void sendError(String caption, String message) {
    overViewModule.sendError(caption, message);
  }

  public void sendInfo(String caption, String message) {
    overViewModule.sendInfo(caption, message);
  }

  private void triggerViewPropertyChanged(Property.ValueChangeEvent event) {
    this.contentModel.update();
    this.overviewModuleChanged.setValue(!overviewModuleChanged.getValue());

  }

  public ObjectProperty<Boolean> getIsChangedFlag() {
    return this.overviewModuleChanged;
  }

  public ObjectProperty<String> getSelectedProject() {
    return this.selectedProject;
  }

  public Item getSelectedProjectItem() {
    return this.selectedProjectItem;
  }

  /**
   * Refreshes the grid
   */
  public void refreshView() {
    try {
      // First, refresh the model (new SQL query!)
      this.contentModel.refresh();

      int timer = 0;

            /*
            If a content change happens, the editor is active.
            Since we are doing autocommit to the backend database,
            we have to wait until this is finished.
             */
      while (true) {
        try {
          this.overViewModule.getOverviewGrid().cancelEditor();
        } catch (Exception exp) {

        }
        if (timer == 5) {
          break;
        } else if (!this.overViewModule.getOverviewGrid().isEditorActive()) {
          break;
        }
        TimeUnit.MILLISECONDS.sleep(500);
        timer++;
      }

      // Second, update the grid
            /*
            The order of the next two lines is crucial!
            Do not change it, otherwise the grid will not
            be refreshed properly
             */
      this.overViewModule.init();
      if (contentModel.getFollowingProjects().size() == 0) {
        this.overViewModule.noProjectMessage();
      } else {
        this.overViewModule.getOverviewGrid()
            .setContainerDataSource(this.contentModel.getTableContent());
        this.overViewModule.showGrid();
      }

    } catch (Exception exc) {
    }
  }

  public boolean isProjectInFollowingTable(String projectCode) {

    String query = String
        .format("SELECT * FROM %s WHERE projectID=\'%s\'", overviewTable, projectCode);

    JDBCConnectionPool pool = connection.getConnectionPool();
    Connection conn = null;
    try {
      conn = pool.reserveConnection();
    } catch (SQLException exc) {
    }

    int size = 0;

    try {
      if (conn != null) {
        Statement statement = conn.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        try {
          resultSet.last();
          size = resultSet.getRow();
          resultSet.beforeFirst();
        } catch (Exception ex) {
          size = 0;
        }
        statement.close();
        conn.commit();
      }
    } catch (SQLException exc) {
    } finally {
      pool.releaseConnection(conn);
    }
    return size > 0;

  }

  public void createNewProjectEntry(String selectedProject) {

    String query = String
        .format("INSERT INTO %s (projectID) VALUES (\'%s\')", overviewTable, selectedProject);

    JDBCConnectionPool pool = connection.getConnectionPool();
    Connection conn = null;
    try {
      conn = pool.reserveConnection();
    } catch (SQLException exc) {
    }

    try {
      if (conn != null) {
        Statement statement = conn.createStatement();
        statement.executeUpdate(query);
        statement.close();
        conn.commit();
      }
    } catch (SQLException exc) {
    } finally {
      pool.releaseConnection(conn);
    }

  }

  public void clearSelection() {
    overViewModule.getOverviewGrid().getSelectionModel().reset();
    unfollowButton.setEnabled(false);
    detailsButton.setEnabled(false);
  }

  public Map<String, Integer> getTimeLineStats() {
    return this.contentModel.getProjectsTimeLineStats();
  }

  public Button getUnfollowButton() {
    return unfollowButton;
  }

  public Button getDetailsButton() {
    return detailsButton;
  }
}