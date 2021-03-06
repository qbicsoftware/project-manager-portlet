package life.qbic.portal.portlet.connection.openbis;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermSearchCriteria;
import com.vaadin.data.util.BeanItemContainer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import life.qbic.portal.portlet.project.ProjectBean;
import life.qbic.portal.portlet.project.ProjectToProjectBeanConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Created by sven1103 on 8/12/16.
 */
public class OpenBisConnection {

  private static final Logger LOG = LogManager.getLogger(OpenBisConnection.class);

  private BeanItemContainer<ProjectBean> projectBeanBeanItemContainer = new BeanItemContainer<ProjectBean>(
      ProjectBean.class);
  private String sessionToken;
  private IApplicationServerApi app;

  public OpenBisConnection(IApplicationServerApi app, String sessionToken) {
    LOG.info("Obtained new connection to openBIS using {} as a session token", sessionToken);
    this.app = app;
    this.sessionToken = sessionToken;
  }

  public String getSpaceOfProject(String projectCode) {
    ProjectSearchCriteria projectSearchCriteria = new ProjectSearchCriteria();
    projectSearchCriteria.withCode().thatEquals(projectCode);
    ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
    projectFetchOptions.withSpace();
    SearchResult<Project> projects = app
        .searchProjects(sessionToken, projectSearchCriteria, projectFetchOptions);
    if (projects.getObjects().get(0) == null) {
      return null;
    }
    return projects.getObjects().get(0).getSpace().getCode();
  }


  public Date getProjectRegistrationDate(Project project) {
    return project.getRegistrationDate();
  }

  public List<Sample> getSamplesOfProject(Project project) {
    SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
    sampleSearchCriteria.withExperiment().withProject().withCode().thatEquals(project.getCode());
    sampleSearchCriteria.withType().withCode().thatEquals("Q_TEST_SAMPLE");
    SampleFetchOptions fetchOptions = new SampleFetchOptions();
    fetchOptions.withType();
    fetchOptions.withProperties();
    fetchOptions.withChildrenUsing(fetchOptions);
    SearchResult<Sample> samples = app
        .searchSamples(sessionToken, sampleSearchCriteria, fetchOptions);

    List<Sample> measuredSamples = new ArrayList<>();
    for (Sample sample : samples.getObjects()){
      for (Sample child : sample.getChildren()) {
        if (!child.getType().equals("Q_TEST_SAMPLE")){
          measuredSamples.add(child);
        }
      }
    }
    return measuredSamples;
  }

  public String getSpeciesOfProject(Project project) {
    String species = "unknown";
    SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
    sampleSearchCriteria.withExperiment().withProject().withCode().thatEquals(project.getCode());
    sampleSearchCriteria.withType().withCode().thatEquals("Q_BIOLOGICAL_ENTITY");
    SampleFetchOptions fetchOptions = new SampleFetchOptions();
    fetchOptions.withProperties();
    SearchResult<Sample> samples = app
        .searchSamples(sessionToken, sampleSearchCriteria, fetchOptions);

    if (samples.getTotalCount() > 0) {
      species = samples.getObjects().get(0).getProperties().get("Q_NCBI_ORGANISM");
      species = getTaxonomy(species);
    }

    if (species == null || species.equals("")) {
      species = "unknown";
    }
    return species;

  }

  public Set<String> getSampleTypesOfProject(Project project) {
    Set<String> sampleTypes = new HashSet<>();
    SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
    sampleSearchCriteria.withExperiment().withProject().withCode().thatEquals(project.getCode());
    sampleSearchCriteria.withType().withCode().thatEquals("Q_TEST_SAMPLE");
    SampleFetchOptions fetchOptions = new SampleFetchOptions();
    fetchOptions.withProperties();
    SearchResult<Sample> samples = app
        .searchSamples(sessionToken, sampleSearchCriteria, fetchOptions);

    for (Sample sample : samples.getObjects()) {
      sampleTypes.add(sample.getProperty("Q_SAMPLE_TYPE"));
    }

    return sampleTypes;
  }


