/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
 * Copyright (C) 2016       Southwest Research Institute
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
package us.mn.state.dot.tms.client.weather.heatmap;

import us.mn.state.dot.geokit.SphericalMercatorPosition;
import us.mn.state.dot.geokit.ZoomLevel;
import us.mn.state.dot.map.LayerChange;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.map.MapSearcher;
import us.mn.state.dot.map.Theme;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.WeatherSensorHelper;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;
import us.mn.state.dot.tms.client.proxy.ProxyManager;

import java.awt.Graphics2D;
import java.util.Iterator;

/**
 * HeatmapLayerState is for...
 *
 * @author Jacob Barde
 */
public class HeatmapLayerState extends LayerState {
	private static final int DEG_KELVIN = 273;

	private final HeatmapLayer heatmapLayer;

	private final ProxyManager<WeatherSensor> manager;

	/**
	 * Create a new Heatmap Layer
	 * @param layer
	 * @param mb
	 */
	public HeatmapLayerState(HeatmapLayer layer, MapBean mb) {
		super(layer, mb);
		heatmapLayer = layer;
		manager = layer.getManager();
	}
	/**
	 * Create a new Heatmap Layer
	 * @param layer
	 * @param mb
	 * @param theme
	 */
	public HeatmapLayerState(HeatmapLayer layer, MapBean mb, Theme theme) {
		super(layer, mb, theme);
		heatmapLayer = layer;
		manager = layer.getManager();
	}

	/**
	 * Paint the layer
	 *
	 * @param g
	 */
	@Override
	public void paint(final Graphics2D g) {
		super.paint(g);
		if(isVisible()) {
			paintRadii(g);
		}
	}

	private void paintRadii(final Graphics2D g) {
		Iterator<WeatherSensor> wi = WeatherSensorHelper.iterator();
		MapGeoLoc mloc;
		ZoomLevel zoomLevel = map.getModel().getZoomLevel();
		int x = 0;
		int y = 0;
		ItemStyle mtype = manager.getStyleSummary().getStyle();
		while(wi.hasNext()) {
			WeatherSensor ws = wi.next();
			mloc = getManager().findGeoLoc(ws);
			SphericalMercatorPosition pos = GeoLocHelper.getPosition(mloc.getGeoLoc());
			x = (int)zoomLevel.getPixelX(pos.getX());
			y = (int)zoomLevel.getPixelY(pos.getY());

			Integer measurement = getMeasurement(ws, mtype);
		}
	}

	private static Integer getMeasurement(WeatherSensor ws, ItemStyle mtype) {

		// figure the data is bad after an hour
//		if((TimeSteward.currentTimeMillis() - ws.getObsTime()) > 3600000)
//			return null;

		// all values return a minimum of 0. so temperature is returned
		// as Kelvin
		switch (mtype) {
		case AIR_TEMP:
			return ws.getAirTemp();
		case WIND_SPEED:
			return ws.getWindSpeed();
		case PRECIPITATION:
			return ws.getPrecipRate();
		case VISIBILITY:
			return ws.getPrecipRate();
		}

		return null;
	}

	public HeatmapLayer getHeatmapLayer() {

		return heatmapLayer;
	}

	public ProxyManager<WeatherSensor> getManager() {

		return manager;
	}

	@Override
	public MapObject forEach(MapSearcher s) {
		return manager.forEach(s, getScale());
	}

	/** Flag to indicate the tab is selected */
	private boolean tab_selected = false;

	/** Set the tab selected flag */
	public void setTabSelected(boolean ts) {
		tab_selected = ts;
		if(getVisible() == null)
			fireLayerChanged(LayerChange.visibility);
	}

	/** Get the visibility flag */
	@Override
	public boolean isVisible() {
		Boolean v = getVisible();
		//boolean rv = (v != null) ? v : tab_selected || isZoomVisible();
		//boolean rv = (tab_selected || (v==null?Boolean.FALSE : v)) && isZoomVisible();
		boolean rv = (v != null ? v : tab_selected) && isZoomVisible();
		return rv;
	}

	/** Is the layer visible at the current zoom level? */
	private boolean isZoomVisible() {
		int curZoom = map.getModel().getZoomLevel().ordinal();
		return manager.isVisible(curZoom) /*&& curZoom <= 12*/;
	}
}
