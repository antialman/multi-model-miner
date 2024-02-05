package controller;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;

import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import task.DiscoveryTaskDeclare;
import task.DiscoveryTaskResult;
import utils.GraphGenerator;
import utils.AlertUtils;
import utils.ConstraintTemplate;
import utils.DeclarePruningType;
import utils.FileUtils;

public class MainViewController {
	
	private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	@FXML
	private HBox mainHeader;
	@FXML
	private Button redescoverButton;
	@FXML
	private Label eventLogLabel;
	@FXML
	private TabPane resultTabPane;
	@FXML
	private WebView visualizationWebView;

	private Stage stage;

	private File logFile;
	
	private DiscoveryTaskResult discoveryTaskResult;
	private String initialWebViewScript;
	
	

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@FXML
	private void initialize() {
		resultTabPane.setDisable(true);
		redescoverButton.setDisable(true);
		setupVisualizationWebView();
	}


	@FXML
	private void selectLog() {
		File logFile = FileUtils.showLogOpenDialog(stage);
		if (logFile != null) {
			this.logFile = logFile;
			eventLogLabel.setText(logFile.getAbsolutePath());
			redescoverButton.setDisable(false);
			discoverModel();
		}
	}

	@FXML
	private void discoverModel() {
		System.out.println("Starting model discovery from event log: " + logFile.getAbsolutePath());

		mainHeader.setDisable(true);
		resultTabPane.setDisable(true);
		Task<DiscoveryTaskResult> task = createDiscoveryTask();
		addDiscoveryTaskHandlers(task);
		executorService.execute(task);
	}
	
	
	private void setupVisualizationWebView() {
		visualizationWebView.getEngine().load((getClass().getClassLoader().getResource("test.html")).toString());
		visualizationWebView.setContextMenuEnabled(false); //Setting it in FXML causes an IllegalArgumentException
		
		visualizationWebView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
			if(newValue == Worker.State.SUCCEEDED && initialWebViewScript != null) {
				System.out.println("Updating visualization in discovery tab: " + StringUtils.abbreviate(initialWebViewScript, 1000));
				visualizationWebView.getEngine().executeScript(initialWebViewScript);
			}
		});
		
		visualizationWebView.addEventFilter(ScrollEvent.SCROLL, e -> {
			if (e.isControlDown()) {
				double deltaY = e.getDeltaY();
				//Setting the value of zoom slider (instead of WebView), because then the slider also defines min and max zoom levels
				if (deltaY > 0) {
					visualizationWebView.zoomProperty().setValue(visualizationWebView.zoomProperty().getValue() + 0.1d);
				} else if (deltaY < 0) {
					visualizationWebView.zoomProperty().setValue(visualizationWebView.zoomProperty().getValue() - 0.1d);
				}
				e.consume();
			}
		});
		
	}
	
	

	private Task<DiscoveryTaskResult> createDiscoveryTask() {
		List<ConstraintTemplate> templates = List.of(ConstraintTemplate.Precedence, ConstraintTemplate.Response, ConstraintTemplate.Succession, ConstraintTemplate.Not_CoExistence);

		DiscoveryTaskDeclare discoveryTaskDeclare = new DiscoveryTaskDeclare();
		discoveryTaskDeclare.setLogFile(logFile);
		discoveryTaskDeclare.setVacuityDetection(false);
		discoveryTaskDeclare.setConsiderLifecycle(false);
		discoveryTaskDeclare.setPruningType(DeclarePruningType.ALL_REDUCTIONS);
		discoveryTaskDeclare.setSelectedTemplates(templates);
		discoveryTaskDeclare.setMinSupport(100);

		return discoveryTaskDeclare;
	}



	private void addDiscoveryTaskHandlers(Task<DiscoveryTaskResult> task) {
		//Handle task success
		task.setOnSucceeded(event -> {
			discoveryTaskResult = task.getValue();
			mainHeader.setDisable(false);
			resultTabPane.setDisable(false);
			AlertUtils.showSuccess("Declare model discovered!");
			updateVisualization();
			
		});

		//Handle task failure
		task.setOnFailed(event -> {
			mainHeader.setDisable(false);
			AlertUtils.showError("Running Declare Miner failed!");
		});

	}
	
	
	private void updateVisualization() {
		if (discoveryTaskResult != null) {
			String visualizationString;
			String script;
			
			visualizationString = GraphGenerator.createDeclareVisualizationString(discoveryTaskResult.getActivities(), discoveryTaskResult.getConstraints(), true, false);
			if (visualizationString != null) {
				script = "setModel('" + visualizationString + "')";
				if (visualizationWebView.getEngine().getLoadWorker().stateProperty().get() == Worker.State.SUCCEEDED) {
					System.out.println("Executing visualization script: " + StringUtils.abbreviate(script, 1000));
					visualizationWebView.getEngine().executeScript(script);
				} else {
					initialWebViewScript = script;
				}
			}
		} else {
			//Reloading the page in case a previous visualization script is still executing
			//TODO: Should instead track if a visualization script is still executing and stop it (if it is possible)
			initialWebViewScript = null; //Has to be set to null because it will otherwise be executed after reload
			visualizationWebView.getEngine().reload();
		}
	}


}
