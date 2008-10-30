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

package us.mn.state.dot.tms.comm.caws;

import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.MsgActPriority;
import us.mn.state.dot.tms.MsgActPriorityD10;
import us.mn.state.dot.tms.MsgActPriorityProc;
import us.mn.state.dot.tms.InvalidMessageException;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.TrafficDeviceImpl;
import us.mn.state.dot.tms.TrafficDeviceAttribute;
import us.mn.state.dot.tms.TrafficDeviceAttributeHelper;
import us.mn.state.dot.tms.TrafficDeviceAttributeImpl;
import us.mn.state.dot.tms.comm.caws.CawsPoller;
import us.mn.state.dot.tms.comm.caws.MsgActPriorityCallBackBlank;
import us.mn.state.dot.tms.utils.SString;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * CAWS D10CmsMsg. This is a single CMS message.
 *
 * @author Michael Darter
 */
public class D10CmsMsg  implements Serializable
{
	// consts
	private static final String CAWS_OWNER = CawsPoller.CAWS_OWNER;
	private static final String DESC_BLANK = "Blank";
	private static final String DESC_ONEPAGENORM = "1 Page (Normal)";
	private static final String DESC_TWOPAGENORM = "2 Page (Normal)";
	private static final String DOUBLESTROKE = "Double Stroke";
	private static final String SINGLESTROKE = "Single Stroke";

	// fields
	private int m_cmsid = -1;           // cms ID
	private Date m_date = null;         // message date and time
	private String m_desc = "";         // this has predefined valid values
	private String m_multistring = "";  // message as multistring
	private double m_ontime = 0;
	private boolean m_valid = false;    // is message valid?

	// types
	public enum CawsMsgType { BLANK, ONEPAGEMSG, TWOPAGEMSG, TRAVELTIME }

	/** constructor */
	public D10CmsMsg() {}

	/**
	 * Create a new object using text line.
	 * @param line A single line from the CAWS message file. e.g.
	 *             "20080403085910;25;Blank;Single Stroke;Single Stroke;;;;;;;0.0;"
	 */
	public D10CmsMsg(String line) throws IllegalArgumentException {
		this.parse(line);
	}

	/**
	 * Parse a string that contains a single CMS message.
	 * @param argline a single CMS message, fields delimited with ';'.
	 */
	public void parse(String argline) throws IllegalArgumentException {

		// add a space between successive delimiters. This is done so the
		// tokenizer doesn't skip over delimeters with nothing between them.
		// System.err.println("D10CmsMsg.D10CmsMsg() called, argline="+argline);
		String line = argline.replace(";;", "; ;");

		// System.err.println("D10CmsMsg.D10CmsMsg() called, line1="+line);
		line = line.replace(";;", "; ;");

		// System.err.println("D10CmsMsg.D10CmsMsg() called, line2="+line);
		// verify syntax
		StringTokenizer tok = new StringTokenizer(line, ";");

		// validity check
		int numtoks = tok.countTokens();
		if(numtoks != 13) {
			throw new IllegalArgumentException(
			    "Bogus CMS message format (" + argline + ").");
		}

		// date: 20080403085910
		m_date = convertDate(tok.nextToken());

		// id: 39
		m_cmsid = SString.stringToInt(tok.nextToken());

		// message description
		String f02 = tok.nextToken();
		if(!f02.equals(DESC_BLANK) &&!f02.equals(
			DESC_ONEPAGENORM) &&!f02.equals(
			DESC_TWOPAGENORM)) {    // FIXME: verify possibilities
			System.err.println(
			    "WARNING: unknown message description received in D10CmsMsg.parse(): "
			    + f02);
		}

		m_desc = f02;

		// pg 1 font
		String f03 = tok.nextToken();
		if(!f03.equals(SINGLESTROKE) &&!f03.equals(DOUBLESTROKE)) {
			System.err.println(
			    "WARNING: unknown pg 1 font received in D10CmsMsg.parse(): "
			    + f03);
		}

		// pg 2 font
		String f04 = tok.nextToken();
		if(!f04.equals(SINGLESTROKE) &&!f04.equals(DOUBLESTROKE)) {
			System.err.println(
			    "WARNING: unknown pg 2 font received in D10CmsMsg.parse(): "
			    + f04);
		}

		// rows of text
		{
			String row1 = tok.nextToken().trim().toUpperCase();
			String row2 = tok.nextToken().trim().toUpperCase();
			String row3 = tok.nextToken().trim().toUpperCase();
			String row4 = tok.nextToken().trim().toUpperCase();
			String row5 = tok.nextToken().trim().toUpperCase();
			String row6 = tok.nextToken().trim().toUpperCase();

			// create message-pg1
			StringBuilder m = new StringBuilder();

			m.append(row1);
			m.append("[nl]");
			m.append(row2);
			m.append("[nl]");
			m.append(row3);
			m.append("[nl]");

			// pg2
			if(row4.length() + row5.length() + row6.length() > 0) {
				m.append("[np]");
				m.append(row4);
				m.append("[nl]");
				m.append(row5);
				m.append("[nl]");
				m.append(row6);
			}

			m_multistring = m.toString();
		}

		// on time: 0.0
		m_ontime = SString.stringToDouble(tok.nextToken());

		// ignore this field, nothing there
		String f12 = tok.nextToken();
		if(!m_desc.equals(DESC_BLANK)) {
			System.err.println("D10CmsMsg.D10CmsMsg():" + m_date
				+ "," + m_cmsid + "," + m_multistring + ","
				+ m_ontime);
		}

		this.setValid(true);
	}

