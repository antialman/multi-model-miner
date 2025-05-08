package controller.tab.v3;

import java.util.List;
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
	private SplitPane splitPane2;
	@FXML
	private Label constraintFollowersLabel;
	@FXML
	private VBox refinedFollowersRespVBox;
	@FXML
	private VBox refinedFollowersPrecVBox;
	@FXML
	private VBox refinedFollowersSuccVBox;
	@FXML
	private Label constraintPredecessorsLabel;
	@FXML
	private VBox refinedPredecessorsVBox;
	
	
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
				splitPane2.setDividerPositions(0.8);
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

		constraintFollowersLabel.setText("");
		refinedFollowersRespVBox.getChildren().clear();
		refinedFollowersPrecVBox.getChildren().clear();
		refinedFollowersSuccVBox.getChildren().clear();
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
		constraintFollowersLabel.setText(declarePostprocessingResult.getPotentialNextActivities(discoveredActivity).stream().map(DiscoveredActivity::getActivityName).collect(Collectors.joining(", ")));
		refinedFollowersRespVBox.getChildren().clear();
		for (int i = 0; i < refinedClosenessTaskResult.getFollowerRespGroups(discoveredActivity).size(); i++) {
			Set<DiscoveredActivity> followerGroup = refinedClosenessTaskResult.getFollowerRespGroups(discoveredActivity).get(i);
			if (!followerGroup.isEmpty()) {
				Label l = new Label(i+1 + ": " + followerGroup.stream().map(DiscoveredActivity::getActivityName).collect(Collectors.joining(", ")));
				refinedFollowersRespVBox.getChildren().add(l);
			}
		}
		refinedFollowersPrecVBox.getChildren().clear();
		for (int i = 0; i < refinedClosenessTaskResult.getFollowerPrecGroups(discoveredActivity).size(); i++) {
			Set<DiscoveredActivity> followerGroup = refinedClosenessTaskResult.getFollowerPrecGroups(discoveredActivity).get(i);
			if (!followerGroup.isEmpty()) {
				Label l = new Label(i+1 + ": " + followerGroup.stream().map(DiscoveredActivity::getActivityName).collect(Collectors.joining(", ")));
				refinedFollowersPrecVBox.getChildren().add(l);
			}
		}
		refinedFollowersSuccVBox.getChildren().clear();
		if (refinedClosenessTaskResult.getFollowerSuccRelations(discoveredActivity) != null) {
			for (int i = 0; i < refinedClosenessTaskResult.getFollowerSuccRelations(discoveredActivity).size(); i++) {
				List<Set<DiscoveredActivity>> succRelation = refinedClosenessTaskResult.getFollowerSuccRelations(discoveredActivity).get(i);
				if (!succRelation.isEmpty()) {
					Label l = new Label("* " + "(" + succRelation.get(0).stream().map(DiscoveredActivity::getActivityName).collect(Collectors.joining(", ")) + ") -> (" + succRelation.get(1).stream().map(DiscoveredActivity::getActivityName).collect(Collectors.joining(", ")) + ")");
					refinedFollowersSuccVBox.getChildren().add(l);
				}
			}	
		}
		
		
		constraintPredecessorsLabel.setText(declarePostprocessingResult.getPotentialPrevActivities(discoveredActivity).stream().map(DiscoveredActivity::getActivityName).collect(Collectors.joining(", ")));
	}

}
