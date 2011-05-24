package com.joeldietz.resourcy;


import java.io.IOException; import java.io.UnsupportedEncodingException;

import javax.jdo.PersistenceManager; import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet; import javax.servlet.http.HttpServletRequest; import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.*;
import java.util.List;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableMap;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.*;
import com.google.wave.api.AbstractRobot;
import com.google.wave.api.Annotation;
import com.google.wave.api.Blip;
import com.google.wave.api.Context;
import com.google.wave.api.Element;
import com.google.wave.api.ElementType;
import com.google.wave.api.FormElement;
import com.google.wave.api.Gadget;
import com.google.wave.api.JsonRpcResponse;
import com.google.wave.api.Wavelet;
import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;

import com.google.wave.api.JsonRpcConstant.ParamsProperty;
import com.google.wave.api.event.BlipSubmittedEvent;
import com.google.wave.api.event.GadgetStateChangedEvent;
import com.google.wave.api.event.WaveletBlipCreatedEvent;
import com.google.wave.api.event.WaveletSelfAddedEvent;
import com.google.wave.api.event.WaveletCreatedEvent;
import com.google.wave.api.event.FormButtonClickedEvent;
import com.google.wave.api.event.EventHandler.Capability;
import com.joeldietz.resourcy.utilities.Utilities;
import com.sforce.soap.enterprise.Connector;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.QueryResult;
import com.sforce.soap.enterprise.SaveResult;
import com.sforce.soap.enterprise.sobject.Category__c;
import com.sforce.soap.enterprise.sobject.Subcategory__c;
import com.sforce.soap.enterprise.sobject.Resource__c;
import com.sforce.soap.enterprise.sobject.Resource__Feed;
import com.sforce.soap.enterprise.sobject.FeedPost;
import com.sforce.soap.enterprise.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import java.util.SortedMap;
import com.joeldietz.resourcy.connections.*;

@SuppressWarnings("serial")
public class ResourcyServlet extends AbstractRobot {
  
  private static final long serialVersionUID = -1875649295661564335L;
  private static String username = null;
  private static String password = null;
  private static String ConsumerKey = null;
  private static String ConsumerSecret = null;
  
  private static String DUMMY_PATH = null;
  private static String RPC_SERVER_URL = null;
  private static String DELETE_BLIP_PATH = null;
  private static String FEEDBACK_FORM_URL = null;
  private static String BLOGPOST_FORM_URL = null;
  private static String EMBED_COMMENT_WAVE_URL = null;

  
  private static final Integer version = 63;
  private static final Logger LOG = Logger.getLogger(ResourcyServlet.class.getName());
  private ConnectionManager cm = new ConnectionManager();
  private EnterpriseConnection connection;


  public ResourcyServlet()
  {
	  	username = System.getProperty("username");
	  	password = System.getProperty("password");
	  	ConsumerKey = System.getProperty("ConsumerKey");
	  	ConsumerSecret = System.getProperty("ConsumerSecret");
	  	DELETE_BLIP_PATH = System.getProperty("DELETE_BLIP_PATH");
	  	FEEDBACK_FORM_URL = System.getProperty("FEEDBACK_FORM_URL");
	  	BLOGPOST_FORM_URL = System.getProperty("BLOGPOST_FORM_URL");
	  	RPC_SERVER_URL = System.getProperty("RPC_SERVER_URL");
	  	DUMMY_PATH = System.getProperty("DUMMY_PATH");
	  	EMBED_COMMENT_WAVE_URL = System.getProperty("EMBED_COMMENT_WAVE_URL");
	  	
	  	setupOAuth(ConsumerKey, ConsumerSecret, RPC_SERVER_URL);
	    setAllowUnsignedRequests(true);	  
  }

  @Override
  protected String getRobotName() {
    return "Resourcy";
  }
  
