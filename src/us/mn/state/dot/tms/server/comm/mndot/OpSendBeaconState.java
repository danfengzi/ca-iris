/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.mndot;

import java.io.IOException;
import us.mn.state.dot.tms.server.BeaconImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to command a 170 controller beacon
 *
 * @author Douglas Lau
 */
public class OpSendBeaconState extends OpDevice {

	/** Get the appropriate rate for the deployed state */
	static private byte getDeployedRate(boolean f) {
		if(f)
			return MeterRate.CENTRAL;
		else
			return MeterRate.FORCED_FLASH;
	}

	/** Controller memory address */
	private final int address;

	/** New "metering rate" for deploying to beacon */
	private final byte rate;

	/** Create a new send beacon state operation */
	public OpSendBeaconState(BeaconImpl b, boolean f) {
		super(PriorityLevel.COMMAND, b);
		address = Address.RAMP_METER_DATA + Address.OFF_REMOTE_RATE;
		rate = getDeployedRate(f);
	}

	/** Operation equality test */
	@Override
	public boolean equals(Object o) {
		if(o instanceof OpSendBeaconState) {
			OpSendBeaconState op = (OpSendBeaconState)o;
			return device == op.device && rate == op.rate;
		} else
			return false;
	}

	/** Create the second phase of the operation */
	protected Phase phaseTwo() {
		return new SetRate();
	}

	/** Phase to set the metering rate (which controls beacon) */
	protected class SetRate extends Phase {

		/** Write the meter rate to the controller */
		protected Phase poll(CommMessage mess) throws IOException {
			byte[] data = { rate };
			mess.add(new MemoryProperty(address, data));
			mess.storeProps();
			return null;
		}
	}
}