/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Road;

/**
 * ClientGeoLoc is a client-side GeoLoc implementation.
 *
 * @author Douglas Lau
 */
public class ClientGeoLoc implements GeoLoc {

	/** Create a client location */
	public ClientGeoLoc(Road road, short rd, float lt, float ln, double d) {
		roadway = road;
		road_dir = rd;
		lat = lt;
		lon = ln;
		distance = d;
	}

	/** Get the type name */
	public String getTypeName() {
		return "client_geo_loc";
	}

	/** Get the name */
	public String getName() {
		return "client_geo_loc";
	}

	/** Destroy the loc */
	public void destroy() {
		// nothing to do
	}

	/** Roadway */
	protected final Road roadway;

	/** Set the roadway name */
	public void setRoadway(Road f) {
		// part of GeoLoc interface
	}

	/** Get the roadway name */
	public Road getRoadway() {
		return roadway;
	}

	/** Roadway direction */
	protected final short road_dir;

	/** Set the roadway direction */
	public void setRoadDir(short d) {
		// part of GeoLoc interface
	}

	/** Get the roadway direction */
	public short getRoadDir() {
		return road_dir;
	}

	/** Set the cross-street name */
	public void setCrossStreet(Road x) {
		// part of GeoLoc interface
	}

	/** Get the cross-street name */
	public Road getCrossStreet() {
		return null;
	}

	/** Set the cross street direction */
	public void setCrossDir(short d) {
		// part of GeoLoc interface
	}

	/** Get the cross street direction */
	public short getCrossDir() {
		return 0;
	}

	/** Set the cross street modifier */
	public void setCrossMod(short m) {
		// part of GeoLoc interface
	}

	/** Get the cross street modifier */
	public short getCrossMod() {
		return 0;
	}

	/** Latitude */
	private final float lat;

	/** Set the latitude */
	public void setLat(Float lt) {
		// part of GeoLoc interface
	}

	/** Get the latitude */
	public Float getLat() {
		return lat;
	}

	/** Longitude */
	private final float lon;

	/** Set the longitude */
	public void setLon(Float ln) {
		// part of GeoLoc interface
	}

	/** Get the longitude */
	public Float getLon() {
		return lon;
	}

	/** Distance from selected point (in spherical mercator "meters") */
	private final double distance;

	/** Get distance from selected point (in spherical mercator "meters") */
	public double getDistance() {
		return distance;
	}
}
