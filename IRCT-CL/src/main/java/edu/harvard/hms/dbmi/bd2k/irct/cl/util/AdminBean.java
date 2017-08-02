/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.util;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Conversation;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * An application scoped class that is started upon the application startup
 * procedure and is used to manage conversations.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@ApplicationScoped
@Startup
@Named("admin")
public class AdminBean {
	@Inject
	private Conversation conversation;

	/**
	 * Starts a conversation
	 * 
	 * @return Conversation id
	 */
	public String startConversation() {
		if (conversation.isTransient()) {
			conversation.begin();
		}
		return conversation.getId();
	}

	/**
	 * Ends the given conversation
	 * 
	 */
	public void endConversation() {
		if (!conversation.isTransient()) {
			conversation.end();
		}
	}
}