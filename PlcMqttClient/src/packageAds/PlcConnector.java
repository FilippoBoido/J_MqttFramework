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
import de.beckhoff.jni.tcads.AdsState;
import de.beckhoff.jni.tcads.AmsAddr;
import de.beckhoff.jni.tcads.CallbackListenerAdsRouter;
import de.beckhoff.jni.tcads.CallbackListenerAdsState;
import packageExceptions.AdsConnectionException;
import packageSystem.StateMachine;

public class PlcConnector extends StateMachine implements CallbackListenerAdsState  {
	

	long err;
	int hdlLifePkgBuffToInt;
	
	AmsAddr addr;
	JNIByteBuffer 	plcConnectedHdlBuf,
					plcConnectedSymBuf,
					plcConnectedDataBuf,
					lifePkgHandle,
					lifePkgSymBuf,
					lifePkgDataBuf,
					testHandle,
					testSymBuf,
					testDataBuf;
	
	JNILong 	lifePkgNotification,
				checkConnectorNotification,
				checkRouterNotification; 
	
	
	boolean connected;
	String symbol;
	
	AdsNotificationAttrib attr = new AdsNotificationAttrib();
	AdsNotificationAttrib checkConnectionAttr = new AdsNotificationAttrib();
	AdsNotificationAttrib checkRouterAttr = new AdsNotificationAttrib();
    // Create and add listener
	AdsConnectorListener listener; 
	AdsConnectorListener routerListener;
    AdsCallbackObject callObject = new AdsCallbackObject();
    
	private static final String plcConnected = "ADS.fbAdsConnector.cbConnected.bValue";
	private static final String lifePackage = "ADS.fbAdsConnector.fbAdsSupplier.stMqttLifePackage.sDateTime";
	private static final String test = "MAIN.bTest";
	
	private int hdlBuffToInt = 0;
	private boolean connectionLost;
	
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
	protected void init() {
		
		attr.setCbLength(81);
	    attr.setNTransMode(AdsConstants.ADSTRANS_SERVERONCHA);
	    attr.setDwChangeFilter(10000000);   // 1 sec
	    attr.setNMaxDelay(10000000);        // 1 sec
	    
	    checkRouterAttr.setCbLength(2);
	    checkRouterAttr.setNTransMode(AdsConstants.ADSTRANS_SERVERONCHA);
	    checkRouterAttr.setNMaxDelay(0);
	    checkRouterAttr.setDwChangeFilter(0); 
	    
	    checkConnectionAttr.setCbLength(1);
	    checkConnectionAttr.setNTransMode(AdsConstants.ADSTRANS_SERVERONCHA);
	    checkConnectionAttr.setDwChangeFilter(10000000);   // 1 sec
	    checkConnectionAttr.setNMaxDelay(10000000);        // 1 sec
	    
	    checkConnectorNotification = new JNILong();
	    checkRouterNotification = new JNILong();
	    
		plcConnectedHdlBuf = new JNIByteBuffer(Integer.SIZE / Byte.SIZE);	
		plcConnectedSymBuf = new JNIByteBuffer(plcConnected.getBytes());
		plcConnectedDataBuf = new JNIByteBuffer(1);
		
		lifePkgHandle = new JNIByteBuffer(Integer.SIZE / Byte.SIZE);
		lifePkgSymBuf = new JNIByteBuffer(lifePackage.getBytes());
		lifePkgDataBuf = new JNIByteBuffer(89);
		
		testHandle = new JNIByteBuffer(Integer.SIZE / Byte.SIZE);
		testSymBuf = new JNIByteBuffer(test.getBytes());
		testDataBuf = new JNIByteBuffer(1);		
	
	}

	@Override
	protected void ready() throws AdsConnectionException {
		
		//Open communication
		switch(readyStep)
		{
		case 00:
			AdsCallDllFunction.adsSyncSetTimeout(10000);
			AdsCallDllFunction.adsPortOpen();
			
			err = AdsCallDllFunction.getLocalAddress(addr);
			addr.setPort(851);
			
			if(err != 0)
			{
				exceptionMessage = "Error opening ADS communication: 0x" + Long.toHexString(err);
				//System.out.println(exceptionMessage); 
				throw new AdsConnectionException(exceptionMessage,new AdsConnectionException());			
			}
			
			
			System.out.println("[PlcConnector.ready] Success: ADS communication opened.");        
			readyStep = 10;
			bReadyOk = true;
		
		case 10:
			
		}
		
	}

	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	protected void busy() throws AdsConnectionException{
		
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
						plcConnectedHdlBuf.getUsedBytesCount(),
	                	plcConnectedHdlBuf,
	                	plcConnectedSymBuf.getUsedBytesCount(),
	                	plcConnectedSymBuf
					);
	
