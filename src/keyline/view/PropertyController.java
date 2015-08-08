/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyline.view;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import keyline.view.Controller;

/**
 * FXML Controller class
 *
 * @author KMY
 */
public class PropertyController extends Controller implements Initializable {

	@FXML
	private AnchorPane basePropertyPane;

	@FXML
	private AnchorPane propertyRelationPane;

	@FXML
	private AnchorPane propertyParameterPane;

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize (URL url, ResourceBundle rb) {
		// TODO
	}

	public void setBasePropertyPane (Node pane) {
		this.setChildNode(this.basePropertyPane, pane);
	}

	public void setPropertyRelationPane (Node pane) {
		this.setChildNode(this.propertyRelationPane, pane);
	}

	public void setPropertyParameterPane (Node pane) {
		this.setChildNode(this.propertyParameterPane, pane);
	}

}
