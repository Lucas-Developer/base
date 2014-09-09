package org.safehaus.subutai.plugin.storm.ui.wizard;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import org.safehaus.subutai.plugin.storm.api.StormConfig;

public class Wizard {

    private final GridLayout grid;
    private int step = 1;
    private StormConfig config = new StormConfig();

    public Wizard() {
        grid = new GridLayout(1, 20);
        grid.setMargin(true);
        grid.setSizeFull();

        putForm();
    }

    private void putForm() {
        grid.removeComponent(0, 1);
        Component component = null;
        switch(step) {
            case 1: {
                component = new WelcomeStep(this);
                break;
            }
            case 2: {
                component = new NodeSelectionStep(this);
                break;
            }
            case 3: {
                component = new VerificationStep(this);
                break;
            }
            default: {
                break;
            }
        }

        if(component != null)
            grid.addComponent(component, 0, 1, 0, 19);
    }

    public Component getContent() {
        return grid;
    }

    protected void next() {
        step++;
        putForm();
    }

    protected void back() {
        step--;
        putForm();
    }

    protected void init(boolean externalZookeeper) {
        step = 1;
        config = new StormConfig();
        config.setExternalZookeeper(externalZookeeper);
        putForm();
    }

    public StormConfig getConfig() {
        return config;
    }

}