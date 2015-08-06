/*
 * Use components and libraries:
 *     https://github.com/Haixing-Hu/javafx-widgets and what it depends
 */
package keyline;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import keyline.view.MainWindowController;

/**
 *
 * @author KMY
 */
public class Keyline extends Application {

	@Override
	public void start (Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("view/MainWindow.fxml"));
		Parent root = loader.load();
		//Parent root = FXMLLoader.load (getClass ().getResource ("view/MainWindow.fxml"));

		Scene scene = new Scene(root);
		MainWindowController controller = (MainWindowController) loader.getController();

		stage.setScene (scene);
		stage.show();

		controller.initialize(stage);
		//stage.widthProperty().addListener((x) -> controller.resize(((ReadOnlyDoubleProperty) x).get(), 400));
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main (String[] args) {
		launch (args);
	}

}
