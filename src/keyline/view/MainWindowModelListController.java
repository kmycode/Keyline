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
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import keyline.model.CharacterModel;
import keyline.modeltableview.CharacterTableView;

/**
 * FXML Controller class
 *
 * @author KMY
 */
public class MainWindowModelListController implements Initializable {

	@FXML
	SplitPane mainSplitPane;

	private DoubleProperty width = new SimpleDoubleProperty();

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize (URL url, ResourceBundle rb) {
		// TODO
		/*
		 		this.characterTableView.itemsProperty().addListener((newobj) -> {
			this.characterTableView.setPrefHeight((this.characterTableView.getItems().size() + 1) * 36 + 2);
		});
		 */
		CharacterModel c1 = new CharacterModel();
		CharacterModel c2 = new CharacterModel();
		CharacterModel c3 = new CharacterModel();
		c1.lastNameProperty().set("是永");
		c1.firstNameProperty().set("長読");
		c2.lastNameProperty().set("朝霧");
		c2.firstNameProperty().set("かれん");
		c3.lastNameProperty().set("木之本");
		c3.firstNameProperty().set("梓");
		ObservableList<CharacterModel> characterList = FXCollections.observableArrayList(
				c1,
				c2,
				c3
		);

		CharacterTableView characterTableView = new CharacterTableView();
		characterTableView.addModel(c1);
		characterTableView.addModel(c2);
		characterTableView.addModel(c3);
		this.mainSplitPane.getItems().add(characterTableView);

		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("CharacterProperty.fxml"));
		Parent root;
		try {
			root = loader.load();
			this.mainSplitPane.getItems().add(root);
		} catch (IOException ex) {
			Logger.getLogger(MainWindowModelListController.class.getName()).log(Level.SEVERE, null, ex);
		}

		/*
		 		characterLastNameColumn.setCellValueFactory(new PropertyValueFactory<CharacterModel, String>("lastName"));
		characterFirstNameColumn.setCellValueFactory(new PropertyValueFactory<CharacterModel, String>("firstName"));
		this.characterTableView.setItems(characterList);
		 */
	}

	public void setWidthProperty (DoubleProperty wp) {
		this.width.bind(wp);
		this.width.addListener((x) -> this.redraw(((DoubleProperty) x).get()));
	}

	public void redraw (double width) {
	}

}
