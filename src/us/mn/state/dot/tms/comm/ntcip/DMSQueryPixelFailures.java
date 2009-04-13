/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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

import java.io.IOException;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.SystemAttributeHelper;
import us.mn.state.dot.tms.comm.AddressedMessage;

/**
 * This operation queries the pixel failure table.
 *
 * @author Douglas Lau
 */
public class DMSQueryPixelFailures extends DMSOperation {

	/** Flag to indicate whether a pixel test should be performed */
	protected final boolean perform_test;

	/** Stuck ON bitmap */
	protected final BitmapGraphic stuck_on;

	/** Stuck OFF bitmap */
	protected final BitmapGraphic stuck_off;

	/** Number of rows in pixel failure table */
	protected final PixelFailureTableNumRows rows =
		new PixelFailureTableNumRows();

	/** Create a new DMS query pixel failures operation */
	public DMSQueryPixelFailures(DMSImpl d, boolean p) {
		super(DEVICE_DATA, d);
		perform_test = p;
		Integer w = d.getWidthPixels();
		Integer h = d.getHeightPixels();
		if(w == null)
			w = 0;
		if(h == null)
			h = 0;
		stuck_on = new BitmapGraphic(w, h);
		stuck_off = new BitmapGraphic(w, h);
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		if(perform_test)
			return new QueryTestStatus();
		else
			return new QueryRows();
	}

	/** Phase to query the status of pixel test activation */
	protected class QueryTestStatus extends Phase {

		/** Query the status of pixel test activation */
		protected Phase poll(AddressedMessage mess) throws IOException {
			PixelTestActivation test = new PixelTestActivation();
			mess.add(test);
			mess.getRequest();
			if(test.getInteger() == PixelTestActivation.NO_TEST)
				return new ActivatePixelTest();
			else {
				DMS_LOG.log(dms.getName() + ": " + test);
				return new QueryRows();
			}
		}
	}

	/** Phase to activate the pixel test */
	protected class ActivatePixelTest extends Phase {

		/** Activate the pixel test */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new PixelTestActivation());
			mess.setRequest();
			return new CheckTestCompletion();
		}
	}

	/** Phase to check for test completion */
	protected class CheckTestCompletion extends Phase {

		/** Pixel test activation */
		protected final PixelTestActivation test =
			new PixelTestActivation();

		/** Time to stop checking if the test has completed */
		protected final long expire = System.currentTimeMillis() + 
			SystemAttributeHelper.getDmsPixelTestTimeout() * 1000;

		/** Check for test completion */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(test);
			mess.getRequest();
			if(test.getInteger() == PixelTestActivation.NO_TEST)
				return new QueryRows();
			if(System.currentTimeMillis() > expire) {
				DMS_LOG.log(dms.getName() + ": pixel test " +
					"timeout expired -- giving up");
				return new QueryRows();
			} else
				return this;
		}
	}

	/** Phase to query the rows in the pixel failure table */
	protected class QueryRows extends Phase {

		/** Query the rows in pixel failure table */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(rows);
			mess.getRequest();
			DMS_LOG.log(dms.getName() + ": " + rows);
			if(rows.getInteger() > 0)
				return new QueryNextRow();
			else
				return null;
		}
	}

	/** Phase to query the next row in the pixel failure table */
	protected class QueryNextRow extends Phase {

		/** Row to query */
		protected int row = 1;

		/** Add a character to the font table */
		protected Phase poll(AddressedMessage mess) throws IOException {
			PixelFailureXLocation x_loc =
				new PixelFailureXLocation(row);
			PixelFailureYLocation y_loc =
				new PixelFailureYLocation(row);
			PixelFailureStatus status =
				new PixelFailureStatus(row);
			mess.add(x_loc);
			mess.add(y_loc);
			mess.add(status);
			try {
				mess.getRequest();
			}
			catch(SNMP.Message.NoSuchName e) {
				DMS_LOG.log(dms.getName() + ": " +
					e.getMessage());
				errorStatus = e.getMessage();
				return null;
			}
			int x = x_loc.getInteger() - 1;
			int y = y_loc.getInteger() - 1;
			if(status.isStuckOn())
				stuck_on.setPixel(x, y, 1);
			else
				stuck_off.setPixel(x, y, 1);
			row++;
			if(row > rows.getInteger())
				return this;
			else
				return null;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		if(success) {
			String[] status = new String[2];
			status[DMS.STUCK_OFF_BITMAP] =
				Base64.encode(stuck_off.getBitmap());
			status[DMS.STUCK_ON_BITMAP] =
				Base64.encode(stuck_on.getBitmap());
			dms.setPixelStatus(status);
		}
		super.cleanup();
	}
}
