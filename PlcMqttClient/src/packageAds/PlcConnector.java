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

import packageSystem.StateMachine;

public class PlcConnector extends StateMachine {
	

	long err;
	int hdlLifePkgBuffToInt;
	AmsAddr addr;
	JNIByteBuffer 	handleBuff,
					symbolBuff,
					dataBuff,
					lifePkgHandle,
					lifePkgSymBuf,
					lifePkgDataBuf,
					testHandle,
					testSymBuf,
					testDataBuf;
	
	JNILong lifePkgNotification; 
	
	int busyStep;
	boolean connected;
	String symbol;
	
	AdsNotificationAttrib attr = new AdsNotificationAttrib();
    // Create and add listener
	AdsConnectorListener listener; 
    AdsCallbackObject callObject = new AdsCallbackObject();
    
	private static final String plcConnected = "ADS.fbAdsConnector.cbConnected.bValue";
	private static final String lifePackage = "ADS.fbAdsConnector.fbAdsSupplier.stMqttLifePackage.sDateTime";
	private static final String test = "MAIN.bTest";
	
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
		
		attr.setCbLength(81);
	    attr.setNTransMode(AdsConstants.ADSTRANS_SERVERONCHA);
	    attr.setDwChangeFilter(10000000);   // 1 sec
	    attr.setNMaxDelay(10000000);        // 1 sec
	        
		handleBuff = new JNIByteBuffer(Integer.SIZE / Byte.SIZE);
		symbolBuff = new JNIByteBuffer(plcConnected.getBytes());
		dataBuff = new JNIByteBuffer(1);
		
		lifePkgHandle = new JNIByteBuffer(Integer.SIZE / Byte.SIZE);
		lifePkgSymBuf = new JNIByteBuffer(lifePackage.getBytes());
		lifePkgDataBuf = new JNIByteBuffer(89);
		
		testHandle = new JNIByteBuffer(Integer.SIZE / Byte.SIZE);
		testSymBuf = new JNIByteBuffer(test.getBytes());
		testDataBuf = new JNIByteBuffer(1);
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
			
	
	}

	@Override
	protected void Ready() {
		
		
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
			
				//Get Handle for cbConnected.bValue
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
					symbol = new String(symbolBuff.getByteArray());
				    System.out.println("Success: Got " + symbol  +" handle!");
				}
				
				//Get Handle for lifePkg
				err = AdsCallDllFunction
						.adsSyncReadWriteReq
						(
							addr,
							AdsCallDllFunction.ADSIGRP_SYM_HNDBYNAME,
							0x0,
							lifePkgHandle.getUsedBytesCount(),
							lifePkgHandle,
							lifePkgSymBuf.getUsedBytesCount(),
							lifePkgSymBuf
						);
		
				if(err!=0) 
				{ 
				    System.out.println("Error: Get handle: 0x" + Long.toHexString(err)); 
				    
				    Fault(10);
				    return;
				} 
				else 
				{
					symbol = new String(lifePkgSymBuf.getByteArray());
				    System.out.println("Success: Got " + symbol  +" handle!");
				}
				
				hdlLifePkgBuffToInt = Convert.ByteArrToInt(lifePkgHandle.getByteArray());
				lifePkgNotification = new JNILong(hdlLifePkgBuffToInt);
				listener = new AdsConnectorListener(hdlLifePkgBuffToInt);
				callObject.addListenerCallbackAdsState(listener);
				// Create notificationHandle
				err = AdsCallDllFunction.adsSyncAddDeviceNotificationReq(
				        addr,
				        AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,     // IndexGroup
				        hdlLifePkgBuffToInt,        // IndexOffset
				        attr,       // The defined AdsNotificationAttrib object
				        hdlLifePkgBuffToInt,         // Choose arbitrary number
				        lifePkgNotification);
				
				if(err!=0) { 
				    System.out.println("Error adding lifePkg notification: 0x" 
				            + Long.toHexString(err)); 
				}
				
				// Handle: byte[] to int Convert the bytearray handle to an int and feed it to the adsSyncWriteReq
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
				//Read the value back and check if signal succresfully transfered (true)
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
	           
	            busyStep = 20;
				break;
				
			case 20:
				
				if(!listener.isPlcConnected())
				{
					connected = false;
					Fault(10);
					return;
				}
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
			callObject.removeListenerCallbackAdsState(listener);
			AdsCallDllFunction.adsPortClose();
			connected = false;
			break;
		case 20://just report
			break;
		}
	}
	

}

class AdsConnectorListener implements CallbackListenerAdsState {
    private final static long SPAN = 11644473600000L;
    Date notificationDate;
    Date currentDate,resultDate;
    
    int listenerID;
    public AdsConnectorListener(int listenerID)
    {
    	this.listenerID = listenerID;
    }
    // Callback function
    public void onEvent(AmsAddr addr,
                    AdsNotificationHeader notification,
                    long user) {

        // The PLC timestamp is coded in Windows FILETIME.
        // Nano secs since 01.01.1601.
    	if((int) user == listenerID)
    	{
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
    
    public boolean isPlcConnected()
    {
    	
    	if(notificationDate == null)
    		return true;
    	
    	currentDate = new Date();
    	int notificationSeconds = notificationDate.getSeconds();
    	int notificationMinutes = notificationDate.getMinutes();
    	int currentDateSeconds = currentDate.getSeconds();
    	int currentDateMinutes = currentDate.getMinutes();
    	
    	
    	if(currentDateMinutes == notificationMinutes)
    	{
    		if((currentDateSeconds - notificationSeconds) > 5)
    		{
    			System.out.println("isPlcConnected?: No, diffSeconds = " + (currentDateSeconds - notificationSeconds));
    			return false;
    		}
    		else
    			return true;
    	}else if (currentDateMinutes > notificationMinutes)
    	{
    		//cd = 5s nm = 59s 64
    		if((currentDateSeconds + notificationSeconds) < 64 )
    			return true;
    		else
    		{	
    			System.out.println("isPlcConnected?: No, diffSeconds = " + ((currentDateSeconds + notificationSeconds) - 60));
    			return false;
    		}
    	}else
    	{
    		System.out.println("isPlcConnected?: Programming error");
    		return false;
    	}
    
    	
    }
}