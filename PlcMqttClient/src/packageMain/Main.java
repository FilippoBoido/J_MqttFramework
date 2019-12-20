package packageMain;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

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
	
	public static void main(String[] args) {
		
		AdsMqttClient adsMqttClient = new AdsMqttClient("MQTT Examples", "tcp://localhost:1883", "JavaSample");
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
