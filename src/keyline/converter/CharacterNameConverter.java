/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyline.converter;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import keyline.domodel.CharacterModel;

/**
 *
 * @author KMY
 */
public final class CharacterNameConverter extends Converter {

	public static void convert (StringProperty property, CharacterModel model) {
		property.set(model.lastNameProperty().get() + " " + model.firstNameProperty().
				get());
	}
}
