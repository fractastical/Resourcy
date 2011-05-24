package com.joeldietz.resourcy.utilities;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;

import com.google.appengine.api.urlfetch.FetchOptions.Builder;
import static com.google.appengine.api.urlfetch.FetchOptions.Builder.*;
import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.ResponseTooLargeException;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.wave.api.Blip;
import com.google.wave.api.ElementType;
import com.google.wave.api.FormElement;
import com.google.wave.api.Wavelet;
import com.joeldietz.resourcy.connections.*; 
import com.joeldietz.resourcy.models.Like;
import com.joeldietz.resourcy.models.LogEntry;
import com.joeldietz.resourcy.models.ResourceWithLike;
import com.joeldietz.resourcy.models.Tag;
import com.joeldietz.resourcy.models.WaveActionResponse;
import com.joeldietz.resourcy.models.WaveActionType;
import com.joeldietz.resourcy.ResourcyServlet;
import com.joeldietz.resourcy.ResourcyTaskServlet;
import com.sforce.soap.enterprise.Connector;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.QueryResult;
import com.sforce.soap.enterprise.sobject.Resource__c;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;


public class Utilities {
	
	private EnterpriseConnection connection;
	private ConnectionManager cm = new ConnectionManager();
    private static final Logger LOG = Logger.getLogger(ResourcyServlet.class.getName());
    private static final String username = "d3@d3developer.com";
    private static final String password = "fractastic4iKKZ1L5ylfGMFXiCzOeITGMe6";
    private static final String RPC_SERVER_URL = "http://gmodules.com/api/rpc";
    private static final String TUMBLR_WRITE_PATH = "http://www.tumblr.com/api/write";

    
	public static Integer submitLike(String participantId, String resourceId)
	{		
		PersistenceManager pm = PMF.get().getPersistenceManager();

		try {
			
			String query1 = "select from " + Like.class.getName() + " where resourceId == '"+ resourceId + "' && participantId == '" + participantId + "'";
			LOG.log(Level.WARNING, "submitLike with Query 1: " + query1);
			List <Like> ls = (List <Like>) pm.newQuery(query1).execute();
			if (ls==null || ls.size() == 0)
			{
				LOG.log(Level.WARNING, "No likes found that match query 1");
				Like l = new Like();
				l.setParticipantId(participantId);
				l.setResourceId(resourceId);
				pm.makePersistent(l);
			}
			else
			{
				LOG.log(Level.WARNING, ls.size() + " likes found that match query 1");

			}

			String query2 = "select from " + Like.class.getName() + " where resourceId == '"+ resourceId+ "'";
			LOG.log(Level.WARNING,  "submitLike with Query 2: " +query2);

			ls = (List <Like>) pm.newQuery(query2).execute();
			if (ls != null)
			{
				LOG.log(Level.WARNING,  ls.size() + " number of likes found which match Query 2 ");

				return ls.size();
			}
			else
				return 0;
		}
		
		finally {
			pm.close();

		}
		
		
	}
	
	public static void submitTag(String tagName, String resourceId)
	{		
		PersistenceManager pm = PMF.get().getPersistenceManager();

		try {
			
			String query1 = "select from " + Tag.class.getName() + " where resourceId == '"+ resourceId + "' && tagName == '" + tagName + "'";
			LOG.log(Level.WARNING, "submitTag with Query 1: " + query1);
			List <Tag> ts = (List <Tag>) pm.newQuery(query1).execute();
			if (ts==null || ts.size() == 0)
			{
				LOG.log(Level.WARNING, "No tags found that match query 1");
				Tag t = new Tag();
				t.setTagName(tagName);
				t.setResourceId(resourceId);
				pm.makePersistent(t);
			}
			else
			{
				LOG.log(Level.WARNING, ts.size() + " tags found that match query 1");

			}

		}
		
		finally {
			pm.close();

		}
		
		
	}

	

	public static String genLikeString(Integer likeCount)
	{
		  String likeString = "";
		  
		  if (likeCount != 1) 
		  {
			  if (likeCount > 9) 
				  if (likeCount > 99)
				  {
					  if (likeCount > 999)
						  likeString +=" ";
					  else
						  likeString +="  ";
				  }
				  else{
					  likeString +="   ";
				  }
			  else
				  likeString +="    ";			  				  
		  
		  likeString +=likeCount + " people found this helpful."; // 27 + 5 = 32 chars to delete 
		  
		  }
		  else
			  likeString +="    1 person found this helpful."; // also 32 chars
		  
		  return likeString;

		
	}
	
