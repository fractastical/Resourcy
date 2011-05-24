package com.joeldietz.resourcy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;

import com.google.wave.api.AbstractRobot;
import com.google.wave.api.JsonRpcResponse;
import com.google.wave.api.Wavelet;
import com.google.wave.api.JsonRpcConstant.ParamsProperty;
import com.joeldietz.resourcy.models.WaveActionLocation;
import com.joeldietz.resourcy.models.WaveActionResponse;
import com.joeldietz.resourcy.models.WaveActionType;
import com.joeldietz.resourcy.utilities.TestUtils;
import com.joeldietz.resourcy.utilities.Utilities;

public class TesterBot extends AbstractRobot {
	
	 private static final Logger LOG = Logger.getLogger(ResourcyServlet.class.getName());
	 
	 private static String ConsumerKey = null;
	 private static String ConsumerSecret = null;
	 
	 private static String TEST_BATTERY_PATH = null;
	 private static String TEST_BATTERY_PATH_SANDBOX = null;
	 
	 private static String RPC_SERVER_URL = null;
	 private static String RPC_SERVER_URL_SANDBOX = null;
	  
	 private String currDomain = null;
	 private String currRPC_SERVER_URL;
	  
	  public TesterBot()
	  {
		  	ConsumerKey = System.getProperty("ConsumerKey");
		  	ConsumerSecret = System.getProperty("ConsumerSecret");
		  	TEST_BATTERY_PATH = System.getProperty("TEST_BATTERY_PATH");
		  	TEST_BATTERY_PATH_SANDBOX = System.getProperty("TEST_BATTERY_PATH_SANDBOX");

		  	RPC_SERVER_URL_SANDBOX = System.getProperty("RPC_SERVER_URL_SANDBOX");
		  	RPC_SERVER_URL = System.getProperty("RPC_SERVER_URL");
	
		    setupOAuth(ConsumerKey, ConsumerSecret, RPC_SERVER_URL);
		    setAllowUnsignedRequests(true);	  
	  }
	  
	  	  
	  @Override
	  protected String getRobotName() {
	    return "Resourcy Tester";
	  }
	  
	 @Override
	 protected void doPost(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse resp) 
	  {
		    LOG.log(Level.WARNING, "Post request received to TesterBot. Path: " + req.getServletPath());
	    	String msg = "";
	    	Boolean success = false;

	    	ArrayList<Integer> blipnum = new ArrayList<Integer>();
	    	ArrayList<String> wids = new ArrayList<String>();
	    	

		    if (req.getServletPath().equals(TEST_BATTERY_PATH)) {
		    	
		    	currDomain = "googlewave.com";
		    	currRPC_SERVER_URL = RPC_SERVER_URL;

		    		    	
		    	for(Integer i=0; i<=0 ;i+=10)
		    	{
			    	String waveId = createWavelet(i);
			    	if(waveId != null)
			    	{
			    		wids.add(waveId);
			    		blipnum.add(i);
			    		success = true;
			    	}

			    	if(success)
			    		TestUtils.logWaveAction(WaveActionType.CREATE_WAVELET, WaveActionResponse.SUCCESS, WaveActionLocation.WAVE_PREVIEW, true, "waveid: " + waveId + " with " + i + " blips");
			    	else
			    		TestUtils.logWaveAction(WaveActionType.CREATE_WAVELET, WaveActionResponse.FAILURE, WaveActionLocation.WAVE_PREVIEW, true, "with " + i + " blips");

		    		
			    	for(Integer j=0; j < wids.size(); j++)
			    	{
			    		
				    	Boolean fetchSuccess = fetchWavelet(wids.get(j));
				    	
				    	if(fetchSuccess)
				    		TestUtils.logWaveAction(WaveActionType.FETCH_WAVELET, WaveActionResponse.SUCCESS, WaveActionLocation.WAVE_PREVIEW, true, "with " + blipnum.get(j) + " blips");
				    	else
				    		TestUtils.logWaveAction(WaveActionType.FETCH_WAVELET, WaveActionResponse.FAILURE, WaveActionLocation.WAVE_PREVIEW, true, "with " + blipnum.get(j) + " blips");
		    	
			    	}
		    	
		    			    
		    	}
		    }

			if (req.getServletPath().equals(TEST_BATTERY_PATH_SANDBOX)) {
    		    	
					
					currDomain = "wavesandbox.com";
			    	currRPC_SERVER_URL = RPC_SERVER_URL_SANDBOX;
			    		
					for(Integer i=0; i <= 0; i += 10)
			    	{
				    	String waveId = createWavelet(i);
				    	if(waveId != null)
				    	{
				    		wids.add(waveId);
				    		blipnum.add(i);
				    		success = true;
				    	}

				    	if (success)
				    		TestUtils.logWaveAction(WaveActionType.CREATE_WAVELET, WaveActionResponse.SUCCESS, WaveActionLocation.WAVE_SANDBOX, true, "waveid: " + waveId + " with " + i + " blips");
				    	else
				    		TestUtils.logWaveAction(WaveActionType.CREATE_WAVELET, WaveActionResponse.FAILURE, WaveActionLocation.WAVE_SANDBOX, true, "with " + i + " blips");
			    		
				    	for (Integer j=0; j < wids.size(); j++)
				    	{
				    		
					    	Boolean fetchSuccess = fetchWavelet(wids.get(j));
					    	
					    	if(fetchSuccess)
					    		TestUtils.logWaveAction(WaveActionType.FETCH_WAVELET, WaveActionResponse.SUCCESS, WaveActionLocation.WAVE_SANDBOX, true, "with " + blipnum.get(j) + " blips");
					    	else
					    		TestUtils.logWaveAction(WaveActionType.FETCH_WAVELET, WaveActionResponse.FAILURE, WaveActionLocation.WAVE_SANDBOX, true, "with " + blipnum.get(j) + " blips");
			    	
				    	}
			    	
			    			    
			    	}
			    }
			// We should test SFDC Connect, read and write 
			// We need a test category of Resource
			// and 

		    	

	  }


	  	  
	  private String createWavelet(Integer blipNumber) 
	  {
		    String waveId = null;
		    
		    try {
		    	Wavelet w = this.newWave(currDomain, null);
		    	for(Integer i=0; i<blipNumber; i++)
		    		w.reply("\nwoo #" + i);
		    	List<JsonRpcResponse> jrrs  = this.submit(w, currRPC_SERVER_URL);
		    	
		        for(JsonRpcResponse r : jrrs)
		        {
		            Map<ParamsProperty, Object> data = r.getData();
		            if(data.containsKey("WAVE_ID"))
		            {
		                waveId =  (String) data.get("WAVE_ID");
		            }
		            
		        }
		    }
		   catch (Exception e)
		   {
			   
		   }
	       return waveId;
		  
		  
	  }
	  
	  
	  private Boolean fetchWavelet(String waveId) 
	  {
		    Boolean success = false;
		    
		    try {
		    	Wavelet w = this.fetchWavelet(new WaveId(currDomain,waveId), new WaveletId(currDomain, "convo+root"), currRPC_SERVER_URL);
		    	// Maybe iterate through and try to find something
		    	success = true;
		   }
		   catch (Exception e)
		   {
			   LOG.log(Level.WARNING,"Error found while fetching wavelet:" + e);
			   
		   }
	       return success;
		  
		  
	  }

	  
}