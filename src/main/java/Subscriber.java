package com.amway.integration.um.test;

import com.pcbsys.nirvana.client.nBaseClientException;
import com.pcbsys.nirvana.client.nChannel;
import com.pcbsys.nirvana.client.nChannelAlreadySubscribedException;
import com.pcbsys.nirvana.client.nChannelAttributes;
import com.pcbsys.nirvana.client.nChannelNotFoundException;
import com.pcbsys.nirvana.client.nConsumeEvent;
import com.pcbsys.nirvana.client.nEventListener;
import com.pcbsys.nirvana.client.nEventProperties;
import com.pcbsys.nirvana.client.nRequestTimedOutException;
import com.pcbsys.nirvana.client.nSecurityException;
import com.pcbsys.nirvana.client.nSelectorParserException;
import com.pcbsys.nirvana.client.nSelectorParserException;
import com.pcbsys.nirvana.client.nSession;
import com.pcbsys.nirvana.client.nSessionFactory;
import com.pcbsys.nirvana.client.nSessionNotConnectedException;
import com.pcbsys.nirvana.client.nUnexpectedResponseException;
import com.pcbsys.nirvana.client.nUnknownRemoteRealmException;

import java.io.BufferedInputStream;
import java.util.Enumeration;
import java.util.Date;
import java.util.Properties;
import java.time.Instant;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Subscribes to a nirvana channel
 */
public class Subscriber extends Client implements nEventListener 
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Subscriber.class);
	private nChannel channel;
	
	public Subscriber(String rname, String channel, String username, String password)
		throws Exception
	{
		super(rname, channel, username, password);
	}
	
	public void startup()
		throws Exception
	{
		nSession session = getSession();
		nChannelAttributes nca = new nChannelAttributes();
		nca.setName(getChannelName());
		// Obtain the channel reference
		channel = session.findChannel(nca);
		channel.addSubscriber(this);
	}
	
	public void go(nConsumeEvent evt) 
	{
		try
		{
			long subTimeMS = System.currentTimeMillis();
			nEventProperties eprops = evt.getProperties();
			if(eprops == null)
			{
				LOGGER.error(getRname() + " - "  + evt.getChannelName() + " ID:" + evt.getEventID() + " - no properties set!");
				return;
			} else {
				long publishTimeMS = (long) eprops.get("PublishTimeMS");
				long latency = subTimeMS - publishTimeMS;
				Instant subTime = Instant.ofEpochMilli(subTimeMS);
				Instant publishTime = Instant.ofEpochMilli(publishTimeMS);
				LOGGER.error("result message -> {\"rname\":\""  + evt.getChannelName() + "\",\"ID\":" + evt.getEventID() + ",\"TestName\":\"" + eprops.get("TestName") + "\",\"Sequence\":" + eprops.get("Sequence") + ",\"PublishTime\":\"" + publishTime.toString() + "\", \"ProcessTime\":\"" + subTime.toString() +"\",\"LatencyMS\":" + latency + "}");
			}
		} catch(Throwable e) {
			try
			{
				LOGGER.error(getRname() + " - "  + evt.getChannelName() + " ID:" + evt.getEventID() + " - Unable to parse message", e);
			} catch(Throwable e2) {
				LOGGER.error(getRname() + " - Unknown error - Unable to print error message with event", e2);
			}
		}
	}

}
