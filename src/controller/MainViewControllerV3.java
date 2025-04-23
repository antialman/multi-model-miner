package controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import data.v3.ActivitySelector;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import task.DeclareDiscoveryResult;
import task.DeclareDiscoveryTask;
import utils.AlertUtils;
import utils.ConstraintTemplate;
import utils.DeclarePruningType;
import utils.FileUtils;
import utils.LogUtils;
import utils.WebViewUtilsV3;

public class MainViewControllerV3 {

	//For running the Declare Miner and follow-up discovery tasks asynchronously 
	private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	//JavaFX UI elements
	@FXML
	private HBox mainHeader;
	@FXML
	private Label eventLogLabel;
	@FXML
	private Button redescoverButton;
	@FXML
	private TabPane resultTabPane;
	@FXML
	private SplitPane splitPane1;
	@FXML
	private CheckBox altLayoutCheckbox;
	@FXML
	private CheckBox relatedActCheckBox;
	@FXML
	private CheckBox toggleAllActCheckBox;
	@FXML
	private Button applyFilterButton;
	@FXML
	private ListView<ActivitySelector> activityListView;
	@FXML
	private SplitPane splitPane2;
	@FXML
	private WebView declareWebView;
	@FXML
	private ListView<String> constraintLabelListView;

	private Stage stage;

	private File logFile;

	private DeclareDiscoveryResult declareDiscoveryResult;


