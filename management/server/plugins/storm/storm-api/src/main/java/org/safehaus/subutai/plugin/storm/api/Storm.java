package org.safehaus.subutai.plugin.storm.api;

import java.util.UUID;
import org.safehaus.subutai.common.protocol.*;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;

public interface Storm extends ApiBase<StormConfig> {

    public UUID statusCheck(String clusterName, String hostname);

    public UUID startNode(String clusterName, String hostname);

    public UUID stopNode(String clusterName, String hostname);

    public UUID restartNode(String clusterName, String hostname);

    /**
     * Adds a node to specified cluster.
     *
     * @param clusterName the name of cluster to which a node is added
     * @return operation id to track
     */
    public UUID addNode(String clusterName);

    public UUID destroyNode(String clusterName, String hostname);

    public EnvironmentBlueprint getDefaultEnvironmentBlueprint(StormConfig config);

    public ClusterSetupStrategy getClusterSetupStrategy(Environment environment,
            StormConfig config, ProductOperation po);
}