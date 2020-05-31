package packageHmi;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;


import javafx.scene.input.MouseEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.layout.StackPane;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

@SuppressWarnings("restriction")
public class HmiInterface extends Application implements Runnable{
	
	double xOffset,yOffset;
	boolean showWindow = true;
	TrayIcon trayIcon;
	SystemTray tray;

	Rectangle2D screenBounds;
	
	static HmiPlug hmiPlug;
	
	public HmiInterface()
	{
		
	}
	
	public HmiInterface(HmiPlug hmiPlug)
	{
		super();
		if(hmiPlug == null)
			 System.out.println("HmiPlug is null");
		else
			System.out.println("HmiPlug not null");
		this.hmiPlug = hmiPlug;
		
	}

	public static ArrayList<Node> getAllNodes(Parent root) {
	    
		ArrayList<Node> nodes = new ArrayList<Node>();
	    nodes.add(root);
	    addAllDescendents(root, nodes);
	    return nodes;
	}

	private static void addAllDescendents(Parent parent, ArrayList<Node> nodes) {
	    for (Node node : parent.getChildrenUnmodifiable()) {
	        nodes.add(node);
	        if (node instanceof Parent)
	            addAllDescendents((Parent)node, nodes);
	    }
	}

	@Override
	public void start(Stage stage) throws Exception {
		try {

			System.out.println("[HmiInterface.start] App launched");
			if(hmiPlug == null)
				 System.out.println("HmiPlug is null");
			else
				System.out.println("HmiPlug not null");
			if (!SystemTray.isSupported()) {
		        System.out.println("SystemTray is not supported");
		        return;
		    }
			screenBounds = Screen.getPrimary().getBounds();
		    System.out.println(screenBounds);
		    URL url =  new File("C:/Users/fboid/Documents/JavaFX-Test/PlcAds.png").toURI().toURL();
		    Image image = Toolkit.getDefaultToolkit().getImage(url);
		   
		    //image dimensions must be 16x16 on windows
		    trayIcon = new TrayIcon(image, "Plc connector");

		    tray = SystemTray.getSystemTray();

		    //Listener left clic XD
		    trayIcon.addMouseListener(new MouseAdapter() {
		    		
		    	public void mouseClicked(java.awt.event.MouseEvent e) {
		    			
		    		if (e.getButton() == java.awt.event.MouseEvent.BUTTON1) {
		                Platform.runLater(new Runnable() {
		                    @Override
		                    public void run() {
		                    	System.out.println("[SystemTray.mouseClicked] showWindow: " + showWindow);
		                        if (!showWindow) {
		                        	System.out.println("[SystemTray.mouseClicked] hiding stage");
		                            stage.hide();
		                            stage.setIconified(false);
		                            showWindow = true;
		                        } else if (showWindow) {
		                        	System.out.println("[SystemTray.mouseClicked] showing stage");
		                            stage.show();
		                            stage.setIconified(false);
		                            showWindow = false;
		                        }
		                    }
		                });
		    		}           		    		
		    	};
		    });

		    try {
		        tray.add(trayIcon);
		    } catch (AWTException e) {
		        System.out.println("TrayIcon could not be added.");
		    }
			
			
			Parent root = FXMLLoader.load(new File("C:\\Users\\fboid\\Documents\\JavaFX-Test\\PlcAdsHmi.fxml").toURI().toURL());
			
			
			ArrayList<Node> nodes = getAllNodes(root);
			
			
			
			for(Node node : nodes)
			{
				if(node.getId() == null)
					continue;
				System.out.println("[HmiInterface.start] Node id: " + node.getId());
				switch(node.getId())
				{
				
				case "minimizeId":
					
					node.addEventHandler(ActionEvent.ACTION, event -> minimize(stage));
					break;
					
					
				case "closeId":
					
					node.addEventHandler(ActionEvent.ACTION, event -> close(hmiPlug));
					break;
				case "resetId":
					
					node.addEventHandler(ActionEvent.ACTION, event -> hmiPlug.reset());
					break;	
				case "connectId":
					
					node.addEventHandler(ActionEvent.ACTION, event -> hmiPlug.connect());
					break;
					
				case "stackPaneId":
					
					System.out.println("Setting mouse eventHandlers");
					node.setOnMousePressed(new EventHandler<MouseEvent>() {
					    @Override
					    public void handle(MouseEvent event) {
					    	System.out.println("OnMousePressed");
					        xOffset = stage.getX() - event.getScreenX();
					        yOffset = stage.getY() - event.getScreenY();
					    }
					});
					
					node.setOnMouseDragged(new EventHandler<MouseEvent>() {
					    @Override
					    public void handle(MouseEvent event) {
					    	System.out.println("OnMouseDragged");
					    	double setX,setY;
					    	setX = event.getScreenX() + xOffset;
					    	setY = event.getScreenY() + yOffset;
					    	
					    	if(setX < screenBounds.getMinX())
					    		setX = screenBounds.getMinX();
					    	else if(setX > (screenBounds.getMaxX()-stage.getWidth()))
					    		setX = (screenBounds.getMaxX()-stage.getWidth());
					    	
					    	if(setY < screenBounds.getMinY())
					    		setY = screenBounds.getMinY();
					    	else if(setY> screenBounds.getMaxY()-stage.getHeight())
					    		setY = (screenBounds.getMaxY()-stage.getHeight());
					    	
							stage.setX(setX);
					    	stage.setY(setY);
					    }
					});
				}
			}
			
			Scene scene = new Scene(root);
			stage.initStyle(StageStyle.UNDECORATED);
			stage.setScene(scene);		
			stage.setResizable(false);
			Platform.setImplicitExit(false);
			//stage.show();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	private Object close(HmiPlug hmiPlug) {
		
		System.out.println("[Main.close] Closing app");
		
		if(hmiPlug == null)
		{
			System.out.println("[Main.close] HmiPlug is null.");
			return null;
		}
		
		hmiPlug.close();
		
		tray.remove(trayIcon);
		
		System.out.println("[HmiInterface.close] hmiPlug close signale sent");
		while(!hmiPlug.isClosed());
		System.out.println("[HmiInterface.close] hmiPlug closed signal received");
		
		Platform.exit();
		
		return null;
		
	}


	private Object minimize(Stage stage) {
		stage.setIconified(true);
		System.out.println("[Main.minimize] App minimized");
		return null;
	}
	

	@Override
	public void run() {
		launch();
		
	}
}
