package packageMain;

import packageHmi.HmiInterface;

public class Main  {

	
	
	public static void main(String[] args) {
		
		MainLauncher mainLauncher = new MainLauncher();
		Thread mainLauncherThread = new Thread(mainLauncher);
		mainLauncherThread.start();
		
		HmiInterface hmiInterface = new HmiInterface(mainLauncher);
		Thread hmiInterfaceThread = new Thread(hmiInterface);
		hmiInterfaceThread.start();
		
		try {
			mainLauncherThread.join();
			System.out.println("[Main.main] mainLauncherThread shut down");
			hmiInterfaceThread.join();
			System.out.println("[Main.main] hmiInterfaceThread shut down");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}

}

