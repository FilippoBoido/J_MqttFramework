package packageAds;

import de.beckhoff.jni.AdsConstants;
import de.beckhoff.jni.Convert;
import de.beckhoff.jni.JNIByteBuffer;
import de.beckhoff.jni.JNILong;
import de.beckhoff.jni.JNIBool;

import de.beckhoff.jni.tcads.AdsCallDllFunction;
import de.beckhoff.jni.tcads.AdsCallbackObject;
import de.beckhoff.jni.tcads.AdsDevName;
import de.beckhoff.jni.tcads.AdsNotificationAttrib;
import de.beckhoff.jni.tcads.AdsNotificationHeader;
import de.beckhoff.jni.tcads.AdsState;
import de.beckhoff.jni.tcads.AdsSymbolEntry;
import de.beckhoff.jni.tcads.AdsVersion;
import de.beckhoff.jni.tcads.AmsAddr;
import de.beckhoff.jni.tcads.AmsNetId;
import de.beckhoff.jni.tcads.CallbackListenerAdsRouter;
import de.beckhoff.jni.tcads.CallbackListenerAdsState;
import packageSystem.StateMachine;

public class PlcConnector extends StateMachine {
	
	long err;
	AmsAddr addr;
	JNIByteBuffer handleBuff,symbolBuff,dataBuff;
	
	private static final String plcConnected = "ADS.fbAdsConnector.cbConnected";
	
	public PlcConnector()
	{
		super();
		
	}

	@Override
	protected void Init() {
		
		addr = new AmsAddr();
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
		}
		
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
		    AdsCallDllFunction.adsPortClose();
		    return;
		} 
		else 
		{
		    System.out.println("Success: Get handle!");
		}
		
		// Handle: byte[] to int
		int hdlBuffToInt = Convert.ByteArrToInt(handleBuff.getByteArray());
		      
		// Read value by handle
		err = AdsCallDllFunction
				.adsSyncReadReq
				(
					addr,
	                AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,
	                hdlBuffToInt,
	                0x4,
	                dataBuff
                );
		
		if(err!=0)
		{
			System.out.println("Error: Read by handle: 0x" + Long.toHexString(err));
			return;
		}
		else
		{
			// Data: byte[] to boolean
			boolean val = Convert.ByteArrToBool(dataBuff.getByteArray());
			System.out.println("Success: PLCVar value: " + val);
			Start();
		}

	}

	@Override
	protected void Ready() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void Prepare() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void Busy() {
		// TODO Auto-generated method stub
		//AdsCallDllFunction.adsSyncReadWriteReq() 
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
		// TODO Auto-generated method stub
		
	}
	
	

}
