/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyline.ui;

import com.sun.deploy.security.AbstractBrowserAuthenticator;
import keyline.ui.skin.PropertyEditSkin;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.Collection;
import java.util.HashMap;
import javafx.scene.control.Tab;

/**
 *
 * @author KMY
 */
public class PropertyEdit extends Control {

	public final static String DEFAULT_STYLE_CLASS = "";

	public ObservableList<Tab> tabs;

	public PropertyEdit () {
		tabs = FXCollections.observableArrayList();
	}

	@Override
	protected Skin<?> createDefaultSkin () {
		return new PropertyEditSkin(this);
	}

	public ObservableList<Tab> getTabs () {
		return tabs;
	}
}
