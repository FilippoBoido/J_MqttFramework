package packageHmi;

public interface HmiSignaller {

	public void connectionLost();
	public void disconnected();
	public void error();
	public void connected();
	public void connecting();
	
}
