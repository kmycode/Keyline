/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyline.blmodel;

import javafx.scene.control.TreeItem;
import keyline.domodel.CharacterModel;
import keyline.domodel.Model;

/**
 *
 * @author KMY
 */
public class CharacterManager extends ModelManager<CharacterModel> {

	TreeItem<CharacterModel> rootItem = new TreeItem<CharacterModel>(new CharacterModel());

	public CharacterManager () {
		this.rootItem.getValue().firstNameProperty().set("登場人物");
		this.rootItemProperty().set(this.rootItem);

		// TODO: テストコード
		CharacterModel c1 = new CharacterModel();
		c1.lastNameProperty().set("朝霧");
		c1.firstNameProperty().set("かれん");
		this.addModel(c1);
		CharacterModel c2 = new CharacterModel();
		c2.lastNameProperty().set("是永");
		c2.firstNameProperty().set("長読");
		this.addModel(c2);
		CharacterModel c3 = new CharacterModel();
		c3.lastNameProperty().set("長瀬");
		c3.firstNameProperty().set("学");
		this.addModel(c3);
	}

	public void addModel (CharacterModel model) {
		this.rootItem.getChildren().add(new TreeItem<CharacterModel>(model));
	}

}
