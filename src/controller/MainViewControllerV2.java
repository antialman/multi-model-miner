package controller;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import data.DiscoveredActivity;
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
import task.DeclareDiscoveryTask;
import task.v2.DeclarePostprocessingResult;
import task.v2.DeclarePostprocessingTask;
import task.v2.InitialPetriNetResult;
import task.v2.InitialPetriNetTask;
import task.DeclareDiscoveryResult;
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
	private WebView succWebView;
	@FXML
	private WebView respWebView;
	@FXML
	private WebView precWebView;
	@FXML
	private WebView notcoWebView;


	private Stage stage;

	private File logFile;

	private DeclareDiscoveryResult declareDiscoveryResult;
	private DeclarePostprocessingResult declarePostprocessingResult;
	private InitialPetriNetResult initialPetriNetResult;


	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@FXML
	private void initialize() {
		resultTabPane.setDisable(true);
		redescoverButton.setDisable(true);
		addStartEndCheckBox.setSelected(true);

		WebViewUtilsV2.setupWebView(declareWebView);
		WebViewUtilsV2.setupWebView(succWebView);
		WebViewUtilsV2.setupWebView(respWebView);
		WebViewUtilsV2.setupWebView(precWebView);
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
		Task<DeclareDiscoveryResult> task = createDeclareDiscoveryTask();
		addDeclareDiscoveryTaskHandlers(task);
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



	private Task<DeclareDiscoveryResult> createDeclareDiscoveryTask() {
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

		DeclareDiscoveryTask declareDiscoveryTaskDeclare = new DeclareDiscoveryTask();
		declareDiscoveryTaskDeclare.setLogFile(logFile);
		declareDiscoveryTaskDeclare.setVacuityDetection(false);
		declareDiscoveryTaskDeclare.setConsiderLifecycle(false);
		declareDiscoveryTaskDeclare.setPruningType(DeclarePruningType.NONE);
		declareDiscoveryTaskDeclare.setSelectedTemplates(templates);
		declareDiscoveryTaskDeclare.setMinSupport(100);

		declareDiscoveryTaskDeclare.setArtifStartEnd(addStartEndCheckBox.isSelected());

		return declareDiscoveryTaskDeclare;
	}

	private void addDeclareDiscoveryTaskHandlers(Task<DeclareDiscoveryResult> delcareDiscoveryTask) {
		//Handle task success
		delcareDiscoveryTask.setOnSucceeded(event -> {
			declareDiscoveryResult = delcareDiscoveryTask.getValue();
			mainHeader.setDisable(false);
			resultTabPane.setDisable(false);
			updateConstraintLabels();

			//Execute Declare post-processing task
			DeclarePostprocessingTask declarePostprocessingTask = new DeclarePostprocessingTask();
			declarePostprocessingTask.setDeclareDiscoveryResult(declareDiscoveryResult);
			addDeclarePostprocessingTaskHandlers(declarePostprocessingTask);
			executorService.execute(declarePostprocessingTask);

		});

		//Handle task failure
		delcareDiscoveryTask.setOnFailed(event -> {
			mainHeader.setDisable(false);
			AlertUtils.showError("Running Declare Miner failed!");
		});

	}


	private void addDeclarePostprocessingTaskHandlers(Task<DeclarePostprocessingResult> task) {
		//Handle task success
		task.setOnSucceeded(event -> {
			declarePostprocessingResult = task.getValue();
			reqActivitiesListView.getItems().clear();
			noRepActivitiesListview.getItems().clear();
			noCardActivitiesListview.getItems().clear();


			List<DiscoveredConstraint> patternConstraints = declarePostprocessingResult.getPrunedConstraints().stream().filter(c -> (
					c.getTemplate() == ConstraintTemplate.Succession || 
					c.getTemplate() == ConstraintTemplate.Precedence || 
					c.getTemplate() == ConstraintTemplate.Response || 
					c.getTemplate() == ConstraintTemplate.Not_CoExistence ||
					c.getTemplate() == ConstraintTemplate.Existence ||
					c.getTemplate() == ConstraintTemplate.Absence2) 
					).collect(Collectors.toList());
			WebViewUtilsV2.updateSubsetsWebView(declarePostprocessingResult.getAllActivities(), patternConstraints, declareWebView, false);

			AlertUtils.showSuccess("Declare model discovered and post-processing done! Starting Petri net construction.");


			for (DiscoveredActivity reqActivity : declarePostprocessingResult.getReqActivities()) {
				reqActivitiesListView.getItems().add(reqActivity.getActivityName());
			}
			for (DiscoveredActivity noRepActivity : declarePostprocessingResult.getNoRepActivities()) {
				noRepActivitiesListview.getItems().add(noRepActivity.getActivityName());
			}
			for (DiscoveredActivity noCardActivity : declarePostprocessingResult.getNoCardActivities()) {
				noCardActivitiesListview.getItems().add(noCardActivity.getActivityName());
			}

			WebViewUtilsV2.updateSubsetsWebView(declarePostprocessingResult.getSuccActivities(), declarePostprocessingResult.getSuccPrunedConstraints(), succWebView, true);
			WebViewUtilsV2.updateSubsetsWebView(declarePostprocessingResult.getPrecActivities(), declarePostprocessingResult.getPrecPrunedConstraints(), precWebView, true);
			WebViewUtilsV2.updateSubsetsWebView(declarePostprocessingResult.getRespActivities(), declarePostprocessingResult.getRespPrunedConstraints(), respWebView, true);
			WebViewUtilsV2.updateSubsetsWebView(declarePostprocessingResult.getNotcoActivities(), declarePostprocessingResult.getNotcoAllConstraints(), notcoWebView, true);

			//Execute initial Petri net task after successful Declare post-processing
			InitialPetriNetTask initialPetriNetTask = new InitialPetriNetTask(declarePostprocessingResult);
			addInitialPetriNetTaskHandlers(initialPetriNetTask);
			executorService.execute(initialPetriNetTask);
		});

		//Handle task failure
		task.setOnFailed(event -> {
			AlertUtils.showError("Declare post-processing failed");
		});
	}


	private void addInitialPetriNetTaskHandlers(Task<InitialPetriNetResult> task) {
		//Handle task success
		task.setOnSucceeded(event -> {
			initialPetriNetResult = task.getValue();
		});

		//Handle task failure
		task.setOnFailed(event -> {
			AlertUtils.showError("Initial Petri net task failed");
		});
	}



	private void updateConstraintLabels() {
		constraintLabelListView.getItems().clear();
		for (DiscoveredConstraint constraint : declareDiscoveryResult.getConstraints()) {
			constraintLabelListView.getItems().add(constraint.toString());
		}

	}


}
