package packageMain;

import de.beckhoff.jni.tcads.AmsAddr;
import packageAds.PlcConnector;
import packageAds.PlcEventDrivenFetcher;
import packageExceptions.AdsConnectionException;
import packageHmi.HmiInterface;
import packageHmi.HmiPlug;
import packageHmi.HmiSignaller;
import packageMqtt.AdsMqttClient;

public class MainLauncher implements HmiPlug,Runnable{
	
	public enum E_MainStep {
		NO_STEP,
	    INIT,
	    START_PLC_CONNECTOR,
	    EXECUTE_PLC_CONNECTOR,
	    START_MQTT_CLIENT,
	    START_PLC_FETCHER,
	    EXECUTE_MQTT_CLIENT,
	    EXECUTE_PLC_FETCHER,
	    SYSTEM_MONITORING,
	    SYSTEM_MONITORING_AFTER_EXCEPTION,
	    DISCONNECT_AND_RELEASE_RESOURCES,
	    RESTART
	  }

	
	static private E_MainStep eMainStep = E_MainStep.NO_STEP;
	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	static AdsMqttClient adsMqttClient;
	static PlcConnector plcConnector; 
	static PlcEventDrivenFetcher plcFetcher;
	private HmiSignaller hmiSignaller;
	
	boolean shutDown;
	public MainLauncher()
	{	
		
	}
	
	public static String randomAlphaNumeric(int count) {
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}

	public void setHmiSignaler(HmiSignaller hmiSignaler)
	{
		this.hmiSignaller = hmiSignaler;
	}
	
	@Override
	public void connect() {
		
		if(eMainStep == E_MainStep.NO_STEP)
			eMainStep = E_MainStep.INIT;
		
	}
	@Override
	public void close() {
		
		plcConnector.shutDown();
		plcFetcher.shutDown();
		adsMqttClient.shutDown();
		
	}
	@Override
	public void reset() {
		if(eMainStep == E_MainStep.SYSTEM_MONITORING_AFTER_EXCEPTION)
			eMainStep = E_MainStep.DISCONNECT_AND_RELEASE_RESOURCES;
		
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return plcConnector.isConnected();
	}

	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return (plcConnector.isShutDownOk() && plcFetcher.isShutDownOk() && adsMqttClient.isShutDownOk());
	}

	@Override
	public boolean isReset() {
		// TODO Auto-generated method stub
		return (	plcConnector.isReady()
				&& 	plcFetcher.isReady()
				&& 	adsMqttClient.isReady());
	}

	@Override
	public void run() {
		
		String generatedString = randomAlphaNumeric(8);
		System.out.println("[Main.main] Randomly generated client id: " + generatedString);
		adsMqttClient = new AdsMqttClient("tcp://192.168.2.107:1883", generatedString);
		
		AmsAddr addr = new AmsAddr();
		plcConnector = new PlcConnector(addr);
		plcFetcher = new PlcEventDrivenFetcher(addr,adsMqttClient);
		
				
		while(!shutDown)
		{
			
			try {
				
				plcConnector.checkStateMachine();
				plcFetcher.checkStateMachine();
				adsMqttClient.checkStateMachine();
				shutDown = (plcConnector.isShutDownOk() && plcFetcher.isShutDownOk() && adsMqttClient.isShutDownOk());
				if(shutDown)
					System.out.println("[MainLaucher.run] Shutting down");
			} catch ( Throwable e ) {
				// TODO Auto-generated catch block
				if(e.getCause() instanceof AdsConnectionException)
				{
					plcConnector.fault();
					plcFetcher.fault();
					adsMqttClient.fault();
					
					if(hmiSignaller != null)
					{
						hmiSignaller.error();
					}
					
					e.printStackTrace();
					eMainStep = E_MainStep.SYSTEM_MONITORING_AFTER_EXCEPTION;
					
				}
				
			} finally
			{
				if(plcConnector.connectionLost() || adsMqttClient.mqttConnectionLost())
				{
					plcConnector.fault();
					plcFetcher.fault();
					adsMqttClient.fault();
					
					if(hmiSignaller != null)
					{
						hmiSignaller.error();
					}
					
					eMainStep = E_MainStep.SYSTEM_MONITORING_AFTER_EXCEPTION;
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
						if(hmiSignaller != null)
						{
							hmiSignaller.connecting();
						}
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
						if(hmiSignaller != null)
						{
							hmiSignaller.connected();
						}
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
					
				case SYSTEM_MONITORING_AFTER_EXCEPTION:		
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
					
					if(		plcConnector.isReady()
						&& 	plcFetcher.isReady()
						&& 	adsMqttClient.isReady())
					{
						eMainStep = E_MainStep.START_PLC_CONNECTOR;
					}
					
					break;			
			}
					
		}
		
	}

}
