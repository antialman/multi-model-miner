package controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;

import controller.tab.v4.ConstraintsTabController;
import data.DiscoveredActivity;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import task.DeclareDiscoveryResult;
import task.DeclareDiscoveryTask;
import utils.AlertUtils;
import utils.ConstraintTemplate;
import utils.DeclarePruningType;
import utils.FileUtils;
import utils.LogUtils;

public class MainViewControllerV4 {

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
	private Tab constraintsTab;


	private Stage stage;
	private ConstraintsTabController constraintsTabController;

	private File logFile;

	private DeclareDiscoveryResult declareDiscoveryResult;
	private BidiMap<DiscoveredActivity, String> activityToEncodingsMap;



	//Setup methods
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@FXML
	private void initialize() {
		resultTabPane.setDisable(true);
		redescoverButton.setDisable(true);

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("tab/v4/ConstraintsTab.fxml"));
			Region rootPane = loader.load(); //There seems to be a bug in JavaFX framework that causes IllegalStateException to be thrown instead of IOException
			constraintsTabController = loader.getController();
			constraintsTabController.setStage(this.stage);
			constraintsTab.setContent(rootPane);
		} catch (IOException | IllegalStateException e) {
			e.printStackTrace();
			AlertUtils.showError("Error loading FXML for Declare Miner Output tab!");
		}

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


	private Task<DeclareDiscoveryResult> createDeclareDiscoveryTask() {
		List<ConstraintTemplate> templates = List.of(
				ConstraintTemplate.Alternate_Succession,
				ConstraintTemplate.Succession,
				ConstraintTemplate.Alternate_Response,
				ConstraintTemplate.Alternate_Precedence,
				ConstraintTemplate.Response,
				ConstraintTemplate.Precedence,
				ConstraintTemplate.Not_Chain_Succession
				);

		DeclareDiscoveryTask declareDiscoveryTaskDeclare = new DeclareDiscoveryTask();
		declareDiscoveryTaskDeclare.setLogFile(logFile);
		declareDiscoveryTaskDeclare.setVacuityDetection(false);
		declareDiscoveryTaskDeclare.setConsiderLifecycle(false);
		declareDiscoveryTaskDeclare.setPruningType(DeclarePruningType.HIERARCHY_BASED);
		declareDiscoveryTaskDeclare.setSelectedTemplates(templates);
		declareDiscoveryTaskDeclare.setMinSupport(100);
		declareDiscoveryTaskDeclare.setArtifStartEnd(true);
		declareDiscoveryTaskDeclare.setSelfNotChainSuccession(true);

		return declareDiscoveryTaskDeclare;
	}

	private void addDeclareDiscoveryTaskHandlers(Task<DeclareDiscoveryResult> delcareDiscoveryTask) {
		//Handle task success
		delcareDiscoveryTask.setOnSucceeded(event -> {
			declareDiscoveryResult = delcareDiscoveryTask.getValue();
			sortDiscoveredActivities(declareDiscoveryResult.getActivities());
			activityToEncodingsMap = createActivityEncodings(declareDiscoveryResult.getActivities());

			constraintsTabController.setActivityEncodings(activityToEncodingsMap);

			constraintsTabController.updateTabContents(declareDiscoveryResult);

			//Updating the UI
			mainHeader.setDisable(false);
			resultTabPane.setDisable(false);

			//TODO: Construct PN fragments
		});

		//Handle task failure
		delcareDiscoveryTask.setOnFailed(event -> {
			mainHeader.setDisable(false);
			AlertUtils.showError("Running Declare Miner failed!");
		});
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

	private BidiMap<DiscoveredActivity, String> createActivityEncodings(List<DiscoveredActivity> discoveredActivities) {
		activityToEncodingsMap = new DualHashBidiMap<DiscoveredActivity, String>();
		for (DiscoveredActivity discoveredActivity : discoveredActivities) {
			activityToEncodingsMap.put(discoveredActivity, "ac"+activityToEncodingsMap.size());
		}
		return activityToEncodingsMap;
	}

}
