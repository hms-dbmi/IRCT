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
		logger.info("ensureUserExists() Starting " + userId);
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> userRoot = cq.from(User.class);
		cq.where(cb.equal(userRoot.get("userId"), userId));
		cq.select(userRoot);
		User user;
		try{
			user = entityManager.createQuery(cq).getSingleResult();
			logger.debug("ensureUserExists() User found. Already existed.");
			
		} catch(NoResultException e){
			logger.error("ensureUserExists() UserId could not be found by `entityManager`");
			
			user = new User(userId);
			logger.debug("ensureUserExists() Created new `user` object.");
						
			logger.debug("ensureUserExists() Call persist() on `entityManager`");
			entityManager.persist(user);
			logger.debug("ensureUserExists() New `user` object persisted.");
			
		} catch(NonUniqueResultException e){
			logger.error("ensureUserExists() Exception:" + e.getMessage());
			throw new RuntimeException("Duplicate User Found : " + userId, e);
		}
		
		logger.debug("ensureUserExists() Finished");
		return user;
	}
	
	public String createKey(User user) {
		logger.info("createKey() Starting " + user.getName());
		
		SecureRandom random = new SecureRandom();
		String key = new BigInteger(130, random).toString(32);
		
		try {
			// TODO, for now, just use the TOKEN column
			user.setAccessKey(key);
			if (user.getId() == null) {
				entityManager.persist(user);
			} else {
				entityManager.merge(user);
			}
			entityManager.flush();
		} catch (Exception e) {
			logger.error("createKey() Exception"+e.getMessage());
		}

		logger.info("createKey() Created key for " + user.getName());
		return key;
	}
	
	public String updateUserRecord(User user) {
		logger.debug("updateUserRecord() Starting");
		try {
			entityManager.merge(user);
			entityManager.flush();
		} catch (Exception e) {
			logger.error("updateUserRecord() Exception"+e.getMessage());
			return "error "+e.getMessage();
		}
		logger.debug("updateUserRecord() User has been updated.");
		return "ok";
	}
	
	// TODO: This is a temporary solution. While we store the "key" information 
	// in the token field of a user object, which is persisted.
	public User validateKey(String key) {
		logger.info("validateKey() Starting");
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> userRoot = cq.from(User.class);
		cq.where(cb.equal(userRoot.get("accessKey"), key));
		cq.select(userRoot);
		User user = null;
		try{
			user = entityManager.createQuery(cq).getSingleResult();
			logger.debug("validateKey() User found.");
			
		} catch(NoResultException e){
			logger.error("validateKey() The key is not in the database. Cannot authenticate the user.");
			throw new RuntimeException("The key is not in the database. Cannot authenticate the user");
		} catch(Exception e){
			logger.error("validateKey() Exception:" + e.getMessage());
			throw new RuntimeException("User cannot be validated based on the key", e);
		}
		logger.debug("validateKey() Finished");
		return user;

	}

}
