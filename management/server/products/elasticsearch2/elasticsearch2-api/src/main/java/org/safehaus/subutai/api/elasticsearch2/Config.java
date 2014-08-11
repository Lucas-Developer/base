package org.safehaus.subutai.api.elasticsearch2;

import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ConfigBase;

import java.util.HashSet;
import java.util.Set;

public class Config implements ConfigBase {

    public static final String PRODUCT_KEY = "Elasticsearch2";
    private String clusterName = "";

    private int numberOfNodes;
    private int numberOfMasterNodes;

    private Set<Agent> nodes = new HashSet<>();
    private Set<Agent> masterNodes = new HashSet<>();

    public static String getProductKey() {
        return PRODUCT_KEY;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @Override
    public String getProductName() {
        return PRODUCT_KEY;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public void setNumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }

    public int getNumberOfMasterNodes() {
        return numberOfMasterNodes;
    }

    public void setNumberOfMasterNodes( int numberOfSeeds ) {
        this.numberOfMasterNodes = numberOfSeeds;
    }


    public Set<Agent> getNodes() {
        return nodes;
    }


    public Set<Agent> getMasterNodes() {
        return masterNodes;
    }

    public void setMasterNodes( Set<Agent> seedNodes ) {
        this.masterNodes = seedNodes;
    }


    public void reset() {

    }
}
