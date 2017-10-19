/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;

import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

/**
 * A stateless controller for managing security.
 */
@Stateless
public class SecurityController {

	@PersistenceContext(unitName = "primary")
	EntityManager entityManager;

	private Logger logger = Logger.getLogger(this.getClass());

	@javax.annotation.Resource(mappedName ="java:global/KeyTimeOutInMinutes")
	private String keyTimeOut;

	/**
	 * Get a given user from a database from a user id
	 *
	 * @param userId
	 *            User Id
	 * @return User
	 */
	public User ensureUserExists(String userId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> userRoot = cq.from(User.class);
		cq.where(cb.equal(userRoot.get("userId"), userId));
		cq.select(userRoot);
		User user;
		try{
			user = entityManager.createQuery(cq).getSingleResult();
		}catch(NoResultException e){
			user = new User(userId);
			entityManager.persist(user);
		}catch(NonUniqueResultException e){
			throw new RuntimeException("Duplicate User Found : " + userId, e);
		}
		return user;
	}
	
	public String createKey(User user, String token) {
		logger.info("createKey() Starting " + user.getName());
		
		if ((user == null) || (token == null)) {
			return null;
		}
		
		SecureRandom random = new SecureRandom();
		String key = new BigInteger(130, random).toString(32);
		
		// TODO, for now, just use the TOKEN column
		user.setToken(key);
		if (user.getId() == null) {
			entityManager.persist(user);
		} else {
			entityManager.merge(user);
		}
		entityManager.flush();

		logger.info("createKey() Created key for " + user.getName());

		return key;
	}

}
