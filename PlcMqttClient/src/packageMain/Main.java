package packageMain;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Random;

import de.beckhoff.jni.JNIByteBuffer;
import packageAds.FetcherThread;
import packageAds.PlcConnector;
import packageMqtt.AdsMqttClient;
import packageSystem.StateMachine;

public class Main {

	public enum E_MainStep {
	    INIT,
	    DISPATCH_MQTT_PACKS
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
		AdsMqttClient adsMqttClient = new AdsMqttClient("tcp://192.168.2.100:1883", generatedString);
		PlcConnector plcConnector = new PlcConnector(adsMqttClient);
		
					
		while(true)
		{
			plcConnector.CheckStateMachine();
			plcConnector.Execute();
			adsMqttClient.CheckStateMachine();
			
			
			switch(eMainStep)
			{
			
				case INIT:
					FetcherThread lifePackageFetcher = plcConnector.getPlcFetcher().getLifePackageFetcher();
					if(lifePackageFetcher != null )
					{
						if(lifePackageFetcher.isFetching())
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
