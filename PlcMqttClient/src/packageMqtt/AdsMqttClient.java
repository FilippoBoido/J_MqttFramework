package packageMqtt;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import de.beckhoff.jni.JNIByteBuffer;
import packageSystem.StateMachine;

public class AdsMqttClient extends StateMachine{
	
	public enum E_PublishMode
	{
		NO_PUBLISH_MODE,
		LIFE_PACKAGE
	}
	
	private String topic;          
	private String broker;     
	private String clientId;   
	private int qos  = 2;    
	private MemoryPersistence persistence;
	private MqttClient mqttClient;
	
	public MqttClient getMqttClient() {
		return mqttClient;
	}

	private boolean connected;
	//private JNIByteBuffer buffer;


	public boolean isConnected() {
		return connected;
	}

	public AdsMqttClient(String broker, String clientId) 
	{
		
		this.broker = broker;
		this.clientId = clientId;
		persistence = new MemoryPersistence();  
		try {
			mqttClient = new MqttClient(broker, clientId, persistence);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		
	}
	public boolean Subscribe(String topic)
	{
		if(mqttClient != null && connected)
		{
			
			try {
				System.out.println("[AdsMqttClient.Subscribe] Subscribing to topic: " + topic);
				mqttClient.subscribe(topic);
				
				
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			return true;
		}
		return false;
		
	}
	public void Publish(String message)
	{
		if(mqttClient != null && connected)
		{
			try 
			{
				System.out.println("Publishing message: "+message);
	            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
	            mqttMessage.setQos(qos);
	            mqttClient.publish(topic, mqttMessage);
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
	
	public boolean Publish(String topic, byte[] payload)
	{
		if(mqttClient != null && connected)
		{
			try 
			{
				mqttClient.publish(topic,payload,2,false);
		        System.out.println("[AdsMqttClient.Publish] Message published");
			}
			catch(MqttException me) 
			{
	            System.out.println("reason "+me.getReasonCode());
	            System.out.println("msg "+me.getMessage());
	            System.out.println("loc "+me.getLocalizedMessage());
	            System.out.println("cause "+me.getCause());
	            System.out.println("excep "+me);
	            me.printStackTrace();
	            return false;
	        }
			return true;
		}    
		return false;
	}
	
	public void Publish(JNIByteBuffer buffer,E_PublishMode ePublishMode)
	{
		switch(ePublishMode)
		{
			case NO_PUBLISH_MODE:
				
				System.out.println("AdsMqttClient: NO_PUBLISH_MODE.");
				
				break;
				
			case LIFE_PACKAGE:
				
				ByteBuffer bb = ByteBuffer.wrap(buffer.getByteArray());
				bb.order(ByteOrder.LITTLE_ENDIAN);
				short state = bb.getShort();	
				long timeConversion = (long) bb.getInt() * 1000;
				Date date = new Date(timeConversion);
				SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
				sdf.setTimeZone(TimeZone.getTimeZone("Germany/Berlin"));
				
				Publish(StateMachine.getStateAsString(state));
				Publish(sdf.format(date));
							
				break;
		}
	}
	
	@Override
	protected void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void ready() throws MqttException {
		switch(readyStep)
		{
		case 00:
						
		//try {
		
			MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("[AdsMqttClient] Connecting to broker: "+broker);
            mqttClient.connect(connOpts);
            System.out.println("[AdsMqttClient] Connected");
            connected = true;
	    
	        /*
	        } catch(MqttException me) {
	        	
	            System.out.println("reason "+me.getReasonCode());
	            System.out.println("msg "+me.getMessage());
	            System.out.println("loc "+me.getLocalizedMessage());
	            System.out.println("cause "+me.getCause());
	            System.out.println("excep "+me);
	            me.printStackTrace();
	        }
			*/
			bReadyOk = true;
			readyStep = 10;
			break;
		}
		
		
	}

	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void busy() {
		
		bBusyOk = true;
		
	}

	@Override
	protected void idle() {
		try {
			mqttClient.disconnect();
			System.out.println("Mqtt client disconnected.");
			mqttClient.close();
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
	protected void waiting() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void error() {
		// TODO Auto-generated method stub
		switch(errorStep)
		{
		case 00:
			try {
				mqttClient.disconnect();
				System.out.println("Mqtt client disconnected.");
				mqttClient.close();
				System.out.println("Mqtt client closed.");
			} catch(MqttException me) {
	            System.out.println("reason "+me.getReasonCode());
	            System.out.println("msg "+me.getMessage());
	            System.out.println("loc "+me.getLocalizedMessage());
	            System.out.println("cause "+me.getCause());
	            System.out.println("excep "+me);
	            me.printStackTrace();
	        }
			bErrorOk = true;
			errorStep = 10;
			break;
			
		case 10:
			break;
		}
		
	}
	
}