/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyline.blmodel;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeItem;
import keyline.domodel.Model;

/**
 *
 * @author KMY
 */
public abstract class ModelManager<T extends Model> {

	private ObjectProperty<TreeItem<T>> rootItem = new SimpleObjectProperty<TreeItem<T>>();
	private BooleanProperty itemChanged = new SimpleBooleanProperty();

	public ModelManager () {
		rootItem.addListener((obj) -> {
			this.itemChange();
		});
	}

	public ObjectProperty<TreeItem<T>> rootItemProperty () {
		return this.rootItem;
	}

	protected void itemChange () {
		this.itemChanged.set(true);
	}

	public BooleanProperty itemChangedProperty () {
		return this.itemChanged;
	}
}
