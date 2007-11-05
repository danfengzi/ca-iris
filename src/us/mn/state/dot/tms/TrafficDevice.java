/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2005  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.rmi.RemoteException;

/**
 * TrafficDevice
 *
 * @author Douglas Lau
 */
public interface TrafficDevice extends Device {

	/** Get the device ID */
	public String getId() throws RemoteException;

	/** Get the device status */
	public String getStatus() throws RemoteException;

	/** Get a description of the current device operation */
	public String getOperation() throws RemoteException;

	/** Inactive traffic device status code */
	public int STATUS_INACTIVE = 0;

	/** Failed traffic device status code */
	public int STATUS_FAILED = 1;

	/** Unavailable traffic device status code */
	public int STATUS_UNAVAILABLE = 2;

	/** Available traffic device status code */
	public int STATUS_AVAILABLE = 3;

	/** Deployed traffic device status code */
	public int STATUS_DEPLOYED = 4;

	/** Get the current status code */
	public int getStatusCode() throws RemoteException;
}
