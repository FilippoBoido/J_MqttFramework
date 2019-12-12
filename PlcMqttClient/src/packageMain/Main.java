package packageMain;
import packageAds.PlcConnector;

public class Main {

	public static void main(String[] args) {
		
		PlcConnector plcConnector = new PlcConnector();
		
		boolean bFirstCall = true; 
		while(true)
		{
			/*
			if(	plcConnector.eStateMachine == plcConnector.eStateMachine.eReady
				&& bFirstCall	)
			{
				bFirstCall = false;
				
			}
			*/
			plcConnector.Execute();	
			plcConnector.CheckStateMachine();
		}
		
	}

}
