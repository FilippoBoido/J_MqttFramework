package packageAds;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import de.beckhoff.jni.AdsConstants;
import de.beckhoff.jni.Convert;
import de.beckhoff.jni.JNIByteBuffer;
import de.beckhoff.jni.JNILong;
import de.beckhoff.jni.tcads.AdsCallDllFunction;
import de.beckhoff.jni.tcads.AdsCallbackObject;
import de.beckhoff.jni.tcads.AdsNotificationAttrib;
import de.beckhoff.jni.tcads.AdsNotificationHeader;
import de.beckhoff.jni.tcads.AmsAddr;
import de.beckhoff.jni.tcads.CallbackListenerAdsState;
import packageExceptions.AdsConnectionException;
import packageMqtt.AdsMqttClient;


public class PlcEventDrivenFetcher extends PlcFetcher implements MqttCallback,CallbackListenerAdsState {

	
	AdsNotificationAttrib attr = new AdsNotificationAttrib();
    AdsCallbackObject callObject = new AdsCallbackObject();
    
    JNILong subscribingNotification,
    		subscribedNotification,
    		publishingNotification,
    		publishedNotification,
    		subscriptionCounterNotification,
    		publicationCounterNotification;
    
    int notificationID,
    	adsSubscribingHandle,
    	adsSubscribedHandle,
    	adsPublishingHandle,
    	adsPublishedHandle,
    	adsSubscriptionCounterHandle,
    	adsPublicationCounterHandle;
    
    boolean adsSubscribing,
			adsSubscribed,
			adsPublishing,
			adsPublished,
    		subscribingNotificationSignal,
    		publishingNotificationSignal;
    
	int adsSubscriptionCounter,
		adsPublicationCounter,
		adsPublishingStep,
		adsSubscribingStep;
	
	public PlcEventDrivenFetcher(AmsAddr addr, AdsMqttClient adsMqttClient) {
		super(addr, adsMqttClient);
	}

	@Override
	protected void init() {
		
		super.init();
			
		attr.setCbLength(1);
	    attr.setNTransMode(AdsConstants.ADSTRANS_SERVERONCHA);
	    attr.setDwChangeFilter(10000000);   // 1 sec
	    attr.setNMaxDelay(20000000);        // 2 sec
		
	}
	
