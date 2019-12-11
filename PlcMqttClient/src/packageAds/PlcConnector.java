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
	
	public PlcConnector()
	{
		super();
		Start();
	}

	@Override
	protected void Init() {
		// TODO Auto-generated method stub
		
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
