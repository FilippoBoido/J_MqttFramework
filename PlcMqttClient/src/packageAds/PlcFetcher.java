package packageAds;

import de.beckhoff.jni.tcads.AmsAddr;
import packageSystem.StateMachine;
import de.beckhoff.jni.Convert;
import de.beckhoff.jni.JNIByteBuffer;
import de.beckhoff.jni.tcads.AmsAddr;
import de.beckhoff.jni.tcads.AdsCallDllFunction;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.lang.Integer;

public class PlcFetcher extends StateMachine {

	private static final String mqttLifePackage = "ADS.fbAdsConnector.fbAdsSupplier.stMqttLifePackage";
	
	private static final String mqttLifePackage_stateMachine = "ADS.fbAdsConnector.fbAdsSupplier.stMqttLifePackage.uiStateMachine";
	private static final String mqttLifePackage_dtDate = "ADS.fbAdsConnector.fbAdsSupplier.stMqttLifePackage.dtDate";
	
	
	JNIByteBuffer 
		handle_lifePackage,
		symbol_lifePackage,
		handle_stateMachine,
		symbol_stateMachine,
		handle_dtDate,	
		symbol_dtDate; 
	
	JNIByteBuffer buffer_stateMachine = new JNIByteBuffer(2);
	JNIByteBuffer buffer_dtDate = new JNIByteBuffer(4);
	JNIByteBuffer buffer_lifePackage = new JNIByteBuffer(6);
	
	AmsAddr addr;
	long err;
	int hdlBuffToInt;
	
	public PlcFetcher(AmsAddr addr) {
		super();
		this.addr = addr;
	}
	
	@Override
	protected void Init() {
		// TODO Auto-generated method stub
		handle_stateMachine = new JNIByteBuffer(Integer.SIZE / Byte.SIZE);
		handle_dtDate = new JNIByteBuffer(Integer.SIZE / Byte.SIZE);
		handle_lifePackage = new JNIByteBuffer(Integer.SIZE / Byte.SIZE);
		
		symbol_stateMachine = new JNIByteBuffer(mqttLifePackage_stateMachine.getBytes());
		symbol_dtDate = new JNIByteBuffer(mqttLifePackage_dtDate.getBytes());
		symbol_lifePackage = new JNIByteBuffer(mqttLifePackage.getBytes());
		Start();
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

	   err = AdsCallDllFunction
				.adsSyncReadWriteReq
				(
					addr,
					AdsCallDllFunction.ADSIGRP_SYM_HNDBYNAME,
					0x0,
					handle_lifePackage.getUsedBytesCount(),
					handle_lifePackage,
					symbol_lifePackage.getUsedBytesCount(),
					symbol_lifePackage
				);
		
	   if(err!=0) 
	   { 
		   System.out.println("Error: Get handle: 0x"+ Long.toHexString(err)); 
		   Fault(0);
		   return;
		   
	   } 
	   else 
	   {
		   System.out.println("Success: Got handle_lifePackage handle!");
	   }
	   
	   
	   hdlBuffToInt = Convert.ByteArrToInt(handle_lifePackage.getByteArray());
	   
	   err = AdsCallDllFunction
			   .adsSyncReadReq
			   (
				   addr,
				   AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,
				   hdlBuffToInt,
				   0x6,
				   buffer_lifePackage
			   );
	   
	   if(err!=0)
		{
			System.out.println("Error: Read by handle: 0x" + Long.toHexString(err));
			
			Fault(0);
			return;
		}
	   else
	   {
		   
			ByteBuffer bb = ByteBuffer.wrap(buffer_lifePackage.getByteArray());
			bb.order(ByteOrder.LITTLE_ENDIAN);
			System.out.println("Reading mqttLifePackage succesfull!");
			System.out.println("plcStateMachine: " + bb.getShort());
			long timeConversion = (long) bb.getInt() * 1000;

			Date date = new Date(timeConversion);
			System.out.println("plcTimeAndDate: " + date.toString());
			Wait();
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
		// TODO Auto-generated method stub
		
	}

}
class MqttLifePackage
{
	private byte[] plcStateMachine = new byte[2];
	private byte[] plcTimeAndDate = new byte[4];
	private byte[] buffer = new byte[6];
	
	private int intPlcStateMachine;
	private long longPlcTimeAndDate;
	
	public MqttLifePackage()
	{
		;
	}

	public int getPlcStateMachine() {
		return intPlcStateMachine;
	}
	public long getPlcTimeAndDate() {
		return longPlcTimeAndDate;
	}
	

}
