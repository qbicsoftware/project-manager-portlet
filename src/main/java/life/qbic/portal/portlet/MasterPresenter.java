package life.qbic.portal.portlet;

import com.vaadin.data.Property;
import life.qbic.portal.portlet.connection.database.projectInvestigatorDB.ProjectFilter;
import life.qbic.portal.portlet.module.overviewChartModule.OverviewChartPresenter;
import life.qbic.portal.portlet.module.projectFollowerModule.ProjectFollowerPresenter;
import life.qbic.portal.portlet.module.projectOverviewModule.ProjectOVPresenter;
import life.qbic.portal.portlet.module.projectSheetModule.ProjectSheetPresenter;
import life.qbic.portal.portlet.module.projectsStatsModule.ProjectsStatsPresenter;
import life.qbic.portal.portlet.module.timelineChartModule.TimelineChartPresenter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

  MasterPresenter(ProjectOVPresenter projectOverviewPresenter,
      ProjectSheetPresenter projectSheetPresenter,
      ProjectFollowerPresenter projectFollowerPresenter,
      ProjectFilter projectFilter,
      OverviewChartPresenter overviewChartPresenter,
      ProjectsStatsPresenter projectsStatsPresenter,
      TimelineChartPresenter timelineChartPresenter) {
    this.projectOverviewPresenter = projectOverviewPresenter;
    this.projectFollowerPresenter = projectFollowerPresenter;
    this.projectSheetPresenter = projectSheetPresenter;
    this.projectFilter = projectFilter;
    this.overviewChartPresenter = overviewChartPresenter;
    this.projectsStatsPresenter = projectsStatsPresenter;
    this.timelineChartPresenter = timelineChartPresenter;
    init();
  }

  private void init() {
    makeFilter();
    projectOverviewPresenter.init();

    projectOverviewPresenter.getSelectedProject().addValueChangeListener(event -> {
      projectSheetPresenter
          .showInfoForProject(projectOverviewPresenter.getSelectedProjectItem());
      if (projectOverviewPresenter.getSelectedProject().getValue() != null) {
        projectSheetPresenter.getProjectSheetView().createSubWindow();
      }
    });

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
      overviewChartPresenter.update();
      timelineChartPresenter.update();
      projectsStatsPresenter.update();
    }

  }

  private void refreshModuleViews(Property.ValueChangeEvent event) {
    makeFilter();
    projectOverviewPresenter.refreshView();
    overviewChartPresenter.update();
    timelineChartPresenter.update();
    projectsStatsPresenter.update();
    if (projectFollowerPresenter.getFollowingProjects().size() == 0) {
      projectSheetPresenter.getProjectSheetView().getProjectSheet().setVisible(false);
    } else {
      projectSheetPresenter.getProjectSheetView().getProjectSheet().setVisible(true);
    }
    LOG.info("Refreshed views.");
  }

  private void makeFilter() {
    projectFilter.createFilter("projectID", projectFollowerPresenter.getFollowingProjects());
  }
}
