package packageAds;

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
		adsPublicationCounter;
	
	public PlcEventDrivenFetcher(AmsAddr addr, AdsMqttClient adsMqttClient) {
		super(addr, adsMqttClient);
	}

	@Override
	protected void Init() {
		
		super.Init();
		
	}
	
	@Override
	protected void Ready() {
		super.Ready();
		
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
    	
		attr.setCbLength(1);
	    attr.setNTransMode(AdsConstants.ADSTRANS_SERVERONCHA);
	    attr.setDwChangeFilter(10000000);   // 1 sec
	    attr.setNMaxDelay(20000000);        // 2 sec
	    
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
		    System.out.println("Error: Add notification: 0x" 
		            + Long.toHexString(err)); 
		}
		
		// Create notificationHandle
		err = AdsCallDllFunction.adsSyncAddDeviceNotificationReq(
		        addr,
		        AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,     // IndexGroup
		        adsSubscribedHandle,        	// IndexOffset
		        attr,       		// The defined AdsNotificationAttrib object
		        adsSubscribedHandle,         	// Choose arbitrary number
		        subscribedNotification);
		
		if(err!=0) { 
		    System.out.println("Error: Add notification: 0x" 
		            + Long.toHexString(err)); 
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
		    System.out.println("Error: Add notification: 0x" 
		            + Long.toHexString(err)); 
		}
		
		// Create notificationHandle
		err = AdsCallDllFunction.adsSyncAddDeviceNotificationReq(
		        addr,
		        AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,     // IndexGroup
		        adsPublishedHandle,        	// IndexOffset
		        attr,       		// The defined AdsNotificationAttrib object
		        adsPublishedHandle,         	// Choose arbitrary number
		        publishedNotification);
		
		if(err!=0) { 
		    System.out.println("Error: Add notification: 0x" 
		            + Long.toHexString(err)); 
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
	protected void Prepare() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void Busy() {	
		
		
		if(adsSubscribing == true && adsSubscribed == false && subscribingNotificationSignal == true)
		{
			subscribingNotificationSignal = false;
			//Plc wants to subscribe to new topic
			FetchSymbolToBuffer(hdlSubscriptions,sizeOfSubscriptions, buffer_subscriptions);
			FetchSymbolToBuffer(hdlSubscriptionCounter, 2, buffer_subscriptionCounter);
			
			currentSubCounter = Convert.ByteArrToShort(buffer_subscriptionCounter.getByteArray());
			System.out.println("[PlcFetcher.Busy] Subscription counter: "+ currentSubCounter);
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
		
		if(adsPublishing == true && adsPublished == false && publishingNotificationSignal == true)
		{
			publishingNotificationSignal = false;
			//Plc wants to publish a new topic
			FetchSymbolToBuffer(hdlPublications,sizeOfPublications, buffer_publications);
			FetchSymbolToBuffer(hdlPublicationCounter, 2, buffer_publicationCounter);
			
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

	
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		super.messageArrived(topic, message);
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
		
	}
	
	public void onEvent(AmsAddr addr,AdsNotificationHeader notification,long user) {
    		
		Long lUser = new Long(user);
	
		if(lUser.intValue() == adsSubscribingHandle)
		{
			subscribingNotificationSignal = true;
			adsSubscribing =  Convert.ByteArrToBool(notification.getData());
			System.out.println("adsSubscribingHandle Value:\t\t" + adsSubscribing);
			
			
		}
		else if(lUser.intValue() == adsSubscribedHandle)
		{
			adsSubscribed =  Convert.ByteArrToBool(notification.getData());
			System.out.println("adsSubscribedHandle Value:\t\t" + adsSubscribed);
		}
		else if(lUser.intValue() == adsPublishingHandle)
		{
			publishingNotificationSignal = true;
			adsPublishing = Convert.ByteArrToBool(notification.getData());
			System.out.println("adsPublishingHandle Value:\t\t" + adsPublishing);
			
				
		}
		else if(lUser.intValue() == adsPublishedHandle)
		{
			adsPublished = Convert.ByteArrToBool(notification.getData());
			System.out.println("adsPublishedHandle Value:\t\t" + adsPublished);
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

