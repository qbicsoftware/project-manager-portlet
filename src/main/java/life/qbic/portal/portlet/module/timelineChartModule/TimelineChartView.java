package life.qbic.portal.portlet.module.timelineChartModule;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.AxisType;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.Cursor;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.PlotOptionsColumnrange;
import com.vaadin.addon.charts.model.Tooltip;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.addon.charts.model.style.Style;
import java.util.Calendar;
import java.util.Date;

public class TimelineChartView extends Chart {

  private DataSeries unregisteredSeries, intimeSeries, overdueSeries, timeLeftSeries;

  private Configuration conf;

  public TimelineChartView() {
    super(ChartType.COLUMNRANGE);
    conf = this.getConfiguration();
    conf.setTitle("Timeline");
    conf.getChart().setInverted(true);

    XAxis xAxis = new XAxis();
    xAxis.setOpposite(true);
    conf.addxAxis(xAxis);

    YAxis yAxis = new YAxis();
    yAxis.setType(AxisType.DATETIME);
    yAxis.setTitle("Time");
    Calendar cal = Calendar.getInstance();
    yAxis.setMax(new Date().getTime());
    cal.add(Calendar.YEAR, -3);
    yAxis.setMin(cal.getTime());
    conf.addyAxis(yAxis);
    this.setSizeFull();

    Tooltip tooltip = new Tooltip();
    tooltip.setFormatter("'<b> '+  this.point.category + '</b>'");
    conf.setTooltip(tooltip);

    PlotOptionsColumnrange columnRange = new PlotOptionsColumnrange();
    conf.getChart().setBackgroundColor(new SolidColor("#fafafa"));
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

    timeLeftSeries = new DataSeries();
    o = new PlotOptionsColumnrange();
    o.setColor(new SolidColor("#85929E"));
    timeLeftSeries.setPlotOptions(o);
    timeLeftSeries.setName("Time left");


    conf.getChart().setBackgroundColor(new SolidColor("#fafafa"));
    conf.addSeries(overdueSeries);
    conf.addSeries(timeLeftSeries);
    conf.addSeries(intimeSeries);
    conf.addSeries(unregisteredSeries);

    conf.getLegend().setReversed(true);
    conf.getLegend().setEnabled(false);

    Style style = new Style();
    style.setFontSize("10");
    xAxis.getLabels().setPadding(0.1);
    xAxis.getLabels().setStep(1);
    xAxis.getLabels().setReserveSpace(true);
    xAxis.getLabels().setStyle(style);

    yAxis.getLabels().setStyle(style);

    setImmediate(true);
    drawChart(conf);
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

  public DataSeries getTimeLeftSeries() {
    return timeLeftSeries;
  }
}