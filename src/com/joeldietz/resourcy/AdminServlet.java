package com.joeldietz.resourcy;

import javax.servlet.http.HttpServlet;

import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;

public class AdminServlet extends HttpServlet{
	
	/*static void doPost(r,r) {
		
		XMPPService xmpp = XMPPServiceFactory.getXMPPService();
		
		xmpp.sendInvitation(JID("jdietz@gmail.com"));
		
		JID recipient = new JID("jdietz@gmail.com");
		//String body = TestUtilities.
		Message message = new MessageBuilder()
			.withRecipientJids(recipient)
			.withBody("Your dog has reached level 12!")
			.build();
			
		SendResponse success = xmpp.sendMessage(message);
		
		
	}*/

}
