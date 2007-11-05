/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import us.mn.state.dot.tms.TestLaneControlSignal;

/**
 * The test class for LcsPanel.
 *
 * @author    <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 * @version   $Revision: 1.4 $ $Date: 2003/05/15 14:39:42 $
 */
public class LcsPanelTest extends JFrame {

	/** Creates new TestLcsPanel */
	public LcsPanelTest() {
		LcsPanel lcs = new LcsPanel();
		lcs.setLcs( new TestLaneControlSignal() );
		getContentPane().setLayout( new BorderLayout() );
		getContentPane().add( lcs, BorderLayout.CENTER );
		pack();
	}

	public void test() {
		setVisible( true );
	}

	/**
	 * Execute the test.
	 *
	 * @param args  the command line arguments
	 */
	public static void main( String[] args ) {
		LcsPanelTest test = new LcsPanelTest();
		test.test();
	}

}
