package controller;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import task.DiscoveryTaskDeclare;
import task.DiscoveryTaskResult;
import task.ConstraintSubsets;
import task.ConstraintSubsetsTask;
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
	private ChoiceBox<DeclarePruningType> declarePruningChoice;
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
	private ListView<String> reqActivitiesListView;
	@FXML
	private ListView<String> noRepActivitiesListview;
	@FXML
	private ListView<String> noCardActivitiesListview;
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
	private ConstraintSubsets constraintSubsets;



	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@FXML
	private void initialize() {
		resultTabPane.setDisable(true);
		redescoverButton.setDisable(true);
		WebViewUtils.setupWebView(declareWebView);
		WebViewUtils.setupWebView(sucWebView);
		WebViewUtils.setupWebView(preWebView);
		WebViewUtils.setupWebView(resWebView);
		WebViewUtils.setupWebView(notcoWebView);
		
		declarePruningChoice.getItems().setAll(DeclarePruningType.values());
		declarePruningChoice.getSelectionModel().select(DeclarePruningType.HIERARCHY_BASED);
		declarePruningChoice.setConverter(new StringConverter<DeclarePruningType>() {
			@Override
			public String toString(DeclarePruningType declarePruningType) {
				return declarePruningType.getDisplayText();
			}
			@Override
			public DeclarePruningType fromString(String string) {
				return null;
			}
		});
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
		List<ConstraintTemplate> templates = List.of(ConstraintTemplate.Precedence, ConstraintTemplate.Response, ConstraintTemplate.Succession, ConstraintTemplate.Not_CoExistence, ConstraintTemplate.Existence, ConstraintTemplate.Absence2);

		DiscoveryTaskDeclare discoveryTaskDeclare = new DiscoveryTaskDeclare();
		discoveryTaskDeclare.setLogFile(logFile);
		discoveryTaskDeclare.setVacuityDetection(false);
		discoveryTaskDeclare.setConsiderLifecycle(false);
		discoveryTaskDeclare.setPruningType(declarePruningChoice.getSelectionModel().getSelectedItem());
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
			WebViewUtils.updateDeclareVisualization(discoveryTaskResult, declareWebView);
			updateConstraintLabels();
			AlertUtils.showSuccess("Declare model discovered! Finding constraint subsets...");

			ConstraintSubsetsTask constraintSubsetsTask = new ConstraintSubsetsTask(discoveryTaskResult);
			addConstraintSubsetsTaskHandlers(constraintSubsetsTask);
			executorService.execute(constraintSubsetsTask);

		});

		//Handle task failure
		task.setOnFailed(event -> {
			mainHeader.setDisable(false);
			AlertUtils.showError("Running Declare Miner failed!");
		});

	}


	private void addConstraintSubsetsTaskHandlers(Task<ConstraintSubsets> task) {
		//Handle task success
		task.setOnSucceeded(event -> {
			constraintSubsets = task.getValue();
			reqActivitiesListView.getItems().clear();
			noRepActivitiesListview.getItems().clear();
			noCardActivitiesListview.getItems().clear();
			
			for (DiscoveredActivity reqActivity : constraintSubsets.getReqActivities()) {
				reqActivitiesListView.getItems().add(reqActivity.getActivityName());
			}
			for (DiscoveredActivity noRepActivity : constraintSubsets.getNoRepActivities()) {
				noRepActivitiesListview.getItems().add(noRepActivity.getActivityName());
			}
			for (DiscoveredActivity noCardActivity : constraintSubsets.getNoCardActivities()) {
				noCardActivitiesListview.getItems().add(noCardActivity.getActivityName());
			}
			
			WebViewUtils.updateSubsetsWebView(constraintSubsets.getSucActivities(), constraintSubsets.getSucConstraints(), sucWebView);
			WebViewUtils.updateSubsetsWebView(constraintSubsets.getPreActivities(), constraintSubsets.getPreConstraints(), preWebView);
			WebViewUtils.updateSubsetsWebView(constraintSubsets.getResActivities(), constraintSubsets.getResConstraints(), resWebView);
			WebViewUtils.updateSubsetsWebView(constraintSubsets.getNotcoActivities(), constraintSubsets.getNotcoConstraints(), notcoWebView);
		});
		
		//Handle task failure
		task.setOnFailed(event -> {
			mainHeader.setDisable(false);
			AlertUtils.showError("Finding constraint subsets failed");
		});
	}



	private void updateConstraintLabels() {
		constraintLabelListView.getItems().clear();
		for (DiscoveredConstraint constraint : discoveryTaskResult.getConstraints()) {
			constraintLabelListView.getItems().add(constraint.toString());
		}

	}


}
