package life.qbic.portal.portlet.module.overviewChartModule;


import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.PointClickEvent;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.Cursor;
import com.vaadin.addon.charts.model.DataLabels;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.PlotOptionsPie;
import com.vaadin.addon.charts.model.style.SolidColor;

public class OverviewChartView extends Chart {

  private DataSeries series;

  private Configuration conf;

  private PlotOptionsPie plotOptions;

  public OverviewChartView() {
    super(ChartType.PIE);
    conf = this.getConfiguration();
    series = new DataSeries();

    DataLabels dataLabels = new DataLabels();
    dataLabels.setEnabled(true);
    dataLabels
        .setFormatter("'<b>'+ this.point.name +'</b>: '+ this.percentage.toFixed(0) +' %'");
    plotOptions = new PlotOptionsPie();
    plotOptions.setDataLabels(dataLabels);
    plotOptions.setShowInLegend(true);
    // unregistered - in time - overdue
    this.setHeight("300px");
    plotOptions
        .setColors(new SolidColor("#ff9a00"), new SolidColor("#26A65B"), new SolidColor("#c20047"));
    plotOptions.setSize("110px");
    plotOptions.setCursor(Cursor.POINTER);
    plotOptions.setAllowPointSelect(true);
    conf.setPlotOptions(plotOptions);
    conf.setTitle("Status");
    conf.getChart().setBackgroundColor(new SolidColor("#fafafa"));

    series.setName("projects");
    conf.setSeries(series);
    this.setImmediate(true);
    this.drawChart(conf);
  }

  @Override
  public String getDescription() {
    return "Shows the project status";
  }

  public DataSeries getSeries() {
    return series;
  }

  /**
   * @param event point click event
   * @return name of the current data series object
   */
  public String getDataSeriesObject(PointClickEvent event) {
    return this.series.get(event.getPointIndex()).getName();
  }

  /**
   * sets back all sliced items of the chart
   */
  public void unselect() {
    for (int i = 0; i < series.size(); i++) {
      series.setItemSliced(i, false, false, true);
    }
  }
}
