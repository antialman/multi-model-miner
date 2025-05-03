package controller.tab.v3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections15.BidiMap;
import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
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
import javafx.util.StringConverter;
import task.DeclareDiscoveryResult;
import task.v3.DeclarePostprocessingResult;
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
	private SplitPane splitPane3;
	@FXML
	private CheckBox altLayoutDirectCheckBox;
	@FXML
	private CheckBox automatonDirectCheckBox;
	@FXML
	private WebView directRelationsWebView;
	@FXML
	private ListView<String> directConstraintsListView;
	@FXML
	private SplitPane splitPane4;
	@FXML
	private RadioButton followersAmongRadioButton;
	@FXML
	private ToggleGroup amongToggleGroup;
	@FXML
	private RadioButton precedersAmongRadioButton;
	@FXML
	private RadioButton followersOfRadioButton;
	@FXML
	private RadioButton precedersOfRadioButton;
	@FXML
	private CheckBox altLayoutAmongCheckBox;
	@FXML
	private CheckBox automatonAmongCheckBox;
	@FXML
	private WebView amongRelationsWebView;
	@FXML
	private ListView<String> amongConstraintsListView;
	@FXML
	private Label closestPrecedersLabel;
	@FXML
	private Label closestFollowersLabel;

	private BidiMap<DiscoveredActivity, String> activityToEncodingsMap;
	private DeclareDiscoveryResult declareDiscoveryResult;
	private DeclarePostprocessingResult declarePostprocessingResult;

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
				splitPane3.setDividerPositions(0.85);
				splitPane4.setDividerPositions(0.85);
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
				updateVisualization(declareDiscoveryResult.getActivities().get(newIndex.intValue()));
			}
		});

		InvalidationListener visSettingsListener = new InvalidationListener() {
			@Override
			public void invalidated(Observable observable) {
				if (activityListView.getSelectionModel().getSelectedIndex() != -1) {
					updateVisualization(declareDiscoveryResult.getActivities().get(activityListView.getSelectionModel().getSelectedIndex()));
				}
			}
		};

		altLayoutDirectCheckBox.selectedProperty().addListener(visSettingsListener);
		automatonDirectCheckBox.selectedProperty().addListener(visSettingsListener);
		altLayoutAmongCheckBox.selectedProperty().addListener(visSettingsListener);
		automatonAmongCheckBox.selectedProperty().addListener(visSettingsListener);
		amongToggleGroup.selectedToggleProperty().addListener(visSettingsListener);
	}

	public void updateTabContents(DeclareDiscoveryResult declareDiscoveryResult, DeclarePostprocessingResult declarePostprocessingResult) {
		this.declareDiscoveryResult = declareDiscoveryResult;
		this.declarePostprocessingResult = declarePostprocessingResult;
		activityListView.getItems().setAll(declareDiscoveryResult.getActivities());
		if (!activityListView.getItems().isEmpty()) {
			activityListView.getSelectionModel().select(0);
		}
	}


	private void updateVisualization(DiscoveredActivity selectedActivity) {
		//Directly related WebView
		List<DiscoveredActivity> directlyRelatedActivities = declarePostprocessingResult.getDirectlyRelatedActivities(selectedActivity);
		directlyRelatedActivities.add(selectedActivity);
		List<DiscoveredConstraint> directlyRelatedConstraints = declarePostprocessingResult.getDirectlyRelatedConstraints(selectedActivity);
		processArtificialStartEnd(directlyRelatedActivities, directlyRelatedConstraints);

		WebViewUtilsV3.updateWebView(directlyRelatedActivities, directlyRelatedConstraints, directRelationsWebView, altLayoutDirectCheckBox.isSelected(), automatonDirectCheckBox.isSelected(), activityToEncodingsMap);
		populateConstraintLists(directlyRelatedConstraints, directConstraintsListView);

		//Among WebView
		List<DiscoveredActivity> amongActivities = new ArrayList<DiscoveredActivity>();
		List<DiscoveredConstraint> amongConstraints = new ArrayList<DiscoveredConstraint>();
		if (amongToggleGroup.getSelectedToggle() == followersAmongRadioButton) {
			amongActivities.addAll(declarePostprocessingResult.getAllFollowerActivities(selectedActivity));
			amongConstraints.addAll(declarePostprocessingResult.getConstraintsAmongFollowers(selectedActivity));
		} else if (amongToggleGroup.getSelectedToggle() == precedersAmongRadioButton) {
			amongActivities.addAll(declarePostprocessingResult.getAllPrecederActivities(selectedActivity));
			amongConstraints.addAll(declarePostprocessingResult.getConstraintsAmongPreceders(selectedActivity));
		} else { //Visualizing all constraints that are directly related to the potentially closest followers/preceders
			Set<DiscoveredActivity> closestActivities = new HashSet<DiscoveredActivity>();
			if (amongToggleGroup.getSelectedToggle() == followersOfRadioButton) {
				closestActivities.addAll(declarePostprocessingResult.getPotentialClosestFollowers(selectedActivity));
			} else if (amongToggleGroup.getSelectedToggle() == precedersOfRadioButton) {
				closestActivities.addAll(declarePostprocessingResult.getPotentialClosestPreceders(selectedActivity));
			}
			Set<DiscoveredActivity> vizActivities = new LinkedHashSet<DiscoveredActivity>();
			Set<DiscoveredConstraint> vizConstraints = new LinkedHashSet<DiscoveredConstraint>();	
			for (DiscoveredActivity activity : closestActivities) {
				vizActivities.add(activity);
				for (DiscoveredConstraint constraint : declarePostprocessingResult.getDirectlyRelatedConstraints(activity)) {
					vizActivities.add(constraint.getActivationActivity());
					vizActivities.add(constraint.getTargetActivity());
					vizConstraints.add(constraint);
				}
			}
			amongActivities.addAll(vizActivities);
			amongConstraints.addAll(vizConstraints);
		}

		processArtificialStartEnd(amongActivities, amongConstraints);
		WebViewUtilsV3.updateWebView(amongActivities, amongConstraints, amongRelationsWebView, altLayoutAmongCheckBox.isSelected(), automatonAmongCheckBox.isSelected(), activityToEncodingsMap);
		populateConstraintLists(amongConstraints, amongConstraintsListView);

		//Labels for followers/preceders of this activity
		closestFollowersLabel.setText(declarePostprocessingResult.getPotentialClosestFollowers(selectedActivity).stream().map(DiscoveredActivity::getActivityName).collect(Collectors.joining(", ")));
		closestPrecedersLabel.setText(declarePostprocessingResult.getPotentialClosestPreceders(selectedActivity).stream().map(DiscoveredActivity::getActivityName).collect(Collectors.joining(", ")));
	}

	private void processArtificialStartEnd(List<DiscoveredActivity> discoveredActivities, List<DiscoveredConstraint> discoveredConstraints) {
		for (DiscoveredActivity discoveredActivity : discoveredActivities) {
			if (discoveredActivity.getActivityName().equals(LogUtils.ARTIF_START) || discoveredActivity.getActivityName().equals(LogUtils.ARTIF_END)) {
				discoveredConstraints.add(new DiscoveredConstraint(ConstraintTemplate.Exactly1, discoveredActivity, null, 1));
			}
		}
	}

	private void populateConstraintLists(List<DiscoveredConstraint> filteredConstraints, ListView<String> listView) {
		listView.getItems().clear();
		for (DiscoveredConstraint constraint : filteredConstraints) {
			listView.getItems().add(constraint.toString());
		}
	}

	public void setActivityEncodings(BidiMap<DiscoveredActivity, String> activityToEncodingsMap) {
		this.activityToEncodingsMap = activityToEncodingsMap;
	}

}
