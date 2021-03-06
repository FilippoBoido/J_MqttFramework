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


public class PlcEventDrivenFetcher extends PlcFetcher implements CallbackListenerAdsState {

	
	AdsNotificationAttrib attr = new AdsNotificationAttrib();
    AdsCallbackObject callObject = new AdsCallbackObject();
    
    JNILong subscribingNotification,
    		subscribedNotification,
    		publishingNotification,
    		publishedNotification,
    		publishingMessageNotification,
    		publishedMessageNotification,
    		subscriptionCounterNotification,
    		publicationCounterNotification;
    
    int notificationID,
    	adsSubscribingHandle,
    	adsSubscribedHandle,
    	adsPublishingHandle,
    	adsPublishedHandle,
    	adsPublishingMessageHandle,
    	adsPublishedMessageHandle,
    	adsSubscriptionCounterHandle,
    	adsPublicationCounterHandle;
    
    boolean adsSubscribing,
    		adsOldSubscribing,
			adsSubscribed,
			adsPublishing,
			adsOldPublishing,
			adsPublished,
			adsPublishingMessage,
			adsOldPublishingMessage,
			adsPublishedMessage,
    		subscribingNotificationSignal,
    		publishingNotificationSignal,
    		publishingMessageNotificationSignal;
    		
    
	int adsSubscriptionCounter,
		adsPublicationCounter,
		adsPublishingStep,
		adsPublishingMessageStep,
		adsSubscribingStep;
	
	
	
	public PlcEventDrivenFetcher(AmsAddr addr, AdsMqttClient adsMqttClient) {
		super(addr, adsMqttClient);
	}

	@Override
	protected void init() throws AdsConnectionException {
		
		super.init();
			
		attr.setCbLength(1);
	    attr.setNTransMode(AdsConstants.ADSTRANS_SERVERCYCLE);
	    attr.setDwChangeFilter(1000000);   // 1 sec
	    attr.setNMaxDelay(2000000);        // 1 sec
		
	}
	
	@Override
	protected void ready() throws AdsConnectionException {
		switch(readyStep)
		{
		case 00:
				
			super.ready();
			
			adsPublishingStep = 0;
			adsPublishingMessageStep = 0;
			adsSubscribingStep = 0;
			
			
			adsSubscribingHandle = Convert.ByteArrToInt(handle_subscribing.getByteArray());
	    	adsSubscribedHandle = Convert.ByteArrToInt(handle_subscribed.getByteArray());
	    	adsPublishingHandle = Convert.ByteArrToInt(handle_publishing.getByteArray());
	    	adsPublishedHandle = Convert.ByteArrToInt(handle_published.getByteArray());
	    	adsSubscriptionCounterHandle = Convert.ByteArrToInt(handle_subscriptionCounter.getByteArray());
	    	adsPublicationCounterHandle = Convert.ByteArrToInt(handle_publicationCounter.getByteArray());
			adsPublishingMessageHandle = Convert.ByteArrToInt(handle_publishingMessage.getByteArray());
			adsPublishedMessageHandle = Convert.ByteArrToInt(handle_publishedMessage.getByteArray());
	    		    	
	    	subscribingNotification = new JNILong(adsSubscribingHandle);
			subscribedNotification = new JNILong(adsSubscribedHandle);
			publishingNotification = new JNILong(adsPublishingHandle);
			publishedNotification = new JNILong(adsPublishedHandle);
			subscriptionCounterNotification = new JNILong(adsSubscriptionCounterHandle);
			publicationCounterNotification = new JNILong(adsPublicationCounterHandle);	
			publishingMessageNotification = new JNILong(adsPublishingMessageHandle);
			publishedMessageNotification = new JNILong(adsPublishedMessageHandle);
			
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
			
			// Create notificationHandle
			err = AdsCallDllFunction.adsSyncAddDeviceNotificationReq(
			        addr,
			        AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,     // IndexGroup
			        adsPublishingMessageHandle,        	// IndexOffset
			        attr,       		// The defined AdsNotificationAttrib object
			        adsPublishingMessageHandle,         	// Choose arbitrary number
			        publishingMessageNotification);
			
			if(err!=0) { 
				
				exceptionMessage = "Error adding device notification: 0x" + Long.toHexString(err);
			    throw new AdsConnectionException(exceptionMessage,new AdsConnectionException());
			}
			/*
			// Create notificationHandle
			err = AdsCallDllFunction.adsSyncAddDeviceNotificationReq(
			        addr,
			        AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,     // IndexGroup
			        adsPublishedMessageHandle,        	// IndexOffset
			        attr,       		// The defined AdsNotificationAttrib object
			        adsPublishedMessageHandle,         	// Choose arbitrary number
			        publishedMessageNotification);
			
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
		
	}

	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void busy() throws AdsConnectionException , UnsupportedEncodingException{	
		
		/*******************************************************************************************************************************************************************/
		/* Subscribing routine */
		/*******************************************************************************************************************************************************************/
		
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
			hdlSubscriptions = fetchSymbolHdl(handle_subscriptions,symbol_subscriptions);
			fetchSymbolToBuffer(new String(symbol_subscriptions.getByteArray()),hdlSubscriptions,sizeOfSubscriptions, buffer_subscriptions);
			
			hdlSubscriptionCounter = fetchSymbolHdl(handle_subscriptionCounter,symbol_subscriptionCounter);
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
				hdlSubscribed = fetchSymbolHdl(handle_subscribed,symbol_subscribed);
				writeSymbolFromBuffer(new JNIByteBuffer(Convert.BoolToByteArr(true)), hdlSubscribed, 1);	
			}
			
			adsSubscribingStep = 00;
			break;
			
		case 20:
			/*
			hdlSubscribed = fetchSymbolHdl(handle_subscribed,symbol_subscribed);
			fetchSymbolToBuffer(new String(symbol_subscribed.getByteArray()),hdlSubscribed,1,buffer_subscribed);
			
			if(Convert.ByteArrToBool(buffer_subscribed.getByteArray()))
				adsSubscribingStep = 00;
			*/
			break;
			
		}
		
