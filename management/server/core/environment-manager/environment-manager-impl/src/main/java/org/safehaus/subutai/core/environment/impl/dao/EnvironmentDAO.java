package org.safehaus.subutai.core.environment.impl.dao;


import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;

import java.util.List;


/**
 * Environment Manager DAO
 */
public class EnvironmentDAO {

	DbManager dbManager;
	private String source = "ENV";


	public EnvironmentDAO(final DbManager dbManager) {
		this.dbManager = dbManager;
	}


	public List<Environment> getEnvironments() {
		return dbManager.getEnvironmentInfo(source, Environment.class);
	}


	public Environment getEnvironment(final String environmentName) {
		return dbManager.getEnvironmentInfo(source, environmentName, Environment.class);
	}


	public boolean saveEnvironment(final Environment environment) {
		dbManager.saveEnvironmentInfo(source, environment.getName(), environment);
		return true;
	}


	public boolean saveBlueprint(final EnvironmentBlueprint blueprint) {
		//TODO Create table for blueprint objects
		dbManager.saveEnvironmentInfo("BLUEPRINT", blueprint.getName(), blueprint);
		//TODO Return proper result
		return true;
	}


	public List<EnvironmentBlueprint> getBlueprints() {
		return dbManager.getEnvironmentInfo("BLUEPRINT", EnvironmentBlueprint.class);
	}


	public boolean deleteBlueprint(final String blueprintName) {
		dbManager.deleteInfo("BLUEPRINT", blueprintName);
		//TODO return proper result
		return true;
	}
}