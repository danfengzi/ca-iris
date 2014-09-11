/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2014  Minnesota Department of Transportation
 * Copyright (C) 2014  AHMCT, University of California
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
package us.mn.state.dot.tms.client.camera;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.WrapperComboBoxModel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * GUI for viewing camera images.
 *
 * @author Douglas Lau
 * @author Tim Johnson
 * @author Travis Swanston
 */
public class CameraDispatcher extends JPanel {

	/** The system attribute for the number of button presets */
	static private final int NUM_PRESET_BTNS =
		SystemAttrEnum.CAMERA_NUM_PRESET_BTNS.getInt();

	/** Button number to select previous camera */
	static private final int BUTTON_PREVIOUS = 10;

	/** Button number to select next camera */
	static private final int BUTTON_NEXT = 11;

	/** Video size */
	static private final VideoRequest.Size SIZE = VideoRequest.Size.MEDIUM;

	/** User session */
	private final Session session;

	/** Proxy manager for camera devices */
	private final CameraManager manager;

	/** Selection model */
	private final ProxySelectionModel<Camera> sel_model;

	/** Selection listener */
	private final ProxySelectionListener<Camera> sel_listener =
		new ProxySelectionListener<Camera>()
	{
		public void selectionAdded(Camera c) {
			setSelected(sel_model.getSingleSelection());
		}
		public void selectionRemoved(Camera c) {
			setSelected(sel_model.getSingleSelection());
		}
	};

	/** Camera list model */
	private final ProxyListModel<Camera> model;

	/** Camera name label */
	private final JLabel name_lbl = IPanel.createValueLabel();

	/** Camera location label */
	private final JLabel location_lbl = IPanel.createValueLabel();

	/** Video output selection ComboBox */
	private final JComboBox output_cbx;

	/** Selected video monitor output */
	private VideoMonitor video_monitor;

	/** Camera PTZ control */
	private final CameraPTZ cam_ptz;

	/** Camera information panel */
	private final JPanel info_pnl;

	/** Streaming video panel */
	private final StreamPanel stream_pnl;

	/** Camera control panel */
	private final JPanel control_pnl;

	/** PTZ panel */
	private final PTZPanel ptz_pnl;

	/** Panel for lens control */
	private final LensPanel lens_pnl;

	/** Panel for camera utilities */
	private final UtilPanel util_pnl;

	/** Panel for camera presets */
	private final PresetPanel preset_pnl;

	/** Currently selected camera */
	private Camera selected = null;

	/** Joystick PTZ handler */
	private final JoystickPTZ joy_ptz;

	/** Create a new camera dispatcher */
	public CameraDispatcher(Session s, CameraManager man) {
		session = s;
		manager = man;
		setLayout(new BorderLayout());
		sel_model = manager.getSelectionModel();
		model = session.getSonarState().getCamCache().getCameraModel();
		cam_ptz = new CameraPTZ(s);
		joy_ptz = new JoystickPTZ(cam_ptz);
		output_cbx = createOutputCombo();
		info_pnl = createInfoPanel();
		stream_pnl = createStreamPanel();
		ptz_pnl = new PTZPanel(cam_ptz);
		lens_pnl = new LensPanel(cam_ptz);
		util_pnl = new UtilPanel(cam_ptz);
		preset_pnl = new PresetPanel(s);
		control_pnl = createControlPanel();
	}

