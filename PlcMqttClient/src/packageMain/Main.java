package packageMain;
import packageAds.PlcConnector;

public class Main {

	public static void main(String[] args) {
		
		PlcConnector plcConnector = new PlcConnector();
		
		while(true)
		{

			plcConnector.Execute();	
			plcConnector.CheckStateMachine();
		}
		
	}

}
