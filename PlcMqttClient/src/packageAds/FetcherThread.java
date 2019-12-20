package packageAds;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

import de.beckhoff.jni.JNIByteBuffer;
import de.beckhoff.jni.tcads.AdsCallDllFunction;
import de.beckhoff.jni.tcads.AmsAddr;
import packageMqtt.AdsMqttClient;

public class FetcherThread extends Thread {

	private boolean end = false;
	private boolean error = false;
	private boolean fetching = false;
	
	
	public boolean isFetching() {
		return fetching;
	}
	public boolean isError() {
		return error;
	}

	AmsAddr addr;
	int hdlBuffToInt,size;
	long err, amountOfSleep;
	JNIByteBuffer buffer;
	AdsMqttClient.E_PublishMode ePublishMode;
	
	private AdsMqttClient adsMqttClient;
	
	
	public FetcherThread(
			AdsMqttClient adsMqttClient, 
			AdsMqttClient.E_PublishMode ePublishMode,
			long amountOfSleep,
			AmsAddr addr, 
			int hdlBuffToInt,
			int size, 
			JNIByteBuffer buffer
	)
	{
		this.addr = addr;
		this.hdlBuffToInt = hdlBuffToInt;
		this.size = size;
		this.buffer = buffer;
		this.adsMqttClient = adsMqttClient;
		this.ePublishMode = ePublishMode;
		this.amountOfSleep = amountOfSleep;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
				
		while(end==false)
		{
			
			err = AdsCallDllFunction
					   .adsSyncReadReq
					   (
						   addr,
						   AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,
						   hdlBuffToInt,
						   size,
						   buffer
					   );
					   
			if(err!=0)
			{
				System.out.println("Error: Read by handle: 0x" + Long.toHexString(err));
				
				error = true;
				
			}
			else
			{
			   	fetching = true;
			   	
			   	if(adsMqttClient != null)
				{
					adsMqttClient.Publish(buffer,ePublishMode);
					try {
						sleep(amountOfSleep);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void End()
	{
		System.out.println("Ending FetcherThread.");
		end = true;
	}

}
