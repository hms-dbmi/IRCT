package edu.harvard.hms.dbmi.bd2k.iprt.cl.util;

import java.util.logging.Logger;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

public class Resources {
	
	@Produces
	@PersistenceUnit(unitName = "primary")
	private EntityManagerFactory objectEntityManager;
	
	
	@Produces
	public Logger produceLog(InjectionPoint injectionPoint) {
		return Logger.getLogger(injectionPoint.getMember().getDeclaringClass()
				.getName());
	}
}
