/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.widget;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * IPanel is a panel for viewing and editing forms.  It provides a simpler
 * API for using a GridBagLayout.
 *
 * @author Douglas Lau
 */
public class IPanel extends JPanel {

	/** Stretch types */
	public enum Stretch {
		NONE(GridBagConstraints.NONE, 1, GridBagConstraints.EAST, 0.1f),
		SOME(GridBagConstraints.NONE, 1, GridBagConstraints.WEST, 0.5f),
		CENTER(GridBagConstraints.NONE, GridBagConstraints.REMAINDER,
			GridBagConstraints.CENTER, 0),
		FULL(GridBagConstraints.BOTH, GridBagConstraints.REMAINDER,
			GridBagConstraints.CENTER, 1),
		LAST(GridBagConstraints.NONE, GridBagConstraints.REMAINDER,
			GridBagConstraints.WEST, 0.1f);
		private Stretch(int f, int w, int a, float x) {
			fill = f;
			width = w;
			anchor = a;
			wx = x;
		}
		private final int fill;
		private final int width;
		private final int anchor;
		private final float wx;
	}

	/** Color for value label text */
	static private final Color DARK_BLUE = new Color(0, 0, 128);

	/** Create a value label */
	static public JLabel createValueLabel() {
		JLabel lbl = new JLabel();
		lbl.setForeground(DARK_BLUE);
		// By default, labels are BOLD
		lbl.setFont(lbl.getFont().deriveFont(Font.ITALIC));
		return lbl;
	}

	/** Create a value label */
	static public JLabel createValueLabel(String txt) {
		JLabel lbl = createValueLabel();
		lbl.setText(txt);
		return lbl;
	}

	/** Current row on the form */
	private int row = 0;

	/** Create a new panel */
	public IPanel() {
		super(new GridBagLayout());
		setBorder(UI.border);
	}

	/** Dispose of the panel */
	public void dispose() {
		removeAll();
	}

	/** Set the title */
	public void setTitle(String t) {
		setBorder(BorderFactory.createTitledBorder(t));
	}

	/** Create grid bag constraints */
	private GridBagConstraints createConstraints(Stretch s) {
		GridBagConstraints bag = new GridBagConstraints();
		bag.anchor = s.anchor;
		bag.fill = s.fill;
		bag.insets.left = UI.hgap / 2;
		bag.insets.right = UI.hgap / 2;
		bag.insets.top = UI.vgap / 2;
		bag.insets.bottom = UI.vgap / 2;
		bag.weightx = s.wx;
		bag.weighty = 0;
		bag.gridx = GridBagConstraints.RELATIVE;
		bag.gridy = row;
		bag.gridwidth = s.width;
		if(s == Stretch.CENTER || s == Stretch.FULL ||s == Stretch.LAST)
			row++;
		return bag;
	}

	/** Add a label to the current row */
	public void add(String msg, Stretch s) {
		add(new ILabel(msg), s);
	}

	/** Add a label to the current row */
	public void add(String msg) {
		add(msg, Stretch.NONE);
	}

	/** Add a component to the current row */
	public void add(JComponent comp, Stretch s) {
		add(comp, createConstraints(s));
	}

	/** Add a component to the current row */
	public void add(JComponent comp) {
		add(comp, Stretch.SOME);
	}

	/** Add a text area to the current row */
	public void add(JTextArea area, Stretch s) {
		area.setWrapStyleWord(true);
		area.setLineWrap(true);
		add(createScrollPane(area), s);
	}

	/** Add a list to the current row */
	public void add(JList list, Stretch s) {
		add(createScrollPane(list), s);
	}

	/** Add a table to the current row */
	public void add(JTable table, Stretch s) {
		add(createScrollPane(table), s);
	}

	/** Create a scroll pane */
	private JScrollPane createScrollPane(JComponent comp) {
		return new JScrollPane(comp,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	}
}
