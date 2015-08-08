/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyline.presenter;

import java.io.IOException;
import keyline.domodel.CharacterModel;
import keyline.view.CharacterPropertyController;
import keyline.viewmodel.CharacterPropertyViewModel;

/**
 *
 * @author KMY
 */
public class CharacterPropertyPresenter extends Presenter {

	private CharacterPropertyController controller;
	private CharacterPropertyViewModel viewModel;

	private CharacterModel model;

	public CharacterPropertyPresenter () throws IOException {
		FXMLData data = this.loadFXML("CharacterProperty.fxml");
		this.controller = (CharacterPropertyController) data.getController();
		this.viewModel = new CharacterPropertyViewModel();
	}

}
