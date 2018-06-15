package life.qbic.portal.portlet.module.projectsStatsModule;

import java.util.List;
import life.qbic.portal.portlet.ProjectManagerUI;
import life.qbic.portal.portlet.project.ProjectContentModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by spaethju on 12.04.17.
 */
public class ProjectsStatsPresenter {

  private ProjectContentModel model;
  private ProjectsStatsView view;
  private List<String> projects;
  private Integer overdueProjects, unregisteredProjects, intimeProjects;
  private static final Logger LOG = LogManager.getLogger(ProjectsStatsPresenter.class);

  public ProjectsStatsPresenter(ProjectContentModel model, ProjectsStatsView view) {
    this.model = model;
    this.view = view;
    update();
  }

  public void update() {
    projects = model.getFollowingProjects();
    if (projects.size() > 0) {
      LOG.info("Projects: " +projects.size());
      overdueProjects = model.getOverdueProjects();
      LOG.info("Overdue: " + overdueProjects);
      unregisteredProjects = model.getUnregisteredProjects();
      LOG.info("Unregistered: " +unregisteredProjects);
      intimeProjects = model.getInTimeProjects();
      LOG.info("In Time: " + intimeProjects);
    } else {
      LOG.info("No Projects followed." + projects.size());
      overdueProjects = 0;
      intimeProjects = 0;
      overdueProjects = 0;
      unregisteredProjects = 0;
    }

    view.setNumberOfTotalProjects(projects.size());
    view.setNumberOfOverdueProjects(overdueProjects);
    view.setNumberOfInTimeProjects(intimeProjects);
    view.setNumberOfUnregisteredProjects(unregisteredProjects);
  }

  public ProjectsStatsView getView() {
    return view;
  }
}
