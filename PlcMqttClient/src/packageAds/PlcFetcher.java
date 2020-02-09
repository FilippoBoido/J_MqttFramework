package packageAds;

import de.beckhoff.jni.tcads.AmsAddr;
import packageMqtt.AdsMqttClient;
import packageSystem.StateMachine;
import de.beckhoff.jni.Convert;
import de.beckhoff.jni.JNIByteBuffer;
import de.beckhoff.jni.tcads.AmsAddr;
import de.beckhoff.jni.tcads.AdsCallDllFunction;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.lang.Integer;

public class PlcFetcher extends StateMachine implements MqttCallback {
	
	//The paths of the packages that need to be fetched, here.
	private static final String mqttSubscriptionStorage = "ADS.fbMqttClient.aAdsSubscriptionStorage";
	private static final String mqttPublicationStorage = "ADS.fbMqttClient.aAdsPublicationStorage";
	private static final String mqttPublicationCounter = "ADS.fbMqttClient.uiPublicationCounter";
	private static final String mqttSubscriptionCounter = "ADS.fbMqttClient.uiSubscriptionCounter";
	private static final String mqttSizeOfSubscriptions = "ADS.fbMqttClient.uiSizeOfSubscriptions";
	private static final String mqttSizeOfPublications = "ADS.fbMqttClient.uiSizeOfPublications";
	private static final String mqttPublishing = "ADS.fbMqttClient.bPublishing";
	private static final String mqttPublished = "ADS.fbMqttClient.bPublished";
	private static final String mqttSubscribed = "ADS.fbMqttClient.bSubscribed";
	private static final String mqttSubscribing = "ADS.fbMqttClient.bSubscribing";
	private static final String mqttSizeOfAdsShell = "ADS.fbMqttClient.sizeOfAdsShell";
//uiSubscriptionCounter uiPublicationCounter uiSizeOfSubscriptions uiSizeOfPublications
	
	JNIByteBuffer 
		handle_subscriptions,
		symbol_subscriptions,
		handle_publications,
		symbol_publications,
		handle_subscriptionCounter,
		symbol_subscriptionCounter,
		handle_publicationCounter,
		symbol_publicationCounter,
		handle_sizeOfSubscriptions,
		symbol_sizeOfSubscriptions,
		handle_sizeOfPublications,
		symbol_sizeOfPublications,
		handle_publishing,
		symbol_publishing,
		handle_published,
		symbol_published,
		handle_subscribed,
		symbol_subscribed,
		handle_subscribing,
		symbol_subscribing,
		handle_sizeOfAdsShell,
		symbol_sizeOfAdsShell;
	
	//The buffers for the packages that need to be fetched, here.
	

	private JNIByteBuffer buffer_sizeOfSubscriptions = new JNIByteBuffer(2);
	private JNIByteBuffer buffer_sizeOfPublications = new JNIByteBuffer(2);
	private JNIByteBuffer buffer_sizeOfAdsShell = new JNIByteBuffer(2);
	
	private JNIByteBuffer 	buffer_subscriptions,
							buffer_publications,
							buffer_publicationCounter,
							buffer_subscriptionCounter,
							buffer_publishing,
							buffer_published,
							buffer_subscribing,
							buffer_subscribed;
	
	private short 	currentSubCounter,
					currentPubCounter,
					sizeOfSubscriptions,
					sizeOfPublications,
					sizeOfAdsShell;
	 
	private AdsMqttClient adsMqttClient;
	
	AmsAddr addr;
	
	long err;
	int shell = 0;	
	
	int hdlSubscriptions,
		hdlPublications,
		hdlPublicationCounter,
		hdlSubscriptionCounter,
		hdlSizeOfSubscriptions,
		hdlSizeOfPublications,
		hdlSubscribing,
		hdlSubscribed,
		hdlPublishing,
		hdlPublished,
		hdlSizeOfAdsShell;
	
	byte[] topicByteArr = new byte[9];
	byte[] payloadByteArr = new byte[34];
	
	private FetcherThread lifePackageFetcher;
	
	public FetcherThread getLifePackageFetcher() {
		return lifePackageFetcher;
	}


	public PlcFetcher(AmsAddr addr,AdsMqttClient adsMqttClient) {
		super();
		this.addr = addr;
		this.adsMqttClient = adsMqttClient;
	}
	
