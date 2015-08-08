/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyline.presenter;

import java.io.IOException;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import keyline.blmodel.ModelManager;
import keyline.view.ListController;
import keyline.viewmodel.ListViewModel;

/**
 *
 * @author KMY
 */
public class ListPresenter extends Presenter {

	private ListController controller;
	private ListViewModel viewModel;

	public ListPresenter () throws IOException {
		FXMLData data = this.loadFXML("List.fxml");
		this.controller = (ListController) data.getController();
		this.viewModel = new ListViewModel();

		this.controller.rootItemProperty().bind(this.viewModel.rootItemProperty());
	}

	public ListPresenter (ModelManager modelManager) throws IOException {
		this();
		this.setModelManager(modelManager);
	}

	public void setModelManager (ModelManager modelManager) {
		this.viewModel.modelManagerProperty().set(modelManager);
	}

}
