/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyline.view;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 *
 * @author KMY
 */
public abstract class Controller {

	public void setChildNode (Pane parentNode, Node pane) {
		parentNode.getChildren().clear();
		parentNode.getChildren().add(pane);
	}
}
