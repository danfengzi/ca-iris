/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.tms.client.toast.SmartDesktop;

/** 
 * Menu for help information.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class HelpMenu extends JMenu {

	/** Create a new HelpMenu */
	public HelpMenu(final SmartDesktop desktop) { 
		super("Help");
		setMnemonic('H');
		JMenuItem item = new JMenuItem("About IRIS");
		item.setMnemonic('A');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new AboutForm());
			}
		};
		add(item);
	}
}
