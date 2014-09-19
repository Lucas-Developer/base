package org.safehaus.subutai.plugin.spark.ui.manager;


import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

import java.util.UUID;


public class CheckTaskSlave implements Runnable {

    private final String clusterName, lxcHostname;
    private final CompleteEvent completeEvent;
    private final Spark spark;
    private final Tracker tracker;


    public CheckTaskSlave( final Spark spark, final Tracker tracker, String clusterName, String lxcHostname,
                           CompleteEvent completeEvent ) {
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        this.completeEvent = completeEvent;
        this.spark = spark;
        this.tracker = tracker;
    }


    @Override
    public void run() {
        UUID trackID = spark.checkSlaveNode( clusterName, lxcHostname );

        long start = System.currentTimeMillis();
        while ( !Thread.interrupted() ) {
            ProductOperationView po = tracker.getProductOperation( SparkClusterConfig.PRODUCT_KEY, trackID );
            if ( po != null ) {
                if ( po.getState() != ProductOperationState.RUNNING ) {
                    completeEvent.onComplete( po.getLog() );
                    break;
                }
            }

            try {
                Thread.sleep( 1000 );
            } catch ( InterruptedException ex ) {
                break;
            }

            if ( System.currentTimeMillis() - start > 30 * 1000 ) {
                break;
            }
        }
    }
}