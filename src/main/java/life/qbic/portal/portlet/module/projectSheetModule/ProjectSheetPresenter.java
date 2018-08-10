package life.qbic.portal.portlet.module.projectSheetModule;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import life.qbic.portal.portlet.NumberIndicator;
import life.qbic.portal.portlet.module.singleTimelineModule.SingleTimelinePresenter;
import life.qbic.portal.portlet.module.singleTimelineModule.SingleTimelineView;

/**
 * Created by sven1103 on 10/01/17.
 */
public class ProjectSheetPresenter {

  private final ProjectSheetView projectSheetView;
  private final ObjectProperty<Boolean> informationCommittedFlag;
  private Item currentItem;

  public ProjectSheetPresenter(ProjectSheetView projectSheetView) {
    this.projectSheetView = projectSheetView;
    this.informationCommittedFlag = new ObjectProperty<>(false);
  }

  public void showInfoForProject(Item project) {

    if (project == null) {
      projectSheetView.reset();
    } else {
      projectSheetView.reset();
      currentItem = project;
      projectSheetView.getProjectSheet()
          .setCaption("Project Details");
      HorizontalLayout sheetLayout = new HorizontalLayout();
      HorizontalLayout statusLayout = new HorizontalLayout();
      statusLayout.setMargin(true);
      statusLayout.setSpacing(true);
      VerticalLayout projectDetailLayout = new VerticalLayout();
      projectDetailLayout.setMargin(new MarginInfo(true, true, false, true));
      HorizontalLayout bottomLayout = new HorizontalLayout();
      bottomLayout.setSpacing(true);
      SingleTimelinePresenter st = new SingleTimelinePresenter(currentItem,
          new SingleTimelineView());
      bottomLayout.addComponents(getExportButton());
      projectDetailLayout
          .addComponents(getProject(), getDescription(), getProjectDetail(), bottomLayout);
      statusLayout.addComponent(getProjectTime(st));
      statusLayout.addStyleName(ValoTheme.LAYOUT_CARD);
      sheetLayout.addComponents(projectDetailLayout, statusLayout);
      sheetLayout.setSizeFull();
      projectSheetView.getProjectSheet().addComponent(sheetLayout);
      projectSheetView.getProjectSheet().addComponent(st.getChart());

      st.update();
    }

  }

  public Label getProject() {
    String project = (String) currentItem.getItemProperty("projectID").getValue();
    Label label = new Label(project);
    label.setStyleName(ValoTheme.LABEL_COLORED);
    label.addStyleName(ValoTheme.LABEL_H2);
    return label;
  }

  private NumberIndicator getProjectTime(SingleTimelinePresenter st) {
    String projectTime = (String) currentItem.getItemProperty("projectTime").getValue();
    NumberIndicator statusIndicator = new NumberIndicator();
    if (projectTime.equals("overdue")) {
      long daysOverdue;
      long current = TimeUnit.DAYS
          .convert(st.getCurrentDate().getTime() - st.getOverdueDate().getTime(),
              TimeUnit.MILLISECONDS);
      try {
      long analyzed = TimeUnit.DAYS
          .convert(st.getDataAnalyzedDate().getTime() - st.getOverdueDate().getTime(),
              TimeUnit.MILLISECONDS);
      if (current < analyzed) {
        daysOverdue = current;
      } else {
        daysOverdue = analyzed;
      } } catch (NullPointerException e) {
        daysOverdue = current;
      }

      statusIndicator.setNumber(Long.toString(daysOverdue) + " days");
      statusIndicator.setHeader("Overdue");
      statusIndicator.setStyleName("overdue", "overdue");
    } else if (projectTime.equals("unregistered")) {
      long daysUnregistered = TimeUnit.DAYS
          .convert(st.getCurrentDate().getTime() - st.getProjectRegisteredDate().getTime(),
              TimeUnit.MILLISECONDS);
      statusIndicator.setNumber(Long.toString(daysUnregistered) + " days");
      statusIndicator.setHeader("Unregistered");
      statusIndicator.setStyleName("unregistered", "unregistered");
    } else if (projectTime.equals("in time")) {
      long daysIntime = TimeUnit.DAYS
          .convert(st.getCurrentDate().getTime() - st.getRawDataRegisteredDate().getTime(),
              TimeUnit.MILLISECONDS);
      statusIndicator.setNumber(Long.toString(daysIntime) + " days left");
      statusIndicator.setHeader("In Time");
      statusIndicator.setStyleName("intime", "intime");
    }

    return statusIndicator;
  }

