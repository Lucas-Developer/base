package org.safehaus.subutai.plugin.hive.impl.handler;

import org.safehaus.subutai.core.command.api.AgentResult;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.command.api.RequestBuilder;
import org.safehaus.subutai.plugin.hive.impl.CommandType;
import org.safehaus.subutai.plugin.hive.impl.Commands;
import org.safehaus.subutai.plugin.hive.impl.HiveImpl;
import org.safehaus.subutai.plugin.hive.impl.Product;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.*;

public class CheckInstallHandler {

	private final HiveImpl manager;

	public CheckInstallHandler(HiveImpl manager) {
		this.manager = manager;
	}

	/**
	 * Checks whether specified product is installed on the host.
	 *
	 * @param product the product to check
	 * @param node    the node to check
	 * @return <tt>true</tt> if specified product is installed on host;
	 * <tt>false</tt> otherwise
	 */
	public boolean check(Product product, Agent node) {
		Map<Agent, Boolean> map = check(product, Arrays.asList(node));
		Boolean b = map.get(node);
		return b != null ? b : false;
	}

	/**
	 * Checks whether specified nodes have installed product.
	 *
	 * @param product the product to check
	 * @param nodes   set of nodes to check
	 * @return map where key is a node instance and value is boolean value
	 * indicating if the product is installed or not
	 */
	public Map<Agent, Boolean> check(Product product, Collection<Agent> nodes) {
		String s = Commands.make(CommandType.LIST, null);
		Command cmd = manager.getCommandRunner().createCommand(
				new RequestBuilder(s), new HashSet<>(nodes));
		manager.getCommandRunner().runCommand(cmd);

		if (cmd.hasSucceeded()) {
			Map<Agent, Boolean> map = new HashMap<>();
			for (Map.Entry<UUID, AgentResult> e : cmd.getResults().entrySet()) {
				boolean b = e.getValue().getStdOut().contains(product.getPackageName());
				for (Agent a : nodes)
					if (a.getUuid().equals(e.getKey())) map.put(a, b);
			}
			return map;
		}
		return Collections.emptyMap();
	}

}