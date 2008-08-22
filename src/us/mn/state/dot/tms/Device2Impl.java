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
package us.mn.state.dot.tms;

import us.mn.state.dot.tms.comm.Device2Operation;
import us.mn.state.dot.tms.comm.MessagePoller;

/**
 * Device2Impl is the base class for all field devices, including detectors,
 * cameras, ramp meters, dynamic message signs, etc.
 *
 * @author Douglas Lau
 */
abstract public class Device2Impl extends BaseObjectImpl implements Device2,
	ControllerIO
{
	/** Create a new device */
	protected Device2Impl(String n) throws TMSException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name);
		g.doStore();
		geo_loc = g;
		MainServer.server.addObject(geo_loc);
		notes = "";
	}

	/** Create a device */
	protected Device2Impl(String n, GeoLoc l, ControllerImpl c, int p,
		String nt)
	{
		super(n);
		geo_loc = l;
		controller = c;
		pin = p;
		notes = nt;
	}

	/** Initialize the controller for this device */
	public void initTransients() {
		try {
			ControllerImpl c = controller;
			if(c != null)
				c.setIO(pin, this);
		}
		catch(TMSException e) {
			System.err.println("Device " + getName() +
				" initialization error");
			e.printStackTrace();
		}
	}

	/** Get the active status */
	public boolean isActive() {
		ControllerImpl c = controller;	// Avoid race
		if(c == null)
			return false;
		else
			return c.getActive();
	}

	/** Get the failure status */
	public boolean isFailed() {
		ControllerImpl c = controller;	// Avoid race
		if(c == null)
			return true;
		else
			return c.isFailed();
	}

	/** Get the message poller */
	public MessagePoller getPoller() {
		ControllerImpl c = controller;	// Avoid race
		if(c != null)
			return c.getPoller();
		else
			return null;
	}

	/** Controller associated with this traffic device */
	protected ControllerImpl controller;

	/** Update the controller and/or pin */
	protected void updateController(ControllerImpl oc, ControllerImpl c,
		int p) throws TMSException
	{
		if(oc != null)
			oc.setIO(pin, null);
		try {
			if(c != null)
				c.setIO(p, this);
		}
		catch(TMSException e) {
			if(oc != null)
				oc.setIO(pin, this);
			throw e;
		}
	}

	/** Set the controller of the device */
	public void setController(Controller c) {
		controller = (ControllerImpl)c;
	}

	/** Set the controller of the device */
	public void doSetController(Controller c) throws TMSException {
		if(c == controller)
			return;
		if(c != null && controller != null)
			throw new ChangeVetoException("Device has controller");
		if(c != null && !(c instanceof ControllerImpl))
			throw new ChangeVetoException("Invalid controller");
		updateController(controller, (ControllerImpl)c, pin);
		if(c == null)
			store.update(this, "controller", null);
		else
			store.update(this, "controller", c.getName());
		setController(c);
	}

	/** Get the controller to which this device is assigned */
	public Controller getController() {
		return controller;
	}

	/** Controller I/O pin number */
	protected int pin = 0;

	/** Set the controller I/O pin number */
	public void setPin(int p) {
		pin = p;
	}

	/** Set the controller I/O pin number */
	public void doSetPin(int p) throws TMSException {
		if(p == pin)
			return;
		store.update(this, "pin", p);
		setPin(p);
	}

	/** Get the controller I/O pin number */
	public int getPin() {
		return pin;
	}

	/** Device location */
	protected GeoLoc geo_loc;

	/** Set the device location */
	public void setGeoLoc(GeoLoc l) {
		geo_loc = l;
	}

	/** Set the device location */
	public void doSetGeoLoc(GeoLoc l) throws TMSException {
		if(l == geo_loc)
			return;
		store.update(this, "geo_loc", l.getName());
		setGeoLoc(l);
	}

	/** Get the device location */
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}

	/** Administrator notes for this device */
	protected String notes;

	/** Set the administrator notes */
	public void setNotes(String n) {
		notes = n;
	}

	/** Set the administrator notes */
	public void doSetNotes(String n) throws TMSException {
		if(n.equals(notes))
			return;
		store.update(this, "notes", n);
		setNotes(n);
	}

	/** Get the administrator notes */
	public String getNotes() {
		return notes;
	}

	/** Operation which owns the device */
	protected transient Device2Operation owner;

	/** Acquire ownership of the device */
	public Device2Operation acquire(Device2Operation o) {
		// Name used for unique device acquire/release lock
		synchronized(name) {
			if(owner == null)
				owner = o;
			return owner;
		}
	}

	/** Release ownership of the device */
	public Device2Operation release(Device2Operation o) {
		// Name used for unique device acquire/release lock
		synchronized(name) {
			Device2Operation _owner = owner;
			if(owner == o)
				owner = null;
			return _owner;
		}
	}

	/** Get a description of the current device operation */
	public String getOperation() {
		Device2Operation o = owner;
		if(o == null)
			return "None";
		else
			return o.getOperationDescription();
	}

	/** Notify clients of a status update */
	public void notifyStatus() {
		// FIXME: this is for the operation attribute
	}
}
