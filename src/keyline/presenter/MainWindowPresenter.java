/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyline.presenter;

import java.io.IOException;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import keyline.blmodel.CharacterManager;
import keyline.blmodel.ModelManager;
import keyline.domodel.CharacterModel;
import keyline.view.MainWindowController;
import keyline.viewmodel.MainWindowViewModel;

/**
 *
 * @author KMY
 */
public class MainWindowPresenter extends Presenter implements IWindowable {

	private MainWindowController controller;
	private MainWindowViewModel viewModel;
	private Node node;

	private ListPresenter mainListPresenter;
	private PropertyPresenter secondPropertyPresenter;

	private ModelManager modelManager;

	public MainWindowPresenter () throws IOException {
		FXMLData data = this.loadFXML("MainWindow.fxml");
		this.controller = (MainWindowController) data.getController();
		this.viewModel = new MainWindowViewModel();

		this.mainListPresenter = new ListPresenter();
		this.controller.setMainListPane(this.mainListPresenter.getNode());

		this.secondPropertyPresenter = new PropertyPresenter();
		this.controller.setMainListPane(this.secondPropertyPresenter.getNode());

		this.modelManager = new CharacterManager();
		this.mainListPresenter.setModelManager(this.modelManager);
	}

	@Override
	public void setStage (Stage stage) {
		stage.setScene(new Scene((Parent) this.getNode()));
	}

}