	/** Create camera information panel */
	private JPanel createInfoPanel() {
		JPanel p = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 0;

		p.add(new ILabel("device.name"), gbc);
		gbc.gridx = 1;
		p.add(name_lbl, gbc);
		gbc.gridx = 2;

		gbc.weightx = 0.1;
		p.add(Box.createHorizontalGlue(), gbc);

		gbc.gridx = 3;
		gbc.weightx = 0.0;
		p.add(new ILabel("camera.output"), gbc);
		gbc.gridx = 4;
		p.add(output_cbx, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		p.add(new ILabel("location"), gbc);
		gbc.gridx = 1;
		p.add(location_lbl, gbc);

		return p;
	}

	/** Create the stream panel */
	private StreamPanel createStreamPanel() {
		VideoRequest vr = new VideoRequest(session.getProperties(),
			SIZE);
		vr.setSonarSessionId(session.getSessionId());
		vr.setRate(30);
		return new StreamPanel(vr, cam_ptz);
	}

	/** Create camera control panel */
	private JPanel createControlPanel() {
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(2, 0, 2, 0);
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;

		gbc.gridx = 0;
		gbc.gridy = 0;

		boolean ptz = SystemAttrEnum.CAMERA_PTZ_PANEL_ENABLE
			.getBoolean();
		boolean util = SystemAttrEnum.CAMERA_UTIL_PANEL_ENABLE
			.getBoolean();
		boolean preset = SystemAttrEnum.CAMERA_PRESET_PANEL_ENABLE
			.getBoolean();

		gbc.weightx = 0.1;
		p.add(Box.createHorizontalGlue(), gbc);
		gbc.gridx++;
		gbc.weightx = 0.0;
		if (ptz) {
			p.add(ptz_pnl, gbc);
			gbc.gridx++;
			if (util || preset) {
				gbc.weightx = 0.1;
				p.add(Box.createHorizontalGlue(), gbc);
				gbc.gridx++;
				gbc.weightx = 0.0;
			}
		}

		if (util)
			p.add(util_pnl, gbc);
		else
			p.add(lens_pnl, gbc);
		gbc.gridx++;

		if (preset) {
			gbc.weightx = 0.1;
			p.add(Box.createHorizontalGlue(), gbc);
			gbc.gridx++;
			gbc.weightx = 0.0;
			p.add(preset_pnl, gbc);
			gbc.gridx++;
		}
		gbc.weightx = 0.1;
		p.add(Box.createHorizontalGlue(), gbc);
		return p;
	}

	/** Create the video output selection combo box */
	private JComboBox createOutputCombo() {
		JComboBox box = new JComboBox();
		FilteredMonitorModel m = new FilteredMonitorModel(session);
		box.setModel(new WrapperComboBoxModel(m));
		if(m.getSize() > 1)
			box.setSelectedIndex(1);
		return box;
	}

	/** Initialize the widgets on the panel */
	public void initialize() {
		output_cbx.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				monitorSelected();
			}
		});
		joy_ptz.addJoystickListener(new JoystickListener() {
			public void buttonChanged(JoystickButtonEvent ev) {
				if(ev.pressed)
					doJoyButton(ev);
			}
		});
		setTitle(I18N.get("camera.selected"));
		add(info_pnl, BorderLayout.NORTH);
		add(stream_pnl, BorderLayout.CENTER);
		add(control_pnl, BorderLayout.SOUTH);
		clear();
		sel_model.addProxySelectionListener(sel_listener);
	}

	/** Set the title */
	public void setTitle(String t) {
		setBorder(BorderFactory.createTitledBorder(t));
	}

	/** Process a joystick button event */
	private void doJoyButton(JoystickButtonEvent ev) {
		if(ev.button == BUTTON_NEXT)
			selectNextCamera();
		else if(ev.button == BUTTON_PREVIOUS)
			selectPreviousCamera();
		else if(ev.button >= 0 && ev.button < NUM_PRESET_BTNS)
			selectCameraPreset(ev.button + 1);
	}

	/** Select the next camera */
	private void selectNextCamera() {
		Camera cam = model.higher(selected);
		if(cam != null)
			sel_model.setSelected(cam);
	}

	/** Select the previous camera */
	private void selectPreviousCamera() {
		Camera cam = model.lower(selected);
		if(cam != null)
			sel_model.setSelected(cam);
	}

	/** Command current camera to goto preset location */
	private void selectCameraPreset(int preset) {
		Camera proxy = selected;	// Avoid race
		if(proxy != null)
			proxy.setRecallPreset(preset);
	}

	/** Dispose of the camera viewer */
	public void dispose() {
		sel_model.removeProxySelectionListener(sel_listener);
		joy_ptz.dispose();
		cam_ptz.setCamera(null);
		stream_pnl.dispose();
		selected = null;
		removeAll();
	}

	/** Set the selected camera */
	public void setSelected(final Camera camera) {
		if(camera == selected)
			return;
		cam_ptz.setCamera(camera);
		selected = camera;
		if(camera != null) {
			name_lbl.setText(camera.getName());
			location_lbl.setText(GeoLocHelper.getDescription(
				camera.getGeoLoc()));
			stream_pnl.setCamera(camera);
			selectCamera();
			boolean e = cam_ptz.canControlPtz();
			ptz_pnl.setEnabled(e);
			lens_pnl.setEnabled(e);
			util_pnl.setEnabled(e);
			preset_pnl.setCamera(camera);
			preset_pnl.setEnabled(e);
		} else
			clear();
	}

	/** Called when a video monitor is selected */
	private void monitorSelected() {
		Object o = output_cbx.getSelectedItem();
		if(o instanceof VideoMonitor) {
			video_monitor = (VideoMonitor)o;
			selectCamera();
		} else
			video_monitor = null;
	}

	/** Select a camera on a video monitor */
	private void selectCamera() {
		VideoMonitor vm = video_monitor;
		if(vm != null)
			vm.setCamera(selected);
	}

	/** Clear all of the fields */
	private void clear() {
		name_lbl.setText("");
		location_lbl.setText("");
		stream_pnl.setCamera(null);
		ptz_pnl.setEnabled(false);
		lens_pnl.setEnabled(false);
		util_pnl.setEnabled(false);
		preset_pnl.setEnabled(false);
	}
}