			if(err!=0) 
			{ 
				exceptionMessage = "Error getting handle for " + plcConnected + " : 0x" + Long.toHexString(err);
			    //System.out.println(exceptionMessage); 
			    throw new AdsConnectionException(exceptionMessage,new AdsConnectionException());				   
			} 
			else 
			{
				symbol = new String(plcConnectedSymBuf.getByteArray());
			    System.out.println("[PlcConnector.busy] Success: Got handle for " + symbol);
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
				exceptionMessage = "Error getting handle for " + lifePackage + " : 0x" + Long.toHexString(err);
			    //System.out.println(exceptionMessage); 
			    throw new AdsConnectionException(exceptionMessage,new AdsConnectionException());
			} 
			else 
			{
				symbol = new String(lifePkgSymBuf.getByteArray());
			    System.out.println("[PlcConnector.busy] Success: Got handle for " + symbol);
			}
			
			hdlLifePkgBuffToInt = Convert.ByteArrToInt(lifePkgHandle.getByteArray());
			lifePkgNotification = new JNILong(hdlLifePkgBuffToInt);
			//listener = new AdsConnectorListener(hdlLifePkgBuffToInt);
			//callObject.addListenerCallbackAdsState(listener);
			// Create notificationHandle
			err = AdsCallDllFunction.adsSyncAddDeviceNotificationReq(
			        addr,
			        AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,     // IndexGroup
			        hdlLifePkgBuffToInt,        // IndexOffset
			        attr,       // The defined AdsNotificationAttrib object
			        hdlLifePkgBuffToInt,         // Choose arbitrary number
			        lifePkgNotification);
			
			if(err!=0) {     
			    exceptionMessage = "Error adding lifePkg notification: 0x" + Long.toHexString(err);
			    //System.out.println(exceptionMessage); 
			    throw new AdsConnectionException(exceptionMessage,new AdsConnectionException());
			}
			
