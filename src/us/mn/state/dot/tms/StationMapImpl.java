/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2007  Minnesota Department of Transportation
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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;
import java.rmi.RemoteException;

/**
 * This is a mapping of Station objects, sorted by name.
 *
 * @author Douglas Lau
 */
class StationMapImpl extends AbstractListImpl implements StationMap {

	/** Location of station sample XML file */
	static protected final String SAMPLE_XML = "stat_sample.xml";

	/** Location of station XML file */
	static protected final String STATION_XML = "station.xml";

	/** TreeMap to hold all the stations */
	protected final TreeMap<String, TMSObjectImpl> map;

	/** Most recent time stamp of calculated data */
	protected transient Calendar stamp;

	/** Create a new station map */
	public StationMapImpl() throws RemoteException {
		super(false);
		map = new TreeMap<String, TMSObjectImpl>();
		stamp = Calendar.getInstance();
	}

	/** Get an iterator of the stations in the list */
	Iterator<TMSObjectImpl> iterator() {
		return map.values().iterator();
	}

	/** Append a station to the list */
	public synchronized void add(String n, StationImpl station)
		throws TMSException
	{
		if(map.containsKey(n))
			throw new ChangeVetoException("Duplicate name: " + n);
		map.put(n, station);
		Iterator<String> it = map.keySet().iterator();
		for(int i = 0; it.hasNext(); i++) {
			String search = it.next();
			if(n.equals(search)) {
				notifyAdd(i, station.toString());
				return;
			}
		}
	}

	/** Remove a station from the map */
	public synchronized void remove(String n) throws TMSException {
		StationImpl station = (StationImpl)map.get(n);
		if(station == null)
			throw new ChangeVetoException("Cannot find: " + n);
		Iterator<String> it = map.keySet().iterator();
		for(int i = 0; it.hasNext(); i++) {
			String search = it.next();
			if(n.equals(search)) {
				it.remove();
				notifyRemove(i);
				return;
			}
		}
	}

	/** Get a single element from its key */
	public synchronized Station getElement(String n) {
		return (Station)map.get(n);
	}

	/** Subscribe a listener to this list */
	public synchronized Object[] subscribe(RemoteList listener) {
		super.subscribe(listener);
		if(map.size() < 1)
			return null;
		String[] list = new String[map.size()];
		Iterator<TMSObjectImpl> it = iterator();
		for(int i = 0; it.hasNext(); i++)
			list[i] = it.next().toString();
		return list;
	}

	/** Get the station with the associated detector */
	public synchronized Station getStation(DetectorImpl det) {
		if(det == null)
			return null;
		for(TMSObjectImpl s: map.values()) {
			if(((StationImpl)s).hasDetector(det))
				return (Station)s;
		}
		return null;
	}

	/** Calculate the current data for all stations in the list */
	public synchronized void calculateData(Calendar st) {
		if(stamp.after(st)) {
			System.err.println("StationData OUT OF ORDER: " +
				stamp.getTime() + " > " + st.getTime());
			return;
		}
		stamp = st;
		for(TMSObjectImpl s: map.values())
			((StationImpl)s).calculateData();
		notifyStatus();
	}

	/** Write the station sample data out as XML */
	public void writeSampleXml() {
		XmlWriter w = new XmlWriter(SAMPLE_XML, true) {
			public void print(PrintWriter out) {
				printSampleXmlHead(out);
				printSampleXmlBody(out);
				printSampleXmlTail(out);
			}
		};
		try {
			w.write();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	/** Print the header of the station sample XML file */
	protected void printSampleXmlHead(PrintWriter out) {
		out.println("<?xml version='1.0'?>");
		out.println(
			"<!DOCTYPE traffic_sample SYSTEM 'tms_config.dtd'>");
		out.println("<traffic_sample time_stamp='" + new Date() +
			"' period='30'>");
		out.println("\t&r_nodes;");
		out.println("\t&detectors;");
	}

	/** Print the body of the station sample XML file */
	protected synchronized void printSampleXmlBody(PrintWriter out) {
		for(TMSObjectImpl s: map.values())
			((StationImpl)s).printSampleXmlElement(out);
	}

	/** Print the tail of the station sample XML file */
	protected void printSampleXmlTail(PrintWriter out) {
		out.println("</traffic_sample>");
	}

	/** Write the station data out as XML */
	public void writeStationXml() {
		XmlWriter w = new XmlWriter(STATION_XML, false) {
			public void print(PrintWriter out) {
				printStationXmlHead(out);
				printStationXmlBody(out);
				printStationXmlTail(out);
			}
		};
		try {
			w.write();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	/** Print the header of the station sample XML file */
	protected void printStationXmlHead(PrintWriter out) {
		out.println("<?xml version='1.0'?>");
		out.println("<station_data time_stamp='" + new Date() +
			"' sample_period='30'>");
	}

	/** Print the body of the station sample XML file */
	protected synchronized void printStationXmlBody(PrintWriter out) {
		for(TMSObjectImpl s: map.values())
			((StationImpl)s).printStationXmlElement(out);
	}

	/** Print the tail of the station sample XML file */
	protected void printStationXmlTail(PrintWriter out) {
		out.println("</station_data>");
	}
}
