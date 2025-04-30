package controller.tab.v3;

import data.DiscoveredActivity;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import task.DeclareDiscoveryResult;

public class TemporalTabController {

	@FXML
	private ListView<DiscoveredActivity> activityListView;
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
		activityListView.getSelectionModel().selectedIndexProperty().addListener((obs, oldIndex, newIndex) -> {
			
		});
	}

	public void updateTabContents(DeclareDiscoveryResult declareDiscoveryResult) {
		activityListView.getItems().setAll(declareDiscoveryResult.getActivities());
	}
	
	
	
	

}
