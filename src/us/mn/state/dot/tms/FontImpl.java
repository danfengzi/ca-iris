/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tms;

import java.sql.ResultSet;
import us.mn.state.dot.sonar.server.Namespace;

/**
 * The FontImpl class defines all the attributes of a pixel font. These
 * fonts are used for VMS messages, and are downloaded to NTCIP sign
 * controllers.
 *
 * @author Douglas Lau
 */
public class FontImpl extends BaseObjectImpl implements Font {

	/** Lookup all the fonts */
	static public void lookup(final Namespace ns) throws TMSException {
		store.query("SELECT name, number, height, width, " +
			"line_spacing, char_spacing, version_id " +
			"FROM font;", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				ns.add(new FontImpl(
					row.getString(1),	// name
					row.getInt(2),		// number
					row.getInt(3),		// height
					row.getInt(4),		// width
					row.getInt(5),		// line_spacing
					row.getInt(6),		// char_spacing
					row.getInt(7)		// version_id
				));
			}
		});
	}

	/** Create a new font */
	static public Font doCreate(String name) throws TMSException {
		FontImpl font = new FontImpl(name);
		store.create(font);
		return font;
	}

	/** Get the database table name */
	public String getTable() {
		return SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new font */
	protected FontImpl(String n) {
		super(n);
	}

	/** Create a new font */
	protected FontImpl(String n, int num, int h, int w, int ls, int cs,
		int v)
	{
		this(n);
		number = num;
		height = h;
		width = w;
		lineSpacing = ls;
		charSpacing = cs;
		versionID = v;
	}

	/** Font number (both fontIndex and fontNumber NTCIP objects) */
	protected int number;

	/** Set the font number */
	public void setNumber(int n) {
		number = n;
	}

	/** Set the font number */
	public void doSetNumber(int n) throws TMSException {
		if(n == number)
			return;
		if(n < 1 || n > 8)
			throw new ChangeVetoException("Invalid font number");
		store.update(this, "number", n);
		setNumber(n);
	}

	/** Get the font number. This is used for both the fontIndex and the
	 * fontNumber NTCIP objects. */
	public int getNumber() {
		return number;
	}

	/** Font height (in pixels) */
	protected int height = 7;

	/** Set the font height (pixels) */
	public void setHeight(int h) {
		height = h;
	}

	/** Set the font height (pixels) */
	public void doSetHeight(int h) throws TMSException {
		if(h == height)
			return;
		if(h < 4 || h > 24)
			throw new ChangeVetoException("Invalid height");
//		if(characters.size() > 0)
//			throw new ChangeVetoException("Characters exist");
		store.update(this, "height", h);
		setHeight(h);
	}

	/** Get the font height (pixels) */
	public int getHeight() {
		return height;
	}

	/** Font width (in pixels) */
	protected int width = 5;

	/** Set the font width (pixels) */
	public void setWidth(int w) {
		width = w;
	}

	/** Set the font width (pixels) */
	public void doSetWidth(int w) throws TMSException {
		if(w == width)
			return;
		if(w < 0 || w > 12)
			throw new ChangeVetoException("Invalid width");
//		if(characters.size() > 0)
//			throw new ChangeVetoException("Characters exist");
		store.update(this, "width", w);
		setWidth(w);
	}

	/** Get the font width (pixels) */
	public int getWidth() {
		return width;
	}

	/** Default horizontal spacing between characters (in pixels) */
	protected int charSpacing = 1;

	/** Set the default horizontal spacing between characters (pixels) */
	public void setCharSpacing(int s) {
		charSpacing = s;
	}

	/** Set the default horizontal spacing between characters (pixels) */
	public void doSetCharSpacing(int s) throws TMSException {
		if(s == charSpacing)
			return;
		if(s < 0 || s > 9)
			throw new ChangeVetoException("Invalid spacing");
		store.update(this, "char_spacing", s);
		setCharSpacing(s);
	}

	/** Get the default horizontal spacing between characters (pixels) */
	public int getCharSpacing() {
		return charSpacing;
	}

	/** Default vetical spacing between lines (in pixels) */
	protected int lineSpacing = 0;

	/** Set the default vertical spacing between lines (pixels) */
	public void setLineSpacing(int s) {
		lineSpacing = s;
	}

	/** Set the default vertical spacing between lines (pixels) */
	public void doSetLineSpacing(int s) throws TMSException {
		if(s == lineSpacing)
			return;
		if(s < 0 || s > 9)
			throw new ChangeVetoException("Invalid spacing");
		store.update(this, "line_spacing", s);
		setLineSpacing(s);
	}

	/** Get the default vertical spacing between lines (pixels) */
	public int getLineSpacing() {
		return lineSpacing;
	}

	/** Font version ID */
	protected int versionID = 0;

	/** Set the font version ID */
	public void setVersionID(int v) {
		versionID = v;
	}

	/** Set the font version ID */
	public void doSetVersionID(int v) throws TMSException {
		if(v == versionID)
			return;
		store.update(this, "version_id", v);
		setVersionID(v);
	}

	/** Get the font version ID */
	public int getVersionID() {
		return versionID;
	}

	/** Render text onto a bitmap graphic */
/*	public void renderOn(BitmapGraphic g, int x, int y, String t) {
		for(int i = 0; i < t.length(); i++) {
			int j = t.charAt(i);
			GraphicImpl c = lookupGlyph(j);
			c.renderOn(g, x, y);
			x += c.getWidth() + charSpacing;
		}
	} */

	/** Calculate the width (in pixels) of a line of text */
/*	public int calculateWidth(String t) {
		int width = 0;
		for(int i = 0; i < t.length(); i++) {
			if(i > 0)
				width += charSpacing;
			int j = t.charAt(i);
			GraphicImpl c = lookupGlyph(j);
			width += c.getWidth();
		}
		return width;
	} */

	/** Test if the font matches a specified character height/width */
	protected boolean matches(int h, int w) {
		if(h == 0)
			return w == getWidth();
		else
			return (h == height) && (w == getWidth());
	}

	/** Test if the font matches the specified parameters */
	public boolean matches(int h, int w, int ls) {
		if(ls == 0 || ls == lineSpacing)
			return matches(h, w);
		else
			return false;
	}
}
