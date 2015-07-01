package edu.harvard.hms.dbmi.bd2k.irct.cl.util;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Conversation;
import javax.inject.Inject;
import javax.inject.Named;

@ApplicationScoped
@Startup
@Named("admin")
public class AdminBean {
	@Inject
	private Conversation conversation;
	
	public String startConversation() {
		if(conversation.isTransient()) {
			conversation.begin();
		}
		return conversation.getId();
	}
	
	public void endConversation() {
		if(!conversation.isTransient()) {
			conversation.end();
		}
	}
}