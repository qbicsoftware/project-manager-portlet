package life.qbic.portal.portlet;

import com.vaadin.data.Property;
import life.qbic.portal.portlet.connection.database.projectInvestigatorDB.ProjectFilter;
import life.qbic.portal.portlet.module.overviewChartModule.OverviewChartPresenter;
import life.qbic.portal.portlet.module.projectFollowerModule.ProjectFollowerPresenter;
import life.qbic.portal.portlet.module.projectOverviewModule.ProjectOVPresenter;
import life.qbic.portal.portlet.module.projectSheetModule.ProjectSheetPresenter;
import life.qbic.portal.portlet.module.projectsStatsModule.ProjectsStatsPresenter;
import life.qbic.portal.portlet.module.timelineChartModule.TimelineChartPresenter;
import life.qbic.portal.portlet.project.ProjectContentModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is a master presenter class and helps to communicate between the different modules
 */
public class MasterPresenter {


  private static final Logger LOG = LogManager.getLogger(MasterPresenter.class);
  private final ProjectOVPresenter projectOverviewPresenter;
  private final ProjectSheetPresenter projectSheetPresenter;
  private final ProjectFollowerPresenter projectFollowerPresenter;

  private final ProjectFilter projectFilter;
  private final OverviewChartPresenter overviewChartPresenter;
  private final ProjectsStatsPresenter projectsStatsPresenter;
  private final TimelineChartPresenter timelineChartPresenter;
  private final ProjectContentModel contentModel;

  MasterPresenter(ProjectOVPresenter projectOverviewPresenter,
      ProjectSheetPresenter projectSheetPresenter,
      ProjectFollowerPresenter projectFollowerPresenter,
      ProjectFilter projectFilter,
      OverviewChartPresenter overviewChartPresenter,
      ProjectsStatsPresenter projectsStatsPresenter,
      TimelineChartPresenter timelineChartPresenter, ProjectContentModel contentModel) {
    this.projectOverviewPresenter = projectOverviewPresenter;
    this.projectFollowerPresenter = projectFollowerPresenter;
    this.projectSheetPresenter = projectSheetPresenter;
    this.projectFilter = projectFilter;
    this.overviewChartPresenter = overviewChartPresenter;
    this.projectsStatsPresenter = projectsStatsPresenter;
    this.timelineChartPresenter = timelineChartPresenter;
    this.contentModel = contentModel;
    init();
  }

  private void init() {
    makeFilter();
    projectOverviewPresenter.init();

    projectOverviewPresenter.getIsChangedFlag().addValueChangeListener(this::refreshModuleViews);

    projectSheetPresenter.getInformationCommittedFlag()
        .addValueChangeListener(this::refreshModuleViews);
    projectFilter.createFilter("projectID", projectFollowerPresenter.getFollowingProjects());
    projectFollowerPresenter.getIsChangedFlag().addValueChangeListener(event -> {
      final String selectedProject = projectFollowerPresenter.getCurrentProject();
      boolean doesDBEntryExist = projectOverviewPresenter
          .isProjectInFollowingTable(selectedProject);
      if (!doesDBEntryExist) {
        projectOverviewPresenter.createNewProjectEntry(selectedProject);
      }
      refreshModuleViews(event);
    });

    if (projectFollowerPresenter.getFollowingProjects().size() > 0) {
      makeFilter();
      projectOverviewPresenter.setExportButton(contentModel.exportProjects());
      overviewChartPresenter.update();
      timelineChartPresenter.update();
      projectsStatsPresenter.update();
      overviewChartPresenter.getChart().setVisible(true);
      timelineChartPresenter.getChart().setVisible(true);
      try {
        projectOverviewPresenter.setExportButton(contentModel.exportProjects());
        projectOverviewPresenter.refreshView();
      } catch (Exception e) {
        LOG.error("No summary possible.");
      }
    } else {
      overviewChartPresenter.getChart().setVisible(false);
      timelineChartPresenter.getChart().setVisible(false);
    }

  }

  private void refreshModuleViews(Property.ValueChangeEvent event) {
    makeFilter();
    projectOverviewPresenter.refreshView();
    projectsStatsPresenter.update();
    if (contentModel.getFollowingProjects().size() > 0) {
      overviewChartPresenter.update();
      overviewChartPresenter.getChart().setVisible(true);
      timelineChartPresenter.update();
      timelineChartPresenter.getChart().setVisible(true);
    } else {
      overviewChartPresenter.getChart().setVisible(false);
      timelineChartPresenter.getChart().setVisible(false);
    }

    try {
      projectOverviewPresenter.setExportButton(contentModel.exportProjects());
    } catch (Exception e) {
      LOG.error("No summary possible.");
    }
    LOG.info("Refreshed views.");
  }

  private void makeFilter() {
    projectFilter.createFilter("projectID", projectFollowerPresenter.getFollowingProjects());
  }
}
