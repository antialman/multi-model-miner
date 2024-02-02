package controller;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import task.DiscoveryTaskDeclare;
import task.DiscoveryTaskResult;
import utils.AlertUtils;
import utils.ConstraintTemplate;
import utils.DeclarePruningType;
import utils.FileUtils;

public class MainViewController {
	
	private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	@FXML
	private HBox mainHeader;
	@FXML
	private Label eventLogLabel;
	@FXML
	private Button discoverButton;

	private Stage stage;

	private File logFile;
	
	private DiscoveryTaskResult discoveryTaskResult;
	
	

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@FXML
	private void initialize() {
		discoverButton.setDisable(true);
	}


	@FXML
	private void selectLog() {
		File logFile = FileUtils.showLogOpenDialog(stage);
		if (logFile != null) {
			this.logFile = logFile;
			eventLogLabel.setText(logFile.getAbsolutePath());
			discoverButton.setDisable(false);
		}
	}


	@FXML
	private void discoverModel() {
		System.out.println("Starting model discovery from event log: " + logFile.getAbsolutePath());

		Task<DiscoveryTaskResult> task = createDiscoveryTask();
		addDiscoveryTaskHandlers(task);
		mainHeader.setDisable(true);
		executorService.execute(task);


	}

	private Task<DiscoveryTaskResult> createDiscoveryTask() {
		List<ConstraintTemplate> templates = List.of(ConstraintTemplate.Precedence, ConstraintTemplate.Response, ConstraintTemplate.Succession, ConstraintTemplate.Not_CoExistence);

		DiscoveryTaskDeclare discoveryTaskDeclare = new DiscoveryTaskDeclare();
		discoveryTaskDeclare.setLogFile(logFile);
		discoveryTaskDeclare.setVacuityDetection(false);
		discoveryTaskDeclare.setConsiderLifecycle(false);
		discoveryTaskDeclare.setPruningType(DeclarePruningType.TRANSITIVE_CLOSURE);
		discoveryTaskDeclare.setSelectedTemplates(templates);
		discoveryTaskDeclare.setMinSupport(100);

		return discoveryTaskDeclare;
	}



	private void addDiscoveryTaskHandlers(Task<DiscoveryTaskResult> task) {
		//Handle task success
		task.setOnSucceeded(event -> {
			discoveryTaskResult = task.getValue();
			mainHeader.setDisable(false);
			AlertUtils.showSuccess("Success!");
			
		});

		//Handle task failure
		task.setOnFailed(event -> {
			mainHeader.setDisable(false);
			AlertUtils.showSuccess("Fail!");
		});

	}


}
