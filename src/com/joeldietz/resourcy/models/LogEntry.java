package com.joeldietz.resourcy.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.repackaged.org.joda.time.DateTime;

@Entity
public class LogEntry {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;

	WaveActionType type;
	WaveActionResponse result;
	WaveActionLocation location;
	DateTime createdDateTime;
	Boolean partOfTestBattery;
	String message;
	
	public LogEntry(WaveActionType type, WaveActionResponse result)
	{
		this.type = type; 
		this.result = result;
		location = WaveActionLocation.WAVE_PREVIEW;
		partOfTestBattery = false;
		createdDateTime = new DateTime();
		
	}
	
	public LogEntry(WaveActionType type, WaveActionResponse result, Boolean partOfTestBattery)
	{
		this.type = type; 
		this.result = result;
		this.partOfTestBattery = partOfTestBattery;
		location = WaveActionLocation.WAVE_PREVIEW;
		createdDateTime = new DateTime();
		
	}
	
	public LogEntry(WaveActionType type, WaveActionResponse result, WaveActionLocation location, Boolean partOfTestBattery, String message)
	{
		this.type = type; 
		this.result = result;
		this.location = location;
		this.partOfTestBattery = partOfTestBattery;
		createdDateTime = new DateTime();
		this.message = message;
		
	}




}