	@Override
	protected void Init() {
		
		handle_subscriptions = new JNIByteBuffer(Integer.SIZE / Byte.SIZE);
		handle_publications = new JNIByteBuffer(Integer.SIZE / Byte.SIZE);
		handle_publicationCounter  = new JNIByteBuffer(Integer.SIZE/Byte.SIZE);
		handle_subscriptionCounter = new JNIByteBuffer(Integer.SIZE / Byte.SIZE);
		handle_sizeOfSubscriptions = new JNIByteBuffer(Integer.SIZE / Byte.SIZE);
		handle_sizeOfPublications = new JNIByteBuffer(Integer.SIZE / Byte.SIZE);
		handle_subscribed = new JNIByteBuffer(Integer.SIZE / Byte.SIZE);
		handle_subscribing = new JNIByteBuffer(Integer.SIZE / Byte.SIZE);
		handle_published = new JNIByteBuffer(Integer.SIZE / Byte.SIZE);
		handle_publishing = new JNIByteBuffer(Integer.SIZE / Byte.SIZE);
		handle_sizeOfAdsShell = new JNIByteBuffer(Integer.SIZE / Byte.SIZE);
		
		symbol_subscriptions = new JNIByteBuffer(mqttSubscriptionStorage.getBytes());
		symbol_publications = new JNIByteBuffer(mqttPublicationStorage.getBytes());
		symbol_sizeOfSubscriptions = new JNIByteBuffer(mqttSizeOfSubscriptions.getBytes());
		symbol_sizeOfPublications = new JNIByteBuffer(mqttSizeOfPublications.getBytes());
		symbol_publicationCounter = new JNIByteBuffer(mqttPublicationCounter.getBytes());
		symbol_subscriptionCounter = new JNIByteBuffer(mqttSubscriptionCounter.getBytes());
		symbol_subscribed = new JNIByteBuffer(mqttSubscribed.getBytes());
		symbol_subscribing = new JNIByteBuffer(mqttSubscribing.getBytes());
		symbol_published = new JNIByteBuffer(mqttPublished.getBytes());
		symbol_publishing = new JNIByteBuffer(mqttPublishing.getBytes());
		symbol_sizeOfAdsShell = new JNIByteBuffer(mqttSizeOfAdsShell.getBytes());
		
		hdlSizeOfSubscriptions = FetchSymbolHdl(handle_sizeOfSubscriptions,symbol_sizeOfSubscriptions);
		hdlSizeOfPublications = FetchSymbolHdl(handle_sizeOfPublications,symbol_sizeOfPublications);
		
		FetchSymbolToBuffer(hdlSizeOfSubscriptions, 2, buffer_sizeOfSubscriptions);
		FetchSymbolToBuffer(hdlSizeOfPublications, 2, buffer_sizeOfPublications);

		if(eStateMachine==E_StateMachine.eError)
		{
			System.out.println("[PlcFetcher] Error occured while fetching core data part 1.");
			return;		
		}
		
		sizeOfSubscriptions = Convert.ByteArrToShort(buffer_sizeOfSubscriptions.getByteArray());
		sizeOfPublications = Convert.ByteArrToShort(buffer_sizeOfPublications.getByteArray());
		buffer_subscriptions = new JNIByteBuffer(sizeOfSubscriptions);
		buffer_publications = new JNIByteBuffer(sizeOfPublications);
		
		hdlSubscriptions = FetchSymbolHdl(handle_subscriptions,symbol_subscriptions);
		hdlPublications = FetchSymbolHdl(handle_publications,symbol_publications);
		//get the max subscriptions and max publications first
		FetchSymbolToBuffer(hdlSubscriptions, sizeOfSubscriptions, buffer_subscriptions);
		FetchSymbolToBuffer(hdlPublications, sizeOfPublications, buffer_publications);
		
		hdlPublicationCounter = FetchSymbolHdl(handle_publicationCounter,symbol_publicationCounter);
		hdlSubscriptionCounter = FetchSymbolHdl(handle_subscriptionCounter,symbol_subscriptionCounter);
		
		buffer_publicationCounter = new JNIByteBuffer(2);
		buffer_subscriptionCounter = new JNIByteBuffer(2);
		FetchSymbolToBuffer(hdlPublicationCounter, 2, buffer_publicationCounter);
		FetchSymbolToBuffer(hdlSubscriptionCounter, 2, buffer_subscriptionCounter);
		
		hdlSubscribing = FetchSymbolHdl(handle_subscribing,symbol_subscribing);
		hdlPublishing = FetchSymbolHdl(handle_publishing, symbol_publishing);
		hdlSubscribed = FetchSymbolHdl(handle_subscribed,symbol_subscribed);
		hdlPublished = FetchSymbolHdl(handle_published,symbol_published);
		
		buffer_subscribing = new JNIByteBuffer(1);
		buffer_subscribed = new JNIByteBuffer(1);
		buffer_publishing = new JNIByteBuffer(1);
		buffer_published = new JNIByteBuffer(1);
		
		FetchSymbolToBuffer(hdlSubscribing,1,buffer_subscribing);
		FetchSymbolToBuffer(hdlSubscribed,1,buffer_subscribed);
		FetchSymbolToBuffer(hdlPublishing,1,buffer_publishing);
		FetchSymbolToBuffer(hdlPublished,1,buffer_published);
		
		hdlSizeOfAdsShell = FetchSymbolHdl(handle_sizeOfAdsShell, symbol_sizeOfAdsShell);
		FetchSymbolToBuffer(hdlSizeOfAdsShell,2,buffer_sizeOfAdsShell);
		sizeOfAdsShell = Convert.ByteArrToShort(buffer_sizeOfAdsShell.getByteArray());
		
		if(eStateMachine==E_StateMachine.eError)
		{
			System.out.println("[PlcFetcher] Error occured while fetching core data part 2.");
			return;		
		}
		
		
		//Debug 
		System.out.println("Size of subscriptions: " + sizeOfSubscriptions);
		System.out.println("Size of publications: "+ sizeOfPublications);
		System.out.println("Publication counter: "+Convert.ByteArrToShort(buffer_publicationCounter.getByteArray()));
		System.out.println("Subscription counter: "+Convert.ByteArrToShort(buffer_subscriptionCounter.getByteArray()));
		
		//Set callback for incoming messages
		adsMqttClient.getMqttClient().setCallback(this);
		
		
		Start(); 

	}

