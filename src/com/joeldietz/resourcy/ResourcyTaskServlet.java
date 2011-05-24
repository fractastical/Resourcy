package com.joeldietz.resourcy;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;

import com.google.appengine.api.images.ImagesServicePb.ImagesServiceTransform.Type;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.wave.api.AbstractRobot;
import com.google.wave.api.Annotation;
import com.google.wave.api.Blip;
import com.google.wave.api.ElementType;
import com.google.wave.api.FormElement;
import com.google.wave.api.Wavelet;
import com.joeldietz.resourcy.models.WaveActionResponse;
import com.joeldietz.resourcy.models.WaveActionType;
import com.joeldietz.resourcy.utilities.TestUtils;
import com.joeldietz.resourcy.utilities.Utilities;
import com.sforce.soap.enterprise.sobject.Resource__c;


public class ResourcyTaskServlet extends AbstractRobot {
	
	 private static final Logger LOG = Logger.getLogger(ResourcyServlet.class.getName());

	 private static String DUMMY_PATH = null;
	 private static String CREATE_WAVE_PATH = null;
	 private static String ADD_RESOURCE_PATH = null;
	 private static String RELOAD_WAVE_PATH = null;
	 private static String DELETE_BLIP_PATH = null;
	 private static String FEED_POST_PATH = null;
	 
	 private static String ConsumerKey = null;
	 private static String ConsumerSecret = null;
	 
	 private static String RPC_SERVER_URL = null;
	  
	  public ResourcyTaskServlet()
	  {
		  	ConsumerKey = System.getProperty("ConsumerKey");
		  	ConsumerSecret = System.getProperty("ConsumerSecret");
		  	DUMMY_PATH = System.getProperty("DUMMY_PATH");
		  	CREATE_WAVE_PATH = System.getProperty("CREATE_WAVE_PATH");
		  	ADD_RESOURCE_PATH = System.getProperty("ADD_RESOURCE_PATH");
		  	RELOAD_WAVE_PATH = System.getProperty("RELOAD_WAVE_PATH");
		  	DELETE_BLIP_PATH = System.getProperty("DELETE_BLIP_PATH");
		  	FEED_POST_PATH = System.getProperty("FEED_POST_PATH");
		  	RPC_SERVER_URL = System.getProperty("RPC_SERVER_URL");
		  	
		    setupOAuth(ConsumerKey, ConsumerSecret, RPC_SERVER_URL);
		    setAllowUnsignedRequests(true);	  
	  }

