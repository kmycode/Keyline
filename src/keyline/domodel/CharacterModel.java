/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyline.domodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author KMY
 */
public class CharacterModel {

	private StringProperty lastName = new SimpleStringProperty();
	private StringProperty firstName = new SimpleStringProperty();

	public StringProperty lastNameProperty () {
		return lastName;
	}

	public StringProperty firstNameProperty () {
		return firstName;
	}

}