	@Override
	protected void ready() throws AdsConnectionException {
		switch(readyStep)
		{
		case 00:
				
			super.ready();
			
			adsSubscribingHandle = Convert.ByteArrToInt(handle_subscribing.getByteArray());
	    	adsSubscribedHandle = Convert.ByteArrToInt(handle_subscribed.getByteArray());
	    	adsPublishingHandle = Convert.ByteArrToInt(handle_publishing.getByteArray());
	    	adsPublishedHandle = Convert.ByteArrToInt(handle_published.getByteArray());
	    	adsSubscriptionCounterHandle = Convert.ByteArrToInt(handle_subscriptionCounter.getByteArray());
	    	adsPublicationCounterHandle = Convert.ByteArrToInt(handle_publicationCounter.getByteArray());
			
	    	subscribingNotification = new JNILong(adsSubscribingHandle);
			subscribedNotification = new JNILong(adsSubscribedHandle);
			publishingNotification = new JNILong(adsPublishingHandle);
			publishedNotification = new JNILong(adsPublishedHandle);
			subscriptionCounterNotification = new JNILong(adsSubscriptionCounterHandle);
			publicationCounterNotification = new JNILong(adsPublicationCounterHandle);	
			
		    callObject.addListenerCallbackAdsState(this);
			
			// Create notificationHandle
			err = AdsCallDllFunction.adsSyncAddDeviceNotificationReq(
			        addr,
			        AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,     // IndexGroup
			        adsSubscribingHandle,        	// IndexOffset
			        attr,       		// The defined AdsNotificationAttrib object
			        adsSubscribingHandle,         	// Choose arbitrary number
			        subscribingNotification);
			
			if(err!=0) { 
				
				exceptionMessage = "Error adding device notification: 0x" + Long.toHexString(err);
				throw new AdsConnectionException(exceptionMessage,new AdsConnectionException());
			    
			}
			
			/*
			err = AdsCallDllFunction.adsSyncAddDeviceNotificationReq(
			        addr,
			        AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,     // IndexGroup
			        adsSubscribedHandle,        	// IndexOffset
			        attr,       		// The defined AdsNotificationAttrib object
			        adsSubscribedHandle,         	// Choose arbitrary number
			        subscribedNotification);
			
			if(err!=0) { 
			   
			    exceptionMessage = "Error adding device notification: 0x" + Long.toHexString(err);
			    throw new AdsConnectionException(exceptionMessage,new AdsConnectionException());
			}
			*/
			// Create notificationHandle
			err = AdsCallDllFunction.adsSyncAddDeviceNotificationReq(
			        addr,
			        AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,     // IndexGroup
			        adsPublishingHandle,        	// IndexOffset
			        attr,       		// The defined AdsNotificationAttrib object
			        adsPublishingHandle,         	// Choose arbitrary number
			        publishingNotification);
			
			if(err!=0) { 
				
				exceptionMessage = "Error adding device notification: 0x" + Long.toHexString(err);
			    throw new AdsConnectionException(exceptionMessage,new AdsConnectionException());
			}
			
			/*
			err = AdsCallDllFunction.adsSyncAddDeviceNotificationReq(
			        addr,
			        AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,     // IndexGroup
			        adsPublishedHandle,        	// IndexOffset
			        attr,       		// The defined AdsNotificationAttrib object
			        adsPublishedHandle,         	// Choose arbitrary number
			        publishedNotification);
			
			if(err!=0) { 
				
				exceptionMessage = "Error adding device notification: 0x" + Long.toHexString(err);
			    throw new AdsConnectionException(exceptionMessage,new AdsConnectionException());
			}
			*/
			bReadyOk = true;
			readyStep = 10;
			break;
				
		case 10:
			break;
				
		}
		
		/*
		//change size to 2 bytes for the subscription and publication counter
		attr.setCbLength(2);
	
		// Create notificationHandle
		err = AdsCallDllFunction.adsSyncAddDeviceNotificationReq(
		        addr,
		        AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,     // IndexGroup
		        adsSubscriptionCounterHandle,        	// IndexOffset
		        attr,       		// The defined AdsNotificationAttrib object
		        adsSubscriptionCounterHandle,         	// Choose arbitrary number
		        subscriptionCounterNotification);
		
		if(err!=0) { 
		    System.out.println("Error: Add notification: 0x" 
		            + Long.toHexString(err)); 
		}
		
		// Create notificationHandle
		err = AdsCallDllFunction.adsSyncAddDeviceNotificationReq(
		        addr,
		        AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,     // IndexGroup
		        adsPublicationCounterHandle,        	// IndexOffset
		        attr,       		// The defined AdsNotificationAttrib object
		        adsPublicationCounterHandle,         	// Choose arbitrary number
		        publicationCounterNotification);
		
		if(err!=0) { 
		    System.out.println("Error: Add notification: 0x" 
		            + Long.toHexString(err)); 
		}
		*/
	}

	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void busy() throws AdsConnectionException , UnsupportedEncodingException{	
		
		switch(adsSubscribingStep)
		{
		
		case 00:
			
			if(subscribingNotificationSignal == true)
			{
				subscribingNotificationSignal = false;
				adsSubscribingStep = 10;
			}
			
			break;
			
		case 10:
			
			//Plc wants to subscribe to new topic
			fetchSymbolToBuffer(new String(symbol_subscriptions.getByteArray()),hdlSubscriptions,sizeOfSubscriptions, buffer_subscriptions);
			fetchSymbolToBuffer(new String(symbol_subscriptionCounter.getByteArray()),hdlSubscriptionCounter, 2, buffer_subscriptionCounter);
			
			currentSubCounter = Convert.ByteArrToShort(buffer_subscriptionCounter.getByteArray());
			System.out.println("[PlcEventDrivenFetcher.Busy] Subscription counter: "+ currentSubCounter);
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
			if(adsMqttClient.subscribe(topic))
			{
				//subscribed
				System.out.println("[PlcEventDrivenFetcher.Busy] Subscribed to topic: " + topic);
				writeSymbolFromBuffer(new JNIByteBuffer(Convert.BoolToByteArr(true)), hdlSubscribed, 1);	
			}
			
			adsSubscribingStep = 20;
			break;
			
		case 20:
			
			fetchSymbolToBuffer(new String(symbol_subscribed.getByteArray()),hdlSubscribed,1,buffer_subscribed);
			
			if(Convert.ByteArrToBool(buffer_subscribed.getByteArray()))
				adsSubscribingStep = 00;
			
			break;
			
		}
		
		
		
		switch(adsPublishingStep)
		{
		
		case 00:
			
			if(publishingNotificationSignal == true)
			{
				publishingNotificationSignal = false;
				adsPublishingStep = 10;
			}
			
			break;
			
		case 10:
			
			hdlPublications = fetchSymbolHdl(handle_publications,symbol_publications);
			//System.out.println("[PlcEventDrivenFetcher.Busy] hdlPublications: "+hdlPublications + " sizeOfPublications: " +sizeOfPublications+" length of buffer: "+ buffer_publications.getByteArray().length);
			fetchSymbolToBuffer(new String(symbol_publications.getByteArray()),hdlPublications,sizeOfPublications, buffer_publications);
			fetchSymbolToBuffer(new String(symbol_publicationCounter.getByteArray()),hdlPublicationCounter, 2, buffer_publicationCounter);
			
			currentPubCounter = Convert.ByteArrToShort(buffer_publicationCounter.getByteArray());
			System.out.println("[PlcEventDrivenFetcher.Busy] Publication counter: "+currentPubCounter);
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
			
			if(adsMqttClient.publish(Convert.ByteArrToString(topicByteArr),payloadByteArr))
			{
				
				writeSymbolFromBuffer(new JNIByteBuffer(Convert.BoolToByteArr(true)), hdlPublished, 1);	
			}
			adsPublishingStep = 20;
			
			break;
			
		case 20:
			
			fetchSymbolToBuffer(new String(symbol_published.getByteArray()),hdlPublished,1,buffer_published);
			
			if(Convert.ByteArrToBool(buffer_published.getByteArray()))
				adsPublishingStep = 00;
			
			break;
				
		}
		
		if(!adsMessageList.isEmpty())
		{
			AdsMessage adsMessage = adsMessageList.remove(0);
			//Search the topic in the list of subscriptions
			byte[] locTopicByteArr = new byte[9];
			byte[] locPayloadByteArr = new byte[34];
			byte[] buffer;
			String locTopic;
			JNIByteBuffer locBuffer_subscriptions = new JNIByteBuffer(sizeOfSubscriptions);
			JNIByteBuffer locBuffer_subscriptionCounter = new JNIByteBuffer(2);
			
			fetchSymbolToBuffer(new String(symbol_sizeOfSubscriptions.getByteArray()),hdlSubscriptions,sizeOfSubscriptions, locBuffer_subscriptions);
			fetchSymbolToBuffer(new String(symbol_subscriptionCounter.getByteArray()),hdlSubscriptionCounter, 2, locBuffer_subscriptionCounter);
			
			short currentSubCounter = Convert.ByteArrToShort(locBuffer_subscriptionCounter.getByteArray());
			System.out.println("[PlcEventDrivenFetcher.messageArrived] Topic: " + adsMessage.getTopic());
			
			currentSubCounter -= 1;	
			int loops = currentSubCounter;
			System.out.println("[PlcEventDrivenFetcher.messageArrived] Calculated loops: " + loops);
			//currentSubCounter -= 1;
			int shell = currentSubCounter * sizeOfAdsShell;
			System.out.println("[PlcEventDrivenFetcher.messageArrived] Size of current subscription memory: " + shell);
			
			for(int i = 0; i < (loops * sizeOfAdsShell); i = i + sizeOfAdsShell)
			{
				System.out.println("[PlcEventDrivenFetcher.messageArrived] Size of current subscription offset: " + i);
				for(int k = 0 ; k < 9 ; k++)
				{
					locTopicByteArr[k] = locBuffer_subscriptions.getByteArray()[i+(k+1)];
					if(locTopicByteArr[k] == 0)
						break;
				}
				locTopic = Convert.ByteArrToString(locTopicByteArr);
				locTopic = locTopic.trim();
				//topic = topic.trim();
				
				if( locTopic.equals(adsMessage.getTopic()) == true )
				{
					//System.out.println("[PlcFetcher.messageArrived] Topics equal.");
					//topic found
					//write back into plc
					//locBuffer_subscriptions.setByteArray(message.getPayload());
					buffer = locBuffer_subscriptions.getByteArray();
					//bNewMessage must be set to true
					buffer[i] = 1;
					
					for(int j = 0 ; j < adsMessage.getMessage().getPayload().length ; j++)
					{
						
						locPayloadByteArr[j] = adsMessage.getMessage().getPayload()[j];
						buffer[(i)+12+j] = locPayloadByteArr[j];
					}
					
					String decodedMessage = new String(locPayloadByteArr, "UTF-8");
					System.out.println("[PlcEventDrivenFetcher.messageArrived] decoded message: " + decodedMessage);
					System.out.println("[PlcEventDrivenFetcher.messageArrived] Topic found in plc.");
					locBuffer_subscriptions.setByteArray(buffer, false);
					long start = System.nanoTime();   	
					writeSymbolFromBuffer(locBuffer_subscriptions, hdlSubscriptions, sizeOfSubscriptions);	
					long elapsedTime = System.nanoTime() - start;
					System.out.println("[PlcEventDrivenFetcher.messageArrived] ADS-Transfer time: " + elapsedTime);
					return;
				}
			}
		}
		
		bBusyOk = true;
	
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
		super.error();
		switch(errorStep)
		{
		case 00:
			callObject.removeListenerCallbackAdsState(this);
			bErrorOk = true;
			errorStep = 10;
			break;
			
		case 10:
			break;
		}
		
	}
		
