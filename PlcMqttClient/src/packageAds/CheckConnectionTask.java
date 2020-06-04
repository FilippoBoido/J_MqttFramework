package packageAds;

import de.beckhoff.jni.JNIByteBuffer;
import de.beckhoff.jni.tcads.AdsCallDllFunction;
import de.beckhoff.jni.tcads.AmsAddr;
import packageExceptions.AdsConnectionException;

public class CheckConnectionTask extends java.util.TimerTask {

	private long err;
	
	private CheckConnectionPlug checkConnectionPlug;
	private AmsAddr addr;
	private int plcConnectedIntHdl;
	private JNIByteBuffer plcConnectedDataBuf;
	public CheckConnectionTask(CheckConnectionPlug checkConnectionPlug,int plcConnectedIntHdl,AmsAddr addr,JNIByteBuffer plcConnectedDataBuf)
	{
		this.addr = addr;
		this.plcConnectedIntHdl = plcConnectedIntHdl;
		this.plcConnectedDataBuf = plcConnectedDataBuf;
		this.checkConnectionPlug = checkConnectionPlug;
	}
	@Override
	public void run() {
		
		
		System.out.println("[CheckConnectionTask] Checking connection");
		err = AdsCallDllFunction.adsSyncReadReq(
				addr,
                AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,
                plcConnectedIntHdl,
                0x1,
                plcConnectedDataBuf);
		
		if(err!=0)
		{								
			checkConnectionPlug.signalConnectionLoss();
			
		}
		
	}

}
