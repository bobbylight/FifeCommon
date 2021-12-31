/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.themes;

import org.fife.ui.Hyperlink;
import org.fife.ui.app.AppTheme;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * A base class for application themes.
 */
public abstract class AbstractAppTheme implements AppTheme {

	private final String id;
	private final String name;
	private final String lookAndFeel;
	private final Map<String, Object> extraUiDefaults;


	public AbstractAppTheme(String id, String name, String laf) {
		this.id = id;
		this.name = name;
		this.lookAndFeel = laf;
		this.extraUiDefaults = new HashMap<>();
	}


	public void addExtraUiDefault(String key, Object value) {
		extraUiDefaults.put(key, value);
	}


	@Override
	public boolean equals(Object other) {
		return other instanceof AppTheme &&
			Objects.equals(getId(), ((AppTheme)other).getId());
	}


	@Override
	public Map<String, Object> getExtraUiDefaults() {
		return new HashMap<>(extraUiDefaults);
	}


	@Override
	public Color getHyperlinkForeground() {
		return (Color)extraUiDefaults.get(Hyperlink.UI_PROPERTY_FOREGROUND);
	}


	@Override
	public String getId() {
		return id;
	}


	@Override
	public String getLookAndFeel() {
		return lookAndFeel;
	}


	@Override
	public String getName() {
		return name;
	}


	@Override
	public int hashCode() {
		return id.hashCode();
	}


	@Override
	public void installIntoUiDefaults() {
		for (Map.Entry<String, Object> entry : extraUiDefaults.entrySet()) {
			UIManager.put(entry.getKey(), entry.getValue());
		}
	}


	public void setHyperlinkForeground(Color foreground) {
		extraUiDefaults.put(Hyperlink.UI_PROPERTY_FOREGROUND, foreground);
	}


	@Override
	public String toString() {
		return "[AppTheme: id=" + id + ", name=" + name + "]";
	}
}