	 @Override
	 protected void doPost(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse resp) 
	  {
		    LOG.log(Level.WARNING, "Post request received to ResourcyTaskServlet. Path: " + req.getServletPath());
		
		    if (req.getServletPath().equals(DELETE_BLIP_PATH)) {
				String waveId =  req.getParameter("waveid");
				String waveletId = req.getParameter("waveletid");
				String domain =  req.getParameter("domain");
				String blipid = req.getParameter("blipid");
				
			    LOG.log(Level.WARNING, "wid: " + waveId + " waveletId: " + waveletId + " domain: " + domain + " blipid: " + blipid);
			    
				try {
					//Wavelet wavelet = this.blindWavelet(new WaveId(waveId, domain), new WaveletId(waveletId, domain));
					Wavelet wavelet = this.fetchWavelet(new WaveId(domain, waveId), new WaveletId(domain, waveletId), RPC_SERVER_URL);
			    	TestUtils.logWaveAction(WaveActionType.FETCH_WAVELET, WaveActionResponse.SUCCESS);

					//if(blipid != null && (!blipid.equals("null")) && blipid.length() > 3 && (!blipid.substring(0, 2).equals("TBD"))) {
					//   wavelet.delete(blipid);
					//}
					//else {

					//Ugly and slow way of doing this
					Map<String,Blip> myBlips = wavelet.getBlips();
					LOG.log(Level.WARNING, "We've got "  + myBlips.size() + " Blips to iterate through.");
				    Collection<Blip> bs = myBlips.values();
				    ArrayList <String> blipsToDelete = new ArrayList<String>();
				    
				    /*Blip toDelete = bs.
				    Integer startIndex = 0;
				    Integer endIndex = 0;

				    for (Blip b : bs)
					     
				    {
					    for(Annotation a : b.getAnnotations()) 
					    {
					    	LOG.log(Level.WARNING, "AN: " + a.getName() + " AV: " + a.getValue());
					    	if(a.getName().equals("resourcy.appspot.com/IS_TO_DELETE") && a.getValue().equals("TRUE"))
					    	{
					    		startIndex = a.getRange().getStart();
					    		endIndex = a.getRange().getEnd();
					    		toDelete = b;
					    		break;
					    	}
					    }
				    }
				    toDelete.range(startIndex, endIndex).delete();*/

				    for (Blip b : bs)
				     
				    {
					    for(Annotation a : b.getAnnotations()) 
					    {
					    	LOG.log(Level.WARNING, "AN: " + a.getName() + " AV: " + a.getValue());
					    	if(a.getName().equals("resourcy.appspot.com/IS_TO_DELETE") && a.getValue().equals("TRUE"))
					    		blipsToDelete.add(b.getBlipId());
					    }
				    }
				    
				    
				    for (String btd : blipsToDelete) {
				    	LOG.log(Level.WARNING, "Attempting to delete blip w/ the id: " + btd);
				    	wavelet.delete(btd);
				    }
				    
				    this.submit(wavelet, RPC_SERVER_URL); 
										
				 }  
				catch (IOException e) {

			    	TestUtils.logWaveAction(WaveActionType.FETCH_WAVELET, WaveActionResponse.FAILURE);
				    Queue queue = QueueFactory.getQueue("defer");
				    queue.add(url(DELETE_BLIP_PATH).param("waveid", waveId).param("waveletid", waveletId).param("domain",domain));
  
				}
				catch (Exception e) {
					LOG.log(Level.WARNING, "Error in deleting: " + e);					
				}
		    }
		    else if (req.getServletPath().equals(FEED_POST_PATH))
		    {
				String body =  req.getParameter("body");	
		    	String waveId =  req.getParameter("waveid");
				String waveletId = req.getParameter("waveletid");

				String domain = "googlewave.com";
				waveId = waveId.replace(' ', '+');
				waveletId = waveletId.replace(' ', '+');

			    LOG.log(Level.WARNING, "wid: " + waveId + " waveletId: " + waveletId + " domain: " + domain + " content: " + body);
				
			    try {
			    	
			    	Wavelet wavelet = this.fetchWavelet(new WaveId(domain, waveId), new WaveletId(domain, waveletId), RPC_SERVER_URL);
			    	TestUtils.logWaveAction(WaveActionType.FETCH_WAVELET, WaveActionResponse.SUCCESS);

			    	LOG.log(Level.WARNING, "fetchWavelet");
			    	//Wavelet wavelet = this.blindWavelet(new WaveId(domain,waveId), new WaveletId(domain,waveletId));
			    	//LOG.log(Level.WARNING, "blindWavelet");
			    	Map<String,Blip> m = wavelet.getBlips();
			    	Set<String> s = m.keySet();
			    	Iterator iter = s.iterator();
			    	
			    	while(iter.hasNext())
			    	{	
			    		String blipid = (String) iter.next();
			    		Blip b = wavelet.getBlip(blipid);
			    		Boolean isSubmitNotice = false;
			    		
					    for(Annotation a : b.getAnnotations()) 
					    {
					    	LOG.log(Level.WARNING, "AN: " + a.getName() + " AV: " + a.getValue());
					    	if(a.getName().equals("resourcy.appspot.com/IS_SUB_NOTICE") && a.getValue().equals("TRUE"))
					    		isSubmitNotice = true;
					    }

			    		if (isSubmitNotice)
			    			wavelet.delete(blipid);
			    	}
			    	Blip b = wavelet.reply("\n  ");
					b.append(body);
					this.submit(wavelet, RPC_SERVER_URL);
			    }
			    catch(IOException ioex) {
			    	
					LOG.log(Level.WARNING, "Error loading wavelet while attempting to add post blips: " + ioex);
					TestUtils.logWaveAction(WaveActionType.FETCH_WAVELET, WaveActionResponse.FAILURE);

					Queue queue = QueueFactory.getQueue("defer");
				    queue.add(url(FEED_POST_PATH).param("waveid", waveId).param("waveletid", waveletId).param("body",body));
					
			    }
			    catch(Exception e) {
			    	
					LOG.log(Level.WARNING, "Error in deleting blips: " + e);
					
			    }

		    }
		    if (req.getServletPath().equals(DUMMY_PATH)) {
					LOG.log(Level.WARNING, "Dummy Request received and processed");
		    }
		    if (req.getServletPath().equals(CREATE_WAVE_PATH)) {
		    	
				LOG.log(Level.WARNING, "Creating Wave for received category");
				
				 String catId =  req.getParameter("categoryId");
				 Set<String> participants = new HashSet<String>();
				 participants.add("public@a.gwave.com");
				 Wavelet wave = this.newWave("googlewave.com", participants);
				 try {
					 this.submit(wave, RPC_SERVER_URL);
				 }
				 catch (IOException ioex)
				 {
					 
				 }
			 
				
		    }
		    if (req.getServletPath().equals(ADD_RESOURCE_PATH)) {
				LOG.log(Level.WARNING, "Adding task to reload wave because of new resource");
				String catname =  req.getParameter("catname");
				String catid =  req.getParameter("catid");
		    	String waveId =  req.getParameter("waveid");
				String waveletId = req.getParameter("waveletid");

				String domain = "googlewave.com";
				waveId = waveId.replace(' ', '+');
				waveletId = waveletId.replace(' ', '+');
			    LOG.log(Level.WARNING, "wid: " + waveId + " waveletId: " + waveletId + " domain: " + domain + " catname: " + catname + " catid: " + catid);
			   
			    Queue queue = QueueFactory.getQueue("default");
			    queue.add(url(RELOAD_WAVE_PATH).param("waveid", waveId).param("waveletid", waveletId).param("catname",catname).param("catid",catid));

		    
		    }
		    else if(req.getServletPath().equals(RELOAD_WAVE_PATH))
		    {

				//sort by likes

				String catname =  req.getParameter("catname");
				String catid =  req.getParameter("catid");
		    	String waveId =  req.getParameter("waveid");
				String waveletId = req.getParameter("waveletid");

				String domain = "googlewave.com";
				waveId = waveId.replace(' ', '+');
				waveletId = waveletId.replace(' ', '+');

			    LOG.log(Level.WARNING, "wid: " + waveId + " waveletId: " + waveletId + " domain: " + domain + " catname: " + catname + " catid: " + catid);
				
			    try {
			    	
			    	Wavelet wavelet = this.fetchWavelet(new WaveId(domain, waveId), new WaveletId(domain, waveletId), RPC_SERVER_URL);
			    	LOG.log(Level.WARNING, "fetchWavelet");
			    	TestUtils.logWaveAction(WaveActionType.FETCH_WAVELET, WaveActionResponse.SUCCESS);
			    	//Wavelet wavelet = this.blindWavelet(new WaveId(domain,waveId), new WaveletId(domain,waveletId));
			    	//LOG.log(Level.WARNING, "blindWavelet");
			    	Map<String,Blip> m = wavelet.getBlips();
			    	Set<String> s = m.keySet();
			    	Iterator iter = s.iterator();
		    		String blipid = "";
			    
			    	while(iter.hasNext())
			    	{	
				    	try 
				    	{
				    		blipid = (String) iter.next();
				    		wavelet.delete(blipid);			    		
					    }
				    	catch(ConcurrentModificationException ce)
					    {
				    		LOG.log(Level.WARNING, "ConcModException, most likely while trying to delete a blip w/ a gadget in it." + ce);
				    		try {	
				    			Blip b = wavelet.getBlip(blipid);
				    			b.first(ElementType.BUTTON).delete();
				    			wavelet.delete(blipid);
				    		}
				    		catch(Exception ce2)
				    		{
				    			LOG.log(Level.WARNING, "Failed to delete gadget and blip, if that was the problem: " + ce2);
				    		}
				    		
					    }

			    	}
			    	
			    	// Should be the new rootblip
			    	Blip rb = wavelet.reply("\n  Official Wave for Category " + catname);
					Utilities.outputResources(wavelet, rb, catname);
					
					this.submit(wavelet, RPC_SERVER_URL);
			    }
			    catch(IOException ioex) {
			    	
					LOG.log(Level.WARNING, "Error in loading wave: " + ioex);
					TestUtils.logWaveAction(WaveActionType.FETCH_WAVELET, WaveActionResponse.FAILURE);

					//requeue if we fail on a IOException
			    }
			 
				
		    }

		    else
		    {
		      resp.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
		    }
	  }


	@Override
	protected String getRobotName() {
		return "Task manager for Resourcy Servlet";
	}

	
}
