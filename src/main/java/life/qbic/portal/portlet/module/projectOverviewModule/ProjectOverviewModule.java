package life.qbic.portal.portlet.module.projectOverviewModule;

import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import java.util.List;

/**
 * Created by sven on 11/13/16. This class represents the core of the projectmanager module. It will
 * display the database content of different projects and its progress status.
 */
public class ProjectOverviewModule extends VerticalLayout implements ProjectOverviewI {


  private final Notification info;
  private final Notification error;
  private final ProjectOverviewGrid overviewGrid;
  List<Column> columnList;
  private VerticalLayout gridLayout;

  public ProjectOverviewModule() {
    this.overviewGrid = new ProjectOverviewGrid();
    this.info = new Notification("", "", Notification.Type.TRAY_NOTIFICATION);
    this.error = new Notification("", "", Notification.Type.ERROR_MESSAGE);
    gridLayout = new VerticalLayout();
    gridLayout.setSizeFull();
    this.addComponent(gridLayout);
    this.setSizeFull();
    init();
  }

  /**
   * Make some init settings
   */
  public void init() {
    this.setSpacing(true);
    info.setDelayMsec(1000);
    info.setPosition(Position.TOP_CENTER);
    overviewGrid.setWidth(100, Unit.PERCENTAGE);
    overviewGrid.setHeight(100, Unit.PERCENTAGE);
    this.setWidth(100, Unit.PERCENTAGE);
    this.addStyleName("overview-module-style");
  }

  /**
   * Sends an info notification message to the user on the screen.
   *
   * @param caption The caption
   * @param message Your message
   */
  void sendInfo(String caption, String message) {
    info.setCaption(caption);
    info.setDescription(message);
    info.show(Page.getCurrent());
  }

  /**
   * Sends an error notification message to the user on the screen.
   *
   * @param caption The caption
   * @param message Your message
   */
  void sendError(String caption, String message) {
    error.setCaption(caption);
    error.setDescription(message);
    error.show(Page.getCurrent());
  }


  @Override
  public ProjectOverviewGrid getOverviewGrid() {
    return this.overviewGrid;
  }

  @Override
  public List<Column> getColumnList() {
    return this.columnList;
  }

  public void showGrid() {
    gridLayout.removeAllComponents();
    gridLayout.addComponent(overviewGrid);
    this.setMargin(false);
  }

  public void noProjectMessage() {
    gridLayout.removeAllComponents();
    gridLayout.addComponent(new Label("You are currently not following any projects"));
    this.setMargin(true);
  }

}
