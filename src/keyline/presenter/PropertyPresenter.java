/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyline.presenter;

import keyline.viewmodel.PropertyViewModel;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import keyline.domodel.Model;
import keyline.view.PropertyController;

/**
 *
 * @author KMY
 */
public class PropertyPresenter extends Presenter {

	private PropertyController controller;
	private PropertyViewModel viewModel;

	private Model model;

	private Presenter basePropertyPresenter;
	private PropertyRelationPresenter propertyRelationPresenter;
	private PropertyParameterPresenter propertyParameterPresenter;

	public enum ModelType {

		CHARACTER,
	}

	public PropertyPresenter () throws IOException {
		FXMLData data = this.loadFXML("Property.fxml");
		this.controller = (PropertyController) data.getController();
		this.viewModel = new PropertyViewModel();

		this.basePropertyPresenter = new CharacterPropertyPresenter();
		this.controller.setBasePropertyPane(this.basePropertyPresenter.getNode());

		this.propertyRelationPresenter = new PropertyRelationPresenter();
		this.controller.setPropertyRelationPane(this.propertyRelationPresenter.getNode());

		this.propertyParameterPresenter = new PropertyParameterPresenter();
		this.controller.setPropertyParameterPane(this.propertyParameterPresenter.getNode());
	}
}
