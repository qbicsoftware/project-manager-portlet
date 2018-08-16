package life.qbic.portal.portlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.SQLException;
import life.qbic.portal.portlet.connection.ConnectionHandler;
import life.qbic.portal.portlet.connection.database.projectInvestigatorDB.ProjectFilter;
import life.qbic.portal.portlet.connection.database.projectInvestigatorDB.WrongArgumentSettingsException;
import life.qbic.portal.portlet.module.overviewChartModule.OverviewChartPresenter;
import life.qbic.portal.portlet.module.overviewChartModule.OverviewChartView;
import life.qbic.portal.portlet.module.projectFollowerModule.ProjectFollowerModel;
import life.qbic.portal.portlet.module.projectFollowerModule.ProjectFollowerPresenter;
import life.qbic.portal.portlet.module.projectFollowerModule.ProjectFollowerView;
import life.qbic.portal.portlet.module.projectFollowerModule.ProjectFollowerViewImpl;
import life.qbic.portal.portlet.module.projectOverviewModule.ProjectOVPresenter;
import life.qbic.portal.portlet.module.projectOverviewModule.ProjectOverviewModule;
import life.qbic.portal.portlet.module.projectSheetModule.ProjectSheetPresenter;
import life.qbic.portal.portlet.module.projectSheetModule.ProjectSheetView;
import life.qbic.portal.portlet.module.projectSheetModule.ProjectSheetViewImplementation;
import life.qbic.portal.portlet.module.projectsStatsModule.ProjectsStatsPresenter;
import life.qbic.portal.portlet.module.projectsStatsModule.ProjectsStatsView;
import life.qbic.portal.portlet.module.projectsStatsModule.ProjectsStatsViewImpl;
import life.qbic.portal.portlet.module.timelineChartModule.TimelineChartPresenter;
import life.qbic.portal.portlet.module.timelineChartModule.TimelineChartView;
import life.qbic.portal.portlet.project.ProjectContentModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vaadin.sliderpanel.SliderPanel;
import org.vaadin.sliderpanel.SliderPanelBuilder;
import org.vaadin.sliderpanel.client.SliderMode;
import org.vaadin.sliderpanel.client.SliderTabPosition;


/**
 * Entry point for portlet project-manager-portlet. This class derives from {@link QBiCPortletUI},
 * which is found in the {@code portal-utils-lib} library.
 */
@Theme("mytheme")
@SuppressWarnings("serial")
@Widgetset("life.qbic.portlet.AppWidgetSet")
public class ProjectManagerUI extends QBiCPortletUI {

  private static final Logger LOG = LogManager.getLogger(ProjectManagerUI.class);

