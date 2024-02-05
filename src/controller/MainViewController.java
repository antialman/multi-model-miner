package controller;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import data.DiscoveredConstraint;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import task.DiscoveryTaskDeclare;
import task.DiscoveryTaskResult;
import utils.WebViewUtils;
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
	private WebView declareWebView;
	@FXML
	private ListView<String> constraintLabelListView;
	@FXML
	private WebView reqWebView;
	@FXML
	private WebView sucWebView;
	@FXML
	private WebView preWebView;
	@FXML
	private WebView resWebView;
	@FXML
	private WebView notcoWebView;

	private Stage stage;

	private File logFile;
	
	private DiscoveryTaskResult discoveryTaskResult;
	
	private String initialDeclareWebViewScript;
	private String initialReqWebViewScript;
	private String initialSucWebViewScript;
	private String initialPreWebViewScript;
	private String initialResWebViewScript;
	private String initialNotcoWebViewScript;
	
	

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@FXML
	private void initialize() {
		resultTabPane.setDisable(true);
		redescoverButton.setDisable(true);
		WebViewUtils.setupWebView(declareWebView, initialDeclareWebViewScript);
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
			WebViewUtils.updateDeclareVisualization(discoveryTaskResult, declareWebView, initialDeclareWebViewScript);
			updateConstraintLabels();
			
		});

		//Handle task failure
		task.setOnFailed(event -> {
			mainHeader.setDisable(false);
			AlertUtils.showError("Running Declare Miner failed!");
		});

	}
	
	

	private void updateConstraintLabels() {
		constraintLabelListView.getItems().clear();
		for (DiscoveredConstraint constraint : discoveryTaskResult.getConstraints()) {
			constraintLabelListView.getItems().add(constraint.toString());
		}
		
	}


}
