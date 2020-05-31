package packageHmi;

public interface HmiPlug {

	/*
case "closeId":
	
	//node.addEventHandler(ActionEvent.ACTION, event -> close(stage));
	break;
case "resetId":
	
	//node.addEventHandler(ActionEvent.ACTION, event -> close(stage));
	break;	
case "connectId":
	
	//node.addEventHandler(ActionEvent.ACTION, event -> mqttConnect());
	break;
	*/
	
	public void connect();
	public boolean isConnected();
	public void close();
	public boolean isClosed();
	public void reset();
	public boolean isReset();
	
}
