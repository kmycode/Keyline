/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyline.view;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import keyline.domodel.CharacterModel;

/**
 *
 * @author KMY
 */
public class MainWindowController extends Controller implements Initializable {

	@FXML
	SplitPane mainSplitPane;

	private Stage stage;
	private DoubleProperty width = new SimpleDoubleProperty();
	private DoubleProperty height = new SimpleDoubleProperty();

	@Override
	public void initialize (URL url, ResourceBundle rb) {
	}

	public void setMainListPane (Node node) {
		this.mainSplitPane.getItems().add(node);
	}

}
