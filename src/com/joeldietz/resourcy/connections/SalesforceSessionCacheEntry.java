package com.joeldietz.resourcy.connections;
import java.io.Serializable;

public class SalesforceSessionCacheEntry implements Serializable{

	        private String sessionId;
	        private String serverUrl;
	        
	        public String getSessionId() {
	                return sessionId;
	        }

	        public void setSessionId(String sessionId) {
	                this.sessionId = sessionId;
	        }

	        public String getServerUrl() {
	                return serverUrl;
	        }

	        public void setServerUrl(String serverUrl) {
	                this.serverUrl = serverUrl;
	        }
}