  @Override
  protected String getRobotAvatarUrl() {
	
	  return "http://i909.photobucket.com/albums/ac295/fractastical/resourcy_wave.jpg";
	  
  }

  
  @Override
  // The following line prevents us from sending extra data over the wire
  @Capability(contexts = {Context.SELF})
  public void onWaveletSelfAdded(WaveletSelfAddedEvent event) {
	  
	  LOG.info("onWaveletSelfAdded");
	  
	  Wavelet wavelet = event.getWavelet();
	  wavelet.delete(wavelet.getRootBlip());

	  Blip rootblip = wavelet.reply("\n\n");
	  Integer startindex = rootblip.length();
	  rootblip.append("Resourcy the Resource Manager welcomes you!");
	  rootblip.append(" V." + version + " ");
	  rootblip.range(startindex,rootblip.length()-1).annotate("style/fontWeight", "bold");
	  rootblip.append("\n");
	  
	  // Lastblip exists because of indentation problem
	  Blip lastblip = rootblip.reply();
	  lastblip.append("Visit us at ");
	  startindex = lastblip.length();
	  lastblip.append("http://www.resourcy.com  ");
	  lastblip.range(startindex,lastblip.length()-2).annotate("link/manual", "http://www.resourcy.com");
	  lastblip.append("or follow @resourcy on Twitter  \n\n\n");
      HashMap<String, String> imageProps = new HashMap<String, String>();
      imageProps.put("width", "120");
      imageProps.put("height", "30");
      imageProps.put("url", "http://code.google.com/appengine/images/appengine-silver-120x30.gif");
      lastblip.append(new Element(ElementType.IMAGE, imageProps));
	  lastblip.append(" ");
	  lastblip.append(new FormElement(ElementType.BUTTON, "feedback_button", "Send Us Feedback"));
	  lastblip.append(new FormElement(ElementType.BUTTON, "blogpost_button", "New Blog Post"));

	               
	  /*  can't insert a hyperlinked image yet.
	   * 
	  lastblip.range(startindex,lastblip.length()-2).annotate("link/manual", "http://www.resourcy.com");
	  lastblip.append("or ");
	  startindex = lastblip.length();
      HashMap<String, String> imageProps = new HashMap<String, String>();
      imageProps.put("width", "100");
      imageProps.put("height", "75");
      imageProps.put("url", "http://i909.photobucket.com/albums/ac295/fractastical/twitter3.png");
      lastblip.append(new Element(ElementType.IMAGE, imageProps));
      lastblip.append("  ");
	  lastblip.range(startindex,lastblip.length()-2).annotate("link/manual", "http://www.twitter.com/resourcy");

	   */

	  ArrayList<Category__c> categories = getCategories();
	  if (categories.size() > 1)
	  {
		  
		rootblip.append("\nPlease select the type of resources you are interested in:\n");
		rootblip.append(new FormElement(ElementType.RADIO_BUTTON_GROUP, "cat_group"));

	    for (int i = 0; i < categories.size(); i++) {
			 LOG.log(Level.WARNING, "I:" + i );

			 rootblip.append(new FormElement(ElementType.RADIO_BUTTON, categories.get(i).getName(), "cat_group"));
			 rootblip.append(new FormElement(ElementType.LABEL, categories.get(i).getName(), categories.get(i).getName()));
			 rootblip.append("\n");
			 LOG.log(Level.WARNING, "Radio Button created w/ name: " +categories.get(i).getName());

	    }
	    
	    rootblip.append("\n");
		rootblip.append(new FormElement(ElementType.BUTTON, "select_button", "Show resources in this category"));
		//createBlipsWithCategory(wavelet, rootblip, "Salesforce Chatter");

		  
	  }
	  else if (categories.size() == 1)
	  {
		  createBlipsWithCategory(wavelet, rootblip, categories.get(0).getName());
		  
	  }
	  else
		  rootblip.append("\nNo Categories Found. Please contact Resourcy Admin at d3developer.com");

	  rootblip.append("\n");

}
  

