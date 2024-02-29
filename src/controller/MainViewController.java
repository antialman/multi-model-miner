package controller;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.TransitionNode;
import task.DiscoveryTaskDeclare;
import task.DiscoveryResult;
import task.MergeStep1Task;
import task.MergeStep2Task;
import task.InitialFragmentsResult;
import task.InitialFragmentsTask;
import task.MergeStep1Result;
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
	private CheckBox addStartEndCheckBox;
	@FXML
	private ChoiceBox<DeclarePruningType> initialPruningChoice;
	@FXML
	private CheckBox pruneSubsetsCheckBox;
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
	@FXML
	private WebView fragmentsWebView;
	@FXML
	private WebView mergeStep1WebView;
	@FXML
	private WebView mergeStep2WebView;


	private Stage stage;

	private File logFile;

	private DiscoveryResult discoveryResult;
	private ConstraintSubsets constraintSubsets;
	private InitialFragmentsResult initialFragmentsResult;
	private MergeStep1Result mergeStep1Result;
	private Set<TransitionNode> mergeStep2Result;



	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@FXML
	private void initialize() {
		resultTabPane.setDisable(true);
		redescoverButton.setDisable(true);
		WebViewUtils.setupWebView(declareWebView);
		WebViewUtils.setupWebView(sucWebView);
		WebViewUtils.setupWebView(resWebView);
		WebViewUtils.setupWebView(preWebView);
		WebViewUtils.setupWebView(notcoWebView);
		WebViewUtils.setupWebView(fragmentsWebView);
		WebViewUtils.setupWebView(mergeStep1WebView);
		WebViewUtils.setupWebView(mergeStep2WebView);

		initialPruningChoice.getItems().setAll(DeclarePruningType.values());
		initialPruningChoice.getSelectionModel().select(DeclarePruningType.NONE);
		initialPruningChoice.setConverter(new StringConverter<DeclarePruningType>() {
			@Override
			public String toString(DeclarePruningType declarePruningType) {
				return declarePruningType.getDisplayText();
			}
			@Override
			public DeclarePruningType fromString(String string) {
				return null;
			}
		});

		addStartEndCheckBox.setSelected(true);
		pruneSubsetsCheckBox.setSelected(true);
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
		List<ConstraintTemplate> templates = List.of(ConstraintTemplate.Precedence, ConstraintTemplate.Response, ConstraintTemplate.Succession, ConstraintTemplate.Not_CoExistence, ConstraintTemplate.Existence, ConstraintTemplate.Absence2);

		DiscoveryTaskDeclare discoveryTaskDeclare = new DiscoveryTaskDeclare();
		discoveryTaskDeclare.setLogFile(logFile);
		discoveryTaskDeclare.setVacuityDetection(false);
		discoveryTaskDeclare.setConsiderLifecycle(false);
		discoveryTaskDeclare.setPruningType(initialPruningChoice.getSelectionModel().getSelectedItem());
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
			WebViewUtils.updateDeclareVisualization(discoveryResult, declareWebView);
			updateConstraintLabels();
			AlertUtils.showSuccess("Declare model discovered! Finding constraint subsets...");

			//Execute constraint filtering and pruning task after successful discovery
			ConstraintSubsetsTask constraintSubsetsTask = new ConstraintSubsetsTask(discoveryResult, pruneSubsetsCheckBox.isSelected());
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

			//Execute initial fragments task after successful constraint filtering and pruning
			InitialFragmentsTask initialFragmentsTask = new InitialFragmentsTask(discoveryResult.getActivities(), constraintSubsets);
			addInitialFragmentsTaskHandlers(initialFragmentsTask);
			executorService.execute(initialFragmentsTask);


		});

		//Handle task failure
		task.setOnFailed(event -> {
			AlertUtils.showError("Finding constraint subsets failed");
		});
	}



	private void addInitialFragmentsTaskHandlers(InitialFragmentsTask task) {
		//Handle task success
		task.setOnSucceeded(event -> {
			initialFragmentsResult = task.getValue();

			WebViewUtils.updateFragmentsWebView(initialFragmentsResult.getFragmentMainTransitions(), fragmentsWebView);

			//Execute step 1 of merging fragments
			MergeStep1Task mergeStep1Task = new MergeStep1Task(initialFragmentsResult);
			addMergeStep1TaskHandlers(mergeStep1Task);
			executorService.execute(mergeStep1Task);
		});

		//Handle task failure
		task.setOnFailed(event -> {
			AlertUtils.showError("Finding initial fragments failed!");
		});
	}

	private void addMergeStep1TaskHandlers(MergeStep1Task task) {
		//Handle task success
		task.setOnSucceeded(event -> {
			mergeStep1Result = task.getValue();

			WebViewUtils.updateFragmentsWebView(mergeStep1Result.getStep1MainTransitions(), mergeStep1WebView);

			//Execute step 2 of merging fragments
			MergeStep2Task mergeStep2Task = new MergeStep2Task(initialFragmentsResult, mergeStep1Result);
			addMergeStep2TaskHandlers(mergeStep2Task);
			executorService.execute(mergeStep2Task);
		});

		//Handle task failure
		task.setOnFailed(event -> {
			AlertUtils.showError("Step 1 of merge failed");
		});
	}

	private void addMergeStep2TaskHandlers(MergeStep2Task task) {
		//Handle task success
		task.setOnSucceeded(event -> {
			mergeStep2Result = task.getValue();

			WebViewUtils.updateFragmentsWebView(mergeStep2Result, mergeStep2WebView);

			
		});

		//Handle task failure
		task.setOnFailed(event -> {
			AlertUtils.showError("Step 1 of merge failed");
		});
	}

	private void updateConstraintLabels() {
		constraintLabelListView.getItems().clear();
		for (DiscoveredConstraint constraint : discoveryResult.getConstraints()) {
			constraintLabelListView.getItems().add(constraint.toString());
		}

	}


}