			// Handle: byte[] to int Convert the bytearray handle to an int and feed it to the adsSyncWriteReq
			hdlBuffToInt = Convert.ByteArrToInt(plcConnectedHdlBuf.getByteArray());
			
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
				exceptionMessage = "Error writing by handle: "+ plcConnected +": 0x" + Long.toHexString(err);				
				//System.out.println(exceptionMessage); 
				throw new AdsConnectionException(exceptionMessage,new AdsConnectionException());			
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
		                plcConnectedDataBuf
	                );
			
			if(err!=0)
			{					
				exceptionMessage = "Error reading by handle: "+ plcConnected +": 0x" + Long.toHexString(err);				
				//System.out.println(exceptionMessage); 
				throw new AdsConnectionException(exceptionMessage,new AdsConnectionException());
			}
			else
			{
				// Data: byte[] to boolean
				boolean val = Convert.ByteArrToBool(plcConnectedDataBuf.getByteArray());
				if(val)
				{
					System.out.println("[PlcConnector.busy] Connection signal successfully transfered.");
					busyStep = 10;
					
				}
				else
				{
					System.out.println("[PlcConnector.busy] Connection signal transfer failed.");
				}
				
			}
			
			break;
			
		//********************************************************************************************************
		case 10:
			
			callObject.addListenerCallbackAdsState(this);
			err = AdsCallDllFunction.adsSyncAddDeviceNotificationReq(
			        addr,
			        AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,     // IndexGroup
			        hdlBuffToInt,        	// IndexOffset
			        checkConnectionAttr,       		// The defined AdsNotificationAttrib object
			        hdlBuffToInt,         	// Choose arbitrary number
			        checkConnectorNotification);
			
			if(err!=0) { 
				
				exceptionMessage = "Error adding device notification: 0x" + Long.toHexString(err);
			    throw new AdsConnectionException(exceptionMessage,new AdsConnectionException());
			}
			
			
			AdsCallDllFunction.adsAmsRegisterRouterNotification();
			routerListener = new AdsConnectorListener();
			callObject.addListenerCallbackAdsRouter(routerListener);
			
			
			
			err = AdsCallDllFunction.adsSyncAddDeviceNotificationReq(
			        addr,
			        AdsCallDllFunction.ADSIGRP_DEVICE_DATA,     // IndexGroup
			        AdsCallDllFunction.ADSIOFFS_DEVDATA_DEVSTATE,        	// IndexOffset
			        checkRouterAttr,       		// The defined AdsNotificationAttrib object
			        AdsCallDllFunction.ADSIOFFS_DEVDATA_DEVSTATE,         	// Choose arbitrary number
			        checkRouterNotification);
			
			if(err!=0) { 
				
				exceptionMessage = "Error adding device notification: 0x" + Long.toHexString(err);
			    throw new AdsConnectionException(exceptionMessage,new AdsConnectionException());
			}
			callObject.addListenerCallbackAdsState(this);
			connected = true;
			bBusyOk = true;
			break;
			
		}
	
	}

	public boolean connectionLost()
	{
		return connectionLost;
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
		
		switch(errorStep)
		{
		case 00:
			
			callObject.removeListenerCallbackAdsState(this);
			callObject.removeListenerCallbackAdsRouter(routerListener);
			AdsCallDllFunction.adsAmsUnRegisterRouterNotification();
			AdsCallDllFunction.adsPortClose();
			connectionLost = false;
			connected = false;
			errorStep = 10;
			bErrorOk = true;
			break;
			
		case 10:
							
		}
		
	}

	@Override
	protected void shuttingDown() throws Throwable {
		
		switch(shutDownStep)
		{
		case 00:
			callObject.removeListenerCallbackAdsState(this);
			callObject.removeListenerCallbackAdsRouter(routerListener);
			AdsCallDllFunction.adsAmsUnRegisterRouterNotification();
			AdsCallDllFunction.adsPortClose();
			connected = false;
			bShutDownOk = true;
			break;
			
		case 10:
			break;
		}
		
	}

	@Override
	public void onEvent(AmsAddr addr, AdsNotificationHeader notification,long user) {
		if((int) user == hdlBuffToInt)
    	{
    		
			if(Convert.ByteArrToBool(notification.getData()) == false)
			{
				connectionLost = true;
			}
    		
    	}
		else if((int) user ==  AdsCallDllFunction.ADSIOFFS_DEVDATA_DEVSTATE)
		{
			System.out.println("[AdsConnectorListener.onEvent] argument: " + Convert.ByteArrToShort(notification.getData()));
		}
		
	}

}

class AdsConnectorListener implements CallbackListenerAdsRouter {
	
    public AdsConnectorListener()
    {
    	
    }
    // Callback function
    
	@Override
	public void onEvent(long arg0) {
		System.out.println("[AdsConnectorListener.onEvent] argument: " + arg0);
		switch(new Long(arg0).intValue())
		{
		
		case AdsState.ADSSTATE_STOP:
			System.out.println("[AdsConnectorListener.onEvent] ADSSTATE_STOP");
			break;
		case AdsState.ADSSTATE_ERROR:
			System.out.println("[AdsConnectorListener.onEvent] ADSSTATE_ERROR");
			break;
		case AdsState.ADSSTATE_IDLE:
			System.out.println("[AdsConnectorListener.onEvent] ADSSTATE_IDLE.");
			break;
		case AdsState.ADSSTATE_RUN:
			System.out.println("[AdsConnectorListener.onEvent] ADSSTATE_RUN");
			break;
		case AdsState.ADSSTATE_RESET:
			System.out.println("[AdsConnectorListener.onEvent] ADSSTATE_RESET");
			break;
		case AdsState.ADSSTATE_SHUTDOWN:
			System.out.println("[AdsConnectorListener.onEvent] ADSSTATE_SHUTDOWN");
			break;
		case AdsState.ADSSTATE_START:
			System.out.println("[AdsConnectorListener.onEvent] ADSSTATE_START");
			break;
		case AdsState.ADSSTATE_SUSPEND:
			System.out.println("[AdsConnectorListener.onEvent] ADSSTATE_SUSPEND");
			break;
		
			
		}
		
	}
    
}