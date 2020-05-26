package packageAds;

import java.util.Date;

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
import packageSystem.StateMachine;

public class PlcConnector extends StateMachine {
	

	long err;
	AmsAddr addr;
	JNIByteBuffer handleBuff,symbolBuff,dataBuff;
	JNILong notification = new JNILong();
	
	int busyStep;
	boolean connected;
	
	
	private static final String plcConnected = "ADS.fbAdsConnector.cbConnected.bValue";
	private static final String lifePackage = "ADS.fbAdsConnector.fbAdsSupplier.stMqttLifePackage";
	
	public PlcConnector(AmsAddr addr)
	{
		super();
		this.addr = addr;	
	}

	public boolean isConnected()
	{
		
		return connected;
	}
	
	@Override
	protected void Init() {
		
		
		handleBuff = new JNIByteBuffer(Integer.SIZE / Byte.SIZE);
		symbolBuff = new JNIByteBuffer(plcConnected.getBytes());
		dataBuff = new JNIByteBuffer(1);
		
		//Open communication
		AdsCallDllFunction.adsPortOpen();
		err = AdsCallDllFunction.getLocalAddress(addr);
		
		addr.setPort(851);
		
		if(err != 0)
		{
			System.out.println("Error: Open communication: 0x" + Long.toHexString(err));
			return;
		}
		else
		{
			System.out.println("Success: Open communication!");
			// Specify attributes of the notificationRequest
            
		}
		Start();		

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
		
		switch(busyStep)
		{
			//********************************************************************************************************
			case 0:
			
				err = AdsCallDllFunction
						.adsSyncReadWriteReq
						(
							addr,
							AdsCallDllFunction.ADSIGRP_SYM_HNDBYNAME,
							0x0,
							handleBuff.getUsedBytesCount(),
		                	handleBuff,
		                	symbolBuff.getUsedBytesCount(),
		                	symbolBuff
						);
		
				if(err!=0) 
				{ 
				    System.out.println("Error: Get handle: 0x" + Long.toHexString(err)); 
				    
				    Fault(10);
				    return;
				} 
				else 
				{
				    System.out.println("Success: Get handle!");
				}
				
				// Handle: byte[] to int
				int hdlBuffToInt = Convert.ByteArrToInt(handleBuff.getByteArray());
				
				// Write value by handle
				err = AdsCallDllFunction
						.adsSyncWriteReq
						(
							addr,
			                AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,
			                hdlBuffToInt,
			                0x1,
			                new JNIByteBuffer(Convert.BoolToByteArr(true))
		                );
				
				if(err!=0)
				{
					System.out.println("Error: Write by handle: 0x" + Long.toHexString(err));
					Fault(20);
					return;
				}
				
				// Read value by handle
				err = AdsCallDllFunction
						.adsSyncReadReq
						(
							addr,
			                AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,
			                hdlBuffToInt,
			                0x1,
			                dataBuff
		                );
				
				if(err!=0)
				{
					System.out.println("Error: Read by handle: 0x" + Long.toHexString(err));
					
					Fault(20);
					return;
				}
				else
				{
					// Data: byte[] to boolean
					boolean val = Convert.ByteArrToBool(dataBuff.getByteArray());
					if(val)
					{
						System.out.println("Connection signal successfully transfered.");
						busyStep = 10;
						
					}
					else
					{
						System.out.println("Connection signal transfer failed.");
					}
					
				}
				
				break;
				
			//********************************************************************************************************
			case 10:
				connected = true;
				// Specify attributes of the notificationRequest
	            AdsNotificationAttrib attr = new AdsNotificationAttrib();
	            attr.setCbLength(Integer.SIZE / Byte.SIZE);
	            attr.setNTransMode(AdsConstants.ADSTRANS_SERVERONCHA);
	            attr.setDwChangeFilter(10000000);   // 1 sec
	            attr.setNMaxDelay(20000000);        // 2 sec

	            // Create and add listener
	            AdsListener listener = new AdsListener();
	            AdsCallbackObject callObject = new AdsCallbackObject();
	            callObject.addListenerCallbackAdsState(listener);

	            // Create notificationHandle
	            err = AdsCallDllFunction.adsSyncAddDeviceNotificationReq(
	                addr,
	                0x4020,     // IndexGroup
	                0x0,        // IndexOffset
	                attr,       // The defined AdsNotificationAttrib object
	                42,         // Choose arbitrary number
	                notification);
	            if(err!=0) { 
	                System.out.println("Error: Add notification: 0x" 
	                        + Long.toHexString(err)); 
	            }
	            busyStep = 20;
				break;
				
			case 20:
				break;
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
		switch(errorType)
		{
		case 10://close connection
			AdsCallDllFunction.adsPortClose();
			connected = false;
			break;
		case 20://just report
			break;
		}
	}
	

}

class AdsListener implements CallbackListenerAdsState {
    private final static long SPAN = 11644473600000L;

    // Callback function
    public void onEvent(AmsAddr addr,
                    AdsNotificationHeader notification,
                    long user) {

        // The PLC timestamp is coded in Windows FILETIME.
        // Nano secs since 01.01.1601.
        long dateInMillis = notification.getNTimeStamp();

        // Date accepts millisecs since 01.01.1970.
        // Convert to millisecs and substract span.
        Date notificationDate = new Date(dateInMillis / 10000 - SPAN);

        System.out.println("Value:\t\t"
                + Convert.ByteArrToInt(notification.getData()));
        System.out.println("Notification:\t" + notification.getHNotification());
        System.out.println("Time:\t\t" + notificationDate.toString());
        System.out.println("User:\t\t" + user);
        System.out.println("ServerNetID:\t" + addr.getNetIdString() + "\n");
    }
}