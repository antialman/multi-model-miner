package controller.tab.v3;

import java.util.Set;
import java.util.stream.Collectors;

import data.DiscoveredActivity;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import task.DeclareDiscoveryResult;
import task.v3.DeclarePostprocessingResult;
import task.v3.RefinedClosenessTaskResult;

public class RefinedTabController {

	@FXML
	private SplitPane splitPane1;
	@FXML
	private ListView<DiscoveredActivity> activityListView;
	@FXML
	private VBox resultsVBox;
	@FXML
	private Label activityNameLabel;
	@FXML
	private Label constraintFollowersLabel;
	@FXML
	private VBox refinedFollowersHBox;
	@FXML
	private Label constraintPredecessorsLabel;
	@FXML
	private Label refinedPredecessorsLabel;
	
	
	private DeclareDiscoveryResult declareDiscoveryResult;
	private DeclarePostprocessingResult declarePostprocessingResult;
	private RefinedClosenessTaskResult refinedClosenessTaskResult;

	@FXML
	private void initialize() {

		//Setup for getting the SplitPane dividers working correctly at startup
		ChangeListener<Number> changeListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				splitPane1.setDividerPositions(0.1);
			}
		};
		splitPane1.widthProperty().addListener(changeListener);


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

		activityNameLabel.setText("");
		constraintFollowersLabel.setText("");
		refinedFollowersHBox.getChildren().clear();
	}

	public void updateTabContents(DeclareDiscoveryResult declareDiscoveryResult, DeclarePostprocessingResult declarePostprocessingResult, RefinedClosenessTaskResult refinedClosenessTaskResult) {
		this.declareDiscoveryResult = declareDiscoveryResult;
		this.declarePostprocessingResult = declarePostprocessingResult;
		this.refinedClosenessTaskResult = refinedClosenessTaskResult;
		
		activityListView.getItems().setAll(declareDiscoveryResult.getActivities());
		if (!activityListView.getItems().isEmpty()) {
			activityListView.getSelectionModel().select(0);
		}
	}
	

	private void updateVisualization(DiscoveredActivity discoveredActivity) {
		activityNameLabel.setText(discoveredActivity.getActivityName());
		constraintFollowersLabel.setText(declarePostprocessingResult.getPotentialNextActivities(discoveredActivity).stream().map(DiscoveredActivity::getActivityName).collect(Collectors.joining(", ")));

		refinedFollowersHBox.getChildren().clear();
		for (int i = 0; i < refinedClosenessTaskResult.getFollowerGroups(discoveredActivity).size(); i++) {
			Set<DiscoveredActivity> followerGroup = refinedClosenessTaskResult.getFollowerGroups(discoveredActivity).get(i);
			Label l = new Label(i+1 + ": " + followerGroup.stream().map(DiscoveredActivity::getActivityName).collect(Collectors.joining(", ")));
			refinedFollowersHBox.getChildren().add(l);
		}
	}

}
