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
	private MqttClient sampleClient;
	private boolean connected;
	//private JNIByteBuffer buffer;


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
		if(sampleClient != null && connected)
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
	protected void Init() {
		// TODO Auto-generated method stub
		Start();
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
	            connected = true;
	            
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