/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyline.view;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 *
 * @author KMY
 */
public class MainWindowController implements Initializable {

	/*
		@FXML
	private Label label;

	@FXML
	private void handleButtonAction (ActionEvent event) {
		System.out.println ("You clicked me!");
		label.setText ("Hello World!");
		}
	 */
	@FXML
	private MainWindowModelListController mainModelListController;

	@FXML
	private AnchorPane characterModePane;

	private Stage stage;
	private DoubleProperty width = new SimpleDoubleProperty();
	private DoubleProperty height = new SimpleDoubleProperty();

	@Override
	public void initialize (URL url, ResourceBundle rb) {
		// TODO
	}

	public void initialize (Stage myStage) {
		this.stage = myStage;
		this.width.bind(this.stage.widthProperty());
		this.mainModelListController.setWidthProperty(this.width);
		this.height.bind(this.stage.heightProperty());

		/*
		 * 		BooleanProperty tb = new SimpleBooleanProperty();
		tb.addListener((b) -> {
			try {
				Thread.sleep(3000);
				System.out.println("おはよう");
			} catch (InterruptedException ex) {
				Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
			}
		});
		tb.set(true);
		System.out.println("こんにちは");
		 */
	}

}
