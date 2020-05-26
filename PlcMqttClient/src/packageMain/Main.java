package packageMain;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Random;

import de.beckhoff.jni.JNIByteBuffer;
import de.beckhoff.jni.tcads.AmsAddr;
import packageAds.FetcherThread;
import packageAds.PlcConnector;
import packageAds.PlcFetcher;
import packageMqtt.AdsMqttClient;
import packageSystem.StateMachine;
import packageSystem.StateMachine.E_StateMachine;

public class Main {

	public enum E_MainStep {
	    INIT,
	    DISPATCH_MQTT_PACKS
	  }

	
	public static E_MainStep eMainStep  = E_MainStep.INIT;
	private static final String ALPHA_NUMERIC_STRING = "0123456789";//"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	
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
		PlcConnector plcConnector = new PlcConnector(addr,Integer.parseInt(generatedString));
		PlcFetcher plcFetcher = new PlcFetcher(addr,adsMqttClient);
					
		while(true)
		{
			plcConnector.CheckStateMachine();
			if(plcConnector.isConnected())
			{
				adsMqttClient.CheckStateMachine();
				plcFetcher.CheckStateMachine();
			}
			
			
			switch(eMainStep)
			{
			
				case INIT:
					
					if(plcFetcher != null )
					{
						if(plcFetcher.eStateMachine == E_StateMachine.eBusy)
						{
							adsMqttClient.Execute();		
							eMainStep =E_MainStep.DISPATCH_MQTT_PACKS;
						}
						
					}
					break;
					
					
				case DISPATCH_MQTT_PACKS:
					if(adsMqttClient.isConnected())
					{	
						;	
					}	
					break;
			
			}
					
		}
		
	}

}
