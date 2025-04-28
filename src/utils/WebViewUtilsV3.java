package utils;

import java.util.List;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.lang3.StringUtils;
import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import javafx.scene.input.ScrollEvent;
import javafx.scene.web.WebView;

public class WebViewUtilsV3 {

	//Private constructor to avoid unnecessary instantiation of the class
	private WebViewUtilsV3() {
	}

	public static void setupWebView(WebView webView) {
		webView.getEngine().load((WebViewUtilsV3.class.getClassLoader().getResource("test.html")).toString());
		webView.setContextMenuEnabled(false); //Setting it in FXML causes an IllegalArgumentException

		webView.addEventFilter(ScrollEvent.SCROLL, e -> {
			if (e.isControlDown()) {
				double deltaY = e.getDeltaY();
				//Setting the value of zoom slider (instead of WebView), because then the slider also defines min and max zoom levels
				if (deltaY > 0) {
					webView.zoomProperty().setValue(webView.zoomProperty().getValue() + 0.1d);
				} else if (deltaY < 0) {
					webView.zoomProperty().setValue(webView.zoomProperty().getValue() - 0.1d);
				}
				e.consume();
			}
		});

	}

	public static void updateWebView(List<DiscoveredActivity> activities, List<DiscoveredConstraint> constraints, WebView webView, boolean alternativeLayout, boolean automaton, BidiMap<DiscoveredActivity, String> activityToEncodingsMap) {
		if (constraints != null) {
			String visualizationString;
			String script;

			if (automaton) {
				visualizationString = GraphGeneratorV3.createAutomatonVisualizationString(activities, constraints, alternativeLayout, activityToEncodingsMap);
			} else {
				visualizationString = GraphGeneratorV3.createDeclareVisualizationString(activities, constraints, alternativeLayout);
			}
			
			if (visualizationString != null) {
				script = "setModel('" + visualizationString + "')";
				System.out.println("Executing visualization script: " + StringUtils.abbreviate(script, 2000));
				webView.getEngine().executeScript(script);
			}
		}
	}
	
}
