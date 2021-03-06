package life.qbic.portal.portlet.module.singleTimelineModule;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.AxisType;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.PlotOptionsColumnrange;
import com.vaadin.addon.charts.model.Tooltip;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.charts.model.style.SolidColor;

public class SingleTimelineView extends Chart {

  private DataSeries unregisteredSeries, intimeSeries, overdueSeries, potentialtimeSeries;

  private Configuration conf;

  public SingleTimelineView() {
    super(ChartType.COLUMNRANGE);
    conf = this.getConfiguration();
    conf.setTitle("Timeline");
    conf.getChart().setInverted(true);

    XAxis xAxis = new XAxis();
    conf.addxAxis(xAxis);

    YAxis yAxis = new YAxis();
    yAxis.setType(AxisType.DATETIME);
    yAxis.setTitle("Time");
    conf.addyAxis(yAxis);
    this.setSizeFull();
    Tooltip tooltip = new Tooltip();
    tooltip.setFormatter(
        "this.dataseries.name +': '+ Highcharts.dateFormat('YYYY/mm/dd', this.point.low) + ' - ' + Highcharts.dateFormat('YYYY/mm/dd', this.point.high)");
    conf.setTooltip(tooltip);

    PlotOptionsColumnrange columnRange = new PlotOptionsColumnrange();
    columnRange.setGrouping(false);

    conf.setPlotOptions(columnRange);

    unregisteredSeries = new DataSeries();
    unregisteredSeries.setName("Unregistered");
    PlotOptionsColumnrange o = new PlotOptionsColumnrange();
    o.setColor(new SolidColor("#ff9a00"));
    unregisteredSeries.setPlotOptions(o);

    overdueSeries = new DataSeries();
    o = new PlotOptionsColumnrange();
    o.setColor(new SolidColor("#c20047"));
    overdueSeries.setPlotOptions(o);
    overdueSeries.setName("Overdue");

    intimeSeries = new DataSeries();
    o = new PlotOptionsColumnrange();
    o.setColor(new SolidColor("#26A65B"));
    intimeSeries.setPlotOptions(o);
    intimeSeries.setName("In time");

    potentialtimeSeries = new DataSeries();
    o = new PlotOptionsColumnrange();
    o.setColor(new SolidColor("#85929E"));
    potentialtimeSeries.setPlotOptions(o);
    potentialtimeSeries.setName("Time left");

    conf.getChart().setBackgroundColor(new SolidColor("#ffffff"));
    conf.addSeries(overdueSeries);
    conf.addSeries(potentialtimeSeries);
    conf.addSeries(intimeSeries);
    conf.addSeries(unregisteredSeries);

    conf.getLegend().setReversed(true);

    this.drawChart();
  }

  @Override
  public String getDescription() {
    return "Shows the progress of each project";
  }

  public DataSeries getUnregisteredSeries() {
    return unregisteredSeries;
  }

  public DataSeries getIntimeSeries() {
    return intimeSeries;
  }

  public DataSeries getOverdueSeries() {
    return overdueSeries;
  }

  public Configuration getConf() {
    return conf;
  }

  public DataSeries getPotentialtimeSeries() {
    return potentialtimeSeries;
  }
}