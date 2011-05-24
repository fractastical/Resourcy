package com.joeldietz.resourcy.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.joeldietz.resourcy.ResourcyServlet;

public class GoogleChartUtils {

	private static final Logger LOG = Logger.getLogger(ResourcyServlet.class.getName());

	public static String generatePieChartURL(String title, List<Integer> incomingData, List<String> incomingLabels, Map <String,String> valuePairs)
	{
		String chartPath = "http://chart.apis.google.com/chart?";    

		return "";
		
	}


	public static String generateBarChartURL(String title, List<Integer> incomingData, List<String> incomingLabels, Map <String,String> valuePairs)
    {  
     	if (incomingData.size() == 0)
    		return "";
    	
    	String dataString = "";
    	Integer highest = 0;
    	for (Integer d : incomingData) {
    		if (d > highest)
    			highest = d;
    		dataString += d + ",";
    	}
    	 dataString = dataString.substring(0, dataString.length() - 1);
		 String data = "chd=t:" + dataString;        

     	 Integer step = calculateStepSize(highest);
		 highest = roundUpToClosestStep(highest, step);
		 
		//vertical bar chart is default
		 String chartType = "cht=";
		 if (valuePairs.containsKey("charttype"))
			 chartType += valuePairs.get("charttype");
		 else if(valuePairs.containsKey("cht"))
			 chartType += valuePairs.get("cht");
		 else
		 	chartType += "bvg"; 
			 
		 //blue and light blue are defaults
		 String colors = "chco=";       
		 if (valuePairs.containsKey("colors"))
			 colors += valuePairs.get("colors");
		 else if(valuePairs.containsKey("chco"))
			 colors += valuePairs.get("chco");
		 else
			 colors += "4D89F9,C6D9FD"; 
			 
		 String spacing = "chbh=";       
		 if (valuePairs.containsKey("spacing"))
			 spacing += valuePairs.get("spacing");
		 else if(valuePairs.containsKey("chbh"))
			 spacing += valuePairs.get("chbh");
		 else
			 spacing += "20,4,20"; 
			 
		 String chartSize = "chs=";       
		 if (valuePairs.containsKey("chartsize"))
			 chartSize += valuePairs.get("chartsize");
		 else if(valuePairs.containsKey("chs"))
			 chartSize += valuePairs.get("chs");
		 else
			 chartSize += "600x200"; 
		 
		 String YAxisRange = "chxr=";       
		 if (valuePairs.containsKey("yaxisrange"))
			 YAxisRange += valuePairs.get("yaxisrange");
		 else if(valuePairs.containsKey("chxr"))
			 YAxisRange += valuePairs.get("chxr");
		 else
			 YAxisRange += "1,0," + highest + "," + step; 
		 
		 String scaling = "chds=";       
		 if (valuePairs.containsKey("scaling"))
			 scaling += valuePairs.get("scaling");
		 else if(valuePairs.containsKey("chds"))
			 scaling += valuePairs.get("chds");
		 else
			 scaling += "0," + highest;
		 
		 //y axis, start value, highest value, step	size

		 String chartLabels = "chxt=x,y&chxl=0:|";
		 for (String l: incomingLabels)
		 	chartLabels += l + "|";   
		 chartLabels = chartLabels.substring(0, chartLabels.length() - 1);
		 	 
		 String chartTitle = "chtt=" + title.replaceAll(" ","+");  
	  
		 String chartPath = "http://chart.apis.google.com/chart?";    
		 return  chartPath + chartType + "&" + chartTitle + "&" + YAxisRange + "&" + colors + "&" + scaling + "&" + chartLabels + "&" + spacing + "&"  + data + "&" + chartSize;
    }

    // The step is the distance between each label on the y axis
    public static Integer calculateStepSize(Integer highestVal)
    {
	     	//calculates the appropriate step to display on the y axis
		  ArrayList<Integer> possibleSteps = new ArrayList<Integer>();
		  possibleSteps.add(1);
		  possibleSteps.add(2);
		  possibleSteps.add(5);
		  possibleSteps.add(10);
		  possibleSteps.add(20);
		  possibleSteps.add(50);
		  possibleSteps.add(100);

		  for(Integer i= (possibleSteps.size() - 1);i >= 0; i--) {
		  	LOG.log(Level.WARNING, "HIGH DIV 6:" + (highestVal / 6) + " POSS STEP:" + possibleSteps.get(i));  
		  	if ((highestVal / 6) > possibleSteps.get(i))  
		  		return possibleSteps.get(i);
		  }
		  return 1;            
    }
    
    public static Integer roundUpToClosestStep(Integer highestVal, Integer step)
    {
    	  return highestVal + (step - (highestVal % step));   
    }	
    
	
}
