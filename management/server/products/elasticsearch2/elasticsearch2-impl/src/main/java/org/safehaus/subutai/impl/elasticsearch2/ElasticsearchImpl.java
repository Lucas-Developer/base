package org.safehaus.subutai.impl.elasticsearch2;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.elasticsearch2.Elasticsearch;
import org.safehaus.subutai.api.elasticsearch2.Config;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.networkmanager.NetworkManager;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.api.lxcmanager.LxcManager;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;


public class ElasticsearchImpl implements Elasticsearch {

    private DbManager dbManager;
    private Tracker tracker;
    private LxcManager lxcManager;
    private ExecutorService executor;
    private NetworkManager networkManager;
    private CommandRunner commandRunner;
    private AgentManager agentManager;

    public AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public void init() {
        Commands.init(commandRunner);
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        executor.shutdown();
    }

    public void setLxcManager(LxcManager lxcManager) {
        this.lxcManager = lxcManager;
    }

    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    public void setCommandRunner(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }


    public UUID installCluster(final Config config) {
        final ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY, "Installing Elasticsearch...");

        executor.execute(new Runnable() {

            public void run() {
                if (dbManager.getInfo(Config.PRODUCT_KEY, config.getClusterName(), Config.class) != null) {
                    po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName()));
                    return;
                }

                try {
                    po.addLog(String.format("Creating %d LXC containers for Elasticsearch cluster...", config.getNumberOfNodes()));
                    Map<Agent, Set<Agent>> lxcAgentsMap = CustomPlacementStrategy.createNodes(
                            lxcManager, config.getNumberOfNodes());

                    for (Map.Entry<Agent, Set<Agent>> entry : lxcAgentsMap.entrySet()) {
                        config.getNodes().addAll(entry.getValue());
                    }

                    Set<Agent> masterNodes = new HashSet();
                    for ( Agent agent : config.getNodes() ) {
                        masterNodes.add( agent );
                        if ( masterNodes.size() == config.getNumberOfMasterNodes() ) {
                            break;
                        }
                    }
                    config.setMasterNodes( masterNodes );

                    po.addLog("Lxc containers created successfully.");
                    po.addLog("Updating db...");

                    if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                        po.addLog("Cluster info saved to DB");

                        // Install

                        po.addLog("Installing...");
                        Command installCommand = Commands.getInstallCommand(config.getNodes());
                        commandRunner.runCommand(installCommand);

                        if (installCommand.hasSucceeded()) {
                            po.addLog("Installation succeeded");
                        } else {
                            po.addLogFailed(String.format("Installation failed, %s", installCommand.getAllErrors()));
                            return;
                        }

                        // Setting cluster name

                        po.addLog( "Setting cluster name: " + config.getClusterName() );

                        Command setClusterNameCommand = Commands.getConfigureCommand(config.getNodes(), "cluster.name " + config.getClusterName());
                        commandRunner.runCommand(setClusterNameCommand);

                        if (setClusterNameCommand.hasSucceeded()) {
                            po.addLog("Configure cluster name succeeded");
                        } else {
                            po.addLogFailed(String.format("Installation failed, %s", setClusterNameCommand.getAllErrors()));
                            return;
                        }

                        // Setting master nodes

                        po.addLog( "Setting master nodes..." );

                        Command setMasterNodesCommand = Commands.getConfigureCommand( config.getMasterNodes(), " node.master true " );
                        commandRunner.runCommand(setMasterNodesCommand);

                        if (setMasterNodesCommand.hasSucceeded()) {
                            po.addLog("Master nodes setup successful");
                        } else {
                            po.addLogFailed(String.format("Installation failed, %s", setMasterNodesCommand.getAllErrors()));
                            return;
                        }

                        po.addLogDone("Installation of Elasticsearch cluster succeeded");

                    } else {
                        // Destroy all lxcs also
                        try {
                            lxcManager.destroyLxcs(config.getNodes());
                        } catch (LxcDestroyException ex) {
                            po.addLogFailed("Could not save cluster info to DB! Please see logs. Use LXC module to cleanup\nInstallation aborted");
                        }
                        po.addLogFailed("Could not save cluster info to DB! Please see logs\nInstallation aborted");
                    }
                } catch(LxcCreateException ex) {
                    po.addLogFailed(ex.getMessage());
                }

            }
        });

        return po.getId();
    }


    @Override
    public UUID uninstallCluster(final String clusterName) {
        final ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Destroying cluster %s", clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                po.addLog("Destroying lxc containers...");

                try {
                    lxcManager.destroyLxcs(config.getNodes());
                    po.addLog("Lxc containers successfully destroyed");
                } catch (LxcDestroyException ex) {
                    po.addLog(String.format("%s, skipping...", ex.getMessage()));
                }
                po.addLog("Updating db...");
                if (dbManager.deleteInfo(Config.PRODUCT_KEY, config.getClusterName())) {
                    po.addLogDone("Cluster info deleted from DB\nDone");
                } else {
                    po.addLogFailed("Error while deleting cluster info from DB. Check logs.\nFailed");
                }

            }
        });

        return po.getId();
    }



    @Override
    public UUID startAllNodes(final String clusterName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Starting cluster %s", clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }
                Command startServiceCommand = Commands.getStartCommand(config.getNodes());
                commandRunner.runCommand(startServiceCommand);

                if (startServiceCommand.hasSucceeded()) {
                    po.addLogDone("Start succeeded");
                } else {
                    po.addLogFailed(String.format("Start failed, %s", startServiceCommand.getAllErrors()));
                }

            }
        });

        return po.getId();
    }

    @Override
    public UUID stopAllNodes(final String clusterName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Stopping cluster %s", clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                Command stopServiceCommand = Commands.getStopCommand(config.getNodes());
                commandRunner.runCommand(stopServiceCommand);

                if (stopServiceCommand.hasSucceeded()) {
                    po.addLogDone("Stop succeeded");
                } else {
                    po.addLogFailed(String.format("Start failed, %s", stopServiceCommand.getAllErrors()));
                }

            }
        });

        return po.getId();
    }

    @Override
    public UUID startCassandraService(final String agentUUID) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Starting Cassandra service on %s", agentUUID));
        executor.execute(new Runnable() {
            Agent agent = agentManager.getAgentByUUID(UUID.fromString(agentUUID));

            public void run() {
                Command startServiceCommand = Commands.getStartCommand(Util.wrapAgentToSet(agent));
                commandRunner.runCommand(startServiceCommand);
                if (startServiceCommand.hasSucceeded()) {
                    AgentResult ar = startServiceCommand.getResults().get(agent.getUuid());
                    if (ar.getStdOut().contains("starting Cassandra ...") ||
                            ar.getStdOut().contains("is already running...")) {
                        po.addLog(ar.getStdOut());
                        po.addLogDone("Start succeeded");
                    }
                } else {
                    po.addLogFailed(String.format("Start failed, %s", startServiceCommand.getAllErrors()));
                }
            }
        });
        return po.getId();
    }

    @Override
    public UUID stopCassandraService(final String agentUUID) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Stopping Cassandra service on %s", agentUUID));
        executor.execute(new Runnable() {
            Agent agent = agentManager.getAgentByUUID(UUID.fromString(agentUUID));

            public void run() {
                Command stopServiceCommand = Commands.getStopCommand(Util.wrapAgentToSet(agent));
                commandRunner.runCommand(stopServiceCommand);
                if (stopServiceCommand.hasSucceeded()) {
                    AgentResult ar = stopServiceCommand.getResults().get(agent.getUuid());
                    po.addLog(ar.getStdOut());
                    po.addLogDone("Stop succeeded");
                } else {
                    po.addLogFailed(String.format("Stop failed, %s", stopServiceCommand.getAllErrors()));
                }
            }
        });
        return po.getId();
    }

    @Override
    public UUID statusCassandraService(final String agentUUID) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Checking status of Cassandra service on %s", agentUUID));
        executor.execute(new Runnable() {
            Agent agent = agentManager.getAgentByUUID(UUID.fromString(agentUUID));

            public void run() {
                Command statusServiceCommand = Commands.getStatusCommand(Util.wrapAgentToSet(agent));
                commandRunner.runCommand(statusServiceCommand);
                if (statusServiceCommand.hasSucceeded()) {
                    AgentResult ar = statusServiceCommand.getResults().get(agent.getUuid());
                    if (ar.getStdOut().contains("is running")) {
                        po.addLogDone("Cassandra is running");
                    } else {
                        po.addLogFailed("Cassandra is not running");
                    }
                } else {
                    po.addLogFailed("Cassandra is not running");
//                    po.addLogFailed(String.format("Status check failed, %s", statusServiceCommand.getAllErrors()));
                }
            }
        });
        return po.getId();
    }

    @Override
    public UUID checkAllNodes(final String clusterName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Checking cluster %s", clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                Command checkStatusCommand = Commands.getStatusCommand(config.getNodes());
                commandRunner.runCommand(checkStatusCommand);

                if (checkStatusCommand.hasSucceeded()) {
                    po.addLogDone("All nodes are running.");
                } else {
                    po.addLogFailed(String.format("Check status failed, %s", checkStatusCommand.getAllErrors()));
                }

            }
        });

        return po.getId();
    }

    public List<Config> getClusters() {

        return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);

    }

    @Override
    public Config getCluster(String clusterName) {
        return dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
    }


}