	//Setup methods
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	@FXML
	private void initialize() {
		resultTabPane.setDisable(true);
		redescoverButton.setDisable(true);

		WebViewUtilsV3.setupWebView(declareWebView);

		//Setup for getting the SplitPane dividers working correctly at startup
		ChangeListener<Number> changeListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				splitPane1.setDividerPositions(0.1);
				splitPane2.setDividerPositions(0.8);
			}
		};
		splitPane1.widthProperty().addListener(changeListener);
		splitPane2.heightProperty().addListener(changeListener);

		//Setup for alternate layout
		altLayoutCheckbox.selectedProperty().addListener((ev) -> {
			WebViewUtilsV3.updateDeclareWebView(declareDiscoveryResult.getActivities(), declareDiscoveryResult.getConstraints(), declareWebView, altLayoutCheckbox.isSelected());
		});

		//Setup for activity filtering list
		StringConverter<ActivitySelector> activityConverter = new StringConverter<ActivitySelector>() {
			@Override
			public String toString(ActivitySelector object) {
				return object != null ? object.getDiscoveredActivity().getActivityName() : "";
			}
			@Override
			public ActivitySelector fromString(String string) {
				return null;
			}
		};
		activityListView.setCellFactory(CheckBoxListCell.forListView(ActivitySelector::isSelectedProperty, activityConverter));
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
	private void applyFilter() { //Automatic update would be possible, but requires special care because execution of the visualization script is asynchronous
		List<DiscoveredActivity> filteredActivities = new ArrayList<DiscoveredActivity>();
		activityListView.getItems().filtered(item -> item.getIsSelected()).forEach(item -> filteredActivities.add(item.getDiscoveredActivity()));

		List<DiscoveredConstraint> filteredConstraints = new ArrayList<DiscoveredConstraint>();
		for (DiscoveredConstraint discoveredConstraint : declareDiscoveryResult.getConstraints()) {
			if (relatedActCheckBox.isSelected()) {
				if (!filteredConstraints.contains(discoveredConstraint) && (filteredActivities.contains(discoveredConstraint.getActivationActivity()) || filteredActivities.contains(discoveredConstraint.getTargetActivity()))) {
					filteredConstraints.add(discoveredConstraint);
				}
			} else {
				if (!filteredConstraints.contains(discoveredConstraint) && (filteredActivities.contains(discoveredConstraint.getActivationActivity()) && filteredActivities.contains(discoveredConstraint.getTargetActivity()))) {
					filteredConstraints.add(discoveredConstraint);
				}
			}
		}
		if (relatedActCheckBox.isSelected()) {
			for (DiscoveredConstraint discoveredConstraint : filteredConstraints) {
				if(!filteredActivities.contains(discoveredConstraint.getActivationActivity())) filteredActivities.add(discoveredConstraint.getActivationActivity());
				if(!filteredActivities.contains(discoveredConstraint.getTargetActivity())) filteredActivities.add(discoveredConstraint.getTargetActivity());
			}
		}

		WebViewUtilsV3.updateDeclareWebView(filteredActivities, new ArrayList<DiscoveredConstraint>(filteredConstraints), declareWebView, altLayoutCheckbox.isSelected());
		applyFilterButton.setStyle("-fx-font-weight: Normal;");
	}



	private Task<DeclareDiscoveryResult> createDeclareDiscoveryTask() {
		List<ConstraintTemplate> templates = List.of(
				ConstraintTemplate.Alternate_Succession,
				ConstraintTemplate.Succession,
				ConstraintTemplate.Alternate_Response,
				ConstraintTemplate.Alternate_Precedence,
				ConstraintTemplate.Response,
				ConstraintTemplate.Precedence
				);

		DeclareDiscoveryTask declareDiscoveryTaskDeclare = new DeclareDiscoveryTask();
		declareDiscoveryTaskDeclare.setLogFile(logFile);
		declareDiscoveryTaskDeclare.setVacuityDetection(false);
		declareDiscoveryTaskDeclare.setConsiderLifecycle(false);
		declareDiscoveryTaskDeclare.setPruningType(DeclarePruningType.HIERARCHY_BASED);
		declareDiscoveryTaskDeclare.setSelectedTemplates(templates);
		declareDiscoveryTaskDeclare.setMinSupport(100);
		declareDiscoveryTaskDeclare.setArtifStartEnd(true);

		return declareDiscoveryTaskDeclare;
	}

	private void addDeclareDiscoveryTaskHandlers(Task<DeclareDiscoveryResult> delcareDiscoveryTask) {
		//Handle task success
		delcareDiscoveryTask.setOnSucceeded(event -> {
			declareDiscoveryResult = delcareDiscoveryTask.getValue();
			sortDiscoveredActivities(declareDiscoveryResult.getActivities());
			
			//Updating the UI
			mainHeader.setDisable(false);
			resultTabPane.setDisable(false);
			WebViewUtilsV3.updateDeclareWebView(declareDiscoveryResult.getActivities(), declareDiscoveryResult.getConstraints(), declareWebView, altLayoutCheckbox.isSelected());
			updateActivityFilters();
			updateConstraintLabels();


			//Execute Declare post-processing task //TODO
			//			DeclarePostprocessingTask declarePostprocessingTask = new DeclarePostprocessingTask();
			//			declarePostprocessingTask.setDeclareDiscoveryResult(declareDiscoveryResult);
			//			addDeclarePostprocessingTaskHandlers(declarePostprocessingTask);
			//			executorService.execute(declarePostprocessingTask);

		});

		//Handle task failure
		delcareDiscoveryTask.setOnFailed(event -> {
			mainHeader.setDisable(false);
			AlertUtils.showError("Running Declare Miner failed!");
		});
	}

	private void updateActivityFilters() {
		activityListView.getItems().clear();
		relatedActCheckBox.setSelected(true);
		toggleAllActCheckBox.setSelected(true);
		declareDiscoveryResult.getActivities().forEach(act -> activityListView.getItems().add(new ActivitySelector(act, true)));

		//Updating toggle all checkbox state based on individual activity selections
		for (ActivitySelector activitySelector : activityListView.getItems()) {
			activitySelector.isSelectedProperty().addListener((observable, oldValue, newValue) -> {
				int totalElemSize = activityListView.getItems().size();
				int selectedElemSize = activityListView.getItems().filtered(item -> item.getIsSelected()).size();

				if (totalElemSize == selectedElemSize) {
					toggleAllActCheckBox.setIndeterminate(false);
					toggleAllActCheckBox.setSelected(true);
				} else if (selectedElemSize == 0) {
					toggleAllActCheckBox.setIndeterminate(false);
					toggleAllActCheckBox.setSelected(false);
				} else {
					toggleAllActCheckBox.setIndeterminate(true);
				}

				applyFilterButton.setStyle("-fx-font-weight: Bold;");

			});
		}

		//Toggle all checkbox behavior
		toggleAllActCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
			activityListView.getItems().forEach(item -> {item.setIsSelected(newValue);});
		});

	}

	private void updateConstraintLabels() {
		constraintLabelListView.getItems().clear();
		for (DiscoveredConstraint constraint : declareDiscoveryResult.getConstraints()) {
			constraintLabelListView.getItems().add(constraint.toString());
		}
	}

	private void sortDiscoveredActivities(List<DiscoveredActivity> discoveredActivities) {
		//Sorting activities by name, and moving artificial start and end to be the first activities in the activity list (makes the UI a bit nicer)
		discoveredActivities.sort((o1,o2)->{
			return o1.getActivityName().compareToIgnoreCase(o2.getActivityName());
		});
		DiscoveredActivity artificialStart = null;
		DiscoveredActivity artificialEnd = null;
		for (DiscoveredActivity discoveredActivity : declareDiscoveryResult.getActivities()) {
			if (discoveredActivity.getActivityName().equals(LogUtils.ARTIF_START)) 
				artificialStart = discoveredActivity;
			if (discoveredActivity.getActivityName().equals(LogUtils.ARTIF_END)) 
				artificialEnd = discoveredActivity;
		}
		if (artificialEnd != null) {
			discoveredActivities.remove(artificialEnd);
			discoveredActivities.add(0, artificialEnd);
		}
		if (artificialStart != null) {
			discoveredActivities.remove(artificialStart);
			discoveredActivities.add(0, artificialStart);
		}
	}

}
