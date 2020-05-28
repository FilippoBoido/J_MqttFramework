package packageMain;

import de.beckhoff.jni.tcads.AmsAddr;
import packageAds.PlcConnector;
import packageAds.PlcEventDrivenFetcher;
import packageExceptions.AdsConnectionException;
import packageMqtt.AdsMqttClient;


public class Main {

	public enum E_MainStep {
	    INIT,
	    START_PLC_CONNECTOR,
	    EXECUTE_PLC_CONNECTOR,
	    START_MQTT_CLIENT,
	    START_PLC_FETCHER,
	    EXECUTE_MQTT_CLIENT,
	    EXECUTE_PLC_FETCHER,
	    SYSTEM_MONITORING,
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
		//Set callback for incoming messages
		adsMqttClient.setCallback(plcFetcher);
			
		
		while(true)
		{
			
			try {
				
				plcConnector.checkStateMachine();
				plcFetcher.checkStateMachine();
				adsMqttClient.checkStateMachine();
				
			} catch ( Throwable e ) {
				// TODO Auto-generated catch block
				if(e.getCause() instanceof AdsConnectionException)
				{
					plcConnector.fault();
					plcFetcher.fault();
					adsMqttClient.fault();
					
					e.printStackTrace();
					
					eMainStep = E_MainStep.DISCONNECT_AND_RELEASE_RESOURCES;
					
				}
				
			}
						
			switch(eMainStep)
			{
			
				case INIT:
					
					if(plcConnector.isInitOk())
					{
						plcConnector.start();
						eMainStep = E_MainStep.START_PLC_CONNECTOR;
					}
					
					break;
					
				case START_PLC_CONNECTOR:
					
					if(plcConnector.isReadyOk())
					{
						plcConnector.execute();
						eMainStep = E_MainStep.EXECUTE_PLC_CONNECTOR;
					}
					
					break;
					
				case EXECUTE_PLC_CONNECTOR:
					
					if(plcConnector.isConnected())
					{
						plcFetcher.start();
						plcConnector.waitLoop();
						eMainStep = E_MainStep.START_PLC_FETCHER;
						
					}
					
					break;
								
				case START_PLC_FETCHER:
					
					if(plcFetcher.isReadyOk())
					{
						plcFetcher.execute();
						eMainStep = E_MainStep.EXECUTE_PLC_FETCHER;
					}
					
					break;
					
				case EXECUTE_PLC_FETCHER:
					
					if(plcFetcher.isBusyOk())
					{
						adsMqttClient.start();
						eMainStep = E_MainStep.START_MQTT_CLIENT;
					}
							
					break;
					
				case START_MQTT_CLIENT:	
					
					if(adsMqttClient.isReadyOk())
					{
						adsMqttClient.execute();
						eMainStep = E_MainStep.EXECUTE_MQTT_CLIENT;
					}
					
					break;
					
				case EXECUTE_MQTT_CLIENT:
					
					if(adsMqttClient.isBusyOk())
					{	
						eMainStep = E_MainStep.SYSTEM_MONITORING;
					}	
					
					break;	
					
				case SYSTEM_MONITORING:		
					break;
					
				case DISCONNECT_AND_RELEASE_RESOURCES:
					
					if(		plcConnector.isErrorOk() 
						&&	adsMqttClient.isErrorOk()
						&&	plcFetcher.isErrorOk() ) {
						
						plcConnector.reset();
						plcFetcher.reset();
						adsMqttClient.reset();
						
						eMainStep = E_MainStep.RESTART;
					}
					
					break;
					
				case RESTART:
					
					if(plcConnector.isReady())
					{
						eMainStep = E_MainStep.START_PLC_CONNECTOR;
					}
					
					break;
			
			}
					
		}
		
	}

}

