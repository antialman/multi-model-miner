package utils;

import org.apache.commons.lang3.StringUtils;

import javafx.concurrent.Worker;
import javafx.scene.input.ScrollEvent;
import javafx.scene.web.WebView;
import task.DiscoveryTaskResult;

public class WebViewUtils {

	//Private constructor to avoid unnecessary instantiation of the class
	private WebViewUtils() {
	}

	public static void setupWebView(WebView webView, String initialWebViewScript) {
		webView.getEngine().load((WebViewUtils.class.getClassLoader().getResource("test.html")).toString());
		webView.setContextMenuEnabled(false); //Setting it in FXML causes an IllegalArgumentException
		
		webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
			if(newValue == Worker.State.SUCCEEDED && initialWebViewScript != null) {
				webView.getEngine().executeScript(initialWebViewScript);
			}
		});
		
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
	
	
	public static void updateDeclareVisualization(DiscoveryTaskResult discoveryTaskResult, WebView declareWebView, String initialDeclareWebViewScript) {
		if (discoveryTaskResult != null) {
			String visualizationString;
			String script;
			
			visualizationString = GraphGenerator.createDeclareVisualizationString(discoveryTaskResult.getActivities(), discoveryTaskResult.getConstraints(), true, false);
			if (visualizationString != null) {
				script = "setModel('" + visualizationString + "')";
				if (declareWebView.getEngine().getLoadWorker().stateProperty().get() == Worker.State.SUCCEEDED) {
					System.out.println("Executing visualization script: " + StringUtils.abbreviate(script, 1000));
					declareWebView.getEngine().executeScript(script);
				} else {
					initialDeclareWebViewScript = script;
				}
			}
		} else {
			//Reloading the page in case a previous visualization script is still executing
			//TODO: Should instead track if a visualization script is still executing and stop it (if it is possible)
			initialDeclareWebViewScript = null; //Has to be set to null because it will otherwise be executed after reload
			declareWebView.getEngine().reload();
		}
	}
}