  public Date getFirstRegisteredDate(Project project) {
    SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
    sampleSearchCriteria.withExperiment().withProject().withCode().thatEquals(project.getCode());
    SampleFetchOptions fetchOptions = new SampleFetchOptions();
    fetchOptions.withDataSets();
    SearchResult<Sample> samples = app
        .searchSamples(sessionToken, sampleSearchCriteria, fetchOptions);
    ArrayList<Date> datesRegistered = new ArrayList<>();
    for (int i = 0; i < samples.getObjects().size(); i++) {
      Sample rawDataSample = samples.getObjects().get(i);
      for (DataSet dataSet : rawDataSample.getDataSets()) {
        datesRegistered.add(dataSet.getRegistrationDate());
      }
    }

    Date firstRegistered = null;
    if (!datesRegistered.isEmpty()) {
      firstRegistered = Collections.min(datesRegistered);
    }

    return firstRegistered;
  }

  public Date getFirstAnalyzedDate(Project project) {
    SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
    sampleSearchCriteria.withExperiment().withProject().withCode().thatEquals(project.getCode());
    SampleFetchOptions fetchOptions = new SampleFetchOptions();
    fetchOptions.withDataSets().withType();
    fetchOptions.withDataSets().withProperties();
    SearchResult<Sample> samples = app
        .searchSamples(sessionToken, sampleSearchCriteria, fetchOptions);

    ArrayList<Date> datesAnalyzed = new ArrayList<>();
    for (int i = 0; i < samples.getObjects().size(); i++) {
      Sample rawDataSample = samples.getObjects().get(i);
      for (DataSet dataSet : rawDataSample.getDataSets()) {
        if (rawDataSample.getCode().startsWith("Q") && rawDataSample.getCode()
            .endsWith("000") && dataSet.getType().getCode().equals("Q_PROJECT_DATA") && dataSet.getProperties().get("Q_ATTACHMENT_TYPE").equals("RESULT")) {
          datesAnalyzed.add(dataSet.getRegistrationDate());
        }
      }
    }

    Date firstAnalyzed = null;
    if (!datesAnalyzed.isEmpty()) {
      firstAnalyzed = Collections.min(datesAnalyzed);
    }

    return firstAnalyzed;
  }

  public BeanItemContainer<ProjectBean> getListOfProjects() {
    if (projectBeanBeanItemContainer.size() > 0) {
      projectBeanBeanItemContainer.removeAllItems();
    }
    ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
    projectFetchOptions.withSpace();
    SearchResult<Project> projects = app
        .searchProjects(sessionToken, new ProjectSearchCriteria(), projectFetchOptions);
    for (Project project : projects.getObjects()) {
      projectBeanBeanItemContainer.addBean(
          ProjectToProjectBeanConverter.convertToProjectBean(project));
    }

    return projectBeanBeanItemContainer;
  }

  public String getProjectDescription(String projectCode) {
    ProjectSearchCriteria projectSearchCriteria = new ProjectSearchCriteria();
    projectSearchCriteria.withCode().thatEquals(projectCode);
    ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();

    SearchResult<Project> projects = app
        .searchProjects(sessionToken, projectSearchCriteria, projectFetchOptions);

    return projects.getObjects().get(0).getDescription();
  }

  public String getTaxonomy(String ncbi_code) {
    String taxonomy = "unknown";
    VocabularyTermSearchCriteria vocabularyTermSearchCriteria = new VocabularyTermSearchCriteria();
    vocabularyTermSearchCriteria.withCode().thatEquals(ncbi_code);
    SearchResult<VocabularyTerm> vocabularyTermSearchResult = app
        .searchVocabularyTerms(sessionToken, vocabularyTermSearchCriteria,
            new VocabularyTermFetchOptions());
    for (VocabularyTerm vocabularyTerm : vocabularyTermSearchResult.getObjects()) {
      if (vocabularyTerm.getCode().equals(ncbi_code)) {
        taxonomy = vocabularyTerm.getLabel();
      }
    }
    return taxonomy;
  }

  public Project getProjectByCode(String projectCode) {
    ProjectSearchCriteria projectSearchCriteria = new ProjectSearchCriteria();
    projectSearchCriteria.withCode().thatEquals(projectCode);
    ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
    SearchResult<Project> projects = app
        .searchProjects(sessionToken, projectSearchCriteria, projectFetchOptions);
    if (projects.getObjects().get(0) == null) {
      return null;
    }
    return projects.getObjects().get(0);
  }

  public IApplicationServerApi getApp() {
    return app;
  }

}
