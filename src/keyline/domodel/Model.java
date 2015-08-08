/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyline.domodel;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author KMY
 */
public abstract class Model {

	protected StringProperty name = new SimpleStringProperty();

	public ReadOnlyStringProperty nameProperty () {
		return this.name;
	}

	//
	// rootなど、名前のみを保持するインスタンス
	//
	public static class NameOnlyModel extends Model {

		public NameOnlyModel (String nameStr) {
			this.name.set(nameStr);
		}
	}
}
