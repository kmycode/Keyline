/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyline.viewmodel;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeItem;
import keyline.blmodel.ModelManager;
import keyline.domodel.Model;
import keyline.type.NotifiableObjectProperty;

/**
 *
 * @author KMY
 */
public class ListViewModel extends ViewModel {

	private ObjectProperty<ModelManager> modelManager = new SimpleObjectProperty<ModelManager>();
	private ObjectProperty<TreeItem<Model>> rootItem = new SimpleObjectProperty<TreeItem<Model>>();

	public ListViewModel () {
		this.modelManager.addListener((obj) -> {
			this.rootItem.bind(this.modelManager.get().rootItemProperty());
		});
	}

	public ObjectProperty<ModelManager> modelManagerProperty () {
		return this.modelManager;
	}

	public ObjectProperty<TreeItem<Model>> rootItemProperty () {
		return this.rootItem;
	}

}
