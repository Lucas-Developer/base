package org.safehaus.subutai.plugin.common.api;


public interface ClusterOperationHandlerInterface
{

    /**
     * This method will be used to send START, STOP and STATUS operations to containers.
     * @param operationType type of operation (START, STOP, STATUS)
     */
    public void runOperationOnContainers( OperationType operationType );


    /**
     * Setup cluster
     */
    public void setupCluster();


    /**
     * Destroy cluster
     */
    public void destroyCluster();
}
