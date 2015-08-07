/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyline.view;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import keyline.domodel.CharacterModel;

/**
 *
 * @author KMY
 */
public class ListController extends TableView<CharacterModel> {

	private TableColumn<CharacterModel, String> lastNameColumn = new TableColumn<CharacterModel, String>("姓");
	private TableColumn<CharacterModel, String> firstNameColumn = new TableColumn<CharacterModel, String>("名前");

	public ListController () {
		this.getStyleClass().add("keyline-property");

		this.lastNameColumn.setCellValueFactory(new PropertyValueFactory<CharacterModel, String>("lastName"));
		this.lastNameColumn.setPrefWidth(100);
		this.getColumns().add(this.lastNameColumn);
		this.firstNameColumn.setCellValueFactory(new PropertyValueFactory<CharacterModel, String>("firstName"));
		this.firstNameColumn.setPrefWidth(100);
		this.getColumns().add(this.firstNameColumn);
	}

	public void addModel (CharacterModel model) {
		this.getItems().add(model);
	}

}
