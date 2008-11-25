/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.ntcip;

import us.mn.state.dot.tms.DMSType;

/**
 * Ntcip DmsSignType object
 *
 * @author Douglas Lau
 */
public class DmsSignType extends DmsSignCfg implements ASN1Integer {

	/** Create a new DmsSignType object */
	public DmsSignType() {
		super(2);
	}

	/** Get the object name */
	protected String getName() {
		return "dmsSignType";
	}

	/** Sign type */
	protected int type;

	/** Set the integer value */
	public void setInteger(int value) {
		type = value;
	}

	/** Get the integer value */
	public int getInteger() {
		return type;
	}

	/** Get the object value as a String */
	public String getValue() {
		StringBuilder b = new StringBuilder();
		if((type & 0x80) != 0)
			b.append("Portable ");
		b.append(getValueEnum().description);
		return b.toString();
	}

	/** Get the object value as a DMSType */
	public DMSType getValueEnum() {
		return DMSType.fromOrdinal(type & 0x7f);
	}
}