  @Override
  protected Layout getPortletContent(final VaadinRequest request) {
    LOG.info("Project-Manager started.");

    // Init Filter
    ProjectFilter projectFilter = new ProjectFilter();

    ConnectionHandler connectionHandler = new ConnectionHandler(projectFilter);
    LOG.info("User " + connectionHandler.getUserID() + " logged in.");

    // Init Project Follower (Slider)
    LOG.info("Init Project Follower");
    ProjectFollowerModel followerModel = new ProjectFollowerModel(
        connectionHandler.getProjectDatabase());
    ProjectFollowerView followerView = new ProjectFollowerViewImpl()
        .setSpaceCaption("Project")
        .setProjectCaption("Sub-Project")
        .build();
    ProjectFollowerPresenter followerPresenter = new ProjectFollowerPresenter(followerView,
        followerModel, connectionHandler.getOpenBisConnection());
    followerPresenter.setUserID(connectionHandler.getUserID())
        .setSQLTableName("followingprojects").setPrimaryKey("id");
    try {
      followerPresenter.startOrchestration();
    } catch (Exception e) {
      Notification notification = new Notification("Connection could not be established.");
      notification.show(Page.getCurrent());
      e.printStackTrace();
    }

    // Init Project Content (Database)
    LOG.info("Init Project Content");
    ProjectContentModel model = new ProjectContentModel(connectionHandler.getProjectDatabase(),
        connectionHandler.getUserManagementDB(),
        followerModel.getAllFollowingProjects(), connectionHandler.getOpenBisConnection());

    // Init Project Overview (Table)
    LOG.info("Init Project Overview Table");
    ProjectOverviewModule projectOverviewModule = new ProjectOverviewModule();
    OverviewChartView overviewChartView = new OverviewChartView();
    OverviewChartPresenter overviewChartPresenter = new OverviewChartPresenter(model,
        overviewChartView);
    ProjectOVPresenter projectOVPresenter = new ProjectOVPresenter(model,
        projectOverviewModule, overviewChartPresenter, connectionHandler.getOpenBisConnection(),
        connectionHandler.getProjectDatabase());

    // Init Project Sheet (Project Information)
    LOG.info("Init Project Information");
    ProjectSheetView projectSheetView = new ProjectSheetViewImplementation();
    ProjectSheetPresenter projectSheetPresenter = new ProjectSheetPresenter(projectSheetView);
    ProjectsStatsView projectsStatsView = new ProjectsStatsViewImpl();

    // Init Time Line Chart
    LOG.info("Init Time Line Chart");
    TimelineChartView timelineChartView = new TimelineChartView();
    TimelineChartPresenter timelineChartPresenter = new TimelineChartPresenter(model,
        timelineChartView);

    //Init project stats
    LOG.info("Init Project Statistics");
    ProjectsStatsPresenter projectsStatsPresenter = new ProjectsStatsPresenter(model,
        projectsStatsView);

    // Init unfollow button
    LOG.info("Init Buttons");
    projectOVPresenter.getUnfollowButton().addClickListener(event -> {
      try {
        String id = projectOVPresenter.getSelectedProject().getValue();
        followerModel
            .unfollowProject("followingprojects", id, connectionHandler.getUserID(), "id");
        followerPresenter.refreshProjects();
        followerPresenter.switchIsChangedFlag();
        projectOverviewModule.getOverviewGrid().deselectAll();
        projectSheetView.reset();
      } catch (SQLException | WrongArgumentSettingsException | NullPointerException e) {
        LOG.error("Could not unfollow project with ID:" + projectOVPresenter.getSelectedProject()
            .getValue());
        e.printStackTrace();
      }
    });

    // Init details button
    projectOVPresenter.getDetailsButton().addClickListener(event -> {
      String id = projectOVPresenter.getSelectedProject().getValue();
      projectSheetPresenter
          .showInfoForProject(projectOVPresenter.getSelectedProjectItem());
      if (projectOVPresenter.getSelectedProject().getValue() != null) {
        projectSheetPresenter.getProjectSheetView().createSubWindow();
      }

    });

    // Init slider
    LOG.info("Init Slider");
    SliderPanel sliderPanel = new SliderPanelBuilder(followerView.getUI())
        .caption("FOLLOW PROJECTS")
        .mode(SliderMode.TOP)
        .tabPosition(SliderTabPosition.MIDDLE)
        .style("slider-format")
        .animationDuration(100).zIndex(1).build();
    VerticalLayout sliderFrame = new VerticalLayout();
    sliderFrame.addComponent(sliderPanel);
    sliderFrame.setComponentAlignment(sliderPanel, Alignment.MIDDLE_CENTER);
    sliderFrame.setSizeFull();

    // Init combined Layouts
    LOG.info("Create final Layout");
    VerticalLayout mainContent = new VerticalLayout();
    VerticalLayout mainFrame = new VerticalLayout();
    HorizontalLayout statisticsLayout = new HorizontalLayout();
    VerticalLayout statsLayout = new VerticalLayout();
    statsLayout.addComponents(overviewChartView, projectsStatsView.getStatsLayout());
    statsLayout.setSizeFull();
    statsLayout.setComponentAlignment(projectsStatsView.getStatsLayout(), Alignment.MIDDLE_CENTER);
    statisticsLayout.addComponent(timelineChartView);
    statisticsLayout.addComponent(statsLayout);
    statisticsLayout.setStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
    statisticsLayout.setSizeFull();
    statisticsLayout.setMargin(new MarginInfo(false, true, false, true));
    statisticsLayout.setSpacing(false);

    projectsStatsPresenter.update();

    mainContent.addComponent(statisticsLayout);
    mainContent.addComponent(projectOverviewModule);
    mainContent.setSpacing(true);
    mainFrame.setSpacing(true);
    mainFrame.addComponent(sliderFrame);
    mainFrame.setComponentAlignment(sliderFrame, Alignment.MIDDLE_CENTER);
    mainFrame.addComponent(mainContent);
    mainFrame.setExpandRatio(mainContent, 1);
    mainFrame.setStyleName("mainpage");

    // Init Master Presenter
    final MasterPresenter masterPresenter = new MasterPresenter(projectOVPresenter,
        projectSheetPresenter, followerPresenter, projectFilter, overviewChartPresenter,
        projectsStatsPresenter, timelineChartPresenter, model);

    LOG.info("Project Manager initialized.");

    return mainFrame;
  }


}