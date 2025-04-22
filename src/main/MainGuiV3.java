package main;

import controller.MainViewControllerV3;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainGuiV3 extends Application {

	private static Scene scene;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		String fxmlPath = "MainViewV3.fxml";
		FXMLLoader fxmlLoader = new FXMLLoader(MainGuiV3.class.getClassLoader().getResource(fxmlPath));
		Parent parent = fxmlLoader.load();
		MainViewControllerV3 mainViewControllerV3 = ((MainViewControllerV3)fxmlLoader.getController());
		mainViewControllerV3.setStage(primaryStage);
		
		scene = new Scene(parent);
		//scene.getStylesheets().add("css/main.css");
		
		primaryStage.setTitle("multi-model-miner-v2");
		primaryStage.setScene(scene);
		//Setting minimum and default window size
		primaryStage.setMinWidth(1280);
		primaryStage.setMinHeight(720);
		primaryStage.setWidth(1600);
		primaryStage.setHeight(900);
		primaryStage.show();
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				Platform.exit();
				System.exit(0);
			}
		});
		
		mainViewControllerV3.markInitDone();
	}
	
}