	/**
	 *  Convert a local time date string from the d10 cms file to a Date.
	 *  @params date String date/time in the format "20080403085910" which is local time.
	 *  @returns A Date cooresponding to the argument.
	 *  @throws IllegalArgumentException if the argument is bogus.
	 */
	private static Date convertDate(String argdate)
		throws IllegalArgumentException {

		// sanity check
		if((argdate == null) || (argdate.length() != 14)) {
			throw new IllegalArgumentException(
			    "Bogus date string received: " + argdate);
		}

		// note the column range is: inclusive, exclusive
		// year
		int y = SString.stringToInt(argdate.substring(0, 4));
		if(y < 2008) {
			throw new IllegalArgumentException(
			    "Bogus year received:" + argdate + "," + y);
		}

		// month
		int m = SString.stringToInt(argdate.substring(4, 6)) - 1;    // zero based
		if((m < 0) || (m > 11)) {
			throw new IllegalArgumentException(
			    "Bogus month received:" + argdate + "," + m);
		}

		// day
		int d = SString.stringToInt(argdate.substring(6, 8));
		if((d < 1) || (d > 31)) {
			throw new IllegalArgumentException(
			    "Bogus day received:" + argdate + "," + d);
		}

		// hour
		int h = SString.stringToInt(argdate.substring(8, 10));
		if((h < 0) || (h > 23)) {
			throw new IllegalArgumentException(
			    "Bogus hour received:" + argdate + "," + h);
		}

		// min
		int mi = SString.stringToInt(argdate.substring(10, 12));
		if((mi < 0) || (mi > 59)) {
			throw new IllegalArgumentException(
			    "Bogus minute received:" + argdate + "," + mi);
		}

		// sec
		int s = SString.stringToInt(argdate.substring(12, 14));
		if((s < 0) || (s > 59)) {
			throw new IllegalArgumentException(
			    "Bogus second received:" + argdate + "," + s);
		}

		// create Date
		Calendar c = new GregorianCalendar(y, m, d, h, mi, s);

		// c.setTimeZone(TimeZone.getTimeZone("UTC"));  // sets UTC time zone
		Date date = c.getTime();

		return (date);
	}

	/** set valid */
	private void setValid(boolean v) {
		m_valid = v;
	}

	/** return true if the CMS message is valid else false */
	public boolean getValid() {
		return (m_valid);
	}

	/** get message type */
	public CawsMsgType getCawsMsgType() {

		CawsMsgType ret = CawsMsgType.BLANK;

		if(m_desc.equals(DESC_BLANK)) {
			ret = CawsMsgType.BLANK;
		} else if(m_desc.equals(DESC_ONEPAGENORM)) {
			ret = CawsMsgType.ONEPAGEMSG;
		} else if(m_desc.equals(DESC_TWOPAGENORM)) {
			ret = CawsMsgType.TWOPAGEMSG;
		} else if(false) {	//FIXME: add in the future
			ret = CawsMsgType.TRAVELTIME;
		} else {
			String msg="D10CmsMsg.getCawsMsgType: Warning: unknown D10 message description encountered ("+m_desc+").";
			assert false : msg;
			System.err.println(msg);
			ret = CawsMsgType.BLANK;
		}

		return (ret);
	}

	/**
	 *  Activate the message. CAWS messages are activated only if the DMS is
	 *  currently blank or contains a message owned by CAWS.
	 *  @param dms Activate the message on this DMS.
	 */
	public void activate(DMSImpl dms) {
		System.err.println("-----D10CmsMsg.activate("+dms+") called, msg=" + this);
		boolean activate=shouldSendMessage(dms);
		if(activate)
			this.sendMessage(dms);
	}