	public static Integer getLikeCount(String resourceId)
	{
		PersistenceManager pm = PMF.get().getPersistenceManager();

		try {
			
			String query = "select from " + Like.class.getName() + " where resourceId == '"+ resourceId+ "'";
			List <Like> ls = (List<Like>) pm.newQuery(query).execute();
			if (ls != null)
				return ls.size();
			else
				return 0;
		}
		
		finally {
			pm.close();
		}
		
	}
	
	  public static ArrayList<Resource__c> getResources(String category) {
		  ArrayList<Resource__c> myResources = new ArrayList<Resource__c>();

		  try {
				   ConnectionManager cm = new ConnectionManager();
				   EnterpriseConnection connection = cm.getConnection(username,password);
				   QueryResult result = null;
			   
			    // We should escape category against SOQL injection ? 
			   	// This needs to be a chatter enabled object
			   	String queryString = "SELECT r.Id, r.Name, r.Url__c, r.Source__c, r.SubCategory__r.name, r.WaveId__c, r.WaveletId__c, r.Description__c from Resource__c r where SubCategory__r.Category__r.name = '" + category + "' and approved__c = true order by SubCategory__r.name, name limit 40 ";
			   	LOG.log(Level.WARNING, "QS:" + queryString);
			   	
			   	result = connection.query(queryString);
			   			
		        for (int i=0; i < result.getRecords().length;i++) 
		        {
		        	Resource__c resource = (Resource__c)result.getRecords()[i];
		        	myResources.add(resource);
		        	
		        } 

		  } catch (ConnectionException ce) {
			  LOG.log(Level.WARNING, "Connection Error:" + ce.getMessage() + ":" + ce.getClass());
		  }
		  LOG.log(Level.WARNING,"Resources found. Size:" + myResources.size());
		  return myResources;
	}  
	  
	  public static ArrayList<ResourceWithLike> getResourcesWithLike(String category) {
		  ArrayList<ResourceWithLike> myResources = new ArrayList<ResourceWithLike>();

		  try {
				   ConnectionManager cm = new ConnectionManager();
				   EnterpriseConnection connection = cm.getConnection(username,password);
				   QueryResult result = null;
			   
			   	// This needs to be a chatter enabled object
			   	String queryString = "SELECT r.Id, r.Name, r.Url__c, r.Source__c, r.SubCategory__r.name, r.WaveId__c, r.WaveletId__c, r.Description__c from Resource__c r where SubCategory__r.Category__r.name = '" + category + "' and approved__c = true order by SubCategory__r.name, name limit 40 ";
			   	LOG.log(Level.WARNING, "QS:" + queryString);
			   	
			   	result = connection.query(queryString);
		        ArrayList<ArrayList <ResourceWithLike>> resBrokenDownBySubCat = new ArrayList<ArrayList <ResourceWithLike>>();

		        String lastSubCat = "";
		        ArrayList<ResourceWithLike> currResources = new ArrayList<ResourceWithLike>();
			   	
		        Boolean first = true;
		        
		        for (int i=0; i < result.getRecords().length;i++) 
		        {
		        	Resource__c resource = (Resource__c)result.getRecords()[i];
		        	if (!lastSubCat.equals(resource.getSubcategory__r().getName()))
		        	{

		        		if (first)
		        			first = false;
		        		else
		        			resBrokenDownBySubCat.add(currResources);
		        		
		        		currResources = new ArrayList<ResourceWithLike>();		        		
		        	}
		        	
	        		lastSubCat = resource.getSubcategory__r().getName();
		        	currResources.add(new ResourceWithLike(resource,getLikeCount(resource.getId())));

		        } 
    			resBrokenDownBySubCat.add(currResources);

		        //Separate by subcat then sort

		        for(ArrayList<ResourceWithLike> rs : resBrokenDownBySubCat)
		        {
		        	Collections.sort(rs);
		        	for(ResourceWithLike r : rs)
		        		myResources.add(r);
		        }
		        		        		        
		        
		  } catch (ConnectionException ce) {
			  LOG.log(Level.WARNING, "Connection Error:" + ce.getMessage() + ":" + ce.getClass());
		  }
		  LOG.log(Level.WARNING,"Resources found. Size:" + myResources.size());
		  return myResources;
	}  


