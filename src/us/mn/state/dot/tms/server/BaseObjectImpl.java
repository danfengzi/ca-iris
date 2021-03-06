/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2016  Minnesota Department of Transportation
 * Copyright (C) 2014-2015  AHMCT, University of California
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
package us.mn.state.dot.tms.server;

import java.text.NumberFormat;
import java.util.Date;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.server.Server;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.tms.TMSException;
import static us.mn.state.dot.tms.server.MainServer.FLUSH;
import us.mn.state.dot.tms.server.event.BaseEvent;

/**
 * Base object class for storable SONAR objects.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
abstract public class BaseObjectImpl implements Storable, SonarObject {

	/** SONAR namespace */
	static public ServerNamespace namespace;

	/** SQL connection to database */
	static protected SQLConnection store;

	/** Corridor manager */
	static public final CorridorManager corridors = new CorridorManager();

	/** Load all objects from the database into the SONAR Namespace */
	static void loadAll(SQLConnection s, ServerNamespace ns)
		throws TMSException
	{
		store = s;
		namespace = ns;
		SystemAttributeImpl.loadAll();
		GraphicImpl.loadAll();
		FontImpl.loadAll();
		GlyphImpl.loadAll();
		VideoMonitorImpl.loadAll();
		RoadImpl.loadAll();
		GeoLocImpl.loadAll();
		MapExtentImpl.loadAll();
		IncidentDetailImpl.loadAll();
		CommLinkImpl.loadAll();
		ModemImpl.loadAll();
		CabinetStyleImpl.loadAll();
		CabinetImpl.loadAll();
		ControllerImpl.loadAll();
		R_NodeImpl.loadAll();
		/* NOTE: must happen after r_nodes are loaded */
		corridors.createCorridors();
		AlarmImpl.loadAll();
		DetectorImpl.loadAll();
		TollZoneImpl.loadAll();
		CameraImpl.loadAll();
		CameraPresetImpl.loadAll();
		BeaconImpl.loadAll();
		WeatherSensorImpl.loadAll();
		RampMeterImpl.loadAll();
		SignMessageImpl.loadAll();
		DMSImpl.loadAll();
		SignGroupImpl.loadAll();
		DmsSignGroupImpl.loadAll();
		QuickMessageImpl.loadAll();
		SignTextImpl.loadAll();
		GateArmArrayImpl.loadAll();
		GateArmImpl.loadAll();
		TagReaderImpl.loadAll();
		LaneMarkingImpl.loadAll();
		LCSArrayImpl.loadAll();
		LCSImpl.loadAll();
		LCSIndicationImpl.loadAll();
		LaneUseMultiImpl.loadAll();
		IncidentImpl.loadAll();
		IncDescriptorImpl.loadAll();
		IncLocatorImpl.loadAll();
		IncAdviceImpl.loadAll();
		HolidayImpl.loadAll();
		DayPlanImpl.loadAll();
		PlanPhaseImpl.loadAll();
		ActionPlanImpl.loadAll();
		TimeActionImpl.loadAll();
		DmsActionImpl.loadAll();
		BeaconActionImpl.loadAll();
		LaneActionImpl.loadAll();
		MeterActionImpl.loadAll();
		WordImpl.loadAll();
		DMSImpl.updateAllStyles();
		PresetAliasImpl.loadAll();
		SiteDataImpl.loadAll();
	}

	/** Get the time as a time stamp */
	static protected Date asTimestamp(Long ts) {
		return (ts != null) ? new Date(ts) : null;
	}

	/** Get time as milliseconds since epoch */
	static protected Long stampMillis(Date ts) {
		return (ts != null) ? ts.getTime() : null;
	}

	/** Compare two (possibly-null) integers for equality */
	static protected boolean integerEquals(Integer i0, Integer i1) {
		return (i0 == null) ? i1 == null : i0.equals(i1);
	}

	/** Compare two (possibly-null) strings for equality */
	static protected boolean stringEquals(String s0, String s1) {
		return (s0 == null) ? s1 == null : s0.equals(s1);
	}

	/** Lookup a sonar object */
	static private SonarObject lookupObject(String st, String name) {
		return (namespace != null)
			? namespace.lookupObject(st, name) : null;
	}

	/** Lookup a road */
	static protected RoadImpl lookupRoad(String name) {
		SonarObject so = lookupObject(RoadImpl.SONAR_TYPE, name);
		return (so instanceof RoadImpl) ? (RoadImpl) so : null;
	}

	/** Lookup a geo location */
	static protected GeoLocImpl lookupGeoLoc(String name) {
		SonarObject so = lookupObject(GeoLocImpl.SONAR_TYPE, name);
		return (so instanceof GeoLocImpl) ? (GeoLocImpl) so : null;
	}

	/** Lookup a cabinet */
	static protected CabinetImpl lookupCabinet(String name) {
		SonarObject so = lookupObject(CabinetImpl.SONAR_TYPE, name);
		return (so instanceof CabinetImpl) ? (CabinetImpl) so : null;
	}

	/** Lookup a comm link */
	static protected CommLinkImpl lookupCommLink(String name) {
		SonarObject so = lookupObject(CommLinkImpl.SONAR_TYPE, name);
		return (so instanceof CommLinkImpl) ? (CommLinkImpl) so : null;
	}

	/** Lookup a controller */
	static protected ControllerImpl lookupController(String name) {
		SonarObject so = lookupObject(ControllerImpl.SONAR_TYPE, name);
		return (so instanceof ControllerImpl)
			? (ControllerImpl)so : null;
	}

	/** Lookup a toll zone */
	static protected TollZoneImpl lookupTollZone(String name) {
		SonarObject so = lookupObject(TollZoneImpl.SONAR_TYPE, name);
		return (so instanceof TollZoneImpl) ? (TollZoneImpl) so : null;
	}

	/** Lookup a beacon */
	static protected BeaconImpl lookupBeacon(String name) {
		SonarObject so = lookupObject(BeaconImpl.SONAR_TYPE, name);
		return (so instanceof BeaconImpl) ? (BeaconImpl) so : null;
	}

	/** Lookup a camera */
	static protected CameraImpl lookupCamera(String name) {
		SonarObject so = lookupObject(CameraImpl.SONAR_TYPE, name);
		return (so instanceof CameraImpl) ? (CameraImpl) so : null;
	}

	/** Lookup a camera preset */
	static protected CameraPresetImpl lookupPreset(String name) {
		SonarObject so = lookupObject(CameraPresetImpl.SONAR_TYPE,name);
		return (so instanceof CameraPresetImpl)
			? (CameraPresetImpl) so : null;
	}

	/** Lookup a sign group */
	static protected SignGroupImpl lookupSignGroup(String name) {
		SonarObject so = lookupObject(SignGroupImpl.SONAR_TYPE, name);
		return (so instanceof SignGroupImpl) ? (SignGroupImpl)so : null;
	}

	/** Lookup an incident detail */
	static protected IncidentDetailImpl lookupIncDetail(String name) {
		SonarObject so = lookupObject(IncidentDetailImpl.SONAR_TYPE,
			name);
		return (so instanceof IncidentDetailImpl)
		      ? (IncidentDetailImpl) so
		      : null;
	}

	/** Lookup an incident */
	static protected IncidentImpl lookupIncident(String name) {
		SonarObject so = lookupObject(IncidentImpl.SONAR_TYPE, name);
		return (so instanceof IncidentImpl) ? (IncidentImpl) so : null;
	}

	/** Get the primary key name */
	public String getKeyName() {
		return "name";
	}

	/** Get the primary key */
	public String getKey() {
		return name;
	}

	/** Base object name */
	protected final String name;

	/** Get the object name */
	public String getName() {
		return name;
	}

	/** Create a new base object */
	protected BaseObjectImpl(String n) {
		// FIXME: validate for SQL injection
		name = n;
	}

	/** Get a string representation of the object */
	@Override
	public final String toString() {
		return name;
	}

	/** Calculate a hash code */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/** Test if the object equals another */
	@Override
	public boolean equals(Object o) {
		if (o instanceof BaseObjectImpl)
			return name.equals(((BaseObjectImpl) o).name);
		else
			return false;
	}

	/** Store an object */
	public void doStore() throws TMSException {
		store.create(this);
		initTransients();
	}

	/** Destroy an object */
	public void destroy() {
		// Handled by doDestroy() method
	}

	/** Destroy an object */
	public void doDestroy() throws TMSException {
		store.destroy(this);
	}

	/** Initialize the transient fields */
	protected void initTransients() throws TMSException {
		// Override this to initialize new objects
	}

	/** Notify SONAR clients of an object created */
	public void notifyCreate() throws SonarException {
		Server s = MainServer.server;
		if(s != null)
			s.createObject(this);
		else
			namespace.storeObject(this);
	}

	/** Notify SONAR clients of an object removed */
	public void notifyRemove() {
		Server s = MainServer.server;
		if(s != null)
			s.removeObject(this);
	}

	/** Notify SONAR clients of a change to an attribute */
	protected void notifyAttribute(String aname) {
		Server s = MainServer.server;
		if(s != null)
			s.setAttribute(this, aname);
	}

	/** Format a float value */
	static String formatFloat(float value, int digits) {
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(digits);
		return nf.format(value);
	}

	/** Format a double value */
	static String formatDouble(double value) {
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(5);
		return nf.format(value);
	}

	/** Log an event */
	static public void logEvent(final BaseEvent ev) {
		FLUSH.addJob(new Job() {
			public void perform() throws TMSException {
				ev.doStore();
			}
		});
	}
}
