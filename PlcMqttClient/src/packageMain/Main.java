package packageMain;
import packageAds.PlcConnector;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PlcConnector plcConnector = new PlcConnector();
		while(true)
		{
			plcConnector.checkStateMachine();
		}
		
	}

}