	synchronized void FetchSymbolToBuffer(int hdl,int size, JNIByteBuffer buffer)
	{
		long err = AdsCallDllFunction
				   .adsSyncReadReq
				   (
					   addr,
					   AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,
					   hdl,
					   size,
					   buffer
				   );
				   
		if(err!=0)
		{
			System.out.println("Error: Read by handle: 0x" + Long.toHexString(err));
		
			Fault(0);
		}
	}
	
	int FetchSymbolHdl(JNIByteBuffer handle, JNIByteBuffer symbol)
	{
		long err = AdsCallDllFunction
				.adsSyncReadWriteReq
				(
					addr,
					AdsCallDllFunction.ADSIGRP_SYM_HNDBYNAME,
					0x0,
					handle.getUsedBytesCount(),
					handle,
					symbol.getUsedBytesCount(),
					symbol
				);
			
	   if(err!=0) 
	   { 
		   System.out.println("Error: Get handle: 0x"+ Long.toHexString(err)); 
		   Fault(0);
		   return 0;
		   
	   } 
	   else 
	   {
		   System.out.println("Success: Got handle!");
	   }
		return Convert.ByteArrToInt(handle.getByteArray());
	}
	
	synchronized void WriteSymbolFromBuffer(JNIByteBuffer buffer, int hdl, int size)
	{
		// Write value by handle
		err = AdsCallDllFunction
				.adsSyncWriteReq
				(
					addr,
	                AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,
	                hdl,
	                size,
	                buffer
                );
		
		if(err!=0)
		{
			System.out.println("Error: Write by handle: 0x" + Long.toHexString(err));
			Fault(0);
			return;
		}
	}
	
	
	@Override
	protected void Ready() {
		Execute();
		
	}

	
	
