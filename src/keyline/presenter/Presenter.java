/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyline.presenter;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import keyline.view.Controller;
import keyline.viewmodel.ViewModel;
import keyline.Keyline;

/**
 *
 * @author KMY
 */
public abstract class Presenter {

	private Node node;

	protected FXMLData loadFXML (String fxmlName) throws IOException {

		FXMLData data = new FXMLData();

		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlName));
		loader.setLocation(this.getClass().getResource("/keyline/view/" + fxmlName));
		data.node = loader.load();
		data.controller = (Controller) loader.getController();

		this.node = data.node;

		return data;
	}

	public Node getNode () {
		return this.node;
	}

	protected static class FXMLData {

		private Controller controller;
		private Node node;

		public Controller getController () {
			return this.controller;
		}

		public Node getNode () {
			return this.node;
		}
	}
}
