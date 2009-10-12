/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import java.util.LinkedList;

/**
 * Communication protocol enumeration.
 *
 * @author Douglas Lau
 */
public enum CommProtocol {

	/** NTCIP Class B (0) */
	NTCIP_B("NTCIP Class B"),

	/** MnDOT 170 4-bit (1) */
	MNDOT_4("MnDOT 170 (4-bit)"),

	/** MnDOT 170 5-bit (2) */
	MNDOT_5("MnDOT 170 (5-bit)"),

	/** Wavetronix SmartSensor 105 (3) */
	SS_105("SmartSensor 105"),

	/** 3M Canoga (4) */
	CANOGA("Canoga"),

	/** Vicon video matrix switcher (5) */
	VICON_SWITCHER("Vicon Switcher"),

	/** Pelco D camera control (6) */
	PELCO_D_PTZ("Pelco D PTZ"),

	/** NTCIP Class C (7) */
	NTCIP_C("NTCIP Class C"),

	/** Manchester camera control (8) */
	MANCHESTER_PTZ("Manchester PTZ"),

	/** DMS Lite (9) */
	DMSLITE("DMS Lite"),

	/** AWS (10) */
	AWS("AWS"),

	/** NTCIP Class A (11) */
	NTCIP_A("NTCIP Class A"),

	/** Pelco video matrix switcher (12) */
	PELCO_SWITCHER("Pelco Switcher"),

	/** Vicon camera control (13) */
	VICON_PTZ("Vicon PTZ"),

	/** Wavetronix SmartSensor 125 HD (14) */
	SS_125("SmartSensor 125 HD");

	/** Create a new comm protocol value */
	private CommProtocol(String d) {
		description = d;
	}

	/** Protocol description */
	public final String description;

	/** Get a comm protocol from an ordinal value */
	static public CommProtocol fromOrdinal(short o) {
		for(CommProtocol cp: CommProtocol.values()) {
			if(cp.ordinal() == o)
				return cp;
		}
		return null;
	}

	/** Get an array of comm protocol descriptions */
	static public String[] getDescriptions() {
		LinkedList<String> d = new LinkedList<String>();
		for(CommProtocol cp: CommProtocol.values())
			d.add(cp.description);
		return d.toArray(new String[0]);
	}
}