	@Override
	public void connectionLost(Throwable cause) {
		super.connectionLost(cause);
		
	}

	
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		super.messageArrived(topic, message);
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		super.deliveryComplete(token);
		
	}
	
	public synchronized void onEvent(AmsAddr addr,AdsNotificationHeader notification,long user) {
    		
		Long lUser = new Long(user);
	
		if(lUser.intValue() == adsSubscribingHandle)
		{
			adsSubscribing =  Convert.ByteArrToBool(notification.getData());
			
			if(adsSubscribing == true)
				subscribingNotificationSignal = true;
			
			System.out.println("[PlcEventDrivenFetcher.onEvent] adsSubscribingHandle Value: " + adsSubscribing);	
		}
		else if(lUser.intValue() == adsSubscribedHandle)
		{
			adsSubscribed =  Convert.ByteArrToBool(notification.getData());
			System.out.println("[PlcEventDrivenFetcher.onEvent] adsSubscribedHandle Value: " + adsSubscribed);
		}
		else if(lUser.intValue() == adsPublishingHandle)
		{
			
			adsPublishing = Convert.ByteArrToBool(notification.getData());
			
			if(adsPublishing)
				publishingNotificationSignal = true;
			
			System.out.println("[PlcEventDrivenFetcher.onEvent] adsPublishingHandle Value: " + adsPublishing);
			
				
		}
		else if(lUser.intValue() == adsPublishedHandle)
		{
			adsPublished = Convert.ByteArrToBool(notification.getData());
			System.out.println("[PlcEventDrivenFetcher.onEvent] adsPublishedHandle Value: " + adsPublished);
		}
			
	}
		/*
		else if(lUser.intValue() == adsSubscriptionCounterHandle)
		{
			adsSubscriptionCounter = Convert.ByteArrToShort(notification.getData());
			System.out.println("adsSubscriptionCounterHandle Value:\t\t"+ adsSubscriptionCounter);
		}
		else if(lUser.intValue() == adsPublicationCounterHandle)
		{
			adsPublicationCounter = Convert.ByteArrToShort(notification.getData());
			System.out.println("adsPublicationCounterHandle Value:\t\t"+ adsPublicationCounter);
		}
		*/
	

}

