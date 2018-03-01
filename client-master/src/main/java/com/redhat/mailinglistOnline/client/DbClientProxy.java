package com.redhat.mailinglistOnline.client;

import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 * This class is just passing dbClient instance, needed because without it it opens connections
 * to DB all the time.
 *
 * @author Július Staššík
 *
 */

@Stateless(mappedName="dbClientProxy",name="dbClientProxy")
public class DbClientProxy {
	
	@Inject
	DbClient dbclient;
	
	public DbClientProxy(){
		
	}
	
	public DbClient getDbClient() {
		return dbclient;
	}

}
