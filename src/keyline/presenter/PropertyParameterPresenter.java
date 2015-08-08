/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyline.presenter;

import java.io.IOException;
import keyline.domodel.Model;
import keyline.view.PropertyParameterController;
import keyline.viewmodel.PropertyParameterViewModel;

/**
 *
 * @author KMY
 */
public class PropertyParameterPresenter extends Presenter {

	private PropertyParameterController controller;
	private PropertyParameterViewModel viewModel;

	private Model model;

	public PropertyParameterPresenter () throws IOException {
		FXMLData data = this.loadFXML("PropertyParameter.fxml");
		this.controller = (PropertyParameterController) data.getController();
		this.viewModel = new PropertyParameterViewModel();
	}

}
