package io.subutai.core.hubmanager.impl.environment.state.build;


import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Sets;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.environment.AnsibleExtraVars;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.hub.share.dto.environment.ContainerStateDto;
import io.subutai.hub.share.dto.environment.EnvironmentDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodeDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodesDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


public class ConfigureEnvironmentStateHandler extends StateHandler
{
    private final CommandUtil commandUtil = new CommandUtil();


    public ConfigureEnvironmentStateHandler( Context ctx )
    {
        super( ctx, "Configure environment" );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws HubManagerException
    {
        try
        {
            logStart();

            Object result = configure( peerDto );

            logEnd();

            return result;
        }
        catch ( Exception e )
        {
            throw new HubManagerException( e );
        }
    }


    private Object configure( final EnvironmentPeerDto peerDto ) throws HubManagerException
    {
        if ( StringUtils.isNotEmpty( peerDto.getAnsible() ) && StringUtils.isNotEmpty( peerDto.getPlaybook() ) )
        {
            EnvironmentDto envDto =
                    ctx.restClient.getStrict( path( "/rest/v1/environments/%s", peerDto ), EnvironmentDto.class );

            Set<EnvironmentNodeDto> allNodes = new HashSet<>();
            Set<EnvironmentNodeDto> modifyingNodes = new HashSet<>();

            Set<ContainerStateDto> modifyingStates =
                    Sets.immutableEnumSet( ContainerStateDto.ACTIVATING, ContainerStateDto.DEACTIVATING );

            for ( EnvironmentNodesDto nodes : envDto.getNodes() )
            {
                for ( EnvironmentNodeDto node : nodes.getNodes() )
                {
                    allNodes.add( node );

                    if ( modifyingStates.contains( node.getState() ) )
                    {
                        modifyingNodes.add( node );
                    }
                }
            }


            StringBuilder logs = new StringBuilder();

            for ( EnvironmentNodeDto nodeDto : modifyingNodes )
            {
                try
                {
                    Host host = ctx.localPeer.getContainerHostById( peerDto.getAnsible() );
                    String stdOut = handleNode( host, peerDto.getPlaybook(), nodeDto, allNodes );
                    logs.append( stdOut );
                    logs.append( "\n" );
                    nodeDto.setState( getDesiredState( nodeDto.getState() ) );
                    allNodes.add( nodeDto );
                }
                catch ( Exception e )
                {
                    nodeDto.setState( ContainerStateDto.UNKNOWN );
                    logs.append(
                            String.format( "Error on handling: %s. %s", nodeDto.getContainerName(), e.getMessage() ) );
                    logs.append( "\n" );
                }
            }

            peerDto.setMessage( logs.toString() );
            peerDto.setOperationNodes( modifyingNodes );
        }

        return peerDto;
    }


    private ContainerStateDto getDesiredState( final ContainerStateDto state )
    {
        ContainerStateDto result = ContainerStateDto.UNKNOWN;
        switch ( state )
        {
            case ACTIVATING:
                result = ContainerStateDto.ACTIVATED;
                break;
            case DEACTIVATING:
                result = ContainerStateDto.DEACTIVATED;
                break;
        }
        return result;
    }


    private String handleNode( Host host, final String playbook, EnvironmentNodeDto node,
                               final Set<EnvironmentNodeDto> activeNodes )
            throws HostNotFoundException, CommandException
    {
        String result = "";
        switch ( node.getState() )
        {
            case ACTIVATING:
                result = runActivatePlaybook( host, playbook, node, activeNodes );
                break;
            case DEACTIVATING:
                result = runDeactivatePlaybook( host, playbook, node, activeNodes );
                break;
        }
        return result;
    }


    private String runActivatePlaybook( Host host, String playbook, EnvironmentNodeDto operationNode,
                                        Set<EnvironmentNodeDto> activeNodes )
            throws HostNotFoundException, CommandException
    {
        //  ansible-playbook site.yaml --extra-vars '{"cassandra_seeds":["172.16.27.3","172.16.27.5"],
        // "operation_hosts":["172.16.27.4"]}'

        CommandResult result;

        AnsibleExtraVars extraVars = new AnsibleExtraVars();

        extraVars.addContainerAll( activeNodes );
        extraVars.addModifyingContainer(
                new AnsibleExtraVars.Container( operationNode.getContainerId(), operationNode.getContainerName(),
                        operationNode.getTemplateName(), operationNode.getIp(), operationNode.getState(),
                        operationNode.isActivated() ) );

        final String command =
                String.format( "cd /root/playbooks/%s/activate && ansible-playbook site.yaml --extra-vars '%s'",
                        playbook, JsonUtil.toJsonString( extraVars ) );

        RequestBuilder rb = new RequestBuilder( command );
        rb.withTimeout( ( int ) TimeUnit.MINUTES.toSeconds( 5 ) );

        result = commandUtil.execute( rb, host );


        return result.getStdOut();
    }


    private String runDeactivatePlaybook( Host host, String playbook, EnvironmentNodeDto node,
                                          Set<EnvironmentNodeDto> allNodes )
            throws HostNotFoundException, CommandException
    {
        CommandResult result;

        AnsibleExtraVars extraVars = new AnsibleExtraVars();

        extraVars.addModifyingContainer(
                new AnsibleExtraVars.Container( node.getContainerId(), node.getContainerName(), node.getTemplateName(),
                        node.getIp(), node.getState(), node.isActivated() ) );

        final String command =
                String.format( "cd /root/playbooks/%s/activate && ansible-playbook site.yaml --extra-vars '%s'",
                        playbook, JsonUtil.toJsonString( extraVars ) );

        RequestBuilder rb = new RequestBuilder( command );
        rb.withTimeout( ( int ) TimeUnit.MINUTES.toSeconds( 5 ) );

        result = commandUtil.execute( rb, host );


        return result.getStdOut();
    }
}