  @Override
  //TODO: narrow context
  @Capability(contexts = {Context.ALL})
  public void onWaveletCreated(WaveletCreatedEvent event) {
	
	Wavelet newWavelet = event.getWavelet();
	Blip rootblip = newWavelet.getRootBlip();
	
	String resourceid = "";
	String originatingBlipID = "";
	String originatingWaveId = "";
	String originatingWaveletId = "";
	String originatingDomain = "";
	
    for(Annotation a : rootblip.getAnnotations()) 
    {
    	LOG.log(Level.WARNING, "AN: " + a.getName() + " AV: " + a.getValue());
    	if(a.getName().equals("resourcy.appspot.com/ORIGINATING_BLIP"))
    		originatingBlipID = a.getValue();
    	if(a.getName().equals("resourcy.appspot.com/ORIGINATING_DOMAIN"))
    		originatingDomain = a.getValue();
    	if(a.getName().equals("resourcy.appspot.com/RESOURCEID"))
    		resourceid = a.getValue();
    	if(a.getName().equals("resourcy.appspot.com/ORIGINATING_WAVE"))
    		originatingWaveId = a.getValue();
    	if(a.getName().equals("resourcy.appspot.com/ORIGINATING_WAVELET"))
    		originatingWaveletId = a.getValue();
    }
    
    if ( (!resourceid.equals("")) && (!originatingBlipID.equals(""))  && (!originatingWaveId.equals(""))  && (!originatingWaveletId.equals(""))  && (!originatingDomain.equals("")) )
    {
	    WaveId oWaveId = new WaveId(originatingDomain, originatingWaveId);	 
	    WaveletId oWaveletId = new WaveletId(originatingDomain, originatingWaveletId);
	    Resource__c res = getResource(resourceid);
	    res.setWaveId__c(newWavelet.getWaveId().getId());
		res.setWaveletId__c(newWavelet.getWaveletId().getId());
	
		//We add the new waveId to the resource
		
		String response = updateResource(res);

		Wavelet originatingWavelet = this.blindWavelet(oWaveId,oWaveletId);

	    Blip blipWeAreCommentingOn = originatingWavelet.getBlip(originatingBlipID);

	    Map<String,Blip> bco = originatingWavelet.getBlips();
		LOG.log(Level.WARNING, "this many blips:" + bco.size());
		Blip testBlip = bco.get(originatingBlipID);
	    Blip reply = blipWeAreCommentingOn.reply();
		
    	Gadget gadget = new Gadget(EMBED_COMMENT_WAVE_URL);
       	
		LOG.log(Level.WARNING, "Embedding gadget with Wave id: " + newWavelet.getWaveId().getId());

	    gadget.setProperty("waveid", newWavelet.getWaveId().serialise()); 
	    reply.append(gadget);
	    blipWeAreCommentingOn.first(ElementType.BUTTON, FormElement.restrictByName("comment_button")).delete();
		reply.append(response);
		
    }


  }
  
  
	@Capability(contexts = {Context.ALL})
	@Override
	public void onFormButtonClicked(FormButtonClickedEvent e){
		LOG.info("onFormButtonClicked");
		Blip blip = e.getBlip();
		Wavelet originalWavelet = e.getWavelet();
	    String buttonName = e.getButtonName();
    	LOG.log(Level.WARNING, "Button:" + buttonName);

		String category = "";
		String resourceid = "";
		String oldLikeCount = "";
	    for(Annotation a : blip.getAnnotations()) 
	    {
	    	LOG.log(Level.WARNING, "AN: " + a.getName() + " AV: " + a.getValue());
	    	if(a.getName().equals("resourcy.appspot.com/CATEGORY"))
	    		category = a.getValue();
	    	if(a.getName().equals("resourcy.appspot.com/RESOURCEID"))
	    		resourceid = a.getValue();
	    	if(a.getName().equals("resourcy.appspot.com/LIKECOUNT"))
	    		oldLikeCount = a.getValue();

	    }
	    	    
		if(buttonName.equals("add_resource_button")) {
			
			Set<String> participants = new HashSet<String>();
			participants.add(e.getModifiedBy());
			if(category.equals("")) 
			{
				SortedMap<Integer, Element> elements = blip.getElements();
				LOG.log(Level.WARNING, "Searching for category");

				//LOG.log(Level.WARNING, "CAT is: " + elements.get("cat_group").getProperty("value").toString());

				for(Integer key:elements.keySet()){
					Element elem = elements.get(key);
					if(elem.isFormElement()){

						if(elem.getProperty("name").equals("cat_group"))
								category = elem.getProperty("value");

					}
				}
			}
			
			createAddResourceWavelet(originalWavelet, category, participants);
			
		} // add resource button
		else if(buttonName.equals("feedback_button"))
		{
    		  blip.first(ElementType.BUTTON, FormElement.restrictByName("feedback_button")).delete();
		  	  Gadget gadget = new Gadget(FEEDBACK_FORM_URL);
		      blip.append(gadget);
		}
		else if(buttonName.equals("blogpost_button"))
		{
    		  blip.first(ElementType.BUTTON, FormElement.restrictByName("blogpost_button")).delete();
		  	  Gadget gadget = new Gadget(BLOGPOST_FORM_URL);
		      blip.append(gadget);
		}

		else if(buttonName.equals("submit_button")) {
			
			processSubmitForm(blip, originalWavelet.getWaveId().getId(), originalWavelet.getWaveletId().getId());

		} 
		else if(buttonName.equals("comment_button")) {
			
			Set<String> participants = new HashSet<String>();
			participants.add(e.getModifiedBy());
			createCommentWavelet(originalWavelet, blip, participants, resourceid);

		} 
		else if(buttonName.equals("like_button")) {
			
			Integer newLikeCount = Utilities.submitLike(e.getModifiedBy(),resourceid);
			if(!oldLikeCount.equals(newLikeCount.toString()))
			{
			 blip.range(blip.length() - 32, blip.length()).delete();
			 blip.append(Utilities.genLikeString(newLikeCount));
			 blip.all().clearAnnotation("resourcy.appspot.com/LIKECOUNT");
			 blip.all().annotate("resourcy.appspot.com/LIKECOUNT", newLikeCount.toString());
			}
			else
			{
				Blip reply = blip.reply();
				reply.append("You already submitted your vote for this item.");
				reply.all().annotate("resourcy.appspot.com/IS_TO_DELETE", "TRUE");
				//Integer sindex = blip.length();
				//blip.append("You already submitted your vote for this item.");
				//blip.range(sindex,blip.length()).annotate("resourcy.appspot.com/IS_TO_DELETE", "TRUE");
				
				/*try{ 
					List <JsonRpcResponse> jrrl  = this.submit(originalWavelet, RPC_SERVER_URL);
						for (JsonRpcResponse jrr : jrrl) {
							LOG.log(Level.WARNING, "JRRL size :" + jrrl.size());

							Map<ParamsProperty, Object> m = jrr.getData();	
							if(m!=null)
							{
								LOG.log(Level.WARNING, "Map size :" + m.size());
						    	for (Object o : m.values())
						    	{
						    		LOG.log(Level.WARNING, "JRR has value: " + ((String) o));
						    	}
							}
					    	
					    }

				}
				catch (IOException ex) {
		    		LOG.log(Level.WARNING, "JRRL crawl failed: " + ex); 

				}*/
			    Queue queue = QueueFactory.getDefaultQueue();
			    queue.add(url(DUMMY_PATH));
			    queue.add(url(DUMMY_PATH));
			    queue.add(url(DUMMY_PATH));
			    queue.add(url(DELETE_BLIP_PATH).param("waveid", originalWavelet.getWaveId().getId()).param("waveletid", originalWavelet.getWaveletId().getId()).param("domain",originalWavelet.getDomain()));
			    
			}

		} 

		else if(buttonName.equals("hide_button")) {
			
			
			List<Blip> sibs = blip.getParentBlip().getChildBlips();
			LOG.log(Level.WARNING, "Total children:" + sibs.size());

			Blip parentBlip = blip.getParentBlip();

			for (Blip s : sibs)
			{
				List<String> kidids =	s.getChildBlipIds();
				LOG.log(Level.WARNING, "Attempting to hide BlipId S:" + s + " with parent: " + parentBlip.getBlipId());

				for(String k : kidids)
				{
					LOG.log(Level.WARNING, "Attempting to hide BlipId K:" + k + " with parent: " + s);
					originalWavelet.delete(k);
				}
				originalWavelet.delete(s);


			}
			originalWavelet.delete(parentBlip);


			//LOG.log(Level.WARNING, "Total children now:" + sibs.size());

//			for(Blip b: )
			//originalWavelet.delete(toDelete);

				//originalWavelet.delete(s);
			
		
		} 

		else if(buttonName.equals("select_button")) {
			
			processCatSelect(originalWavelet, blip);
			
		} 

}

@Override
@Capability(contexts = {Context.SELF, Context.ROOT}) 
public void onBlipSubmitted(BlipSubmittedEvent e)
{
	LOG.log(Level.WARNING, "BlipSubmitted");
	Blip blip = e.getBlip();

    String isToDelete = "FALSE";
    for(Annotation a : blip.getAnnotations())
    {
     LOG.log(Level.WARNING, "Blip1 AN: " + a.getName() + " AV: " + a.getValue());
     if(a.getName() == "resourcy.appspot.com/IS_TO_DELETE")
    	 isToDelete = a.getValue();
    }
    
}
	
@Override
 @Capability(contexts = {Context.SELF, Context.ROOT}) 
 public void onWaveletBlipCreated(WaveletBlipCreatedEvent e) {
	
	LOG.log(Level.WARNING, "WaveletBlipCreatedEvent"); 
    Blip blip = e.getNewBlip();
	LOG.log(Level.WARNING, "New Blip content " +  blip.getContent()); 
	Blip blip2 = e.getBlip();
	LOG.log(Level.WARNING, "Other Blip content " +  blip2.getContent()); 

   
    String isToDelete = "FALSE";
    for(Annotation a : blip.getAnnotations())
    {
     LOG.log(Level.WARNING, "AN: " + a.getName() + " AV: " + a.getValue());
     if(a.getName() == "resourcy.appspot.com/IS_TO_DELETE")
    	 isToDelete = a.getValue();
    }
    if(isToDelete.equals("TRUE"))
    {
	    Wavelet originalWavelet = e.getWavelet();
	    Queue queue = QueueFactory.getDefaultQueue();
	    // Default queue is set to one sec per task so we load two add. tasks in order to get to a ~2 sec delay
	    queue.add(url("/dummy")); 
	    queue.add(url("/dummy"));
	    queue.add(url("/dummy"));
	    queue.add(url(DELETE_BLIP_PATH).param("waveid", originalWavelet.getWaveId().getId()).param("waveletid", originalWavelet.getWaveletId().getId()).param("blipid", blip.getBlipId()).param("domain",originalWavelet.getDomain()));

    }     	

  }
  