	/**
	 * decide if a caws message should be sent to a DMS.
	 * @params dms The associated DMS.
	 * @return true to send the message.
	 */
	protected boolean shouldSendMessage(DMSImpl dms) {
		if(dms == null || m_multistring==null || m_multistring.length()<=0)
			return (false);

		// is caws activated for the sign?
		if(!TrafficDeviceAttributeHelper.awsControlled(
			getIrisCmsId())) 
		{
			System.err.println("D10CmsMsg.shouldSendMessage(): DMS " + getIrisCmsId() + " is NOT activated for CAWS control.");
			return false;
		}
		System.err.println("D10CmsMsg.shouldSendMessage(): DMS "+getIrisCmsId()+" is activated for CAWS control.");

		// be safe and send the caws message by default
		boolean send=true;

		// message already deployed?
		if(dms.getStatusCode() == DMS.STATUS_DEPLOYED) {
			SignMessage cur = dms.getMessage();
			if(cur!=null)
				send = !cur.equals(m_multistring);
			System.err.println("D10CmsMsg.shouldSendMessage(): DMS is deployed, m_multistring="+m_multistring+", cur msg="+cur.toString());
		}

		System.err.println("D10CmsMsg.shouldSendMessage(): should send="+send);
		return send;
	}

	/**
	 * Send a message to the specified DMS.
	 * @params dms The associated DMS.
	 */
	protected void sendMessage(DMSImpl dms) {
		CawsMsgType mtype = this.getCawsMsgType();

		// blank the message
		if(mtype == CawsMsgType.BLANK) {
			System.err.println("D10CmsMsg.sendMessage(): will blank DMS "+ this.getIrisCmsId() + " for DMS=" + dms + ".");
			dms.clearMessageUsingActivationPriority(
				CAWS_OWNER,createMsgActPriority());

		// 1 or 2 pg msg
		} else if((mtype == CawsMsgType.ONEPAGEMSG)
			|| (mtype == CawsMsgType.TWOPAGEMSG)) {

			System.err.println("D10CmsMsg.sendMessage(): will activate DMS " + this.getIrisCmsId() + ":" + this);
			try {
				dms.setMessage(this.toSignMessage(dms));
				dms.updateMessageGraphic();
			} catch (InvalidMessageException e) {
				System.err.println("D10CmsMsg.sendMessage(): exception:" + e);
			}

		// travel time message
		} else if(mtype==CawsMsgType.TRAVELTIME) {
			//FIXME: add in future

		// error
		} else {
			assert false : "D10CmsMsg.activate(): ERROR--unknown CawsMsgType.";
		}

	}

	/**
	 * toSignMessage builds a SignMessage version of this message.
	 * @params dms The associated DMS.
	 * @returns A SignMessage that contains the text of the message and a rendered bitmap.
	 */
	public SignMessage toSignMessage(DMSImpl dms) {
		System.err.println(
		    "D10CmsMsg.toSignMessage() called: dms.SignWidth="
		    + dms.getSignWidthPixels() + ", getId()=" + dms.getId()
		    + ", dms.SignHeight=" + dms.getSignHeightPixels());

		// create multistring
		MultiString multi = new MultiString(m_multistring);

		// create bitmap
		Map<Integer, BitmapGraphic> bitmaps =
			dms.createPixelMaps(multi);

		// create sign message
		String owner = CAWS_OWNER;
		SignMessage sm = new SignMessage(owner, multi, bitmaps,
			SignMessage.DURATION_INFINITE);

		// set activation priority
		sm.setActivationPriority(createMsgActPriority());

		return sm;
	}

	/** Return a MsgActPriority as a function of the message */
	protected MsgActPriority createMsgActPriority() {
		MsgActPriority ret=null;
		if(getCawsMsgType()==CawsMsgType.BLANK) {
			System.err.println("D10CmsMsg.createMsgActPriority(): creating blank caws message");
			// create a procedural activation priority
			ret=new MsgActPriorityProc(MsgActPriorityD10.VAL_D10_CAWS_BLANK,
				new MsgActPriorityCallBackBlank());
		}
		else if(getCawsMsgType()==CawsMsgType.ONEPAGEMSG || 
			getCawsMsgType()==CawsMsgType.TWOPAGEMSG)
			ret=MsgActPriorityD10.PRI_D10_CAWS_MSG;
		else if(getCawsMsgType()==CawsMsgType.TRAVELTIME)
			ret=MsgActPriorityD10.PRI_D10_CAWS_TRAVELTIME;
		else {
			ret=MsgActPriority.PRI_OPER_MSG;
			String em="D10CmsMsg.createMsgActPriority(): unknown type encountered";
			assert false : em;
			System.err.println(em);
		}
		assert ret!=null : "ret is null";
		return ret;
	}

	/** toString */
	public String toString() {
		String s = "";
		s += "Message: " + m_multistring + ", ";
		s += "On time: " + m_ontime;
		return s;
	}

	/** get the CMS id, e.g. "39" */
	public int getCmsId() {
		return m_cmsid;
	}

	/** get the CMS id in IRIS form, e.g. "V39" */
	public String getIrisCmsId() {
		Integer id = this.getCmsId();
		return "V" + id.toString();
	}
}
