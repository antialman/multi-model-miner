package controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import controller.tab.v3.ConstraintsTabController;
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
	private Tab constraintsTab;

	private Stage stage;
	private ConstraintsTabController constraintsTabController;

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
		
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("tab/ConstraintsTab.fxml"));
			Region rootPane = loader.load(); //There seems to be a bug in JavaFX framework that causes IllegalStateException to be thrown instead of IOException
			constraintsTabController = loader.getController();

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
			constraintsTabController.updateTabContents(declareDiscoveryResult);
			
			//Updating the UI
			mainHeader.setDisable(false);
			resultTabPane.setDisable(false);

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
	
	public File getLogFile() {
		return logFile;
	}
	
	public DeclareDiscoveryResult getDeclareDiscoveryResult() {
		return declareDiscoveryResult;
	}
	

}