	  public static void outputResources(Wavelet wavelet, Blip rootblip, String catName)
	  {
		  LOG.log(Level.WARNING, "Sending resources from category: " + catName + " to Output stream");

		  ArrayList<Resource__c> resources = getResources(catName);
		  String lastSubcategory = "";
		  Blip subCatBlip = rootblip;
		  // has to be initialized
		  Blip b = subCatBlip;

		  for(Resource__c r : resources)
		  {	

			  // This should always be the case the first time 
			  if(!r.getSubcategory__r().getName().equals(lastSubcategory)) 
			  {	  
				  subCatBlip = rootblip.reply();
				  subCatBlip.append("Subcategory: " + r.getSubcategory__r().getName());
				  Blip lastblip = subCatBlip.reply();
				  lastblip.append(new FormElement(ElementType.BUTTON, "hide_button", "Hide this Subcategory"));
				  LOG.log(Level.WARNING, "Creating last blip. BlipId: " + lastblip.getBlipId() + " ParentId:" + lastblip.getParentBlipId());

				  
			  }
			  lastSubcategory = r.getSubcategory__r().getName();
			  b = subCatBlip.reply();
			  LOG.log(Level.WARNING, "Creating reply blip of SubCat. BlipId: " + b.getBlipId() + " ParentId:" + b.getParentBlipId());

			  b.append("Name: " + r.getName() + "\n\n");
			  if(r.getDescription__c()!=null)
				  b.append("Description: " + r.getDescription__c() + "\n\n");
			  Integer startIndex = b.length() + 8;
			  LOG.log(Level.WARNING, "Start Index:" + startIndex + " for item:" + r.getName());
			  b.append("Source: " + r.getSource__c() + "\n\n");
			  b.range(startIndex,  b.length() -1).annotate("link/manual", r.getUrl__c());
			  b.append(new FormElement(ElementType.BUTTON, "comment_button", "Add your comment"));
			  
			  Integer commentNumber = 0;
			  try {
				  if (r.getWaveId__c() != null)
				  {
					  ResourcyTaskServlet rt = new ResourcyTaskServlet();
					  Wavelet commentWave = rt.fetchWavelet(new WaveId(wavelet.getDomain(), r.getWaveId__c()), new WaveletId(wavelet.getDomain(), r.getWaveletId__c()), RPC_SERVER_URL);
					  commentNumber = commentWave.getBlips().size();
					  if (commentNumber != 1)
						  b.append("   There are " + commentNumber + " comments on this item so far\n");
					  else
						  b.append("   There is one comment on this item\n");

				  }
	  
			  }
			  catch (Exception e)
			  {
				  LOG.log(Level.WARNING, "Unable to load wave with parameters waveId:" + r.getWaveId__c() + "  waveletId:" + r.getWaveletId__c() + " Domain: " + wavelet.getDomain() + " error: "  + e);
			  }
		 	  
			  b.append(new FormElement(ElementType.BUTTON, "like_button", "Like"));
			  
			  Integer likeCount = Utilities.getLikeCount(r.getId());
			  //must be the same number of characters for us to delete the correct section each time
			  
			  b.append(Utilities.genLikeString(likeCount));
			  b.all().annotate("resourcy.appspot.com/RESOURCEID", r.getId());
			  b.all().annotate("resourcy.appspot.com/LIKECOUNT", likeCount.toString());
			  LOG.log(Level.WARNING, "resourcy.appspot.com/CATEGORY annotated: "+ catName);
			  b.all().annotate("resourcy.appspot.com/CATEGORY", catName);
			  
			  if(r.getWaveId__c() != null)
			  {
				 b.all().annotate("resourcy.appspot.com/COMMENT_WAVE_ID", r.getWaveId__c());
				  LOG.log(Level.WARNING, "resourcy.appspot.com/COMMENT_WAVE_ID annotated: "+ r.getWaveId__c());
			  }
			  
		  } 
		  if (resources.size() == 0)
			  subCatBlip.append("Sorry there are no resources yet in this category.");

	  }


