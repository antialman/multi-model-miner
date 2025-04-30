package controller.tab.v3;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections15.BidiMap;
import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import data.v3.ActivitySelector;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import task.DeclareDiscoveryResult;
import utils.AlertUtils;
import utils.ConstraintTemplate;
import utils.FileUtils;
import utils.LogUtils;
import utils.ModelExporter;
import utils.WebViewUtilsV3;

public class ConstraintsTabController {

	@FXML
	private SplitPane splitPane1;
	@FXML
	private CheckBox altLayoutCheckBox;
	@FXML
	private CheckBox automatonCheckBox;
	@FXML
	private CheckBox cardinalityCheckBox;
	@FXML
	private CheckBox relatedActCheckBox;
	@FXML
	private CheckBox toggleAllActCheckBox;
	@FXML
	private Button updateModelButton;
	@FXML
	private ListView<ActivitySelector> activityListView;
	@FXML
	private SplitPane splitPane2;
	@FXML
	private WebView declMinerWebView;
	@FXML
	private ListView<String> constraintLabelListView;
	
	private Stage stage;
	
	private DeclareDiscoveryResult declareDiscoveryResult;
	private BidiMap<DiscoveredActivity, String> activityToEncodingsMap;
	
	private List<DiscoveredActivity> filteredActivities; //Used for updating visualization and also for exporting the model
	private List<DiscoveredConstraint> filteredConstraints; //Used for updating visualization and also for exporting the model
	
	//Setup methods
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	@FXML
	private void initialize() {
		WebViewUtilsV3.setupWebView(declMinerWebView);

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
		
		InvalidationListener visSettingsListener = new InvalidationListener() {
			@Override
			public void invalidated(Observable observable) {
				updateVisualization();
			}
		};

		altLayoutCheckBox.selectedProperty().addListener(visSettingsListener);
		automatonCheckBox.selectedProperty().addListener(visSettingsListener);
		cardinalityCheckBox.selectedProperty().addListener(visSettingsListener);
		relatedActCheckBox.selectedProperty().addListener(visSettingsListener);

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
	private void updateVisualization() { //Automatic update would be possible, but requires special care because execution of the visualization script is asynchronous
		filteredActivities = new ArrayList<DiscoveredActivity>();
		activityListView.getItems().filtered(item -> item.getIsSelected()).forEach(item -> filteredActivities.add(item.getDiscoveredActivity()));

		filteredConstraints = new ArrayList<DiscoveredConstraint>();
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
		if (relatedActCheckBox.isSelected()) { //Adding the related (but unselected) activities if necessary
			for (DiscoveredConstraint discoveredConstraint : filteredConstraints) {
				if(!filteredActivities.contains(discoveredConstraint.getActivationActivity())) filteredActivities.add(discoveredConstraint.getActivationActivity());
				if(!filteredActivities.contains(discoveredConstraint.getTargetActivity())) filteredActivities.add(discoveredConstraint.getTargetActivity());
			}
		}
		if (cardinalityCheckBox.isSelected()) {
			for (DiscoveredActivity filteredActivity : filteredActivities) {
				if (filteredActivity.getActivityName().equals(LogUtils.ARTIF_START) || filteredActivity.getActivityName().equals(LogUtils.ARTIF_END)) {
					filteredConstraints.add(new DiscoveredConstraint(ConstraintTemplate.Exactly1, filteredActivity, null, 1));
				}
			}
		}

		WebViewUtilsV3.updateWebView(filteredActivities, filteredConstraints, declMinerWebView, altLayoutCheckBox.isSelected(), automatonCheckBox.isSelected(), activityToEncodingsMap);
		populateConstraintLabels(filteredConstraints);
		updateModelButton.setStyle("-fx-font-weight: Normal;");
	}
	
	
	@FXML
	public void exportModel() {
		File chosenFile = FileUtils.showDeclSaveDialog(stage);
		
		if (chosenFile != null) {
			try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(chosenFile.getAbsolutePath()))) {
				writer.write(ModelExporter.getDeclString(filteredActivities, filteredConstraints));
				AlertUtils.showSuccess("Model successfully exported");
			} catch (IOException e) {
				AlertUtils.showError("Exporting the model failed!");
				e.printStackTrace();
			}
		}
	}
	
	
	public void updateTabContents(DeclareDiscoveryResult declareDiscoveryResult) {
		this.declareDiscoveryResult = declareDiscoveryResult;
		populateActivityFilters();
		updateVisualization();
	}
	
	private void populateActivityFilters() {
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

				updateModelButton.setStyle("-fx-font-weight: Bold;");

			});
		}

		//Toggle all checkbox behavior
		toggleAllActCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
			activityListView.getItems().forEach(item -> {item.setIsSelected(newValue);});
		});

	}

	private void populateConstraintLabels(List<DiscoveredConstraint> filteredConstraints) {
		constraintLabelListView.getItems().clear();
		for (DiscoveredConstraint constraint : filteredConstraints) {
			constraintLabelListView.getItems().add(constraint.toString());
		}
	}

	public void setActivityEncodings(BidiMap<DiscoveredActivity, String> activityToEncodingsMap) {
		this.activityToEncodingsMap = activityToEncodingsMap;
	}

}
