package packageMain;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Random;

import de.beckhoff.jni.JNIByteBuffer;
import de.beckhoff.jni.tcads.AmsAddr;
import packageAds.FetcherThread;
import packageAds.PlcConnector;
import packageAds.PlcEventDrivenFetcher;
import packageAds.PlcFetcher;
import packageMqtt.AdsMqttClient;
import packageSystem.StateMachine;
import packageSystem.StateMachine.E_StateMachine;

public class Main {

	public enum E_MainStep {
	    INIT,
	    START_PLC_CONNECTOR,
	    EXECUTE_PLC_CONNECTOR,
	    START_MQTT_CLIENT,
	    START_PLC_FETCHER,
	    EXECUTE_MQTT_CLIENT,
	    EXECUTE_PLC_FETCHER,
	    DISPATCH_MQTT_PACKS,
	    DISCONNECT_AND_RELEASE_RESOURCES,
	    RESTART
	  }

	
	public static E_MainStep eMainStep  = E_MainStep.INIT;
	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	
	public static String randomAlphaNumeric(int count) {
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}
	
	public static void main(String[] args) {
		
		
	    String generatedString = randomAlphaNumeric(8);
	    System.out.println("Randomly generated client id: " + generatedString);
		AdsMqttClient adsMqttClient = new AdsMqttClient("tcp://192.168.2.107:1883", generatedString);
		AmsAddr addr = new AmsAddr();
		PlcConnector plcConnector = new PlcConnector(addr);
		PlcEventDrivenFetcher plcFetcher = new PlcEventDrivenFetcher(addr,adsMqttClient);
			
		
		while(true)
		{
			plcConnector.CheckStateMachine();
			adsMqttClient.CheckStateMachine();
			plcFetcher.CheckStateMachine();
			/*
			if(plcConnector.isConnected())
			{
				
			}
			*/
					
			switch(eMainStep)
			{
			
				case INIT:
					
					if(plcConnector.isInitialized())
					{
						plcConnector.Start();
						eMainStep =E_MainStep.START_PLC_CONNECTOR;
					}
					break;
					
				case START_PLC_CONNECTOR:
					
					if(plcConnector.isReady())
					{
						plcConnector.Execute();
						eMainStep = E_MainStep.EXECUTE_PLC_CONNECTOR;
					}
					break;
					
				case EXECUTE_PLC_CONNECTOR:
					
					if(plcConnector.isBusy() && plcConnector.isConnected())
					{
						plcFetcher.Start();
						eMainStep = E_MainStep.START_PLC_FETCHER;
						
					}
					break;
								
				case START_PLC_FETCHER:
					
					if(plcFetcher.isReady())
					{
						plcFetcher.Execute();
						eMainStep = E_MainStep.EXECUTE_PLC_FETCHER;
					}
					break;
					
				case EXECUTE_PLC_FETCHER:
					
					if(plcFetcher.isBusy())
					{
						adsMqttClient.Start();
						eMainStep = E_MainStep.START_MQTT_CLIENT;
					}
					
					
					break;
					
				case START_MQTT_CLIENT:	
					
					if(adsMqttClient.isReady())
					{
						adsMqttClient.Execute();
						eMainStep = E_MainStep.EXECUTE_MQTT_CLIENT;
					}
					break;
					
				case EXECUTE_MQTT_CLIENT:
					
					if(adsMqttClient.isBusy())
					{	
						;
					}	
					break;	
					
				case DISCONNECT_AND_RELEASE_RESOURCES:
					break;
					
				case RESTART:
					break;
			
			}
					
		}
		
	}

}

