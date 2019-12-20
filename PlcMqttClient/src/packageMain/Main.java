package packageMain;
import packageAds.PlcConnector;
import packageMqtt.AdsMqttClient;

public class Main {

	public enum E_MainStep {
	    INIT,
	    DISPATCH_MQTT_PACKS,
	    ePrepare,
	    eBusy,
	    eIdle,
	    eWaiting,
	    eError
	  }
	
	public static E_MainStep eMainStep;
	
	public static void main(String[] args) {
		
		PlcConnector plcConnector = new PlcConnector();
		AdsMqttClient adsMqttClient = new AdsMqttClient("MQTT Examples", "tcp://localhost:1883", "JavaSample");
		plcConnector.Execute();	
		plcConnector.CheckStateMachine();
		
		while(true)
		{

			switch(eMainStep)
			{
			
				case INIT:
					if(plcConnector.getPlcFetcher().isFetching())
					{

						adsMqttClient.Execute();		
						eMainStep =E_MainStep.DISPATCH_MQTT_PACKS;
					}
					break;
					
				case DISPATCH_MQTT_PACKS:
					if(adsMqttClient.isConnected())
					{
						//if new message dispatch
					}
					
			
			}
			
			
		}
		
	}

}