	@Override
	protected void Prepare() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void Busy() {
		
		FetchSymbolToBuffer(hdlSubscribing,1,buffer_subscribing);	
		FetchSymbolToBuffer(hdlPublishing,1,buffer_publishing);
		FetchSymbolToBuffer(hdlSubscribed,1,buffer_subscribed);
		FetchSymbolToBuffer(hdlPublished,1,buffer_published);
		FetchSymbolToBuffer(hdlPublicationCounter, 2, buffer_publicationCounter);
		FetchSymbolToBuffer(hdlSubscriptionCounter, 2, buffer_subscriptionCounter);
		
		if(Convert.ByteArrToBool(buffer_subscribing.getByteArray()) == true
				&& Convert.ByteArrToBool( buffer_subscribed.getByteArray() ) == false)
		{
			//Plc wants to subscribe to new topic
			FetchSymbolToBuffer(hdlSubscriptions,sizeOfSubscriptions, buffer_subscriptions);
			
			currentSubCounter = Convert.ByteArrToShort(buffer_subscriptionCounter.getByteArray());
			System.out.println("[PlcFetcher.Busy] Subscription counter: "+currentSubCounter);
			currentSubCounter -= 2;
			
				//Extract the topic and subscribe
			shell = currentSubCounter * sizeOfAdsShell;
			
			for(int i = 0 ; i < 9 ; i++)
			{
				topicByteArr[i] = buffer_subscriptions.getByteArray()[shell+(i+1)];
				if(topicByteArr[i] == 0)
					break;
			}
			
			
			String topic = Convert.ByteArrToString(topicByteArr);
			if(adsMqttClient.Subscribe(topic))
			{
				//subscribed
				System.out.println("[PlcFetcher.Busy] Subscribed to topic: " + topic);
				WriteSymbolFromBuffer(new JNIByteBuffer(Convert.BoolToByteArr(true)), hdlSubscribed, 1);	
			}
		}
		
		if(Convert.ByteArrToBool(buffer_publishing.getByteArray() ) == true 
				&& Convert.ByteArrToBool( buffer_published.getByteArray() ) == false)
		{
			//Plc wants to publish a new topic
			FetchSymbolToBuffer(hdlPublications,sizeOfPublications, buffer_publications);
			
			currentPubCounter = Convert.ByteArrToShort(buffer_publicationCounter.getByteArray());
			System.out.println("[PlcFetcher.Busy] Publication counter: "+currentPubCounter);
			currentPubCounter -= 2;
			
			shell = currentPubCounter * sizeOfAdsShell;
			
			for(int i = 0 ; i < 9 ; i++)
			{
				topicByteArr[i] = buffer_publications.getByteArray()[shell+(i+1)];
				if(topicByteArr[i] == 0)
					break;
			}
			for(int i = 0 ; i < 34 ; i++)
			{
				payloadByteArr[i] = buffer_publications.getByteArray()[shell+(i+10)];
			}
			
			if(adsMqttClient.Publish(Convert.ByteArrToString(topicByteArr),payloadByteArr))
			{
				
				WriteSymbolFromBuffer(new JNIByteBuffer(Convert.BoolToByteArr(true)), hdlPublished, 1);	
			}
			//Published
		}	

	}

	@Override
	protected void Idle() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void Waiting() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void Error() {
		
		
	}


	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		//Search the topic in the list of subscriptions
		byte[] locTopicByteArr = new byte[9];
		byte[] locPayloadByteArr = new byte[34];
		byte[] buffer;
		String locTopic;
		JNIByteBuffer locBuffer_subscriptions = new JNIByteBuffer(sizeOfSubscriptions);
		JNIByteBuffer locBuffer_subscriptionCounter = new JNIByteBuffer(2);
		
		FetchSymbolToBuffer(hdlSubscriptions,sizeOfSubscriptions, locBuffer_subscriptions);
		FetchSymbolToBuffer(hdlSubscriptionCounter, 2, locBuffer_subscriptionCounter);
		
		short currentSubCounter = Convert.ByteArrToShort(locBuffer_subscriptionCounter.getByteArray());
		System.out.println("[PlcFetcher.messageArrived] Topic: " + topic);
		
		currentSubCounter -= 1;	
		int loops = currentSubCounter;
		System.out.println("[PlcFetcher.messageArrived] Calculated loops: " + loops);
		//currentSubCounter -= 1;
		int shell = currentSubCounter * sizeOfAdsShell;
		System.out.println("[PlcFetcher.messageArrived] Size of current subscription memory: " + shell);
		
		for(int i = 0; i < (loops * sizeOfAdsShell); i = i + sizeOfAdsShell)
		{
			System.out.println("[PlcFetcher.messageArrived] Size of current subscription offset: " + i);
			for(int k = 0 ; k < 9 ; k++)
			{
				locTopicByteArr[k] = locBuffer_subscriptions.getByteArray()[i+(k+1)];
				if(locTopicByteArr[k] == 0)
					break;
			}
			locTopic = Convert.ByteArrToString(locTopicByteArr);
			locTopic = locTopic.trim();
			topic = topic.trim();
			
			if( locTopic.equals(topic) == true )
			{
				//System.out.println("[PlcFetcher.messageArrived] Topics equal.");
				//topic found
				//write back into plc
				//locBuffer_subscriptions.setByteArray(message.getPayload());
				buffer = locBuffer_subscriptions.getByteArray();
				//bNewMessage must be set to true
				buffer[i] = 1;
				
				for(int j = 0 ; j < message.getPayload().length ; j++)
				{
					
					locPayloadByteArr[j] = message.getPayload()[j];
					buffer[(i)+12+j] = locPayloadByteArr[j];
				}
				String decodedMessage = new String(locPayloadByteArr, "UTF-8");
				System.out.println("[PlcFetcher.messageArrived] decoded message: " + decodedMessage);
				System.out.println("[PlcFetcher.messageArrived] Topic found in plc.");
				locBuffer_subscriptions.setByteArray(buffer, false);
				WriteSymbolFromBuffer(locBuffer_subscriptions, hdlSubscriptions, sizeOfSubscriptions);	
				return;
			}
		}
	}


	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
		
	}


}


