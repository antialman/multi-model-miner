package controller;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import data.DiscoveredConstraint;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import task.DiscoveryTaskDeclare;
import task.DiscoveryResult;
import utils.WebViewUtilsV2;
import utils.AlertUtils;
import utils.ConstraintTemplate;
import utils.DeclarePruningType;
import utils.FileUtils;

public class MainViewControllerV2 {

	private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	@FXML
	private HBox mainHeader;
	@FXML
	private CheckBox addStartEndCheckBox;
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
	private SplitPane subsetsSplitPane;
	@FXML
	private ListView<String> reqActivitiesListView;
	@FXML
	private ListView<String> noRepActivitiesListview;
	@FXML
	private ListView<String> noCardActivitiesListview;
	@FXML
	private WebView sucWebView;
	@FXML
	private WebView resWebView;
	@FXML
	private WebView preWebView;
	@FXML
	private WebView notcoWebView;


	private Stage stage;

	private File logFile;

	private DiscoveryResult discoveryResult;


	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@FXML
	private void initialize() {
		resultTabPane.setDisable(true);
		redescoverButton.setDisable(true);
		addStartEndCheckBox.setSelected(true);
		
		WebViewUtilsV2.setupWebView(declareWebView);
		WebViewUtilsV2.setupWebView(sucWebView);
		WebViewUtilsV2.setupWebView(resWebView);
		WebViewUtilsV2.setupWebView(preWebView);
		WebViewUtilsV2.setupWebView(notcoWebView);
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
		Task<DiscoveryResult> task = createDiscoveryTask();
		addDiscoveryTaskHandlers(task);
		executorService.execute(task);
	}

	@FXML
	private void expandCard() {
		subsetsSplitPane.setDividerPositions(0.6, 0.7, 0.8, 0.9);
	}

	@FXML
	private void expandSuc() {
		subsetsSplitPane.setDividerPositions(0.1, 0.7, 0.8, 0.9);
	}

	@FXML
	private void expandRes() {
		subsetsSplitPane.setDividerPositions(0.1, 0.2, 0.8, 0.9);
	}

	@FXML
	private void expandPre() {
		subsetsSplitPane.setDividerPositions(0.1, 0.2, 0.3, 0.9);
	}

	@FXML
	private void expandNotco() {
		subsetsSplitPane.setDividerPositions(0.1, 0.2, 0.3, 0.4);
	}



	private Task<DiscoveryResult> createDiscoveryTask() {
		List<ConstraintTemplate> templates = List.of(
				ConstraintTemplate.Precedence,
				ConstraintTemplate.Response,
				ConstraintTemplate.Succession,
				ConstraintTemplate.Not_Succession,
				ConstraintTemplate.CoExistence,
				ConstraintTemplate.Not_CoExistence,
				ConstraintTemplate.Existence,
				ConstraintTemplate.Absence2
				);

		DiscoveryTaskDeclare discoveryTaskDeclare = new DiscoveryTaskDeclare();
		discoveryTaskDeclare.setLogFile(logFile);
		discoveryTaskDeclare.setVacuityDetection(false);
		discoveryTaskDeclare.setConsiderLifecycle(false);
		discoveryTaskDeclare.setPruningType(DeclarePruningType.NONE);
		discoveryTaskDeclare.setSelectedTemplates(templates);
		discoveryTaskDeclare.setMinSupport(100);

		discoveryTaskDeclare.setArtifStartEnd(addStartEndCheckBox.isSelected());

		return discoveryTaskDeclare;
	}

	private void addDiscoveryTaskHandlers(Task<DiscoveryResult> task) {
		//Handle task success
		task.setOnSucceeded(event -> {
			discoveryResult = task.getValue();
			mainHeader.setDisable(false);
			resultTabPane.setDisable(false);
			WebViewUtilsV2.updateDeclareVisualization(discoveryResult, declareWebView);
			updateConstraintLabels();
			AlertUtils.showSuccess("Declare model discovered! Finding constraint subsets...");

			//Execute constraint filtering and pruning task after successful discovery
//			DataStorePrepTask dataStorePrepTask = new DataStorePrepTask(discoveryResult);
//			addDataStorePrepTaskHandlers(dataStorePrepTask);
//			executorService.execute(dataStorePrepTask);

		});

		//Handle task failure
		task.setOnFailed(event -> {
			mainHeader.setDisable(false);
			AlertUtils.showError("Running Declare Miner failed!");
		});

	}


//	private void addDataStorePrepTaskHandlers(Task<DataStore> task) {
//		//Handle task success
//		task.setOnSucceeded(event -> {
//			dataStore = task.getValue();
//			reqActivitiesListView.getItems().clear();
//			noRepActivitiesListview.getItems().clear();
//			noCardActivitiesListview.getItems().clear();
//
//			for (DiscoveredActivity reqActivity : dataStore.getReqActivities()) {
//				reqActivitiesListView.getItems().add(reqActivity.getActivityName());
//			}
//			for (DiscoveredActivity noRepActivity : dataStore.getNoRepActivities()) {
//				noRepActivitiesListview.getItems().add(noRepActivity.getActivityName());
//			}
//			for (DiscoveredActivity noCardActivity : dataStore.getNoCardActivities()) {
//				noCardActivitiesListview.getItems().add(noCardActivity.getActivityName());
//			}
//
//			WebViewUtilsV2.updateSubsetsWebView(constraintSubsets.getSucActivities(), constraintSubsets.getSucConstraints(), sucWebView);
//			WebViewUtilsV2.updateSubsetsWebView(constraintSubsets.getPreActivities(), constraintSubsets.getPreConstraints(), preWebView);
//			WebViewUtilsV2.updateSubsetsWebView(constraintSubsets.getResActivities(), constraintSubsets.getResConstraints(), resWebView);
//			WebViewUtilsV2.updateSubsetsWebView(constraintSubsets.getNotcoActivities(), constraintSubsets.getNotcoConstraints(), notcoWebView);
//
//			//Execute initial fragments task after successful constraint filtering and pruning
//			//InitialFragmentsTask initialFragmentsTask = new InitialFragmentsTask(discoveryResult.getActivities(), constraintSubsets);
//			//addInitialFragmentsTaskHandlers(initialFragmentsTask);
//			//executorService.execute(initialFragmentsTask);
//
//
//		});
//
//		//Handle task failure
//		task.setOnFailed(event -> {
//			AlertUtils.showError("Finding constraint subsets failed");
//		});
//	}



	

	private void updateConstraintLabels() {
		constraintLabelListView.getItems().clear();
		for (DiscoveredConstraint constraint : discoveryResult.getConstraints()) {
			constraintLabelListView.getItems().add(constraint.toString());
		}

	}


}
