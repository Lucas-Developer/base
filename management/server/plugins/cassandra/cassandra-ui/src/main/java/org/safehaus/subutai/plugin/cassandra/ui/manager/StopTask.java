package org.safehaus.subutai.plugin.cassandra.ui.manager;


import java.util.UUID;

import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;


public class StopTask implements Runnable
{

    private final String clusterName, lxcHostname;
    private final CompleteEvent completeEvent;
    private Cassandra cassandra;
    private Tracker tracker;


    public StopTask( Cassandra cassandra, Tracker tracker, String clusterName, String lxcHostname,
                     CompleteEvent completeEvent )
    {
        this.cassandra = cassandra;
        this.tracker = tracker;
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        this.completeEvent = completeEvent;
    }


    @Override
    public void run()
    {

        UUID trackID = cassandra.stopService( clusterName, lxcHostname );

        long start = System.currentTimeMillis();
        while ( !Thread.interrupted() )
        {
            ProductOperationView po = tracker.getProductOperation( CassandraClusterConfig.PRODUCT_KEY, trackID );
            if ( po != null )
            {
                if ( po.getState() != ProductOperationState.RUNNING )
                {
                    completeEvent.onComplete( po.getLog() );
                    break;
                }
            }
            try
            {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException ex )
            {
                break;
            }
            if ( System.currentTimeMillis() - start > ( 30 + 3 ) * 1000 )
            {
                break;
            }
        }
    }
}