package utils;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import javafx.scene.input.ScrollEvent;
import javafx.scene.web.WebView;
import model.TransitionNode;
import task.DiscoveryTaskResult;

public class WebViewUtils {

	//Private constructor to avoid unnecessary instantiation of the class
	private WebViewUtils() {
	}

	public static void setupWebView(WebView webView) {
		webView.getEngine().load((WebViewUtils.class.getClassLoader().getResource("test.html")).toString());
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


	public static void updateDeclareVisualization(DiscoveryTaskResult discoveryTaskResult, WebView declareWebView) {
		if (discoveryTaskResult != null) {
			String visualizationString;
			String script;

			visualizationString = GraphGenerator.createDeclareVisualizationString(discoveryTaskResult.getActivities(), discoveryTaskResult.getConstraints(), true, false);
			if (visualizationString != null) {
				script = "setModel('" + visualizationString + "')";
				System.out.println("Executing visualization script: " + StringUtils.abbreviate(script, 1000));
				declareWebView.getEngine().executeScript(script);
			}
		}
	}

	public static void updateSubsetsWebView(List<DiscoveredActivity> activities, List<DiscoveredConstraint> constraints, WebView webView) {
		if (constraints != null) {
			String visualizationString;
			String script;

			visualizationString = GraphGenerator.createDeclareVisualizationString(activities, constraints, true, true);
			if (visualizationString != null) {
				script = "setModel('" + visualizationString + "')";
				System.out.println("Executing visualization script: " + StringUtils.abbreviate(script, 1000));
				webView.getEngine().executeScript(script);
			}
		}

	}

	public static void updateFragmentsWebView(List<TransitionNode> fragmentMainTransitions, WebView fragmentsWebView) {
		if (fragmentMainTransitions != null && !fragmentMainTransitions.isEmpty()) {
			String visualizationString;
			String script;
			
			visualizationString = GraphGenerator.createFragmentsVisualizationString(fragmentMainTransitions);
			if (visualizationString != null) {
				script = "setModel('" + visualizationString + "')";
				System.out.println("Executing visualization script: " + StringUtils.abbreviate(script, 1000));
				fragmentsWebView.getEngine().executeScript(script);
			}
		}
		
	}
}
