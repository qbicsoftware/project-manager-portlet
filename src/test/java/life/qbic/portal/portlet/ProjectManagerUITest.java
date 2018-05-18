package life.qbic.portal.portlet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ProjectManagerUITest {

  @Test
  public void mainUIExtendsQBiCPortletUI() {
    assertTrue("The main UI class must extend life.qbic.portlet.QBiCPortletUI",
        QBiCPortletUI.class.isAssignableFrom(ProjectManagerUI.class));
  }

  @Test
  public void mainUIIsNotQBiCPortletUI() {
    assertFalse("The main UI class must be different to life.qbic.portlet.QBiCPortletUI",
        QBiCPortletUI.class.equals(ProjectManagerUI.class));
  }
}