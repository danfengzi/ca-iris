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
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import us.mn.state.dot.tms.TestLaneControlSignal;

/**
 * Test class for the LcsDispatcher.
 *
 * @author    <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 * @version   $Revision: 1.5 $ $Date: 2003/05/15 14:39:42 $
 */
public class LcsDispatcherTest extends JFrame {

	/** Creates new LcsDispatcherTest */
	public LcsDispatcherTest() { 
		setSize( new Dimension( 450, 420 ) );
		addWindowListener( new WindowAdapter() {
			public void windowClosing( WindowEvent evt ) {
				System.exit( 0 );
			}
		});
		getContentPane().setLayout( new BorderLayout() );
		final LcsDispatcher dispatcher = new LcsDispatcher();
		getContentPane().add( dispatcher, BorderLayout.CENTER );
		dispatcher.setSelected( new TestLaneControlSignal() );
		dispatcher.addComponentListener( new ComponentAdapter() {
			public void componentResized( ComponentEvent evt ) {
				System.out.println( "Width = " + dispatcher.getWidth() +
					" Height = " + dispatcher.getHeight() );
			}
		});
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
		LcsDispatcherTest test = new LcsDispatcherTest();
		test.test();
	}
}
