package com.joeldietz.resourcy.models;

import com.sforce.soap.enterprise.sobject.Resource__c;

public class ResourceWithLike implements Comparable {
	
	Resource__c resource;
	Integer likeCount;
	
	public ResourceWithLike(Resource__c resource, Integer likeCount)
	{
		this.resource = resource;
		this.likeCount = likeCount;
	}
	
	public ResourceWithLike(Resource__c resource)
	{
		this.resource = resource;
		likeCount = 0;
	}
	
	public Resource__c getResource()
	{
		return resource;
	}
	
	public Integer getLikeCount()
	{
		return likeCount;
	}

	 public int compareTo(Object anotherResource) throws ClassCastException {
		    if (!(anotherResource instanceof ResourceWithLike))
		      throw new ClassCastException("A ResourceWithLike object expected.");
		    int otherLikeCount = ((ResourceWithLike) anotherResource).getLikeCount();  
		    return this.likeCount - otherLikeCount;    
	}


}
