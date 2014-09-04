package org.safehaus.subutai.plugin.hive.ui.wizard;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import java.util.UUID;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.plugin.hive.api.SetupType;
import org.safehaus.subutai.plugin.hive.ui.HiveUI;
import org.safehaus.subutai.server.ui.component.ProgressWindow;

public class VerificationStep extends Panel {

    public VerificationStep(final Wizard wizard) {

        setSizeFull();

        GridLayout grid = new GridLayout(1, 5);
        grid.setSpacing(true);
        grid.setMargin(true);
        grid.setSizeFull();

        Label confirmationLbl = new Label("<strong>Please verify the installation settings "
                + "(you may change them by clicking on Back button)</strong><br/>");
        confirmationLbl.setContentMode(ContentMode.HTML);

        final HiveConfig config = wizard.getConfig();
        final HadoopClusterConfig hc = wizard.getHadoopConfig();
        ConfigView cfgView = new ConfigView("Installation configuration");
        cfgView.addStringCfg("Installation name", config.getClusterName());
        if(config.getSetupType() == SetupType.OVER_HADOOP) {
            cfgView.addStringCfg("Server node", config.getServer().getHostname());
            for(Agent agent : config.getClients()) {
                cfgView.addStringCfg("Node(s) to install", agent.getHostname() + "");
            }
        } else if(config.getSetupType() == SetupType.WITH_HADOOP) {
            cfgView.addStringCfg("Hadoop cluster name", hc.getClusterName());
            cfgView.addStringCfg("Number of Hadoop slave nodes", hc.getCountOfSlaveNodes() + "");
            cfgView.addStringCfg("Replication factor", hc.getReplicationFactor() + "");
            cfgView.addStringCfg("Domain name", hc.getDomainName());
        }

        Button install = new Button("Install");
        install.addStyleName("default");
        install.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                UUID trackId = null;
                if(config.getSetupType() == SetupType.OVER_HADOOP)
                    trackId = HiveUI.getManager().installCluster(config);
                else if(config.getSetupType() == SetupType.WITH_HADOOP)
                    trackId = HiveUI.getManager().installCluster(config, hc);
                ProgressWindow window = new ProgressWindow(HiveUI.getExecutor(),
                        HiveUI.getTracker(), trackId, HiveConfig.PRODUCT_KEY);
                window.getWindow().addCloseListener(new Window.CloseListener() {
                    @Override
                    public void windowClose(Window.CloseEvent closeEvent) {
                        wizard.init();
                    }
                });
                getUI().addWindow(window.getWindow());
            }
        });

        Button back = new Button("Back");
        back.addStyleName("default");
        back.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                wizard.back();
            }
        });

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent(back);
        buttons.addComponent(install);

        grid.addComponent(confirmationLbl, 0, 0);
        grid.addComponent(cfgView.getCfgTable(), 0, 1, 0, 3);
        grid.addComponent(buttons, 0, 4);

        setContent(grid);

    }

}