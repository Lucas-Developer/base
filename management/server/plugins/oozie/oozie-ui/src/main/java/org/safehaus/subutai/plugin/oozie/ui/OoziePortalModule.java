/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.oozie.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.oozie.api.Oozie;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


/**
 * @author dilshat
 */
public class OoziePortalModule implements PortalModule
{


    public static final String MODULE_IMAGE = "oozie.png";
    Oozie oozieManager;
    AgentManager agentManager;
    Tracker tracker;
    Hadoop hadoopManager;
    CommandRunner commandRunner;
    ExecutorService executor;


    public OoziePortalModule()
    {
    }


    public Oozie getOozieManager()
    {
        return oozieManager;
    }


    public void setOozieManager( final Oozie oozieManager )
    {
        this.oozieManager = oozieManager;
    }


    public AgentManager getAgentManager()
    {
        return agentManager;
    }


    public void setAgentManager( final AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( final Tracker tracker )
    {
        this.tracker = tracker;
    }


    public Hadoop getHadoopManager()
    {
        return hadoopManager;
    }


    public void setHadoopManager( final Hadoop hadoopManager )
    {
        this.hadoopManager = hadoopManager;
    }


    public CommandRunner getCommandRunner()
    {
        return commandRunner;
    }


    public void setCommandRunner( final CommandRunner commandRunner )
    {
        this.commandRunner = commandRunner;
    }


    public ExecutorService getExecutor()
    {
        return executor;
    }


    public void init()
    {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        oozieManager = null;
        agentManager = null;
        tracker = null;
        hadoopManager = null;
        executor.shutdown();
    }


    @Override
    public String getId()
    {
        return OozieClusterConfig.PRODUCT_KEY;
    }


    public String getName()
    {
        return OozieClusterConfig.PRODUCT_KEY;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( OoziePortalModule.MODULE_IMAGE, this );
    }


    public Component createComponent()
    {
        return new OozieComponent( this );
    }


    @Override
    public Boolean isCorePlugin()
    {
        return false;
    }
}