	  public static void outputResourcesWithLike(Wavelet wavelet, Blip rootblip, String catName)
	  {
		  LOG.log(Level.WARNING, "Sending resources from category: " + catName + " to Output stream");

		  ArrayList<ResourceWithLike> resources = getResourcesWithLike(catName);
		  String lastSubcategory = "";
		  Blip subCatBlip = rootblip;
		  // has to be initialized
		  Blip b = subCatBlip;

		  //We iterate backwards since the first blips created will be the farthest away (down) the discussion thread
		  for(Integer i = resources.size() - 1; i >= 0; i--)
		  {	
			  ResourceWithLike r = resources.get(i);

			  // This should always be the case the first time 
			  if(!r.getResource().getSubcategory__r().getName().equals(lastSubcategory)) 
			  {	  
				  subCatBlip = rootblip.reply();
				  subCatBlip.append("Subcategory: " + r.getResource().getSubcategory__r().getName());
				  Blip lastblip = subCatBlip.reply();
				  lastblip.append(new FormElement(ElementType.BUTTON, "hide_button", "Hide this Subcategory"));
				  LOG.log(Level.WARNING, "Creating last blip. BlipId: " + lastblip.getBlipId() + " ParentId:" + lastblip.getParentBlipId());
				  
			  }
			  lastSubcategory = r.getResource().getSubcategory__r().getName();
			  b = subCatBlip.reply();
			  LOG.log(Level.WARNING, "Creating reply blip of SubCat. BlipId: " + b.getBlipId() + " ParentId:" + b.getParentBlipId());

			  b.append("Name: " + r.getResource().getName() + "\n\n");
			  if(r.getResource().getDescription__c()!=null)
				  b.append("Description: " + r.getResource().getDescription__c() + "\n\n");
			  Integer startIndex = b.length() + 8;
			  LOG.log(Level.WARNING, "Start Index:" + startIndex + " for item:" + r.getResource().getName());
			  b.append("Source: " + r.getResource().getSource__c() + "\n\n");
			  b.range(startIndex,  b.length() -1).annotate("link/manual", r.getResource().getUrl__c());
			  b.append(new FormElement(ElementType.BUTTON, "comment_button", "Add your comment"));
		 	  
			  b.append(new FormElement(ElementType.BUTTON, "like_button", "Like"));
			  
			  b.append(Utilities.genLikeString(r.getLikeCount()));
			  b.all().annotate("resourcy.appspot.com/RESOURCEID", r.getResource().getId());
			  b.all().annotate("resourcy.appspot.com/LIKECOUNT", r.getLikeCount().toString());
			  b.all().annotate("resourcy.appspot.com/CATEGORY", catName);
			  
			  if(r.getResource().getWaveId__c() != null)
			  {
				 b.all().annotate("resourcy.appspot.com/COMMENT_WAVE_ID", r.getResource().getWaveId__c());
				  LOG.log(Level.WARNING, "resourcy.appspot.com/COMMENT_WAVE_ID annotated: "+ r.getResource().getWaveId__c());
			  }
			  
		  } 
		  if (resources.size() == 0)
			  subCatBlip.append("Sorry there are no resources yet in this category.");

	  }
	  
	  public static Boolean tumblrBlogPost(String title)
	  {
		  Boolean success = false;
		  try {
			    URL url = new URL(TUMBLR_WRITE_PATH);
	            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	            connection.setDoOutput(true);
	            connection.setRequestMethod("POST");
	           
	            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
	            writer.write("email=" + "sagejoel@yahoo.com");
	            writer.write("&password=" + "wakka4fed");
	            writer.write("&type=" + "regular");
	            writer.write("&format=" + "html");
	            
	            //myblog.tumblr.com/post/123456/this-is-the-slug
	            //writer.write("&slug=" + title);
	            
	            writer.write("&title=" + "test post");
	            writer.write("&body=" + "my test content &lt;script type=&quot;text/javascript&quot;&gt;alert(&#39;woo&#39;);&lt;/script&gt; more content");

	            writer.close();
	   
	            if (connection.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
	                success = true;
	            } else {
	            	LOG.log(Level.WARNING, "Server returned HTTP error code:" +connection.getResponseCode() );	
	            	////503 means that server is busy and should be retried. 400 on invalid input data, 403 for authentication or permissions failures, or 404 on a not-found post or an incorrect reblog-key value

	            	//if(connection.getResponseCode() == HttpURLConnection.HTTP_UNAVAILABLE)
	            		//requeue

	            }
			  			  			  
		  } catch (ResponseTooLargeException e) {
          	LOG.log(Level.WARNING, "ResponseTooLargeException:" + e);	            
		  } catch (MalformedURLException e) {
          	LOG.log(Level.WARNING, "MalformedURLException" + e);	            
		  } catch (IOException e) {
          	LOG.log(Level.WARNING, "IOException" + e);	            
			  
		  }
		  
		  
		  return success;
		  
	  }



}
