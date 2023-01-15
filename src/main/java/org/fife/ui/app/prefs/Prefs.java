/*
 * 02/13/2010
 *
 * Prefs.java - Base class for a simple preferences implementation.
 * Copyright (C) 2010 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.prefs;

import org.fife.ui.app.prefs.loaders.*;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.swing.KeyStroke;



/**
 * A simple wrapper for preferences for some object.  This class can be used
 * as a simpler replacement for the Java Preferences API for the following
 * reasons:
 *
 * <ol>
 *    <li>No need to call <code>putXXX()</code> methods for each individual
 *        preference when saving preferences and <code>getXXX()</code> methods
 *        while loading them; all public fields are loaded and stored
 *        appropriately automatically.</li>
 *    <li>Uses a simple properties file on all platforms.  The standard
 *        Preferences API inconveniently hides its data in the registry on
 *        Windows.</li>
 *    <li>As opposed to using <code>java.util.Properties</code> directly,
 *        using this class keeps you from having to convert each type of
 *        preference to and from String.  <code>java.awt.Color</code>, for
 *        example, is handled automatically.</li>
 * </ol>
 *
 * Subclasses should declare all fields as public.  All public instance fields
 * will be saved and loaded in properties file format automatically.  While
 * public fields are usually considered bad OO design, since this class is
 * solely used for preferences and should usually only be read and used (once)
 * by a single class, this really isn't such a big deal.<p>
 *
 * Common usage should be as follows:  say a class <code>Foo</code> needs to
 * store preferences between runs of the application.  A class could be created
 * for its preferences, say <code>FooPrefs</code>, that extends
 * <code>Prefs</code>.  The <code>Foo</code> instance could then load its
 * preferences like so:
 *
 * <pre>
 * public void loadPreferences() throws IOException {
 *    FooPrefs prefs = new FooPrefs(); // Initializes to defaults
 *    prefs.load(new File((String)System.getProperty("user.home"), ".foo.prefs"));
 *    this.count = prefs.count;
 *    this.id = prefs.id;
 *    this.background = prefs.bgColor;
 * }
 * </pre>
 *
 * and save its preferences similarly:
 *
 * <pre>
 * public void savePreferences() throws IOException {
 *    FooPrefs prefs = new FooPrefs();
 *    prefs.count = this.count;
 *    prefs.id = this.id;
 *    prefs.bgColor = this.background;
 *    prefs.save(new File((String)System.getProperty("user.home"), ".foo.prefs"));
 * }
 * </pre>
 *
 * Alternatively (and perhaps more simply), the <code>Foo</code> instance could
 * instantiate and keep the <code>FooPrefs</code> as a private member.  Its
 * getters and setters that modify its preferences could manipulate the
 * <code>FooPrefs</code>'s values directly.  Then at shutdown time, the
 * <code>Foo</code> instance would simply have to call
 * <code>prefs.save(File)</code> to save any changes.<p>
 *
 * Modification of the generated properties files by hand is discouraged unless
 * you are familiar with the details of that specific concrete
 * <code>Prefs</code> implementation.<p>
 *
 * Out of the box, this class handles fields of the following types:
 * <ul>
 *    <li>All primitives (int, double, float, boolean, etc.)
 *    <li>int[] and boolean[]
 *    <li>String
 *    <li>String[]
 *    <li>Dimension
 *    <li>Color
 *    <li>Color[]
 *    <li>ComponentOrientation
 *    <li>File
 *    <li>Font
 *    <li>KeyStroke
 *    <li>Point
 *    <li>Object (if the value is actually of type Color or Image)
 * </ul>
 *
 * Support for enums can be added via {@link EnumLoader}, and support for more
 * complex types can be added.  See the {@link #addTypeLoader(Class, TypeLoader)} method.
 *
 * @author Robert Futrell
 * @version 2.0
 * @see AppPrefs
 */
public abstract class Prefs {

	private Map<Class<?>, TypeLoader<?>> typeLoaderMap;

	/**
	 * Constructor.  Sets the value of all preferences to their defaults by
	 * calling {@link #setDefaults()}.
	 */
	public Prefs() {
		setDefaults();
		createTypeLoaderMap();
	}


	/**
	 * Adds a type loader for a custom type.  This is useful if you have
	 * custom complex types you want to store in preferences, but don't
	 * want to break it down into smaller, more primitive types in the
	 * properties file.
	 *
	 * @param clazz The type of the object.
	 * @param typeLoader The loader for the object.
	 */
	public void addTypeLoader(Class<?> clazz, TypeLoader<?> typeLoader) {
		typeLoaderMap.put(clazz, typeLoader);
	}


