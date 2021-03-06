/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
 * Copyright (C) 2016       California Department of Transportation
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

import us.mn.state.dot.tms.client.map.LayerChange;
import us.mn.state.dot.tms.client.map.LayerState;
import us.mn.state.dot.tms.client.map.MapBean;
import us.mn.state.dot.tms.client.map.MapObject;
import us.mn.state.dot.tms.client.map.MapSearcher;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.WeatherSensorHelper;
import us.mn.state.dot.tms.client.proxy.IrisRunnable;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.geo.SphericalMercatorPosition;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import static us.mn.state.dot.tms.SystemAttrEnum.RWIS_MEASUREMENT_RADIUS;
import static us.mn.state.dot.tms.client.widget.SwingRunner.runSwing;

/**
 * HeatMapLayerState manages the rendering of the low/medium/high weather station
 * area circles that denote an easy means of determining important weather-related
 * information for the given area.
 *
 * @author Jacob Barde
 */
public class HeatMapLayerState extends LayerState {

	/** color array */
	static final private Color[] COLOR_ARRAY = new Color[] { WeatherMeasurementDataSet.LOCOLOR, WeatherMeasurementDataSet.MOCOLOR,
		WeatherMeasurementDataSet.HOCOLOR };

	/** radius of circles (meters) */
	static final private float RADIUS_METERS = RWIS_MEASUREMENT_RADIUS.getFloat();

	/** heatmap layer */
	final private HeatMapLayer heatMapLayer;

	/** manager */
	final private ProxyManager<WeatherSensor> manager;

	/** weather measurement data set */
	final private WeatherMeasurementDataSet dataSet;

	private ItemStyle current_style = null;

	/** Listener to handle the style selection changing */
	final private ActionListener style_listener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			ItemStyle s = ItemStyle.lookupStyle(e.getActionCommand());
			current_style = s;
			refreshDataSet(s);
		}
	};

	final private ProxyListener<WeatherSensor> sensor_listener = new ProxyListener<WeatherSensor>() {

		@Override
		public void proxyAdded(WeatherSensor proxy) { }

		@Override
		public void enumerationComplete() { }

		@Override
		public void proxyRemoved(WeatherSensor proxy) { }

		@Override
		public void proxyChanged(final WeatherSensor proxy, final String a) {
			runSwing(new IrisRunnable() {
				public void run() {
					customMessage = "weather sensor (heatmap layer state) proxyChanged, " +
						proxy.getTypeName() + ": " + proxy.getName() + ", attr=" + a;
					refreshDataSet(current_style);
					fireLayerChanged(LayerChange.status);
				}
			});
		}
	};

	/**
	 * Create a new Heatmap LayerState
	 *
	 * @param layer heatmap layer
	 * @param mb    mapbean
	 */
	public HeatMapLayerState(HeatMapLayer layer, MapBean mb) {
		super(layer, mb, new HeatMapTheme(layer.getManager()));
		heatMapLayer = layer;
		manager = layer.getManager();
		dataSet = new WeatherMeasurementDataSet();
		manager.getStyleSummary().addSelectionListener(style_listener);
		manager.getCache().addProxyListener(sensor_listener);
	}

	/** Dispose of the layer state */
	@Override
	public void dispose() {
		super.dispose();
		manager.getStyleSummary().removeSelectionListener(style_listener);
		manager.getCache().removeProxyListener(sensor_listener);
	}

	/**
	 * Refresh the dataset dependent for the [selected] item style
	 *
	 * @param s item style
	 */
	public void refreshDataSet(ItemStyle s) {
		if (s == null)
			return;

		dataSet.changeDataType(s);

		Iterator<WeatherSensor> wi = WeatherSensorHelper.iterator();
		while (wi.hasNext())
			dataSet.add(wi.next());
	}

	/** Paint the layer */
	@Override
	public void paint(final Graphics2D g) {
		super.paint(g);
		if (isVisible())
			paintRadii(g);
	}

	/**
	 * paint the circles on the the graphics object in order of lowest
	 * threshold to highest threshold
	 */
	private void paintRadii(final Graphics2D g) {
		Composite origComposite = g.getComposite();
		//FIXME fix compositing so later drawings disregard any overlap from previous drawing so as
		// not to affect transparency nor color.
		//AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
		//g.setComposite(ac);
		for (Color c : COLOR_ARRAY)
			refreshRadii(g, c);

		g.setComposite(origComposite);
	}

	/**
	 * paint the circles of a color type
	 *
	 * @param g graphics
	 * @param c color to paint
	 */
	private void refreshRadii(final Graphics2D g, Color c) {
		g.setColor(c);
		MapGeoLoc mloc;
		int r = (int) RADIUS_METERS;
		int x;
		int y;
		List<WeatherMeasurementSample> samples = dataSet.getList(c);
		for (WeatherMeasurementSample sample : samples) {
			WeatherSensor ws = sample.getWeatherSensor();
			mloc = getManager().findGeoLoc(ws);
			SphericalMercatorPosition pos = GeoLocHelper.getPosition(mloc.getGeoLoc());

			x = (int) pos.getX() - r;
			y = (int) pos.getY() - r;

			g.fillOval(x, y, 2 * r, 2 * r);
		}
	}

	/** Get the heatmap layer */
	public HeatMapLayer getHeatMapLayer() {

		return heatMapLayer;
	}

	/** Get the manager */
	public ProxyManager<WeatherSensor> getManager() {

		return manager;
	}

	/** Call the specified callback for each map object in the layer */
	@Override
	public MapObject forEach(MapSearcher s) {
		return manager.forEach(s);
	}

	/** Get the visibility flag */
	@Override
	public boolean isVisible() {
		Boolean v = getVisible();
		boolean rv = (v != null ? v : Boolean.FALSE) && isZoomVisible();
		return rv;
	}

	/** Is the layer visible at the current zoom level? */
	private boolean isZoomVisible() {
		int curZoom = map.getModel().getZoomLevel().ordinal();
		return manager.isVisible(curZoom);
	}
}
