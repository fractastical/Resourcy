package com.joeldietz.resourcy.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Tag {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;
	
	private String resourceId;
	private String tagName;
	
	public Long getId() {
		
		return Id;
		
	}
	
	
	public String getTagNam()
	{
		return tagName;
	}
	
	
	public void setTagName(String tagName)
	{
		this.tagName = tagName;
	}

	
	public String getResourceId()
	{
		return resourceId;
	}
	
	
	public void setResourceId(String resourceId)
	{
		this.resourceId = resourceId;
	}
	


}
