package com.joeldietz.resourcy.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Like {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;
	
	private String resourceId;
	private String participantId;
	
	public Long getId() {
		
		return Id;
		
	}
	
	public String getResourceId()
	{
		return resourceId;
	}
	
	public String getParticipantId()
	{
		return participantId;
	}
	
	public void setResourceId(String resourceId)
	{
		this.resourceId = resourceId;
	}
	
	public void setParticipantId(String participantId)
	{
		this.participantId = participantId;
	}


}
