package main;

import controller.MainViewControllerV1;
import controller.MainViewControllerV2;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainGui extends Application {

	private static Scene scene;
	private int version = 2;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		String fxmlPath = "MainViewV2.fxml";
		FXMLLoader fxmlLoader = new FXMLLoader(MainGui.class.getClassLoader().getResource(fxmlPath));
		Parent parent = fxmlLoader.load();
		
		if (version == 1) {
			((MainViewControllerV1)fxmlLoader.getController()).setStage(primaryStage);
		} else if (version == 2) {
			((MainViewControllerV2)fxmlLoader.getController()).setStage(primaryStage);			
		}
		
		scene = new Scene(parent);
		//scene.getStylesheets().add("css/main.css");
		
		primaryStage.setTitle("multi-model-miner");
		primaryStage.setScene(scene);
		//Setting minimum window size to 720p
		primaryStage.setMinWidth(1280);
		primaryStage.setMinHeight(720);
		//primaryStage.setMaximized(true);
		primaryStage.show();
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				Platform.exit();
				System.exit(0);
			}
		});
	}
	
}
