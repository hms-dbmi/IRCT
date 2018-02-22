/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.security;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.io.Serializable;
import java.security.Principal;
/**
 * A basic user representation. It can be associated with a session in EE 7. The
 * userId, and name are the same in this implementation.
  */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class User implements Principal, Serializable {
	private static final long serialVersionUID = 225027371671010450L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	private String userId;

	private String token;

	@JsonProperty("key")
	private String accessKey;
	
	/**
	 * Creates a new user
	 */
	public User() {
		
	}

	/**
	 * Creates a new user with the given User Id
	 * 
	 * @param userId User Id
	 */
	public User(String userId) {
		this.userId = userId;
	}

	/**
	 * Returns the id of this User object
	 * 
	 * @return Id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the id of this User object
	 * 
	 * @param id Id
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Returns the user id
	 * 
	 * @return User Id
	 */
	public String getUserId() {
		return this.userId;
	}

	/**
	 * Sets the user id
	 * 
	 * @param userId User Id
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public String getName() {
		return this.userId;
	}

	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * @return the accessKey
	 */
	public String getAccessKey() {
		return accessKey;
	}

	/**
	 * @param accessKey the accessKey to set
	 */
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}
}
