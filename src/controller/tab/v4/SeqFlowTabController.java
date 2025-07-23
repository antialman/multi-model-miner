package controller.tab.v4;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections15.BidiMap;

import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import data.v4.ModelSelector;
import javafx.application.Platform;
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
import javafx.util.StringConverter;
import model.v4.HybridModelSet;
import utils.WebViewUtilsV4;

public class SeqFlowTabController {

	
	
	@FXML
	private SplitPane splitPane1;
	@FXML
	private CheckBox cardinalityCheckBox;
	@FXML
	private CheckBox constraintsCheckbox;
	@FXML
	private CheckBox constrainPnCheckbox;
	@FXML
	private CheckBox toggleAllModelsCheckBox;
	@FXML
	private Button updateModelButton;
	@FXML
	private ListView<ModelSelector> modelListView;
	@FXML
	private SplitPane splitPane2;
	@FXML
	private WebView seqFlowWebView;
	@FXML
	private ListView<String> constraintLabelListView;
	
	
	private HybridModelSet seqFlowModelSet;
	private BidiMap<DiscoveredActivity, String> activityToEncodingsMap;
	
	
	@FXML
	private void initialize() {
		WebViewUtilsV4.setupWebView(seqFlowWebView);

		//Setup for getting the SplitPane dividers working correctly at startup
		ChangeListener<Number> changeListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						splitPane1.setDividerPositions(0.1);
						splitPane2.setDividerPositions(0.8);
					}
				});
			}
		};
		splitPane1.widthProperty().addListener(changeListener);
		splitPane1.heightProperty().addListener(changeListener);
		
		InvalidationListener visSettingsListener = new InvalidationListener() {
			@Override
			public void invalidated(Observable observable) {
				updateVisualization();
			}
		};

		cardinalityCheckBox.selectedProperty().addListener(visSettingsListener);
		constraintsCheckbox.selectedProperty().addListener(visSettingsListener);
		constrainPnCheckbox.selectedProperty().addListener(visSettingsListener);
		
		constraintsCheckbox.selectedProperty().addListener((obs, oldV, newV) -> {
			constrainPnCheckbox.setDisable(!newV);
		});

		//Setup for activity filtering list
		StringConverter<ModelSelector> modelConverter = new StringConverter<ModelSelector>() {
			@Override
			public String toString(ModelSelector object) {
				return object != null ? object.getPnContainer().getPetrinet().getLabel() : "";
			}
			@Override
			public ModelSelector fromString(String string) {
				return null;
			}
		};
		modelListView.setCellFactory(CheckBoxListCell.forListView(ModelSelector::isSelectedProperty, modelConverter));
	}

	@FXML
	private void updateVisualization() { //Automatic update would be possible, but requires special care because execution of the visualization script is asynchronous
		ArrayList<DiscoveredConstraint> filteredConstraints = new ArrayList<DiscoveredConstraint>(); //There should be no overlap between remaining constraints and constraints in pnContainers
		if (constraintsCheckbox.isSelected()) {
			filteredConstraints.addAll(seqFlowModelSet.getRemainingConstraints());
		}
		modelListView.getItems().filtered(item -> item.getIsSelected()).forEach(item -> filteredConstraints.addAll(item.getPnContainer().getMatchingConstraints()));
		
		
		populateConstraintList(filteredConstraints);
	}
	
	
	public void updateTabContents(HybridModelSet seqFlowModelSet) {
		this.seqFlowModelSet = seqFlowModelSet;
		populateModelFilters();
		updateVisualization();
	}
	
	private void populateModelFilters() {
		modelListView.getItems().clear();
		toggleAllModelsCheckBox.setSelected(true);
		seqFlowModelSet.getPnContainers().forEach(pnContainer -> modelListView.getItems().add(new ModelSelector(pnContainer, true)));

		//Updating toggle all checkbox state based on individual activity selections
		for (ModelSelector modelSelector : modelListView.getItems()) {
			modelSelector.isSelectedProperty().addListener((observable, oldValue, newValue) -> {
				int totalElemSize = modelListView.getItems().size();
				int selectedElemSize = modelListView.getItems().filtered(item -> item.getIsSelected()).size();

				if (totalElemSize == selectedElemSize) {
					toggleAllModelsCheckBox.setIndeterminate(false);
					toggleAllModelsCheckBox.setSelected(true);
				} else if (selectedElemSize == 0) {
					toggleAllModelsCheckBox.setIndeterminate(false);
					toggleAllModelsCheckBox.setSelected(false);
				} else {
					toggleAllModelsCheckBox.setIndeterminate(true);
				}

				updateModelButton.setStyle("-fx-font-weight: Bold;");

			});
		}

		//Toggle all checkbox behavior
		toggleAllModelsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
			modelListView.getItems().forEach(item -> {item.setIsSelected(newValue);});
		});

	}
	
	
	private void populateConstraintList(List<DiscoveredConstraint> filteredConstraints) {
		constraintLabelListView.getItems().clear();
		for (DiscoveredConstraint constraint : filteredConstraints) {
			constraintLabelListView.getItems().add(constraint.toString());
		}
	}

	public void setActivityEncodings(BidiMap<DiscoveredActivity, String> activityToEncodingsMap) {
		this.activityToEncodingsMap = activityToEncodingsMap;
	}
}
