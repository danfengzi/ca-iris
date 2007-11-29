/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.utils.ActionJob;

/**
 * A form for displaying and editing DMS fonts
 *
 * @author Douglas Lau
 */
public class FontForm extends AbstractForm {

	/** Frame title */
	static protected final String TITLE = "DMS Fonts";

	/** Table model for fonts */
	protected FontModel f_model;

	/** Table to hold the font list */
	protected final JTable f_table = new JTable();

	/** Button to delete the selected font */
	protected final JButton del_font = new JButton("Delete Font");

	/** Font type cache */
	protected final TypeCache<Font> cache;

	/** Admin privileges */
	protected final boolean admin = true;

	/** Create a new font form */
	public FontForm(TypeCache<Font> fc) {
		super(TITLE);
		cache = fc;
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		f_model = new FontModel(cache, admin);
		add(createFontPanel());
	}

	/** Close the form */
	protected void close() {
		super.close();
		f_model.dispose();
	}

	/** Create font panel */
	protected JPanel createFontPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BORDER);
		GridBagConstraints bag = new GridBagConstraints();
		final ListSelectionModel s = f_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		s.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting())
					return;
				selectFont();
			}
		});
		f_table.setModel(f_model);
		f_table.setAutoCreateColumnsFromModel(false);
		f_table.setColumnModel(f_model.createColumnModel());
		JScrollPane pane = new JScrollPane(f_table);
		panel.add(pane, bag);
		if(admin) {
			del_font.setEnabled(false);
			bag.insets.left = 6;
			panel.add(del_font, bag);
			new ActionJob(this, del_font) {
				public void perform() throws Exception {
					int row = s.getMinSelectionIndex();
					if(row >= 0)
						f_model.deleteRow(row);
				}
			};
		}
		return panel;
	}

	/** Change the selected font */
	protected void selectFont() {
		ListSelectionModel s = f_table.getSelectionModel();
		Font f = f_model.getProxy(s.getMinSelectionIndex());
		del_font.setEnabled(f != null);
	}
}
