package com.redhat.mailinglistOnline.client.entities;

import org.bson.types.ObjectId;

/**
 * This class is needed because from server is arriving ObjectId with some unknown fields
 * and Jackson is unable to map it corectly.
 * 
 * @author Július Staššík
 */

public class MyId {

	private int timestamp;
	private int machineIdentifier;
	private short processIdentifier;
	private int counter;
	private Long date;
	private Long time;
	private int timeSecond;
	
	public int getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(int timeStamp) {
		this.timestamp = timeStamp;
	}
	public int getMachineIdentifier() {
		return machineIdentifier;
	}
	public void setMachineIdentifier(int machineIdentifier) {
		this.machineIdentifier = machineIdentifier;
	}
	public short getProcessIdentifier() {
		return processIdentifier;
	}
	public void setProcessIdentifier(short processIdentifier) {
		this.processIdentifier = processIdentifier;
	}
	public int getCounter() {
		return counter;
	}
	public void setCounter(int counter) {
		this.counter = counter;
	}
	public Long getDate() {
		return date;
	}
	public void setDate(Long date) {
		this.date = date;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public int getTimeSecond() {
		return timeSecond;
	}
	public void setTimeSecond(int timeSecond) {
		this.timeSecond = timeSecond;
	}
	
	public String getExpectedId() {
		ObjectId objId = new ObjectId(timestamp, machineIdentifier, processIdentifier, counter);
		return objId.toString();
	}
	
}
