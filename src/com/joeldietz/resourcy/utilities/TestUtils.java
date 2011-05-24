package com.joeldietz.resourcy.utilities;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import com.joeldietz.resourcy.ResourcyServlet;
import com.joeldietz.resourcy.connections.ConnectionManager;
import com.joeldietz.resourcy.connections.PMF;
import com.joeldietz.resourcy.models.LogEntry;
import com.joeldietz.resourcy.models.WaveActionLocation;
import com.joeldietz.resourcy.models.WaveActionResponse;
import com.joeldietz.resourcy.models.WaveActionType;
import com.sforce.soap.enterprise.EnterpriseConnection;

public class TestUtils {

	private EnterpriseConnection connection;
	private ConnectionManager cm = new ConnectionManager();
    private static final Logger LOG = Logger.getLogger(ResourcyServlet.class.getName());

    public static String getSuccessfulCreateChartforLastWeek()
    {
    	
    	return "";		
    }
    public static String getSuccessfulFetchChartforLastWeek()
    {
    	return "";	
    }
    public static String getSuccessfulCreatePercentageforLastWeek()
    {
    	return "";	
    }
    public static String getSuccessfulFetchPercentageforLastWeek()
    {
    	return "";	
    }


	
	public static void logWaveAction(WaveActionType type, WaveActionResponse result, WaveActionLocation location, Boolean partOfTest, String msg)
	{
		
		PersistenceManager pm = PMF.get().getPersistenceManager();

		try {			
			LogEntry l = new LogEntry(type, result, location, partOfTest, msg);
			pm.makePersistent(l);
		}
		catch (Exception e)
		{
			LOG.log(Level.SEVERE, "Cannot save log entry " + e);

		}
		finally {
			pm.close();

		}
		
	}

	public static void logWaveAction(WaveActionType type, WaveActionResponse result) 
	{
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
	
		try {			
			LogEntry l = new LogEntry(type, result);
			pm.makePersistent(l);
		}
		catch (Exception e)
		{
			LOG.log(Level.SEVERE, "Cannot save log entry " + e);
	
		}
		finally {
			pm.close();
	
		}
		
	}

}