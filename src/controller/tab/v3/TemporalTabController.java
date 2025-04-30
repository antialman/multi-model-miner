package controller.tab.v3;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections15.BidiMap;
import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import task.DeclareDiscoveryResult;
import utils.ConstraintTemplate;
import utils.LogUtils;
import utils.WebViewUtilsV3;

public class TemporalTabController {

	@FXML
	private SplitPane splitPane1;
	@FXML
	private ListView<DiscoveredActivity> activityListView;
	@FXML
	private SplitPane splitPane2;
	@FXML
	private CheckBox altLayoutDirectCheckBox;
	@FXML
	private CheckBox automatonDirectCheckBox;
	@FXML
	private WebView directRelationsWebView;
	@FXML
	private RadioButton followersRadioButton;
	@FXML
	private ToggleGroup amongToggleGroup;
	@FXML
	private RadioButton preceedersRadioButton;
	@FXML
	private CheckBox altLayoutAmongCheckBox;
	@FXML
	private CheckBox automatonAmongCheckBox;
	@FXML
	private WebView amongRelationsWebView;
	@FXML
	private Label closestPreceedersLabel;
	@FXML
	private Label closestFollowersLabel;

	private Stage stage;

	private DeclareDiscoveryResult declareDiscoveryResult;
	private BidiMap<DiscoveredActivity, String> activityToEncodingsMap;

	//Setup methods
	public void setStage(Stage stage) {
		this.stage = stage;
	}


