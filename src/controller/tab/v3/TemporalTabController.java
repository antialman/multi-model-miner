package controller.tab.v3;

import data.DiscoveredActivity;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import task.DeclareDiscoveryResult;

public class TemporalTabController {

	@FXML
	private ListView<DiscoveredActivity> activityListView;

	private Stage stage;

	//Setup methods
	public void setStage(Stage stage) {
		this.stage = stage;
	}


	@FXML
	private void initialize() {
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
	}


	public void updateTabContents(DeclareDiscoveryResult declareDiscoveryResult) {
		activityListView.getItems().setAll(declareDiscoveryResult.getActivities());
		
	}

}