	/**
	 * Creates the default implementation of the type loader map for
	 * this preferences object.
	 */
	private void createTypeLoaderMap() {

		typeLoaderMap = new HashMap<>();

		try {
			addTypeLoader(boolean.class, new BooleanLoader());
			addTypeLoader(Class.forName("[Z"), new BooleanArrayLoader());
			addTypeLoader(Boolean[].class, new BooleanArrayLoader());
			addTypeLoader(byte.class, new ByteLoader());
			addTypeLoader(char.class, new CharacterLoader());
			addTypeLoader(Color.class, new ColorLoader());
			addTypeLoader(Color[].class, new ColorArrayLoader());
			addTypeLoader(ComponentOrientation.class, new ComponentOrientationLoader());
			addTypeLoader(Dimension.class, new DimensionLoader());
			addTypeLoader(double.class, new DoubleLoader());
			addTypeLoader(File.class, new FileLoader());
			addTypeLoader(float.class, new FloatLoader());
			addTypeLoader(Font.class, new FontLoader());
			addTypeLoader(int.class, new IntLoader());
			addTypeLoader(Class.forName("[I"), new IntArrayLoader());
			addTypeLoader(Integer[].class, new IntArrayLoader());
			addTypeLoader(KeyStroke.class, new KeyStrokeLoader());
			addTypeLoader(long.class, new LongLoader());
			addTypeLoader(Point.class, new PointLoader());
			addTypeLoader(short.class, new ShortLoader());
			addTypeLoader(String.class, new StringLoader());
			addTypeLoader(String[].class, new StringArrayLoader());
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace(); // Never happens
		}
	}


	/**
	 * Returns a string value to be used to save a field value.
	 *
	 * @param name The field's name.
	 * @param type The field's type.
	 * @param value The field's value.
	 * @param props The properties that field values are getting added to.
	 *         This is used for cases like arrays, where several values need
	 *         to be added to the properties.
	 * @return The string value representing {@code value}.
	 * @throws IOException If an IO error occurs.
	 * @throws IllegalAccessException Should never happen in practice.
	 */
	private String getStringValue(String name, Class<?> type, Object value,
				 Properties props) throws IOException, IllegalAccessException {

		String strVal;

		TypeLoader<?> typeLoader = typeLoaderMap.get(type);
		if (typeLoader != null) {
			strVal = typeLoader.save(name, value, props);
		}

		// For fields defined as type "Object" that are actually one of several
		// possible (supported) concrete types, we store the value in the form
		// "<concrete-class-name>,<value>".  For example, a "background" field
		// that's defined as an Object, but can be either a Color or an Image.
		else if (Object.class == type && value != null) {
			Class<?> actualType = value.getClass();
			return actualType.getName() + "," +
				getStringValue(name, actualType, value, props);
		}

		else {
			throw new IOException("Unhandled field type for field: " +
				name + " (" + type + ")");
		}

		// If null value/error parsing number occurred, store a blank value
		if (strVal == null) {
			strVal = "";
		}

		return strVal;
	}


	private static boolean isPrimitiveNumberType(Class<?> type) {
		return int.class==type || long.class==type || short.class==type ||
				byte.class==type || float.class==type || double.class==type;
	}


	/**
	 * Returns whether a field has the proper modifiers to be saved.
	 *
	 * @param field The field.
	 * @return Whether the field should be saved.
	 */
	private static boolean isSavable(Field field) {
		int mods = field.getModifiers();
		return (mods&Modifier.PUBLIC)==Modifier.PUBLIC &&
				(mods&(Modifier.TRANSIENT|Modifier.FINAL))==0;
	}


