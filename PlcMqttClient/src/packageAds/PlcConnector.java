package packageAds;

import de.beckhoff.jni.Convert;
import de.beckhoff.jni.JNIByteBuffer;
import de.beckhoff.jni.tcads.AdsCallDllFunction;
import de.beckhoff.jni.tcads.AmsAddr;
import packageMqtt.AdsMqttClient;
import packageSystem.StateMachine;

public class PlcConnector extends StateMachine {
	
	PlcFetcher plcFetcher;
	
	public PlcFetcher getPlcFetcher() {
		return plcFetcher;
	}

	long err;
	AmsAddr addr;
	JNIByteBuffer handleBuff,symbolBuff,dataBuff;
	
	int busyStep;
	AdsMqttClient adsMqttClient;
	
	private static final String plcConnected = "ADS.fbAdsConnector.cbConnected.bValue";
	
	public PlcConnector(AdsMqttClient adsMqttClient)
	{
		super();
		this.adsMqttClient = adsMqttClient;
		
	}

	@Override
	protected void Init() {
		
		addr = new AmsAddr();
		plcFetcher = new PlcFetcher(addr,adsMqttClient);
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
						Done();
					}
					else
					{
						System.out.println("Connection signal transfer failed.");
					}
					
				}
				
				break;
				
			//********************************************************************************************************
			case 10:
				plcFetcher.CheckStateMachine();
				plcFetcher.Execute();
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
			break;
		case 20://just report
			break;
		}
	}
	
	

}
