package packageAds;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class AdsMessage {

	String topic;
	MqttMessage message;
	
	public MqttMessage getMessage()
	{
		return message;
	}
	
	public String getTopic()
	{
		return topic;
	}
	
	public AdsMessage(String topic, MqttMessage message) {
		
		this.topic = topic;
		this.message = message;
		
	}
	
}
