/*
 * 02/13/2010
 *
 * Prefs.java - Base class for a simple preferences implementation.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import java.awt.Color;
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
 * <code>Prefs</code>.  The <code>Foo</code> instance could then loads its
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
 * This class currently handles fields of type:
 * <ul>
 *    <li>All primitives (int, double, float, boolean, etc.)
 *    <li>String
 *    <li>String[]
 *    <li>File
 *    <li>Color
 *    <li>KeyStroke
 * </ul>
 *  
 * @author Robert Futrell
 * @version 1.0
 */
public abstract class Prefs {


	/**
	 * Constructor.  Sets the value of all preferences to their defaults by
	 * calling {@link #setDefaults()}.
	 */
	public Prefs() {
		setDefaults();
	}


	/**
	 * Returns a string representation of a color in the format
	 * "<code>$aarrggbb</code>".
	 *
	 * @param c The color.  If this is <code>null</code>, then a string
	 *        representing black is returned.
	 * @return The string.
	 */
	private static final String getColorString(Color c) {
		String str;
		if (c==null) {
			str = "$ff000000";
		}
		else {
			// Shove argb value into a long, using it directly (or even just
			// casting it to long) make it interpreted as -1.
			long argb = c.getRGB() & 0xffffffffL;
			str = "$" + Long.toHexString(argb+0x100000000L).substring(1);
		}
		return str;
	}


	private static final boolean isPrimitiveNumberType(Class<?> type) {
		return int.class==type || long.class==type || short.class==type ||
				byte.class==type || float.class==type || double.class==type;
	}


	/**
	 * Returns whether a field has the proper modifiers to be saved.
	 *
	 * @param field The field.
	 * @return Whether the field should be saved.
	 */
	private static final boolean isSavable(Field field) {
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
		BufferedInputStream in = new BufferedInputStream(
										new FileInputStream(file));
		try {
			load(in);
		} finally {
			in.close();
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

		for (int i=0; i<fields.length; i++) {

			try {

				String name = fields[i].getName();
				String value = props.getProperty(name);

				if (value!=null) {

					Class<?> type = fields[i].getType();
					Object obj = null;

					if (int.class==type) {
						try {
							obj = Integer.valueOf(value);
						} catch (NumberFormatException nfe) {
							nfe.printStackTrace();
						}
					}

					else if (long.class==type) {
						try {
							obj = Long.valueOf(value);
						} catch (NumberFormatException nfe) {
							nfe.printStackTrace();
						}
					}

					else if (short.class==type) {
						try {
							obj = Short.valueOf(value);
						} catch (NumberFormatException nfe) {
							nfe.printStackTrace();
						}
					}

					else if (byte.class==type) {
						try {
							obj = Byte.valueOf(value);
						} catch (NumberFormatException nfe) {
							nfe.printStackTrace();
						}
					}

					else if (float.class==type) {
						try {
							obj = Float.valueOf(value);
						} catch (NumberFormatException nfe) {
							nfe.printStackTrace();
						}
					}

					else if (double.class==type) {
						try {
							obj = Double.valueOf(value);
						} catch (NumberFormatException nfe) {
							nfe.printStackTrace();
						}
					}

					else if (boolean.class==type) {
						obj = Boolean.valueOf(value);
					}

					else if (char.class==type) {
						if (value.length()>0) {
							obj = new Character(value.charAt(0));
						}
					}

					else if (String.class==type) {
						obj = value;
					}

					else if (String[].class==type) {
						String[] temp = null;
						int length = Integer.parseInt(value);
						if (length>-1) { // -1 => null array
							temp = new String[length];
							for (int j=0; j<length; j++) {
								// Property will not be defined if the String
								// should be null, so everything works out
								temp[j] = props.getProperty(name + "." + j);
							}
						}
						obj = temp;
					}

					else if (Color.class==type) {
						if (value.length()==9 && value.charAt(0)=='$') {
							long temp = Long.parseLong(value.substring(1), 16);
							int rgba = (int)(temp & 0xffffffff);
							//System.out.println("... " + temp + " > " + rgba);
							obj = new Color(rgba, true);
						}
					}

					else if (File.class==type){
						// Empty value => still use default
						if (value.length()>0) {
							obj = new File(value);
						}
					}

					else if (KeyStroke.class==type) {
						if (value.length()>0) {
							// returns null if formatted incorrectly
							obj = KeyStroke.getKeyStroke(value);
						}
					}

					else {
						throw new IOException("Unhandled field type for " +
								"field: " + name + " (" + type + ")");
					}

					// Only replace the default if a value was found
					if (obj!=null) {
						fields[i].set(this, obj);
					}

				}

			} catch (IllegalAccessException iae) { // Never happens
				throw new IOException(iae.getMessage());
			}

		}

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
		BufferedOutputStream out = new BufferedOutputStream(
											new FileOutputStream(file));
		try {
			save(out);
		} finally {
			out.close();
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

		for (int i=0; i<fields.length; i++) {
			try {

				if (!isSavable(fields[i])) {
					continue;
				}

				String name = fields[i].getName();
				Class<?> type = fields[i].getType();
				Object value = fields[i].get(this);
				String strVal = null;

				if (isPrimitiveNumberType(type) ||
						boolean.class==type ||
						char.class==type) {
					strVal = value.toString();
				}

				else if (String.class==type) {
					strVal = (String)value;
				}

				else if (String[].class==type) {
					// Store length of array as "main" property, then each
					// item as an extra property of form "name.index".
					String[] array = (String[])value;
					if (array==null) {
						strVal = "-1";
					}
					else {
						strVal = Integer.toString(array.length);
						for (int j=0; j<array.length; j++) {
							// No "name.N" property => String at that index
							// was null (can't put null values into Properties)
							if (array[j]!=null) {
								props.setProperty(name + "." + j, array[j]);
							}
						}
					}
				}

				else if (Color.class==type) {
					Color c = (Color)value;
					strVal = getColorString(c);
				}

				else if (File.class==type) {
					if (value!=null) {
						File file = (File)value;
						strVal = file.getAbsolutePath();
					}
				}

				else if (KeyStroke.class==type) {
					if (value!=null) {
						KeyStroke ks = (KeyStroke)value;
						strVal = ks.toString();
					}
				}

				else {
					throw new IOException("Unhandled field type for field: " +
							name + " (" + type + ")");
				}

				// If null value/error parsing number occurred (Properties
				// won't take null values)
				if (strVal==null) {
					strVal = "";
				}

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


}