/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyline.view;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import keyline.blmodel.ModelManager;
import keyline.domodel.CharacterModel;
import keyline.domodel.Model;

/**
 *
 * @author KMY
 */
public class ListController extends Controller implements Initializable {

	@FXML
	private TreeTableView<Model> mainListPane;

	@FXML
	private TreeTableColumn<Model, String> nameColumn;

	private TreeItem<Model> root;

	private ObjectProperty<TreeItem<Model>> rootItem = new SimpleObjectProperty<TreeItem<Model>>();

	public void initialize (URL url, ResourceBundle rb) {
		this.mainListPane.rootProperty().bind(this.rootItem);

		this.nameColumn.setCellValueFactory((p)
				-> new ReadOnlyStringWrapper(p.getValue().getValue().nameProperty().get()));
	}

	public ObjectProperty<TreeItem<Model>> rootItemProperty () {
		return this.rootItem;
	}

}
