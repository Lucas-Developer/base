package io.subutai.core.hubmanager.impl.environment;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.hub.share.dto.environment.ContainerStateDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodeDto;


public class AnsibleExtraVars
{
    @JsonProperty( value = "containers" )

    private List<Container> containers = new ArrayList<>();

    @JsonProperty( value = "modifying_containers" )
    private List<Container> modifyingContainers = new ArrayList<>();


    public List<Container> getContainers()
    {
        return containers;
    }


    public void setContainers( final List<Container> containers )
    {
        this.containers = containers;
    }


    public List<Container> getModifyingContainers()
    {
        return modifyingContainers;
    }


    public void setModifyingContainers( final List<Container> modifyingContainers )
    {
        this.modifyingContainers = modifyingContainers;
    }


    public void addContainer( Container container )
    {
        if ( container == null )
        {
            throw new IllegalArgumentException( "Container could not be null." );
        }

        this.containers.add( container );
    }


    public void addModifyingContainer( Container container )
    {
        if ( container == null )
        {
            throw new IllegalArgumentException( "Modifying container could not be null." );
        }

        this.modifyingContainers.add( container );
    }


    public void addContainerAll( final Set<EnvironmentNodeDto> activeNodes )
    {
        for ( EnvironmentNodeDto node : activeNodes )
        {
            this.containers.add( new AnsibleExtraVars.Container( node.getContainerId(), node.getContainerName(),
                    node.getTemplateName(), node.getIp(), node.getState(), node.isActivated() ) );
        }
    }


    public static class Container
    {
        String containerId;
        String containerName;
        String templateName;
        String ip;
        ContainerStateDto state;
        boolean activated;


        public Container( final String containerId, final String containerName, final String templateName,
                          final String ip, final ContainerStateDto state, final boolean activated )
        {
            this.containerId = containerId;
            this.containerName = containerName;
            this.templateName = templateName;
            this.ip = ip;
            this.state = state;
            this.activated = activated;
        }


        public boolean isActivated()
        {
            return activated;
        }


        public void setActivated( final boolean activated )
        {
            this.activated = activated;
        }


        public ContainerStateDto getState()
        {
            return state;
        }


        public void setState( final ContainerStateDto state )
        {
            this.state = state;
        }


        public String getIp()
        {
            return ip;
        }


        public void setIp( final String ip )
        {
            this.ip = ip;
        }


        public String getContainerName()
        {
            return containerName;
        }


        public void setContainerName( final String containerName )
        {
            this.containerName = containerName;
        }


        public String getTemplateName()
        {
            return templateName;
        }


        public void setTemplateName( final String templateName )
        {
            this.templateName = templateName;
        }


        public String getContainerId()
        {
            return containerId;
        }


        public void setContainerId( final String containerId )
        {
            this.containerId = containerId;
        }
    }
}