	/**
	 * Loads this preferences class from a file.
	 *
	 * @param file The file.
	 * @throws IOException If an IO error occurs.
	 * @see #load(InputStream)
	 * @see #load(Properties)
	 */
	public void load(File file) throws IOException {
		try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
			load(in);
		}
	}


	/**
	 * Loads this preferences class from an input stream.
	 *
	 * @param in The input stream.  It is the caller's responsibility to close
	 *        this stream.
	 * @throws IOException If an IO error occurs.
	 * @see #load(File)
	 * @see #load(Properties)
	 */
	public void load(InputStream in) throws IOException {
		Properties props = new Properties();
		props.load(in);
		load(props);
	}


	/**
	 * Loads this preferences class from a properties object.  This is useful
	 * if you have multiple preferences objects stored in a single instance
	 * of <code>Properties</code>.
	 *
	 * @param props The properties to load from.
	 * @throws IOException If an IO error occurs.
	 * @see #load(File)
	 * @see #load(InputStream)
	 */
	public void load(Properties props) throws IOException {

		Class<?> clazz = getClass();
		Field[] fields = clazz.getFields();

		for (Field field : fields) {

			try {

				String name = field.getName();
				String value = props.getProperty(name);

				// Empty values are actually nulls
				if (value != null && !value.isEmpty()) {

					Class<?> type = field.getType();
					Object obj = loadPropertyImpl(type, name, value, props);

					// Only replace the default if a value was found
					if (obj != null) {
						obj = possiblyMakePrimitiveArray(type, obj);
						field.set(this, obj);
					}
				}

			} catch (IllegalAccessException iae) { // Never happens
				throw new IOException(iae.getMessage(), iae);
			}

		}

	}


	/**
	 * Parses a value of a given type out of a string representing
	 * the value.
	 *
	 * @param type The type of data represented by the string.
	 * @param name The name of the field whose value is represented.
	 * @param value The actual value, as a string.
	 * @param props The properties that field values are getting loaded from.
	 *         This is used for cases like arrays, where each value in
	 *         the array was added to the properties instance as a single
	 *         value.
	 * @return The actual value for the field.
	 * @throws IOException If an IO error occurs.
	 */
	private Object loadPropertyImpl(Class<?> type, String name,
				   String value, Properties props) throws IOException {

		Object obj = null;

		// This short-circuit is needed since this method is recursively
		// called for fields of type Object
		if (value == null) {
			return null;
		}

		TypeLoader<?> typeLoader = typeLoaderMap.get(type);
		if (typeLoader != null) {
			obj = typeLoader.load(name, value, props);
		}

		// For fields defined as type "Object" that are actually one of several
		// possible (supported) concrete types, we store the value in the form
		// "<concrete-class-name>,<value>".  For example, a "background" field
		// that's defined as an Object, but can be either a Color or an Image.
		else if (Object.class == type) {
			int comma = value.indexOf(',');
			if (comma > -1) {
				String className = value.substring(0, comma);
				try {
					Class<?> actualType = Class.forName(className);
					return loadPropertyImpl(actualType, name,
						value.substring(comma + 1), props);
				} catch (ClassNotFoundException cnfe) {
					// Just fall through and return null
					cnfe.printStackTrace();
				}
			}
		}

		else {
			throw new IOException("Unhandled field type for " +
				"field: " + name + " (" + type + ")");
		}

		return obj;
	}


	/**
	 * Since {@code TypeLoader} instances are genericized, when they return
	 * arrays of primitives they are always boxed.  This method checks
	 * whether the target field is actually a primitive array (as opposed to a
	 * boxed array), and if it is, converts the boxed array to a primitive one.
	 *
	 * @param type The type of the field whose value will be set.
	 * @param value The value the field will be set to.
	 * @return The possibly unboxed version of the array.  If {@code type} is
	 *          not a primitve array, this is {@code value}, unchanged.
	 */
	private static Object possiblyMakePrimitiveArray(Class<?> type, Object value) {

		if (type.isArray()) {
			switch (type.getName()) {
				case "[Z" -> value = toPrimitiveBooleanArray(value);
				case "[I" -> value = toPrimitiveIntArray(value);
			}
		}

		return value;
	}


	/**
	 * Saves these preferences to a file.
	 *
	 * @param file The file to save to.
	 * @throws IOException If an IO error occurs.
	 * @see #save(Properties)
	 * @see #save(OutputStream)
	 */
	public void save(File file) throws IOException {
		try (BufferedOutputStream out = new BufferedOutputStream(
			new FileOutputStream(file))) {
			save(out);
		}
	}


	/**
	 * Saves these preferences to a properties object.  Useful if you want to
	 * save multiple instances of <code>Prefs</code> into a single
	 * <code>Properties</code>.
	 *
	 * @param props The properties to save to.
	 * @throws IOException If an IO error occurs.
	 * @see #save(File)
	 * @see #save(OutputStream)
	 */
	public void save(Properties props) throws IOException {

		Class<?> clazz = getClass();
		Field[] fields = clazz.getFields();

		for (Field field : fields) {
			try {

				if (!isSavable(field)) {
					continue;
				}

				String name = field.getName();
				Class<?> type = field.getType();
				Object value = field.get(this);
				String strVal = getStringValue(name, type, value, props);
				props.setProperty(name, strVal);

			} catch (IllegalAccessException iae) { // Never happens
				iae.printStackTrace();
				throw new IOException(iae.getMessage());
			}
		}

	}


	/**
	 * Saves these preferences to an output stream.  The stream will still be
	 * open after this call is made.
	 *
	 * @param out The stream to write to.
	 * @throws IOException If an IO error occurs.
	 * @see #save(File)
	 * @see #save(Properties)
	 */
	public void save(OutputStream out) throws IOException {
		Class<?> clazz = getClass();
		Properties props = new Properties();
		save(props);
		String header = "Preferences for the " + clazz.getName() + " class";
		props.store(out, header);
	}


	/**
	 * Sets all fields in this class to their default values.
	 */
	public abstract void setDefaults();


	private static boolean[] toPrimitiveBooleanArray(Object boxedBooleanArray) {
		Boolean[] boxed = (Boolean[])boxedBooleanArray;
		boolean[] unboxed = new boolean[boxed.length];
		for (int i = 0; i < boxed.length; i++) {
			unboxed[i] = boxed[i] != null ? boxed[i] : false;
		}
		return unboxed;
	}


	private static int[] toPrimitiveIntArray(Object boxedIntArray) {
		Integer[] boxed = (Integer[])boxedIntArray;
		int[] unboxed = new int[boxed.length];
		for (int i = 0; i < boxed.length; i++) {
			unboxed[i] = boxed[i] != null ? boxed[i] : 0;
		}
		return unboxed;
	}


}
