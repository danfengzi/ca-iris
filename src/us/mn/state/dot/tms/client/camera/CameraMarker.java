/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.client.camera;

import us.mn.state.dot.map.marker.AbstractMarker;

/**
 * Marker used to paint cameras.
 *
 * @author Douglas Lau
 */
public class CameraMarker extends AbstractMarker {

	/** Size (in user coordinates) to render camera marker */
	static protected final int MARKER_SIZE = 600;

	/** Create a new camera marker */
	public CameraMarker() {
		this(MARKER_SIZE);
	}

	/** Create a new camera marker */
	public CameraMarker(float size) {
		super(11);
		float tenth = size / 10;
		float quarter = size / 4;
		float third = size / 3;
		float half = size / 2;
		path.moveTo(0, third);
		path.lineTo(quarter, tenth);
		path.lineTo(third, tenth);
		path.lineTo(third, quarter);
		path.lineTo(size, quarter);
		path.lineTo(size, -quarter);
		path.lineTo(third, -quarter);
		path.lineTo(third, -tenth);
		path.lineTo(quarter, -tenth);
		path.lineTo(0, -third);
		path.closePath();
	}
}
