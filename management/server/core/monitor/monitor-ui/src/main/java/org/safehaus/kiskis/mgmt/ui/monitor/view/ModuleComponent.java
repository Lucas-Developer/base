package org.safehaus.kiskis.mgmt.ui.monitor.view;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.ui.monitor.service.Metric;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.Date;
import java.util.Set;

public class ModuleComponent extends CustomComponent {

    private Chart chart;

    private PopupDateField startDateField;
    private PopupDateField endDateField;
    private ListSelect metricListSelect;

    public ModuleComponent() {
        setHeight("100%");
        setCompositionRoot( getLayout() );
    }

    public Layout getLayout() {

        AbsoluteLayout layout = new AbsoluteLayout();
        layout.setWidth(1000, Sizeable.UNITS_PIXELS);
        layout.setHeight(1000, Sizeable.UNITS_PIXELS);

        addDateFields(layout);
        addMetricList(layout);
        addSubmitButton(layout);
        addChartLayout(layout);

        return layout;
    }

    private void addDateFields(AbsoluteLayout layout) {

        Date endDate = new Date();
        Date startDate = DateUtils.addHours(endDate, -1);

        startDateField = UIUtil.addDateField(layout, "From:", "left: 20px; top: 50px;", startDate);
        endDateField = UIUtil.addDateField(layout, "To:", "left: 20px; top: 100px;", endDate);
    }

    private void addMetricList(AbsoluteLayout layout) {

        metricListSelect = UIUtil.addListSelect(layout, "Metric:", "left: 20px; top: 150px;", "150px", "270px");

        for ( Metric metric : Metric.values() ) {
            metricListSelect.addItem(metric);
        }
    }

    private void addSubmitButton(AbsoluteLayout layout) {

        Button button = UIUtil.getButton("Submit", "150px");

        button.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                submitButtonClicked();
            }
        });

        layout.addComponent(button, "left: 20px; top: 430px;");
    }

    private void addChartLayout(AbsoluteLayout layout) {

        AbsoluteLayout chartLayout = new AbsoluteLayout();
        chartLayout.setWidth(800, Sizeable.UNITS_PIXELS);
        chartLayout.setHeight(400, Sizeable.UNITS_PIXELS);
        chartLayout.setDebugId("chart");

        layout.addComponent(chartLayout, "left: 200px; top: 20px;");
    }

    private void submitButtonClicked() {

        String host = getSelectedNode();
        Metric metric = getSelectedMetric();

        if ( !validParams(host, metric) ) {
            return;
        }

        if (chart == null) {
            chart = new Chart( getWindow() );
        }

        Date startDate = (Date) startDateField.getValue();
        Date endDate = (Date) endDateField.getValue();

        chart.load(host, metric, startDate, endDate);
    }

    private boolean validParams(String host, Metric metric) {

        boolean success = true;

        if ( StringUtils.isEmpty(host) ) {
            getWindow().showNotification("Please select a node");
            success = false;
        } else if (metric == null) {
            getWindow().showNotification("Please select a metric");
            success = false;
        }

        return success;
    }

    private String getSelectedNode() {

        Set<Agent> agents = MgmtApplication.getSelectedAgents();

        return agents == null || agents.size() == 0
                ? null
                : agents.iterator().next().getHostname();
    }

    private Metric getSelectedMetric() {
        return (Metric) metricListSelect.getValue();
    }
}