/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyline.type;

import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author KMY
 */
public class NotifiableObjectProperty<T> extends SimpleObjectProperty<T> {

	public void fireValueChangedEvent () {
		super.fireValueChangedEvent();
	}

}
