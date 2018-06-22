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
      VerticalLayout projectDetailLayout = new VerticalLayout();
      projectDetailLayout.setMargin(new MarginInfo(true, true, false, true));
      HorizontalLayout bottomLayout = new HorizontalLayout();
      bottomLayout.setSpacing(true);
      bottomLayout.addComponents(getProjectTime(), getExportButton());
      projectDetailLayout
          .addComponents(getProject(), getDescription(), getProjectDetail(), bottomLayout);

      SingleTimelinePresenter st = new SingleTimelinePresenter(currentItem,
          new SingleTimelineView());
      projectSheetView.getProjectSheet().addComponent(projectDetailLayout);
      projectSheetView.getProjectSheet().addComponent(st.getChart());

      st.update();
    }

  }

  public Label getProject() {
    String project = currentItem.getItemProperty("projectID").getValue().toString();
    Label label = new Label(project);
    label.setStyleName(ValoTheme.LABEL_COLORED);
    label.addStyleName(ValoTheme.LABEL_H2);
    return label;
  }

  private Label getProjectTime() {
    String project = currentItem.getItemProperty("projectTime").getValue().toString();
    Label label = new Label(project);
    label.addStyleName(ValoTheme.LABEL_SMALL);
    if (label.getValue().equals("overdue")) {
      label.setStyleName("red");
    } else if (label.getValue().equals("unregistered")) {
      label.setStyleName("orange");
    } else if (label.getValue().equals("in time")) {
      label.setStyleName("green");
    }
    return label;
  }

  public Label getDescription() {
    String description = "";
    try {
      description = currentItem.getItemProperty("description").getValue().toString();
    } catch (NullPointerException e) {
      description = "No description available.";
    }
    Label label = new Label(description);
    return label;
  }

  private Label getProjectDetail() {
    String pi, species, samples, sampleTypes;
    try {
      pi = currentItem.getItemProperty("investigatorName").getValue().toString();
    } catch (NullPointerException ex) {
      pi = "Unknown";
    }

    try {
      species = currentItem.getItemProperty("species").getValue().toString();
    } catch (NullPointerException ex) {
      species = "Unknown";
    }

    try {
      samples = currentItem.getItemProperty("samples").getValue().toString();
    } catch (NullPointerException ex) {
      samples = "Unknown";
    }

    try {
      sampleTypes = currentItem.getItemProperty("sampleTypes").getValue().toString();
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
    String fileName = currentItem.getItemProperty("projectID").getValue().toString();
    String projectName =
        "Project," + currentItem.getItemProperty("projectID").getValue().toString();
    String projectStatus =
        "Status," + currentItem.getItemProperty("projectTime").getValue().toString();
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
      File projectFile = File.createTempFile(fileName, ".txt");
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
