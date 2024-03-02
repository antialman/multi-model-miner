package main;

import controller.MainViewControllerV2;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainGuiV2 extends Application {

	private static Scene scene;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		String fxmlPath = "MainViewV2.fxml";
		FXMLLoader fxmlLoader = new FXMLLoader(MainGuiV2.class.getClassLoader().getResource(fxmlPath));
		Parent parent = fxmlLoader.load();
		
		((MainViewControllerV2)fxmlLoader.getController()).setStage(primaryStage);			
		
		scene = new Scene(parent);
		//scene.getStylesheets().add("css/main.css");
		
		primaryStage.setTitle("multi-model-miner-v2");
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
