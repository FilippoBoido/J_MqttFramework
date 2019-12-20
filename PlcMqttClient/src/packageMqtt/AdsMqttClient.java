package packageMqtt;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import packageSystem.StateMachine;

public class AdsMqttClient extends StateMachine{
	
	String topic;          
	String broker;     
	String clientId;   
	int qos  = 2;    
	MemoryPersistence persistence;
	MqttClient sampleClient;
	boolean connected;
	
	public boolean isConnected() {
		return connected;
	}

	public AdsMqttClient(String topic, String broker, String clientId)
	{
		this.topic = topic;	
		this.broker = broker;
		this.clientId = clientId;
		persistence = new MemoryPersistence();  
	}

	public void Publish(String message)
	{
		if(sampleClient != null)
		{
			try 
			{
				System.out.println("Publishing message: "+message);
	            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
	            mqttMessage.setQos(qos);
				sampleClient.publish(topic, mqttMessage);
		        System.out.println("Message published");
			}
			catch(MqttException me) 
			{
	            System.out.println("reason "+me.getReasonCode());
	            System.out.println("msg "+me.getMessage());
	            System.out.println("loc "+me.getLocalizedMessage());
	            System.out.println("cause "+me.getCause());
	            System.out.println("excep "+me);
	            me.printStackTrace();
	        }
			
		}    
	}
	
	@Override
	protected void Init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void Ready() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void Prepare() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void Busy() {
		
		try {
			if(sampleClient == null)
			{
				sampleClient = new MqttClient(broker, clientId, persistence);
				MqttConnectOptions connOpts = new MqttConnectOptions();
	            connOpts.setCleanSession(true);
	            System.out.println("Connecting to broker: "+broker);
	            sampleClient.connect(connOpts);
	            System.out.println("Connected");
	            
			}       
           
            
        } catch(MqttException me) {
        	Fault(0);
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
		
	}

	@Override
	protected void Idle() {
		try {
			sampleClient.disconnect();
			System.out.println("Mqtt client disconnected.");
			sampleClient.close();
			System.out.println("Mqtt client closed.");
		} catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
		
		
	}

	@Override
	protected void Waiting() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void Error() {
		// TODO Auto-generated method stub
		
	}
	
}