	@FXML
	private void initialize() {
		WebViewUtilsV3.setupWebView(directRelationsWebView);
		WebViewUtilsV3.setupWebView(amongRelationsWebView);

		//Setup for getting the SplitPane dividers working correctly at startup
		ChangeListener<Number> changeListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				splitPane1.setDividerPositions(0.1);
				splitPane2.setDividerPositions(0.5);
			}
		};
		splitPane1.widthProperty().addListener(changeListener);
		splitPane2.widthProperty().addListener(changeListener);

		//Setup for activity selection list
		StringConverter<DiscoveredActivity> activityConverter = new StringConverter<DiscoveredActivity>() {
			@Override
			public String toString(DiscoveredActivity object) {
				return object != null ? object.getActivityName() : "";
			}
			@Override
			public DiscoveredActivity fromString(String string) {
				//Could maybe use this to get the selected activity instance by activity name, but easier to use indexes
				return null;
			}
		};
		activityListView.setCellFactory(cf -> new TextFieldListCell<DiscoveredActivity>(activityConverter));
		activityListView.getSelectionModel().selectedIndexProperty().addListener((obs, oldIndex, newIndex) -> {
			if (newIndex.intValue() != -1) {
				DiscoveredActivity selectedActivity = declareDiscoveryResult.getActivities().get(newIndex.intValue());
				updateVisualization(selectedActivity);
			}
		});

		altLayoutDirectCheckBox.selectedProperty().addListener((ev) -> {
			if (activityListView.getSelectionModel().getSelectedIndex() != -1) {
				updateVisualization(declareDiscoveryResult.getActivities().get(activityListView.getSelectionModel().getSelectedIndex()));
			}
		});
		automatonDirectCheckBox.selectedProperty().addListener((ev) -> {
			if (activityListView.getSelectionModel().getSelectedIndex() != -1) {
				updateVisualization(declareDiscoveryResult.getActivities().get(activityListView.getSelectionModel().getSelectedIndex()));
			}
		});
		altLayoutAmongCheckBox.selectedProperty().addListener((ev) -> {
			if (activityListView.getSelectionModel().getSelectedIndex() != -1) {
				updateVisualization(declareDiscoveryResult.getActivities().get(activityListView.getSelectionModel().getSelectedIndex()));
			}
		});
		automatonAmongCheckBox.selectedProperty().addListener((ev) -> {
			if (activityListView.getSelectionModel().getSelectedIndex() != -1) {
				updateVisualization(declareDiscoveryResult.getActivities().get(activityListView.getSelectionModel().getSelectedIndex()));
			}
		});
		amongToggleGroup.selectedToggleProperty().addListener(ev -> {
			if (activityListView.getSelectionModel().getSelectedIndex() != -1) {
				updateVisualization(declareDiscoveryResult.getActivities().get(activityListView.getSelectionModel().getSelectedIndex()));
			}
		});
	}

	public void updateTabContents(DeclareDiscoveryResult declareDiscoveryResult) {
		this.declareDiscoveryResult = declareDiscoveryResult;
		activityListView.getItems().setAll(declareDiscoveryResult.getActivities());
		if (!activityListView.getItems().isEmpty()) {
			activityListView.getSelectionModel().select(0);
		}
	}


	private void updateVisualization(DiscoveredActivity selectedActivity) { //TODO: Need refactoring

		//Directly related constraints WebView
		List<DiscoveredActivity> filteredActivities = new ArrayList<DiscoveredActivity>();
		filteredActivities.add(selectedActivity);
		List<DiscoveredConstraint> filteredConstraints = new ArrayList<DiscoveredConstraint>();

		for (DiscoveredConstraint discoveredConstraint : declareDiscoveryResult.getConstraints()) {
			if (!filteredConstraints.contains(discoveredConstraint) && (discoveredConstraint.getActivationActivity() == selectedActivity || discoveredConstraint.getTargetActivity() == selectedActivity)) {
				filteredConstraints.add(discoveredConstraint);
			}
		}

		for (DiscoveredConstraint discoveredConstraint : filteredConstraints) {
			if(!filteredActivities.contains(discoveredConstraint.getActivationActivity())) filteredActivities.add(discoveredConstraint.getActivationActivity());
			if(!filteredActivities.contains(discoveredConstraint.getTargetActivity())) filteredActivities.add(discoveredConstraint.getTargetActivity());
		}
		for (DiscoveredActivity filteredActivity : filteredActivities) {
			if (filteredActivity.getActivityName().equals(LogUtils.ARTIF_START) || filteredActivity.getActivityName().equals(LogUtils.ARTIF_END)) {
				filteredConstraints.add(new DiscoveredConstraint(ConstraintTemplate.Exactly1, filteredActivity, null, 1));
			}
		}

		WebViewUtilsV3.updateWebView(filteredActivities, filteredConstraints, directRelationsWebView, altLayoutDirectCheckBox.isSelected(), automatonDirectCheckBox.isSelected(), activityToEncodingsMap);
		//		populateConstraintLabels(filteredConstraints);


		//Constraints among WebView
		filteredActivities = new ArrayList<DiscoveredActivity>();
		filteredConstraints = new ArrayList<DiscoveredConstraint>();
		if (amongToggleGroup.getSelectedToggle() == preceedersRadioButton) {
			for (DiscoveredConstraint discoveredConstraint : declareDiscoveryResult.getConstraints()) {
				if (discoveredConstraint.getTemplate().getReverseActivationTarget() && discoveredConstraint.getActivationActivity() == selectedActivity && !filteredActivities.contains(discoveredConstraint.getTargetActivity())) {
					filteredActivities.add(discoveredConstraint.getTargetActivity());
				} else if (!discoveredConstraint.getTemplate().getReverseActivationTarget() && discoveredConstraint.getTargetActivity() == selectedActivity && !filteredActivities.contains(discoveredConstraint.getActivationActivity())) {
					filteredActivities.add(discoveredConstraint.getActivationActivity());
				}
			}
		} else if (amongToggleGroup.getSelectedToggle() == followersRadioButton) {
			for (DiscoveredConstraint discoveredConstraint : declareDiscoveryResult.getConstraints()) {
				if (discoveredConstraint.getTemplate().getReverseActivationTarget() && discoveredConstraint.getTargetActivity() == selectedActivity && !filteredActivities.contains(discoveredConstraint.getActivationActivity())) {
					filteredActivities.add(discoveredConstraint.getActivationActivity());
				} else if (!discoveredConstraint.getTemplate().getReverseActivationTarget() && discoveredConstraint.getActivationActivity() == selectedActivity && !filteredActivities.contains(discoveredConstraint.getTargetActivity())) {
					filteredActivities.add(discoveredConstraint.getTargetActivity());
				}
			}
		}
		for (DiscoveredConstraint discoveredConstraint : declareDiscoveryResult.getConstraints()) {
			if (filteredActivities.contains(discoveredConstraint.getActivationActivity()) && filteredActivities.contains(discoveredConstraint.getTargetActivity())) {
				filteredConstraints.add(discoveredConstraint);
			}
		}
		for (DiscoveredActivity filteredActivity : filteredActivities) {
			if (filteredActivity.getActivityName().equals(LogUtils.ARTIF_START) || filteredActivity.getActivityName().equals(LogUtils.ARTIF_END)) {
				filteredConstraints.add(new DiscoveredConstraint(ConstraintTemplate.Exactly1, filteredActivity, null, 1));
			}
		}

		WebViewUtilsV3.updateWebView(filteredActivities, filteredConstraints, amongRelationsWebView, altLayoutAmongCheckBox.isSelected(), automatonAmongCheckBox.isSelected(), activityToEncodingsMap);
		//		populateConstraintLabels(filteredConstraints);

	}

	public void setActivityEncodings(BidiMap<DiscoveredActivity, String> activityToEncodingsMap) {
		this.activityToEncodingsMap = activityToEncodingsMap;
	}

}
