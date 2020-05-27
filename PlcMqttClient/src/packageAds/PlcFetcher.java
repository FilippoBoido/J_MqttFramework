package packageAds;

import de.beckhoff.jni.tcads.AmsAddr;
import packageExceptions.AdsConnectionException;
import packageMqtt.AdsMqttClient;
import packageSystem.StateMachine;
import de.beckhoff.jni.Convert;
import de.beckhoff.jni.JNIByteBuffer;
import de.beckhoff.jni.tcads.AdsCallDllFunction;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.lang.Integer;
import java.util.ArrayList;


public class PlcFetcher extends StateMachine implements MqttCallback {
	
	//The paths of the packages that need to be fetched, here.
	protected static final String mqttSubscriptionStorage = "ADS.fbMqttClient.aAdsSubscriptionStorage";
	protected static final String mqttPublicationStorage = "ADS.fbMqttClient.aAdsPublicationStorage";
	protected static final String mqttPublicationCounter = "ADS.fbMqttClient.uiPublicationCounter";
	protected static final String mqttSubscriptionCounter = "ADS.fbMqttClient.uiSubscriptionCounter";
	protected static final String mqttSizeOfSubscriptions = "ADS.fbMqttClient.uiSizeOfSubscriptions";
	protected static final String mqttSizeOfPublications = "ADS.fbMqttClient.uiSizeOfPublications";
	protected static final String mqttPublishing = "ADS.fbMqttClient.bPublishing";
	protected static final String mqttPublished = "ADS.fbMqttClient.bPublished";
	protected static final String mqttSubscribed = "ADS.fbMqttClient.bSubscribed";
	protected static final String mqttSubscribing = "ADS.fbMqttClient.bSubscribing";
	protected static final String mqttSizeOfAdsShell = "ADS.fbMqttClient.sizeOfAdsShell";
//uiSubscriptionCounter uiPublicationCounter uiSizeOfSubscriptions uiSizeOfPublications
	
	protected JNIByteBuffer handle_subscriptions,
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
	

	protected JNIByteBuffer buffer_sizeOfSubscriptions = new JNIByteBuffer(2);
	protected JNIByteBuffer buffer_sizeOfPublications = new JNIByteBuffer(2);
	protected JNIByteBuffer buffer_sizeOfAdsShell = new JNIByteBuffer(2);
	
	protected JNIByteBuffer buffer_subscriptions,
							buffer_publications,
							buffer_publicationCounter,
							buffer_subscriptionCounter,
							buffer_publishing,
							buffer_published,
							buffer_subscribing,
							buffer_subscribed;
	
	protected short currentSubCounter,
					currentPubCounter,
					sizeOfSubscriptions,
					sizeOfPublications,
					sizeOfAdsShell;
	 
	protected AdsMqttClient adsMqttClient;
	
	AmsAddr addr;
	
	long err;
	int shell = 0;	
	
	protected int 	hdlSubscriptions,
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
	
	ArrayList<AdsMessage> adsMessageList = new ArrayList<AdsMessage> ();

	public PlcFetcher(AmsAddr addr,AdsMqttClient adsMqttClient) {
		super();
		this.addr = addr;
		this.adsMqttClient = adsMqttClient;
	}
	
	@Override
	protected void init() {
		
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

	}

	synchronized void FetchSymbolToBuffer(int hdl,int size, JNIByteBuffer buffer) throws AdsConnectionException
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
			exceptionMessage = "Error: Read by handle: 0x" + Long.toHexString(err);
			throw new AdsConnectionException(exceptionMessage,new AdsConnectionException());
		}
	}
	
	int FetchSymbolHdl(JNIByteBuffer handle, JNIByteBuffer symbol) throws AdsConnectionException
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
		   
		   exceptionMessage = "Error: Get handle: 0x"+ Long.toHexString(err);
		   throw new AdsConnectionException(exceptionMessage,new AdsConnectionException());
		   
	   } 
	   else 
	   {
		   System.out.println("Success: Got handle!");
	   }
	   return Convert.ByteArrToInt(handle.getByteArray());
	}
	
	synchronized void WriteSymbolFromBuffer(JNIByteBuffer buffer, int hdl, int size) throws AdsConnectionException
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
			exceptionMessage = "Error: Write by handle: 0x" + Long.toHexString(err);
			throw new AdsConnectionException(exceptionMessage,new AdsConnectionException());
		}
	}
	
	
	@Override
	protected void ready() throws AdsConnectionException {
		
		hdlSizeOfSubscriptions = FetchSymbolHdl(handle_sizeOfSubscriptions,symbol_sizeOfSubscriptions);
		hdlSizeOfPublications = FetchSymbolHdl(handle_sizeOfPublications,symbol_sizeOfPublications);
		
		FetchSymbolToBuffer(hdlSizeOfSubscriptions, 2, buffer_sizeOfSubscriptions);
		FetchSymbolToBuffer(hdlSizeOfPublications, 2, buffer_sizeOfPublications);

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
		
		//Debug 
		System.out.println("Size of subscriptions: " + sizeOfSubscriptions);
		System.out.println("Size of publications: "+ sizeOfPublications);
		System.out.println("Publication counter: "+Convert.ByteArrToShort(buffer_publicationCounter.getByteArray()));
		System.out.println("Subscription counter: "+Convert.ByteArrToShort(buffer_subscriptionCounter.getByteArray()));
		
	}

	
	
	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void busy() throws AdsConnectionException, UnsupportedEncodingException {

	}

	@Override
	protected void idle() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void waiting() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void error() {
		
		
	}


	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		
		adsMessageList.add(new AdsMessage(topic,message));
		
	}


	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
		
	}


}


