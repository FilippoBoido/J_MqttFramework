package packageAds;

import java.util.Date;

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


public class PlcEventDrivenFetcher extends PlcFetcher implements MqttCallback {

	
	AdsNotificationAttrib attr = new AdsNotificationAttrib();
	AdsFetcherListener listener = new AdsFetcherListener();
    AdsCallbackObject callObject = new AdsCallbackObject();
    JNILong notification = new JNILong();
    int notificationID,
    	adsSubscribingHandle,
    	adsSubscribedHandle,
    	adsPublishingHandle,
    	adsPublishedHandle,
    	adsSubscriptionCounterHandle,
    	adsPublicationCounterHandle;
	public PlcEventDrivenFetcher(AmsAddr addr, AdsMqttClient adsMqttClient) {
		super(addr, adsMqttClient);
		
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void Init() {
		
		super.Init();
		
		adsSubscribingHandle = Convert.ByteArrToInt(handle_subscribing.getByteArray());
    	adsSubscribedHandle = Convert.ByteArrToInt(handle_subscribed.getByteArray());
    	adsPublishingHandle = Convert.ByteArrToInt(handle_publishing.getByteArray());
    	adsPublishedHandle = Convert.ByteArrToInt(handle_published.getByteArray());
    	adsSubscriptionCounterHandle = Convert.ByteArrToInt(handle_subscriptionCounter.getByteArray());
    	adsPublicationCounterHandle = Convert.ByteArrToInt(handle_publicationCounter.getByteArray());
		
		attr.setCbLength(1);
	    attr.setNTransMode(AdsConstants.ADSTRANS_SERVERONCHA);
	    attr.setDwChangeFilter(10000000);   // 1 sec
	    attr.setNMaxDelay(20000000);        // 2 sec
	    callObject.addListenerCallbackAdsState(listener);
		
		// Create notificationHandle
		err = AdsCallDllFunction.adsSyncAddDeviceNotificationReq(
		        addr,
		        AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,     // IndexGroup
		        adsSubscribingHandle,        	// IndexOffset
		        attr,       		// The defined AdsNotificationAttrib object
		        adsSubscribingHandle,         	// Choose arbitrary number
		        notification);
		
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
		        notification);
		
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
		        notification);
		
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
		        notification);
		
		if(err!=0) { 
		    System.out.println("Error: Add notification: 0x" 
		            + Long.toHexString(err)); 
		}
		
		//change size to 2 bytes for the subscription and publication counter
		attr.setCbLength(2);
	
		// Create notificationHandle
		err = AdsCallDllFunction.adsSyncAddDeviceNotificationReq(
		        addr,
		        AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,     // IndexGroup
		        adsSubscriptionCounterHandle,        	// IndexOffset
		        attr,       		// The defined AdsNotificationAttrib object
		        adsSubscriptionCounterHandle,         	// Choose arbitrary number
		        notification);
		
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
		        notification);
		
		if(err!=0) { 
		    System.out.println("Error: Add notification: 0x" 
		            + Long.toHexString(err)); 
		}
	}
	
	@Override
	protected void Ready() {
		super.Ready();
		
	}

	@Override
	protected void Prepare() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void Busy() {
	/*	
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
	*/
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


}

class AdsFetcherListener implements CallbackListenerAdsState {
    private final static long SPAN = 11644473600000L;
    Date notificationDate;
    Date currentDate,resultDate;
    // Callback function
    public void onEvent(AmsAddr addr,AdsNotificationHeader notification,long user) {

        // The PLC timestamp is coded in Windows FILETIME.
        // Nano secs since 01.01.1601.
        long dateInMillis = notification.getNTimeStamp();

        // Date accepts millisecs since 01.01.1970.
        // Convert to millisecs and substract span.
        notificationDate = new Date(dateInMillis / 10000 - SPAN);

        System.out.println("Value:\t\t"
                + Convert.ByteArrToString(notification.getData()));
        System.out.println("Notification:\t" + notification.getHNotification());
        System.out.println("Time:\t\t" + notificationDate.toString());
        System.out.println("User:\t\t" + user);
        System.out.println("ServerNetID:\t" + addr.getNetIdString() + "\n");
    }
    
    
}