package utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public final class AlertUtils {
	// private constructor to avoid unnecessary instantiation of the class
    private AlertUtils() {
    }
    
    public static void showError(String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setContentText(message);
		alert.showAndWait();
	}
    
    public static void showWarning(String message) {
    	Alert alert = new Alert(AlertType.WARNING);
    	alert.setContentText(message);
    	alert.showAndWait();
    }

	public static Optional<ButtonType> showWarningQuestion(String message) {
		Alert alert = new Alert(AlertType.WARNING, message, ButtonType.OK, ButtonType.CANCEL);
		return alert.showAndWait();
	}
	
    public static void showSuccess(String message) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setContentText(message);
		alert.showAndWait();
	}

}
