/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyline.domodel;

import java.util.EventListener;
import java.util.function.Consumer;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import keyline.converter.CharacterNameConverter;

/**
 *
 * @author KMY
 */
public class CharacterModel extends Model {

	private StringProperty lastName = new SimpleStringProperty("");
	private StringProperty firstName = new SimpleStringProperty("");

	public CharacterModel () {
		this.lastName.addListener((str) -> {
			CharacterNameConverter.convert(this.name, this);
		});
		this.firstName.addListener((str) -> {
			CharacterNameConverter.convert(this.name, this);
		});
	}

	public StringProperty lastNameProperty () {
		return lastName;
	}

	public StringProperty firstNameProperty () {
		return firstName;
	}

	public static Model getRootModel () {
		return new NameOnlyModel("登場人物");
	}

}
