package com.amway.integration.um.test;


import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SpringBootApplication
@EnableAutoConfiguration
public class UMLatencyTesterApplication implements ApplicationRunner
{
	private static final Logger LOGGER = LoggerFactory.getLogger(UMLatencyTesterApplication.class);
	
	@Override
	public void run(ApplicationArguments args) throws Exception 
	{
		LOGGER.info("Application started with command-line arguments: {}", Arrays.toString(args.getSourceArgs()));
		
		boolean runSubscriber = false;
		boolean runPublisher = false;
		long startSequence = 0L;
		long publishCount = -1L;
		long runtimeSeconds = -1L;
		long delayMS = -1L;
		long sizeBytes = 1000L;
		String testname = "UKNOWN";
		
		// Check the local realm details
		List<String> rnames = args.getOptionValues("rname");
		if(rnames ==  null || rnames.size() < 1)
		{
			Usage("rname");
			System.exit(1);
		}
		// check mode
		{
			List<String> mode = args.getOptionValues("mode");
			if(mode ==  null || mode.size() != 1)
			{
				Usage("mode");
				System.exit(1);
			} else {
				runSubscriber = ("sub".equals(mode.get(0)) || "both".equals(mode.get(0)));
				runPublisher = ("pub".equals(mode.get(0)) || "both".equals(mode.get(0)));
			}
		}
		// check mode
		{
			List<String> testnames = args.getOptionValues("testname");
			if(testnames ==  null || testnames.size() != 1)
			{
				Usage("testname");
				System.exit(1);
			} else {
				testname = testnames.get(0);
			}
		}
		// check count
		{
			List<String> count = args.getOptionValues("count");
			if(count !=  null && count.size() == 1)
			{
				try 
				{
					publishCount = Long.parseLong(count.get(0)); 
				} catch(Exception e) {
					System.err.println("Unable to parse count '" + count.get(0) + "' as a long: " + e);
					System.exit(1);
				}
			}
		}
		
		// check runtimeseconds
		{
			List<String> runtimesecondsparm = args.getOptionValues("runtimeseconds");
			if(runtimesecondsparm !=  null && runtimesecondsparm.size() == 1)
			{
				try 
				{
					runtimeSeconds = Long.parseLong(runtimesecondsparm.get(0)); 
				} catch(Exception e) {
					System.err.println("Unable to parse runtimeseconds '" + runtimesecondsparm.get(0) + "' as a long: " + e);
					System.exit(1);
				}
			}
		}
		if(!(runtimeSeconds > 0 || publishCount > 0))
		{
			Usage("runtimeseconds or count");
			System.exit(1);
		}
		
		// check delayms
		{
			List<String> delayms = args.getOptionValues("delayms");
			if(delayms !=  null && delayms.size() == 1)
			{
				try 
				{
					delayMS = Long.parseLong(delayms.get(0));
				} catch(Exception e) {
					System.err.println("Unable to parse delayms '" + delayms.get(0) + "' as a long: " + e);
					System.exit(1);
				}
			}
		}
		
		// check size
		{
			List<String> size = args.getOptionValues("size");
			if(size !=  null && size.size() == 1)
			{
				try 
				{
					sizeBytes = Long.parseLong(size.get(0));
				} catch(Exception e) {
					System.err.println("Unable to parse size '" + size.get(0) + "' as a long: " + e);
					System.exit(1);
				}
			}
		}
		// check startseq
		{
			List<String> startseq = args.getOptionValues("startseq");
			if(startseq != null && startseq.size() == 1)
			{
				try 
				{
					startSequence = Long.parseLong(startseq.get(0));
				} catch(Exception e) {
					System.err.println("Unable to parse startseq '" + startseq.get(0) + "' as a long: " + e);
					System.exit(1);
				}
			}
		}
		// get channel configuration channel
		List<String> channels = args.getOptionValues("channel");
		if(channels == null || channels.size() < 1)
		{
			Usage("channel");
			System.exit(1);
		}
		
		List<String> usernames = args.getOptionValues("username");
		if(usernames == null)
		{	usernames = new ArrayList<String>(); }
		String username = usernames.size() > 0 ? usernames.get(0) : null;
		List<String> passwords = args.getOptionValues("password");
		if(passwords == null)
		{	passwords = new ArrayList<String>(); }
		String password = passwords.size() > 0 ? passwords.get(0) : null;
		
		if(runSubscriber)
		{
			Subscriber subscriber = new Subscriber(rnames.get(0), channels.get(0), username, password);
			subscriber.startup();
		}
		if(runPublisher)
		{
			Publisher publisher = new Publisher(rnames.get(0), channels.get(0), username, password);
			publisher.setTestName(testname);
			if(startSequence > -1L)
			{	publisher.setStartSequence(startSequence); }
			if(publishCount > -1L)
			{	publisher.setPublishCount(publishCount); }
			if(runtimeSeconds > -1L)
			{	publisher.setRuntimeSeconds(runtimeSeconds); }
			if(delayMS > -1L)
			{	publisher.setDelayMS(delayMS); }
			publisher.startup();
		}
		// stay alive
		while(true)
		{	try { Thread.sleep(1000L); } catch(Exception e) { } }
	}
	
	public static void main(String[] args) 
	{
		SpringApplication.run(UMLatencyTesterApplication.class, args);
	}
	
	/**
	 * Prints the usage message for this class
	 */
	private static void Usage(String missing) 
	{
		System.err.println("Missing parmeter: " + missing);
		System.err.println("Usage ...\n");
		System.err.println("  call with each setting as a key:value pair on commandline, e.g.:\n");
		System.err.println("    --rname=nhp://uslx416:9000 --channel=sampleChannel\n");
		System.err.println("----------- Required Arguments> -----------\n");
		System.err.println("channel:  At least one channel name must be set, more than one can be setup");
		System.err.println("rname:    At least one realm must be set, if multiple channels are set, a matching");
		System.err.println("          number of rnames can be passed, otherwise just the first would be used for");
		System.err.println("          all channels.");
		System.err.println("mode:  both - publish and subscribe from this process");
		System.err.println("       pub - publish only from this process");
		System.err.println("       sub - publish only from this process");
		System.err.println("testname:  name to include in the data");
		System.err.println("runtimeseconds:  how long to run for, exclusive with count");
		System.err.println("count:  how many documents to publish, exclusive with runtimeseconds");
		System.err.println("username:  user to connect as, requires password");
		System.err.println("password:  password to authenticate with, requires username");
		System.err.println("\n----------- Optional Arguments -----------\n");
		System.err.println("start:  The Event ID to start subscribing from");
		System.err.println("selector:  The event filter string to use\n");
	}
}