		/*******************************************************************************************************************************************************************/
		/* Publishing Payload routine */
		/*******************************************************************************************************************************************************************/
		
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
			fetchSymbolToBuffer(new String(symbol_publications.getByteArray()),hdlPublications,sizeOfPublications, buffer_publications);
			
			hdlPublicationCounter = fetchSymbolHdl(handle_publicationCounter,symbol_publicationCounter);
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
				hdlPublished = fetchSymbolHdl(handle_published,symbol_published);
				writeSymbolFromBuffer(new JNIByteBuffer(Convert.BoolToByteArr(true)), hdlPublished, 1);	
			}
			adsPublishingStep = 00;
			
			break;
			
		case 20:
			/*
			hdlPublished = fetchSymbolHdl(handle_published,symbol_published);
			fetchSymbolToBuffer(new String(symbol_published.getByteArray()),hdlPublished,1,buffer_published);
			
			if(Convert.ByteArrToBool(buffer_published.getByteArray()))
				adsPublishingStep = 00;
			*/
			break;
				
		}
		
		/*******************************************************************************************************************************************************************/
		/* Publishing Message routine */
		/*******************************************************************************************************************************************************************/
		
		switch(adsPublishingMessageStep)
		{
		
		case 00:
			
			if(publishingMessageNotificationSignal == true)
			{
				publishingMessageNotificationSignal = false;
				adsPublishingMessageStep = 10;
			}
			
			break;
			
		case 10:
			
			hdlMessageCounter = fetchSymbolHdl(handle_messageCounter,symbol_messageCounter);
			fetchSymbolToBuffer(new String(symbol_messageCounter.getByteArray()),hdlMessageCounter,2, buffer_messageCounter);
			
			int counter = Convert.ByteArrToShort(buffer_messageCounter.getByteArray());
			
			hdlMessages[(counter-1)] = fetchSymbolHdl(handles_messages[(counter-1)],symbol_messages[(counter-1)]);
			fetchSymbolToBuffer(new String(symbol_messages[(counter-1)] .getByteArray()),hdlMessages[(counter-1)], SIZE_OF_PLC_MAX_STRING, buffer_messages[(counter-1)]);
			hdlTopics[(counter-1)] = fetchSymbolHdl(handles_topics[(counter-1)],symbol_topics[(counter-1)]);
			fetchSymbolToBuffer(new String(symbol_topics[(counter-1)] .getByteArray()),hdlTopics[(counter-1)], SIZE_OF_PLC_STRING, buffer_topics[(counter-1)]);
			
			if(adsMqttClient.publish(Convert.ByteArrToString(buffer_topics[(counter-1)].getByteArray()),Convert.ByteArrToString(buffer_messages[(counter-1)].getByteArray())))
			{		
				hdlPublishedMessage = fetchSymbolHdl(handle_publishedMessage,symbol_publishedMessage);
				writeSymbolFromBuffer(new JNIByteBuffer(Convert.BoolToByteArr(true)), hdlPublishedMessage, 1);	
				System.out.println("[PlcEventDrivenFetcher.busy] Variable: " + new String(symbol_publishedMessage.getByteArray()) + " written to plc.");
			}
			
			adsPublishingMessageStep = 00;
			
			break;
			
		case 20:
			
			/*
			hdlPublishedMessage = fetchSymbolHdl(handle_publishedMessage,symbol_publishedMessage);
			fetchSymbolToBuffer(new String(symbol_publishedMessage.getByteArray()),hdlPublishedMessage,1,buffer_publishedMessage);
			System.out.println("[PlcEventDrivenFetcher.busy] Variable: " + new String(symbol_publishedMessage.getByteArray()) 
					+ " read from plc with value: " + Convert.ByteArrToBool(buffer_publishedMessage.getByteArray()));
			
			if(Convert.ByteArrToBool(buffer_publishedMessage.getByteArray()))
				adsPublishingMessageStep = 00;
			*/
			
			break;
				
		}
		
		/*******************************************************************************************************************************************************************/
		/* Check for new mqtt messages
		/*******************************************************************************************************************************************************************/
		
		if(!adsMqttClient.getAdsMessageList().isEmpty())
		{
			AdsMessage adsMessage = adsMqttClient.getAdsMessageList().remove(0);
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
			System.out.println("[PlcEventDrivenFetcher.busy] Topic: " + adsMessage.getTopic());
			
			currentSubCounter -= 1;	
			int loops = currentSubCounter;
			System.out.println("[PlcEventDrivenFetcher.busy] Calculated loops: " + loops);
			//currentSubCounter -= 1;
			int shell = currentSubCounter * sizeOfAdsShell;
			System.out.println("[PlcEventDrivenFetcher.busy] Size of current subscription memory: " + shell);
			
			for(int i = 0; i < (loops * sizeOfAdsShell); i = i + sizeOfAdsShell)
			{
				System.out.println("[PlcEventDrivenFetcher.busy] Size of current subscription offset: " + i);
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
					System.out.println("[PlcEventDrivenFetcher.busy] Payload length: " + adsMessage.getMessage().getPayload().length);
					for(int j = 0 ; j < adsMessage.getMessage().getPayload().length ; j++)
					{
						
						locPayloadByteArr[j] = adsMessage.getMessage().getPayload()[j];
						buffer[(i)+12+j] = locPayloadByteArr[j];
					}
					
					String decodedMessage = new String(locPayloadByteArr, "UTF-8");
					System.out.println("[PlcEventDrivenFetcher.busy] decoded message: " + decodedMessage);
					System.out.println("[PlcEventDrivenFetcher.busy] Topic found in plc.");
					locBuffer_subscriptions.setByteArray(buffer, false);
					long start = System.nanoTime();   	
					writeSymbolFromBuffer(locBuffer_subscriptions, hdlSubscriptions, sizeOfSubscriptions);	
					long elapsedTime = System.nanoTime() - start;
					System.out.println("[PlcEventDrivenFetcher.busy] ADS-Transfer time: " + elapsedTime);
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
			errorStep = 10;
			
			bErrorOk = true;
			
			break;
			
		case 10:
			break;
		}
		
	}
	
	@Override
	protected void shuttingDown() throws Throwable {
		// TODO Auto-generated method stub
		super.shuttingDown();
		
		switch(shutDownStep)
		{
		case 00:
			
			callObject.removeListenerCallbackAdsState(this);
			shutDownStep = 10;
			
			bShutDownOk = true;
			
			break;
			
		case 10:
			break;
		}
	}
	
	public synchronized void onEvent(AmsAddr addr,AdsNotificationHeader notification,long user) {
    		
		Long lUser = new Long(user);
	
		if(lUser.intValue() == adsSubscribingHandle)
		{
			adsSubscribing =  Convert.ByteArrToBool(notification.getData());
			
			if(adsSubscribing == true)
			{
				System.out.println("[PlcEventDrivenFetcher.onEvent] adsSubscribingHandle Value: " + adsSubscribing);
				subscribingNotificationSignal = true;
			}
				
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
			{
				System.out.println("[PlcEventDrivenFetcher.onEvent] adsPublishingHandle Value: " + adsPublishing);
				publishingNotificationSignal = true;
			}
			
				
		}
		else if(lUser.intValue() == adsPublishedHandle)
		{
			adsPublished = Convert.ByteArrToBool(notification.getData());
			System.out.println("[PlcEventDrivenFetcher.onEvent] adsPublishedHandle Value: " + adsPublishedHandle);
		}
		else if(lUser.intValue() == adsPublishingMessageHandle)
		{
			
			adsPublishingMessage = Convert.ByteArrToBool(notification.getData());
			
			if(adsPublishingMessage && (adsPublishingMessage!=adsOldPublishingMessage))
			{
				
				System.out.println("[PlcEventDrivenFetcher.onEvent] adsPublishingMessageHandle Value: " + adsPublishingMessage);
				publishingMessageNotificationSignal = true;
			}
			
			adsOldPublishingMessage = adsPublishingMessage;
				
		}
		else if(lUser.intValue() == adsPublishedMessageHandle)
		{
			adsPublishedMessage = Convert.ByteArrToBool(notification.getData());
			System.out.println("[PlcEventDrivenFetcher.onEvent] adsPublishedMessageHandle Value: " + adsPublishedMessage);
		}
			
	}

}

