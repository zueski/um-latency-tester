package com.amway.integration.um.test;

import com.pcbsys.foundation.utils.fEnvironment;
import com.pcbsys.nirvana.client.nAsyncExceptionListener;
import com.pcbsys.nirvana.client.nBaseClientException;
import com.pcbsys.nirvana.client.nDataStream;
import com.pcbsys.nirvana.client.nDataStreamListener;
import com.pcbsys.nirvana.client.nEventAttributes;
import com.pcbsys.nirvana.client.nEventProperties;
import com.pcbsys.nirvana.client.nEventPropertiesIterator;
import com.pcbsys.nirvana.client.nIllegalArgumentException;
import com.pcbsys.nirvana.client.nRealmUnreachableException;
import com.pcbsys.nirvana.client.nReconnectHandler;
import com.pcbsys.nirvana.client.nSecurityException;
import com.pcbsys.nirvana.client.nSession;
import com.pcbsys.nirvana.client.nSessionAlreadyInitialisedException;
import com.pcbsys.nirvana.client.nSessionAttributes;
import com.pcbsys.nirvana.client.nSessionFactory;
import com.pcbsys.nirvana.client.nSessionNotConnectedException;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* Base class that contains standard functionality for a nirvana sample app
*/
public abstract class Client implements nReconnectHandler, nAsyncExceptionListener
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

	private String lastSessionID = null;
	private nSession session = null;
	private nSessionAttributes nsa = null;
	
	private String[] rname;
	private String channel;
	private String username;
	private String password;
	private int threadPoolCount = 5;
	
	private static final String DEFAULT_USERNAME = "um-latency-tester";
	
	public Client(String rname, String channel, String username, String password)
	{
		this.rname = parseRealmProperties(rname);
		this.channel = channel;
		this.username = username;
		this.password = password;
	}
	
	public String getChannelName()
	{	return channel; }
	public String[] getRname()
	{	return rname; }


	protected static String[] parseRealmProperties(String realmdetails)
	{
		String[] rproperties = new String[4];
		int idx = 0;
		String RNAME=null;
		StringTokenizer st=new StringTokenizer(realmdetails,",");
		while(st.hasMoreTokens())
		{
			String someRNAME=(String)st.nextToken();
			rproperties[idx] = someRNAME;
			idx++;
		}
		//Trim the array
		String[] rpropertiesTrimmed=new String[idx];
		System.arraycopy(rproperties,0,rpropertiesTrimmed,0,idx);
		return rpropertiesTrimmed;
	}

	protected nSession getSession()
		throws Exception
	{
		if(session == null)
		{
			LOGGER.info("Starting up UM connection for " + Thread.currentThread().getName());
			nSessionAttributes umSessionAttribs = new nSessionAttributes(rname, 2);
			umSessionAttribs.setFollowTheMaster(true);
			umSessionAttribs.setDisconnectOnClusterFailure(false);
			umSessionAttribs.setName(DEFAULT_USERNAME);
			if(password != null)
			{
				session = nSessionFactory.create(umSessionAttribs, this, username, password);
			} else {
				session = nSessionFactory.create(umSessionAttribs, this, username);
			}
			session.enableThreading(threadPoolCount);
			session.setReadThreadAsDaemon(true);
			session.init(true);
			LOGGER.info("UM connection '" + Thread.currentThread().getName() + "' is up");
		}
		return session;
	}

	
	public void disconnected(nSession anSession)
	{
		try
		{
			System.out.println( "You have been disconnected from "+lastSessionID);
		} catch (Exception ex) {
			System.out.println("Error while disconnecting "+ex.getMessage());
		}
	}

	/**
	* A callback is received by the API to this method to notify the user of a successful reconnection
	* to the realm. The method is enforced by the nReconnectHandler interface but is normally optional.
	* It gives the user a chance to log the reconnection or do something else about it.
	*
	* @param anSession The Nirvana session being reconnected
	*/
	public void reconnected(nSession anSession)
	{
		try
		{
			lastSessionID=session.getId();
			System.out.println ("You have been Reconnected to "+lastSessionID);
		} catch (Exception ex) {
			System.out.println ("Error while reconnecting "+ ex.getMessage());
		}
	}

	/**
	* A callback is received by the API to this method to notify the user that the API is about
	* to attempt reconnecting to the realm. The method is enforced by the nReconnectHandler
	* interface but is normally optional. It allows the user to decide whether further
	* attempts are required or not, whether custom delays should be enforced etc.
	*
	* @param anSession The Nirvana session that will be used to reconnect
	*/
	public boolean tryAgain(nSession anSession)
	{
		try 
		{
			System.out.println ("Attempting to reconnect to "+rname);
		} catch (Exception ex) {
			System.out.println ("Error while trying to reconnect "+ ex.getMessage());
		}
		return true;
	}

	/**
	* A callback is received by the API to this method to notify the user that the an
	* asynchronous exception (in a thread different than the current one) has occured.
	*
	* @param ex The asynchronous exception that was thrown
	*/
	public void handleException(nBaseClientException ex)
	{
		System.out.println("An Asynchronous Exception was received from the Nirvana realm.");
		ex.printStackTrace();
	}

	
} // End of subscriber Class

