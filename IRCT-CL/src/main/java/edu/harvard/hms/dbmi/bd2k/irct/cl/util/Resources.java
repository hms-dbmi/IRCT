package edu.harvard.hms.dbmi.bd2k.irct.cl.util;

import java.util.logging.Logger;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

/**
 * A set of resources that are made available for the IRCT-RI
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class Resources {
	
	@Produces
	@PersistenceUnit(unitName = "primary")
	private EntityManagerFactory objectEntityManager;
	

	/**
	 * Produces the logger as an injectible parameter
	 * 
	 * @param injectionPoint InjectionPoint
	 * @return Logger
	 */
	@Produces
	public Logger produceLog(InjectionPoint injectionPoint) {
		return Logger.getLogger(injectionPoint.getMember().getDeclaringClass()
				.getName());
	}
}
