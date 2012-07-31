/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2012  Minnesota Department of Transportation
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

import javax.swing.JLabel;
import javax.swing.JTextField;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.sched.FocusJob;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A panel for viewing and editing roadway location parameters.
 *
 * @author Douglas Lau
 */
public class R_NodeLocationPanel extends LocationPanel {

	/** Component for editing notes */
	protected final JTextField notes_txt = new JTextField(20);

	/** Label for r_node name */
	protected final JLabel name_lbl = new JLabel();

	/** Node being edited */
	protected R_Node node;

	/** Create a new roadway node location panel */
	public R_NodeLocationPanel(Session s) {
		super(s);
	}

	/** Initialize the widgets on the panel */
	public void initialize() {
		super.initialize();
		addRow(I18N.get("device.notes"), notes_txt);
		setCenter();
		addRow(name_lbl);
		clear();
	}

	/** Create the jobs */
	protected void createJobs() {
		super.createJobs();
		new FocusJob(notes_txt) {
			public void perform() {
				if(wasLost())
					setNotes(notes_txt.getText());
			}
		};
	}

	/** Set the node notes */
	protected void setNotes(String nt) {
		R_Node n = node;
		if(n != null)
			n.setNotes(nt);
	}

	/** Update one attribute */
	public final void update(final R_Node n, final String a) {
		// Serialize on WORKER thread
		new AbstractJob() {
			public void perform() {
				doUpdate(n, a);
			}
		}.addToScheduler();
	}

	/** Update one attribute */
	protected void doUpdate(R_Node n, String a) {
		if(a == null) {
			node = n;
			name_lbl.setText(n.getName());
		}
		if(a == null || a.equals("notes")) {
			notes_txt.setEnabled(canUpdate(n, "notes"));
			notes_txt.setText(n.getNotes());
		}
	}

	/** Test if the user can update an attribute */
	protected boolean canUpdate(R_Node n, String a) {
		return session.canUpdate(n, a);
	}

	/** Clear all attributes */
	protected void doClear() {
		super.doClear();
		node = null;
		name_lbl.setText(I18N.get("r_node.name.none"));
		notes_txt.setEnabled(false);
		notes_txt.setText("");
	}
}
