package org.safehaus.subutai.core.metric.impl;


import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.safehaus.subutai.core.metric.api.MetricListener;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.metric.api.ResourceHostMetric;
import org.safehaus.subutai.core.monitor.api.MonitorException;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerInterface;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


/**
 * Implementation of Monitor
 */
public class MonitorImpl implements Monitor
{
    protected static final Logger LOG = LoggerFactory.getLogger( MonitorImpl.class.getName() );
    //max length of subscriber id length to store in database varchar(100) field
    private static final int MAX_SUBSCRIBER_ID_LEN = 100;
    //set of metric subscribers
    private final Set<MetricListener> metricListeners =
            Collections.newSetFromMap( new ConcurrentHashMap<MetricListener, Boolean>() );

    private final ExecutorService notificationExecutor = Executors.newCachedThreadPool();
    private final MonitorDao monitorDao;
    private final PeerManager peerManager;


    public MonitorImpl( final DataSource dataSource, PeerManager peerManager ) throws DaoException
    {
        Preconditions.checkNotNull( dataSource, "Data source is null" );
        Preconditions.checkNotNull( peerManager, "Peer manager is null" );

        this.monitorDao = new MonitorDao( dataSource );
        this.peerManager = peerManager;
    }


    //TODO implement
    @Override
    public Set<ContainerHostMetric> getContainerMetrics( final Environment environment ) throws MonitorException
    {
        //check if environment exists
        //iterate containers within the environment and get their metrics
        return null;
    }


    @Override
    public Set<ResourceHostMetric> getResourceHostMetrics() throws MonitorException
    {
        //iterate resource hosts and get their metrics
        return null;
    }


    @Override
    public void startMonitoring( final MetricListener metricListener, final Environment environment )
            throws MonitorException
    {
        Preconditions.checkNotNull( metricListener, "Metric listener is null" );
        Preconditions.checkNotNull( environment, "Environment is null" );
        //make sure subscriber id is truncated to 100 characters
        String subscriberId = metricListener.getSubscriberId();
        if ( subscriberId.length() > MAX_SUBSCRIBER_ID_LEN )
        {
            subscriberId = subscriberId.substring( 0, MAX_SUBSCRIBER_ID_LEN );
        }
        //save subscription to database
        try
        {
            monitorDao.addSubscription( environment.getId(), subscriberId );
        }
        catch ( DaoException e )
        {
            LOG.error( "Error in startMonitoring", e );
            throw new MonitorException( e );
        }
    }


    @Override
    public void stopMonitoring( final MetricListener metricListener, final Environment environment )
            throws MonitorException
    {
        Preconditions.checkNotNull( metricListener, "Metric listener is null" );
        Preconditions.checkNotNull( environment, "Environment is null" );
        //make sure subscriber id is truncated to 100 characters
        String subscriberId = metricListener.getSubscriberId();
        if ( subscriberId.length() > MAX_SUBSCRIBER_ID_LEN )
        {
            subscriberId = subscriberId.substring( 0, MAX_SUBSCRIBER_ID_LEN );
        }
        //remove subscription from database
        try
        {
            monitorDao.removeSubscription( environment.getId(), subscriberId );
        }
        catch ( DaoException e )
        {
            LOG.error( "Error in stopMonitoring", e );
            throw new MonitorException( e );
        }
    }


    /**
     * This method is called by REST endpoint from local peer indicating that some container hosted locally is under
     * stress.
     *
     * @param alertBody - body of alert in JSON
     */
    @Override
    public void alertThresholdExcess( final String alertBody ) throws MonitorException
    {
        try
        {
            //deserialize container metric
            ContainerHostMetricImpl containerHostMetric = JsonUtil.fromJson( alertBody, ContainerHostMetricImpl.class );
            //find associated container host
            ContainerHost containerHost =
                    peerManager.getLocalPeer().getContainerHostByName( containerHostMetric.getHostname() );
            //set metric's environment id for future reference on the receiving end
            containerHostMetric.setEnvironmentId( containerHost.getEnvironmentId() );

            //find container's owner peer
            PeerInterface ownerPeer = peerManager.getPeer( containerHost.getOwnerPeerId() );

            //send metric to owner peer
            //TODO send alert to ownerPeer once message queue is implemented
        }
        catch ( PeerException | RuntimeException e )
        {
            LOG.error( "Error in alertThresholdExcess", e );
            throw new MonitorException( e );
        }
    }


    /**
     * This methods is called by REST endpoint when a remote peer sends an alert from one of its hosted containers
     * belonging to this peer
     *
     * @param metric - {@code ContainerHostMetric} metric of the host where thresholds are being exceeded
     */
    @Override
    public void alertThresholdExcess( final ContainerHostMetric metric )
    {
        try
        {
            //search for environment, if not found then no-op
            Set<String> subscribersIds = monitorDao.getEnvironmentSubscribersIds( metric.getEnvironmentId() );
            //search for subscriber if not found then no-op
            for ( String subscriberId : subscribersIds )
            {
                //notify subscriber on alert
                notify( metric, subscriberId );
            }
        }
        catch ( DaoException e )
        {
            LOG.error( "Error in alertThresholdExcess", e );
        }
    }


    private void notify( final ContainerHostMetric metric, String subscriberId )
    {
        for ( final MetricListener listener : metricListeners )
        {
            if ( subscriberId.equalsIgnoreCase( listener.getSubscriberId() ) )
            {
                notificationExecutor.execute( new AlertNotifier( metric, listener ) );

                return;
            }
        }
    }


    @Override
    public String getSubscriberId()
    {
        //since this module is not a subscriber to itself then no-op
        return null;
    }


    public void destroy()
    {
        notificationExecutor.shutdown();
    }


    public void addMetricListener( MetricListener metricListener )
    {
        metricListeners.add( metricListener );
    }


    public void removeMetricListener( MetricListener metricListener )
    {
        metricListeners.remove( metricListener );
    }
}