package life.qbic.portal.portlet.module.projectSheetModule;

import com.vaadin.ui.VerticalLayout;

/**
 * Created by sven1103 on 9/01/17.
 */
public interface ProjectSheetView {

  VerticalLayout getProjectSheet();

  void reset();

  void createSubWindow();

}
