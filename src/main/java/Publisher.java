package com.amway.integration.um.test;

import com.pcbsys.nirvana.client.nBaseClientException;
import com.pcbsys.nirvana.client.nChannel;
import com.pcbsys.nirvana.client.nChannelAttributes;
import com.pcbsys.nirvana.client.nChannelNotFoundException;
import com.pcbsys.nirvana.client.nConsumeEvent;
import com.pcbsys.nirvana.client.nProtobufEvent;
import com.pcbsys.nirvana.client.nEventProperties;
import com.pcbsys.nirvana.client.nRequestTimedOutException;
import com.pcbsys.nirvana.client.nSecurityException;
import com.pcbsys.nirvana.client.nSession;
import com.pcbsys.nirvana.client.nSessionFactory;
import com.pcbsys.nirvana.client.nSessionNotConnectedException;
import com.pcbsys.nirvana.client.nUnexpectedResponseException;
import com.pcbsys.nirvana.client.nUnknownRemoteRealmException;

import java.net.InetAddress;
import java.util.TimeZone;
import java.util.Properties;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Publishes reliably to a nirvana channel
 */
public class Publisher extends Client implements Runnable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Publisher.class);
	
	private nChannel channel;
	
	private String testName = "UNKNOWN";
	private long startSequence = 0L;
	private long publishCount = -1L;
	private long runtimeSeconds = -1L;
	private long delayMS = -1L;
	private int sizeBytes = 1000;
	
	public Publisher(String rname, String channel, String username, String password)
		throws Exception
	{
		super(rname, channel, username, password);
	}
	
	public void setTestName(String testName)
	{	this.testName = testName; }
	public String getTestName()
	{	return testName; }
	
	public void setStartSequence(long startSequence)
	{	this.startSequence = startSequence; }
	public long getStartSequence()
	{	return startSequence; }

	public void setPublishCount(long publishCount)
	{	this.publishCount = publishCount; }
	public long getPublishCount()
	{	return publishCount; }

	public void setRuntimeSeconds(long runtimeSeconds)
	{	this.runtimeSeconds = runtimeSeconds; }
	public long getRuntimeSeconds()
	{	return runtimeSeconds; }

	public void setDelayMS(long delayMS)
	{	this.delayMS = delayMS; }
	public long getDelayMS()
	{	return delayMS; }

	public void setSizeBytes(int sizeBytes)
	{	this.sizeBytes = sizeBytes; }
	public int getSizeBytes()
	{	return sizeBytes; }


	
	public void startup()
		throws Exception
	{
		nSession session = getSession();
		nChannelAttributes nca = new nChannelAttributes();
		nca.setName(getChannelName());
		// Obtain the channel reference
		channel = session.findChannel(nca);
		Thread publisher = new Thread(this);
		publisher.start();
	}
	
	public void run()
	{
		if(runtimeSeconds > 0L)
		{
			
		} else if(publishCount > 0L) {
			doCountLoop();
		}
	}
	
	private void doCountLoop()
	{
		LOGGER.info("Starting up publisher " + Thread.currentThread().getName() + " with fixed count of " + publishCount);
		byte[] payload = new byte[sizeBytes];
		for (int x = 0; x < sizeBytes; x++) 
		{	payload[x] = (byte) ((x % 90) + 32); }
		for(long i = 0; i < publishCount; i++)
		{
			try
			{
				nEventProperties eprops = new nEventProperties();
				eprops.put("TestName", testName);
				eprops.put("Sequence", i);
				eprops.put("PublishTimeMS", System.currentTimeMillis());
				nConsumeEvent evt = new nConsumeEvent("Tag", eprops, payload);
				channel.publish(evt);
				LOGGER.info("published event " + i + " on channel " + channel.getName());
			} catch(Throwable t) {
				LOGGER.error("error publishing", t);
			}
		}
	}
}