  public Label getDescription() {
    String description = "";
    try {
      description = (String) currentItem.getItemProperty("description").getValue();
    } catch (NullPointerException e) {
      description = "No description available.";
    }
    Label label = new Label(description);
    return label;
  }

  private Label getProjectDetail() {
    String pi, species, sampleTypes;
    int samples;
    try {
      pi = (String) currentItem.getItemProperty("investigatorName").getValue();
    } catch (NullPointerException ex) {
      pi = "Unknown";
    }

    try {
      species = (String) currentItem.getItemProperty("species").getValue();
    } catch (NullPointerException ex) {
      species = "Unknown";
    }

    try {
      samples = (int) currentItem.getItemProperty("samples").getValue();
    } catch (NullPointerException ex) {
      samples = -1;
    }

    try {
      sampleTypes = (String) currentItem.getItemProperty("sampleTypes").getValue();
    } catch (NullPointerException ex) {
      sampleTypes = "Unknown";
    }

    Label label = new Label(
        "<ul>" +
            "  <li><b><font color=\"#007ae4\">PI: </b></font>" + pi + "</li>" +
            "  <li><b><font color=\"#007ae4\">Species: </b></font>" + species + "</li>" +
            "  <li><b><font color=\"#007ae4\">Samples: </b></font>" + samples + "</li>" +
            "  <li><b><font color=\"#007ae4\">Types: </b></font>" + sampleTypes + "</li>" +
            "</ul> ",
        ContentMode.HTML);
    return label;
  }

  public ObjectProperty<Boolean> getInformationCommittedFlag() {
    return this.informationCommittedFlag;
  }

  public ProjectSheetView getProjectSheetView() {
    return projectSheetView;
  }

  private Button getExportButton() {
    String fileName = (String) currentItem.getItemProperty("projectID").getValue();
    String projectName =
        "Project," + currentItem.getItemProperty("projectID").getValue();
    String projectStatus =
        "Status," + currentItem.getItemProperty("projectTime").getValue();
    String projectDescription = "Description," + currentItem.getItemProperty("description");
    String projectPI = "PI," + currentItem.getItemProperty("investigatorName");
    String projectSpecies = "Species," + currentItem.getItemProperty("species");
    String projectSamples = "Samples," + currentItem.getItemProperty("samples");
    String projectSampleTypes = "Sample Types," + currentItem.getItemProperty("sampleTypes");
    String projectRegisteredDate =
        "Project Registered," + currentItem.getItemProperty("projectRegisteredDate");
    String rawDataRegisteredDate =
        "Raw Data Registered," + currentItem.getItemProperty("rawDataRegistered");
    String dataAnalyzedDate = "Data Analyzed," + currentItem.getItemProperty("dataAnalyzedDate");

    try {
      File projectFile = File.createTempFile(fileName, ".csv");
      FileWriter fw = new FileWriter(projectFile);
      BufferedWriter bw = new BufferedWriter(fw);
      bw.write(projectName);
      bw.newLine();
      bw.write(projectStatus);
      bw.newLine();
      bw.write(projectDescription);
      bw.newLine();
      bw.write(projectPI);
      bw.newLine();
      bw.write(projectSpecies);
      bw.newLine();
      bw.write(projectSamples);
      bw.newLine();
      bw.write(projectSampleTypes);
      bw.newLine();
      bw.write(projectRegisteredDate);
      bw.newLine();
      bw.write(rawDataRegisteredDate);
      bw.newLine();
      bw.write(dataAnalyzedDate);
      bw.close();
      fw.close();
      FileResource res = new FileResource(projectFile);
      FileDownloader fd = new FileDownloader(res);
      Button downloadButton = new Button("Summary");
      downloadButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
      fd.extend(downloadButton);
      return downloadButton;
    } catch (IOException e) {
      return null;
    }
  }
}
