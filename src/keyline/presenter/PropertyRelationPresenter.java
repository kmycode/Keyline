/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyline.presenter;

import java.io.IOException;
import keyline.domodel.Model;
import keyline.view.PropertyRelationController;
import keyline.viewmodel.PropertyRelationViewModel;

/**
 *
 * @author KMY
 */
public class PropertyRelationPresenter extends Presenter {

	private PropertyRelationController controller;
	private PropertyRelationViewModel viewModel;

	private Model model;

	public PropertyRelationPresenter () throws IOException {
		FXMLData data = this.loadFXML("PropertyRelation.fxml");
		this.controller = (PropertyRelationController) data.getController();
		this.viewModel = new PropertyRelationViewModel();
	}

}