  public ArrayList<Resource__c> getResources(String category) {
	  ArrayList<Resource__c> myResources = new ArrayList<Resource__c>();

	  try {
		   if ( connection == null ) 
		   {
			   //ConnectorConfig config = new ConnectorConfig();
		       //config.setUsername(username);
		       //config.setPassword(password);
		       connection = cm.getConnection(username, password);
		   }
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
  
  public Resource__c getResource(String id) {
	  
	  Resource__c myResource = new Resource__c();

	  try {
		   if ( connection == null ) 
		   {
			   connection = cm.getConnection(username, password);
		   }
		   QueryResult result = null;
		   
		    // We should escape category against SOQL injection ? 
		   	// This needs to be a chatter enabled object
		   	String queryString = "SELECT r.Id, r.WaveId__c, r.WaveletId__c from Resource__c r where id = '" + id + "' limit 1 ";
		   	LOG.log(Level.WARNING, "QS:" + queryString);
		   	
		   	result = connection.query(queryString);
		   			
	        if(result.getRecords().length > 0) 
	        	myResource = (Resource__c)result.getRecords()[0];

	  } catch (ConnectionException ce) {
		  LOG.log(Level.WARNING, "Connection Error:" + ce.getMessage() + ":" + ce.getClass());
	  }
	 // LOG.log(Level.WARNING,"Resources found. Size:" + myResources.size());
	  return myResource;
}  

  

public ArrayList<Category__c> getCategories() {
	 
	  ArrayList<Category__c> myCategories = new ArrayList<Category__c>();

	  try {
		   if ( connection == null ) 
		   {
			   ConnectorConfig config = new ConnectorConfig();
		       config.setUsername(username);
		       config.setPassword(password);
		       connection = Connector.newConnection(config);
		   }
		   QueryResult subCatResult = null;
		   QueryResult result = null;
		   
		   // What we wish we could do:
		   //  (select Category__c from Subcategory__c where id in 
		   // (select Subcategory__c from Resource__c where approved__c = true))
		   //  order by name limit 10
		   //
		   
		   	// This needs to be a chatter enabled object
		   String subcatQueryString = "SELECT Category__c from SubCategory__c where id in (select Subcategory__c from Resource__c where approved__c = true) limit 100";
	       
		   LOG.log(Level.WARNING, "SubCat Query String:" + subcatQueryString);

		   subCatResult = connection.query(subcatQueryString);
		   
		    
		    String catQueryString = "SELECT Id, Name from Category__c";
		    Boolean firstTime = true;
	        for (int i=0; i < subCatResult.getRecords().length;i++) 
	        {
	        	if (firstTime)
	        	{
	        		catQueryString += " where id = '";
	        		firstTime = false;
	        	}
	        	else
	        		catQueryString += " or id = '";
	        	
	        	Subcategory__c subcat = (Subcategory__c)subCatResult.getRecords()[i];
	        	catQueryString += subcat.getCategory__c() + "'";

	        }
	        catQueryString += " limit 15";
	
	        LOG.log(Level.WARNING, "Cat Query String:" + catQueryString);


		   	result = connection.query(catQueryString);		   	

	        for (int i=0; i < result.getRecords().length;i++) 
	        {
	        	Category__c category = (Category__c)result.getRecords()[i];
	        	myCategories.add(category);
	        } 

	  } catch (ConnectionException ce) {
		  //ArrayList <String> errorMsg = new ArrayList <String>();
		  LOG.log(Level.WARNING, "Connection Error:" + ce.getMessage() + ":" + ce.getClass());
	  }
	  return myCategories;
}  


/**
 * Creates a hashmap with the name and value properties
 * @param name data to put in name field
 * @param value data to put in value field
 */
private HashMap<String, String> createProperties(String name, String value) {
	HashMap<String, String> properties = new HashMap<String, String>();
	properties.put("name", name);
	properties.put("value", value);
	return properties;
}


@Override
@Capability(contexts = {Context.SELF})
public void onGadgetStateChanged(GadgetStateChangedEvent e) {
	  	LOG.log(Level.WARNING, "OnGadgetStateChanged");
	  	Blip blip = e.getBlip(); 
	  	
	  	Gadget gadget = Gadget.class.cast(blip.first(ElementType.GADGET).value());
	  	
	  	LOG.log(Level.WARNING, "just looked for the gadget");

	    if(gadget!=null)
	    {
		  	LOG.log(Level.WARNING, "Found Gadget");
		  	if (gadget.getProperties() != null)
			  	LOG.log(Level.WARNING, "Gadget has properties");
	    }


	    
	    if (gadget != null &&
	            gadget.getProperty("sendemail", "no").equals("yes") &&
	            	gadget.getProperty("sentemail", "no").equals("no") ) {
	    	
			    String name = "";
			    String email = "";
			    String url = "";
			    String comment = ""; 

		  	    LOG.log(Level.WARNING, "found sendemail");

		    	name = gadget.getProperty("name", "");
		    	email = gadget.getProperty("email", "");
		    	url = gadget.getProperty("url", "");
		    	comment = gadget.getProperty("comment", "");

	          Boolean success = sendEmail(name, email, url, comment);
	          
	          if (success)
	        	  blip.first(ElementType.GADGET).updateElement(
		              ImmutableMap.of("sentemail", "yes")); 
	          else
	        	  blip.first(ElementType.GADGET).updateElement(
			              ImmutableMap.of("emailfailure", "yes")); 

	        }
	    else if (gadget != null &&
	            gadget.getProperty("sendblogpost", "no").equals("yes") ) {
	    	
		  	    LOG.log(Level.WARNING, "found sendblogpost");
			    String title = "";

		    	title = gadget.getProperty("title", "");

	           Boolean success = Utilities.tumblrBlogPost(title);
	          
	          
	          if (success)
	        	  blip.first(ElementType.GADGET).updateElement(
		              ImmutableMap.of("blogpostsuccess", "yes")); 
	          else
	        	  blip.first(ElementType.GADGET).updateElement(
			              ImmutableMap.of("blogpostfailure", "yes")); 
	          
	        }

	    
}


public Boolean sendEmail(String name, String email, String url, String comment) {
	
	String recipientAddress = "joel.anselm.dietz@gmail.com";
	
	Properties props = new Properties();
	Session session = Session.getDefaultInstance(props,null);
		
	try {
		Message message = new MimeMessage(session);
		// This address must be registered as a 'developer' with Google App Engine 
		message.setFrom(new InternetAddress("jdietz@gmail.com",
				name));
		message.addRecipient(Message.RecipientType.TO,
				new InternetAddress(recipientAddress));
		message.setSubject("Feedback Message from " + name);
		String body;
		if (url!=null && url.length() > 3)
			body = name + " (" + email + ")\n" + "from " + url + " \nsends the following comment: \n" + comment; 
		else
			body = name + " (" + email + ") " + " \nsends the following comment: \n" + comment; 
			
		message.setText(body);
		Transport.send(message);
		return true;
		
	} catch (AddressException e) {
		LOG.log(Level.WARNING, "Address is malformed: " + e);
		
	} catch (MessagingException e) {
		LOG.log(Level.WARNING, "Problem with mail service: " + e);

	} catch (UnsupportedEncodingException e) {
		LOG.log(Level.WARNING, "Problem with encoding: " + e);

	}
	
	return false;
}



private void createAddResourceWavelet(Wavelet originalWavelet, String category, Set<String> participants)
{
	participants.add("public@a.gwave.com");

	Wavelet newWavelet = this.newWave(originalWavelet.getDomain(),
												participants);
	// change focus to new form
	
	Blip subForm = newWavelet.getRootBlip();
	LOG.log(Level.WARNING, "Creating Submit form");

	subForm.append("Resource Submission Form for Category " + category);
				
	subForm.append("\n");
	subForm.append(new FormElement(ElementType.LABEL, "text_input_lab1", "Resource Title:"));
	subForm.append(new FormElement(ElementType.INPUT, "text_input_title",""));
	
	ArrayList<Subcategory__c> subcs = getSubcategories(category);
	subForm.append("\n");
	subForm.append(new FormElement(ElementType.LABEL, "Subcategory", "Subcategory:"));
	subForm.append("\n");
	subForm.append(new FormElement(ElementType.RADIO_BUTTON_GROUP,"subcat_group"));
	for(Subcategory__c subc : subcs)
	{
		subForm.append(new FormElement(ElementType.RADIO_BUTTON, subc.getId(), "subcat_group"));
		subForm.append(new FormElement(ElementType.LABEL, subc.getName(), subc.getName()));
		subForm.append("\n");
		
		LOG.log(Level.WARNING, "Label:" + subc.getName() + " RB: " + subc.getId());
	}


	
	subForm.append("\n");
	subForm.append(new Element(ElementType.LABEL,
			createProperties("text_input_lab4", "URL Title:")));
	subForm.append(new Element(ElementType.INPUT,
			createProperties("text_input_source",
					"")));

	subForm.append("\n");
	subForm.append(new Element(ElementType.LABEL,
			createProperties("text_input_lab5", "URL:")));
	subForm.append(new Element(ElementType.INPUT,
			createProperties("text_input_url",
					"")));

	subForm.append("\n");
	subForm.append(new Element(ElementType.LABEL,
			createProperties("text_input_lab6", "Description:")));
	subForm.append(new Element(ElementType.INPUT,
			createProperties("text_input_desc",
					"")));
	
	subForm.append("\n");
	subForm.append(new Element(ElementType.LABEL,
			createProperties("text_input_lab7", "Your name:")));
	subForm.append(new Element(ElementType.INPUT,
			createProperties("text_input_sub_name",
					"")));

	/*subForm.append("\n");
	subForm.append(new Element(ElementType.LABEL,
			createProperties("text_input_lab8", "Your email (if you wish to be notified):")));
	subForm.append(new Element(ElementType.INPUT,
			createProperties("text_input_email",
					""))); */
	
	subForm.append(new Element(ElementType.BUTTON,
				createProperties("submit_button", "Submit")));
	
	//if(category != "")
	//	subForm.all().annotate("resourcy.appspot.com/CATEGORY", category);

	LOG.log(Level.WARNING, "Creating new Wavelet");

	newWavelet.submitWith(originalWavelet);

	}
	private void createBlipsWithCategory(Wavelet wavelet, Blip rootblip, String category)
	{
		rootblip.append(" ");
		rootblip.range(0, rootblip.length()-1).delete();
		rootblip.append("\nCurrent Category is: " + category + "\n\n");
		rootblip.append(new Element(ElementType.BUTTON,
					createProperties("add_resource_button", "Add New Resource to this Category")));
		rootblip.all().annotate("resourcy.appspot.com/CATEGORY", category);

		Utilities.outputResourcesWithLike(wavelet, rootblip, category);

	}
	
private void processSubmitForm(Blip blip, String currWaveId, String currWaveletId)
	{
		Resource__c res = new Resource__c();
		SortedMap<Integer, Element> elements = blip.getElements();
		HashMap<String, String> responses = new HashMap<String, String>();
		LOG.log(Level.WARNING, "Processing Submit form");

		for(Integer key:elements.keySet()){
			Element elem = elements.get(key);
			if(elem.isFormElement()){
				if(elem.getType() != ElementType.LABEL &&
						elem.getType() != ElementType.BUTTON) {
					responses.put(	elem.getProperty("name"),
									elem.getProperty("value"));
					
					if(elem.getProperty("name").equals("text_input_title"))
						res.setName(elem.getProperty("value"));
					if(elem.getProperty("name").equals("text_input_url"))
						res.setUrl__c(elem.getProperty("value"));
					if(elem.getProperty("name").equals("text_input_desc"))
						res.setDescription__c(elem.getProperty("value"));
					if(elem.getProperty("name").equals("subcat_group"))
					{
						LOG.log(Level.WARNING, "Subcat:" + elem.getProperty("value"));
						res.setSubcategory__c(elem.getProperty("value"));
					}
					if(elem.getProperty("name").equals("text_input_source"))
						res.setSource__c(elem.getProperty("value"));
					if(elem.getProperty("name").equals("text_input_sub_name"))
						res.setSubmitterName__c(elem.getProperty("value"));
					
					res.setWaveId__c(currWaveId);
					res.setWaveletId__c(currWaveletId);
					

				}
			}
		}
		String response = insertNewResource(res);
		blip.all().delete();
		if (res.getSubmitterName__c() != null )
		{
			blip.append("Dear " + res.getSubmitterName__c() + ", " + response);
			blip.all().annotate("resourcy.appspot.com/IS_SUB_NOTICE","TRUE");
		}
		else blip.append(response);

	}

private void createCommentWavelet(Wavelet originalWavelet, Blip blip, Set<String> participants, String resourceId)
{
    WaveId waveId; 
    String waveIdString = "";

	for(Annotation a : blip.getAnnotations())
		if(a.getName().equals("resourcy.appspot.com/COMMENT_WAVE_ID"))
			waveIdString = a.getValue();
	
		
    // this means that there is no associated wave with this resource. we must create one.
    if (waveIdString == "")
    {
    	Resource__c res = getResource(resourceId);
    	if(res.getWaveId__c() == null || res.getWaveId__c().length() < 4 || res.getWaveletId__c().substring(0,2).equals("TBD"))
    	{
		   	LOG.log(Level.WARNING, "No wave ID found. Creating new wave.");
		   	participants.add("public@a.gwave.com");
			Wavelet newWavelet = this.newWave(originalWavelet.getDomain(),participants);
			newWavelet.getRootBlip().all().annotate("resourcy.appspot.com/ORIGINATING_BLIP", blip.getBlipId());
			newWavelet.getRootBlip().all().annotate("resourcy.appspot.com/RESOURCEID", resourceId);
			newWavelet.getRootBlip().all().annotate("resourcy.appspot.com/ORIGINATING_DOMAIN", blip.getWaveId().getDomain());
			newWavelet.getRootBlip().all().annotate("resourcy.appspot.com/ORIGINATING_WAVE", blip.getWaveId().getId());
			newWavelet.getRootBlip().all().annotate("resourcy.appspot.com/ORIGINATING_WAVELET", blip.getWaveletId().getId());
			newWavelet.getRootBlip().append("\nComment here:");
			newWavelet.submitWith(originalWavelet);
	
    	}
    	else
    	{
    		LOG.log(Level.WARNING, "We have a waveid saved in the resource:" + res.getWaveId__c());
        	waveId = new WaveId(originalWavelet.getDomain(), res.getWaveId__c());
        	Gadget gadget = new Gadget("http://resourcy.appspot.com/embed.xml");
    	   	LOG.log(Level.WARNING, "Embedding gadget with Wave id: " + waveId.getId());
            gadget.setProperty("waveid", waveId.serialise());
            blip.reply().append(gadget);
    		blip.first(ElementType.BUTTON, FormElement.restrictByName("comment_button")).delete();
    	}
    }
    else  // we already have an associated wave. we open it.
    {
	   	LOG.log(Level.WARNING, "We have a waveid from the annotation:" + waveIdString);
	   	
    	waveId = new WaveId(originalWavelet.getDomain(), waveIdString);   	
    	Gadget gadget = new Gadget("http://resourcy.appspot.com/embed.xml");
	   	LOG.log(Level.WARNING, "Embedding gadget with Wave id: " + waveId.getId());
        gadget.setProperty("waveid", waveId.serialise());
        blip.reply().append(gadget);
		blip.first(ElementType.BUTTON, FormElement.restrictByName("comment_button")).delete();

    }

	
}

private void processCatSelect(Wavelet originalWavelet, Blip blip)
{
	LOG.log(Level.WARNING, "Process Cat Select");
	Resource__c res = new Resource__c();
	SortedMap<Integer, Element> elements = blip.getElements();
	LOG.log(Level.WARNING, "EL SIZE:" + elements.size());
	
	for(Integer key:elements.keySet()){
		Element elem = elements.get(key);
		if(elem.isFormElement()){

				LOG.log(Level.WARNING,  "EL NAME:" + elem.getProperty("name") + " EL VAL: " + elem.getProperty("value"));
				if(elem.getProperty("name").equals("cat_group"))
				{
					LOG.log(Level.WARNING,  "SELECTED CATEGORY:" + elem.getProperty("value"));
					createBlipsWithCategory(originalWavelet, blip, elem.getProperty("value"));

				}
			
		}
	
	} 

	
} 
public ArrayList<Subcategory__c> getSubcategories(String category) {
	 
	  ArrayList<Subcategory__c> mySubcategories = new ArrayList<Subcategory__c>();

	  try {
		   if ( connection == null ) 
		   {
			   ConnectorConfig config = new ConnectorConfig();
		       config.setUsername(username);
		       config.setPassword(password);
		       connection = Connector.newConnection(config);
		   }
		   QueryResult result = null;
		   
		   	// This needs to be a chatter enabled object
		   	result = connection.query( "SELECT Id, Name from Subcategory__c where Category__r.name ='" + category + "' " +" order by name limit 10");

	        for (int i=0; i < result.getRecords().length;i++) 
	        {
	        	Subcategory__c subcat = (Subcategory__c)result.getRecords()[i];
	        	mySubcategories.add(subcat);
	        } 

	  } catch (ConnectionException ce) {
		  //ArrayList <String> errorMsg = new ArrayList <String>();
		  LOG.log(Level.WARNING, "Connection Error:" + ce.getMessage() + ":" + ce.getClass());
	  }
	  return mySubcategories;
}  

public String insertNewResource(Resource__c res) {
	  String outputString = "";
	  try {
		   if ( connection == null ) {
		    ConnectorConfig config = new ConnectorConfig();
		       config.setUsername(username);
		       config.setPassword(password);
		       connection = Connector.newConnection(config);
		   }
	  } catch (ConnectionException ce) {
		  outputString += ce.getMessage() + ":" + ce.getClass();
	  }
	  try { 
		  SaveResult sr[] = connection.create(new SObject[] { (SObject)res });
		  LOG.log(Level.WARNING, "Save Results: " + sr.toString());
		  outputString = "Resource submitted successfully. Please wait for additional feedback";
		} 
		catch ( ConnectionException ce ) { 
			outputString += ce.getMessage(); 
		   	LOG.log(Level.WARNING, "Inserting new resource with message:" + outputString);

		}
		   
	return outputString;
	
}



// called for adding WaveIds to existing resources
private String updateResource(Resource__c res) {
	  String outputString = "";
	  try {
		   if ( connection == null ) {
		    ConnectorConfig config = new ConnectorConfig();
		       config.setUsername(username);
		       config.setPassword(password);
		       connection = Connector.newConnection(config);
		   }
	  } catch (ConnectionException ce) {
		  outputString += ce.getMessage() + ":" + ce.getClass();
	  }
	  try { 
		  connection.update(new SObject[] {(SObject)res });
		   	LOG.log(Level.WARNING, "Updated resource ");

		  //outputString = "Resource updated successfully. Please wait for additional feedback";
		} 
		catch ( ConnectionException ce ) { 
			outputString += ce.getMessage(); 
		   	LOG.log(Level.WARNING, "Inserting new resource with message:" + outputString);

		}
		   
	return outputString;
	
}


}
