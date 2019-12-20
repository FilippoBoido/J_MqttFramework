package packageAds;

import de.beckhoff.jni.tcads.AmsAddr;
import packageMqtt.AdsMqttClient;
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
	
	//The paths of the packages that need to be fetched, here.
	
	
	JNIByteBuffer 
		handle_lifePackage,
		symbol_lifePackage,
		handle_stateMachine,
		symbol_stateMachine,
		handle_dtDate,	
		symbol_dtDate; 
	
	//The buffers for the packages that need to be fetched, here.
	
	private JNIByteBuffer buffer_lifePackage = new JNIByteBuffer(6);
	
	private AdsMqttClient adsMqttClient;
	

	public JNIByteBuffer getLifePackage() {
		return buffer_lifePackage;
	}

	AmsAddr addr;
	long err;
	int hdlBuffToInt;
	
	
	private FetcherThread lifePackageFetcher;
	
	public FetcherThread getLifePackageFetcher() {
		return lifePackageFetcher;
	}


	public PlcFetcher(AmsAddr addr,AdsMqttClient adsMqttClient) {
		super();
		this.addr = addr;
		this.adsMqttClient = adsMqttClient;
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

		if(hdlBuffToInt == 0)
		{
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
		   
		   
		   lifePackageFetcher = 
				   new FetcherThread
				   (	
					   adsMqttClient,
					   AdsMqttClient.E_PublishMode.LIFE_PACKAGE,
					   1000,//amount of sleep
					   addr,
					   hdlBuffToInt,
					   0x6,//size of package
					   buffer_lifePackage 
				   );
		   
		   lifePackageFetcher.start();
		}
	   
		if(lifePackageFetcher.isError())
		{
			 System.out.println("Failure: Error while retrieving LifePackage.");
			Fault(0);
			return;
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
		
		lifePackageFetcher.End();
		
	}


}


