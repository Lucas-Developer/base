package org.safehaus.subutai.core.peer.impl;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.RemotePeer;
import org.safehaus.subutai.core.strategy.api.Criteria;


/**
 * Created by timur on 10/22/14.
 */
public class RemotePeerImpl implements RemotePeer
{
    private Peer peer;


    public RemotePeerImpl( final Peer peer )
    {
        this.peer = peer;
    }


    @Override
    public boolean isOnline() throws PeerException
    {
        return false;
    }


    @Override
    public UUID getOwnerId()
    {
        return null;
    }


    @Override
    public Set<ContainerHost> getContainerHostsByEnvironmentId( final UUID environmentId ) throws PeerException
    {
        return null;
    }


    @Override
    public Set<ContainerHost> createContainers( final UUID environmentId, final String templateName, final int quantity,
                                                final String strategyId, final List<Criteria> criteria )
            throws ContainerCreateException
    {
        return null;
    }


    @Override
    public void startContainer( final ContainerHost containerHost ) throws PeerException
    {

    }


    @Override
    public void stopContainer( final ContainerHost containerHost ) throws PeerException
    {

    }


    @Override
    public void destroyContainer( final ContainerHost containerHost ) throws PeerException
    {

    }


    @Override
    public boolean isConnected( final Host host ) throws PeerException
    {
        return false;
    }
}