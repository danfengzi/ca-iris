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
package us.mn.state.dot.tms.client.dms;

import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignRequest;
import us.mn.state.dot.tms.SystemAttributeHelper;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.sonar.ProxySelectionListener;
import us.mn.state.dot.tms.client.sonar.ProxySelectionModel;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.utils.I18NMessages;

/**
 * The DMSDispatcher is a GUI component for creating and deploying DMS messages.
 * It uses a number of optional controls which appear or do not appear on screen
 * as a function of the agency.
 * @see FontComboBox, Font, FontImpl, SignMessage, DMSPanel
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSDispatcher extends FormPanel
	implements ProxySelectionListener<DMS>
{
	/** Panel used for drawing a DMS */
	protected final DMSPanel pnlSign;

	/** Displays the id of the DMS */
	protected final JTextField txtId = new JTextField();

	/** Displays the verify camera for the DMS */
	protected final JTextField txtCamera = new JTextField();

	/** Displays the location of the DMS */
	protected final JTextField txtLocation = new JTextField();

	/** Displays the current operation of the DMS */
	protected final JTextField txtOperation = new JTextField();

	/** Displays the controller status (optional) */
	protected JTextField txtControllerStatus = new JTextField();

	/** Displays the brightness of the DMS */
	protected final JTextField txtBrightness = new JTextField();

	/** Used to select the expires time for a message (optional) */
	protected JComboBox cmbExpire = new JComboBox(Expiration.values());

	/** Used to select the DMS font for a message (optional) */
	protected FontComboBox cmbFont = null;

	/** Button used to send a message to the DMS */
	protected final JButton btnSend = 
		new JButton(I18NMessages.get("DMSDispatcher.SendButton"));

	/** Button used to clear the DMS.
	 * FIXME: should just use ClearDmsAction */
	protected final JButton btnClear = 
		new JButton(I18NMessages.get("dms.clear_button"));

	/** Button used to get the DMS status (optional) */
	protected final JButton btnGetStatus = new JButton(I18NMessages.get(
		"DMSDispatcher.GetStatusButton"));

	/** AWS checkbox (optional) */
	protected AwsCheckBox awsCheckBox = null;

	/** Selection model */
	protected final ProxySelectionModel<DMS> selectionModel;

	/** Currently logged in user name */
	protected final String userName;

	/** Currently selected DMS */
	protected DMS selectedSign = null;

	/** Message selector widget */
	protected final MessageSelector messageSelector;

	/** Create a new DMS dispatcher */
	public DMSDispatcher(DMSManager manager, final SonarState st,
		TmsConnection tc)
	{
		super(true);
		setOptionalControlUse();
		messageSelector = new MessageSelector(st.getDmsSignGroups(),
			st.getSignText(),st.lookupUser(tc.getUser().getName()));
		userName = manager.getUser().getName();
		selectionModel = manager.getSelectionModel();
		pnlSign = new DMSPanel(st.getSystemAttributes());
		setBorder(BorderFactory.createTitledBorder(
			I18NMessages.get("dms.selected_title")));

		txtId.setEditable(false);
		txtCamera.setEditable(false);
		txtLocation.setEditable(false);
		txtBrightness.setEditable(false);
		txtOperation.setEditable(false);

		add("ID", txtId);
		addRow("Camera", txtCamera);
		add("Location", txtLocation);
		addRow("Brightness", txtBrightness);
		if(m_useControllerStatusField) {
			add("Operation", txtOperation);
			addRow("Status", txtControllerStatus);
		} else
			addRow("Operation", txtOperation);
		addRow(pnlSign);
		addRow(createDeployBox());

		clearSelected();
		selectionModel.addTmsSelectionListener(this);
	}

	/** Create a component to deploy signs */
	protected Box createDeployBox() {
		Box boxRight = Box.createVerticalBox();
		boxRight.add(Box.createVerticalGlue());
		if(m_useDurationComboBox)
			boxRight.add(buildDurationBox());
		if(m_useFontsComboBox) {
			JPanel fjp = buildFontSelectorBox(st.getFonts());
			if(fjp != null)
				boxRight.add(fjp);
		}
		if(m_useAwsCheckBox) {
			awsCheckBox = new AwsCheckBox(getAwsProxyName(), 
				I18NMessages.get("DMSDispatcher.AwsCheckBox"));
			JPanel p = new JPanel(new FlowLayout());
			p.add(awsCheckBox);
			boxRight.add(p);
		}
		boxRight.add(Box.createVerticalStrut(4));
		boxRight.add(buildButtonPanel());
		boxRight.add(Box.createVerticalGlue());
		Box deployBox = Box.createHorizontalBox();
		deployBox.add(messageSelector);
		deployBox.add(boxRight);
		return deployBox;
	}

	/** flags for optional controls */
	protected boolean m_useFontsComboBox;
	protected boolean m_useDurationComboBox;
	protected boolean m_useControllerStatusField;
	protected boolean m_useGetStatusButton;
	protected boolean m_useAwsCheckBox;

	/** set flags for optional controls */
	protected void setOptionalControlUse() {
		m_useFontsComboBox = 
			SystemAttributeHelper.isAgencyCaltransD10();
		m_useDurationComboBox = 
			!SystemAttributeHelper.isAgencyCaltransD10();
		m_useControllerStatusField = 
			SystemAttributeHelper.isAgencyCaltransD10();
		m_useGetStatusButton = 
			SystemAttributeHelper.isAgencyCaltransD10();
		m_useAwsCheckBox = 
			SystemAttributeHelper.useAwsCheckBox();
	}

	/** Dispose of the dispatcher */
	public void dispose() {
		removeAll();
		selectedSign = null;
		selectionModel.removeTmsSelectionListener(this);
	}

	/** Build the button panel */
	protected Box buildButtonPanel() {
		new ActionJob(btnSend) {
			public void perform() throws Exception {
				sendMessage();
			}
		};
		btnSend.setToolTipText(I18NMessages.get(
			"DMSDispatcher.SendButton.ToolTip"));
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(btnSend);
		box.add(Box.createHorizontalGlue());
		box.add(btnClear);
		box.add(Box.createHorizontalGlue());

		// add optional 'get status' button
		if(m_useGetStatusButton) {
			btnGetStatus.setToolTipText(I18NMessages.get(
				"DMSDispatcher.GetStatusButton.ToolTip"));
			new ActionJob(this, btnGetStatus) {
				public void perform() throws Exception {
					selectedSign.setSignRequest(
						SignRequest.QUERY_STATUS);
				}
			};
			box.add(btnGetStatus);
			box.add(Box.createHorizontalGlue());
		}
		return box;
	}

	/** Build the optional message duration box */
	protected JPanel buildDurationBox() {
		assert m_useDurationComboBox;
		if(!m_useDurationComboBox)
			return null;
		JPanel p = new JPanel(new FlowLayout());
		p.add(new JLabel("Duration"));
		p.add(cmbExpire);
		cmbExpire.setSelectedIndex(0);
		return p;
	}

	/** Build the font selector combo box */
	protected JPanel buildFontSelectorBox(TypeCache<Font> tcf) {
		assert m_useFontsComboBox;
		if(!m_useFontsComboBox)
			return null;
		assert tcf != null;
		if(tcf == null)
			return null;
		cmbFont = new FontComboBox(this, tcf);
		JPanel p = new JPanel(new FlowLayout());
		p.add(new JLabel("Font"));
		p.add(cmbFont);
		return p;
	}

	/** Set the selected DMS */
	protected void setSelected(DMS proxy) {
		selectedSign = proxy;
		if(proxy == null)
			clearSelected();
		else {
			btnSend.setEnabled(true);
			btnClear.setEnabled(true);
			btnClear.setAction(new ClearDmsAction(proxy, userName));
			if(m_useGetStatusButton)
				btnGetStatus.setEnabled(true);
			if(m_useDurationComboBox) {
				cmbExpire.setEnabled(true);
				cmbExpire.setSelectedIndex(0);
			}
			if(m_useFontsComboBox) {
				cmbFont.setEnabled(true);
				cmbFont.setDefaultSelection();
			}
			if(m_useAwsCheckBox) {
				awsCheckBox.setProxy(getAwsProxyName());
			}
			proxy.updateUpdateInfo(); // update global messages
			pnlSign.setSign(proxy);
			refreshUpdate();
			refreshStatus();
		}
	}

	/** Clear the selected DMS */
	protected void clearSelected() {
		selectedSign = null;
		txtId.setText("");
		txtCamera.setText("");
		txtLocation.setText("");
		txtBrightness.setText("");
		if(m_useDurationComboBox) {
			cmbExpire.setEnabled(false);
			cmbExpire.setSelectedIndex(0);
		}
		if(m_useFontsComboBox) {
			cmbFont.setEnabled(false);
			cmbFont.setDefaultSelection();
		}
		if(m_useAwsCheckBox) {
			awsCheckBox.setProxy(null);
		}
		messageSelector.setEnabled(false);
		messageSelector.clearSelections();
		btnSend.setEnabled(false);
		btnClear.setEnabled(false);
		if(m_useGetStatusButton)
			btnGetStatus.setEnabled(false);
		pnlSign.setSign(null);
	}

	/** Get the selected duration */
	protected Integer getDuration() {
		if(m_useDurationComboBox) {
			Expiration e = (Expiration)cmbExpire.getSelectedItem();
			if(e != null)
				return e.duration;
		}
		return null;
	}

	/** Send a new message to the selected DMS object */
	protected void sendMessage() {
		DMS proxy = selectedSign;	// Avoid NPE race
		String message = messageSelector.getMessage();
		String fontName = (m_useFontsComboBox ? 
			cmbFont.getSelectedItemName() : null);
		if(proxy != null && message != null) {
			proxy.dms.setMessage(userName, message, 
				getDuration(), fontName);
			messageSelector.updateMessageLibrary();
		}
	}

	/** Called whenever a sign is added to the selection */
	public void selectionAdded(DMS s) {
		if(selectionModel.getSelectedCount() <= 1)
			setSelected(s);
	}

	/** Called whenever a sign is removed from the selection */
	public void selectionRemoved(DMS s) {
		if(selectionModel.getSelectedCount() == 1) {
			for(DMS dms: selectionModel.getSelected())
				setSelected(dms);
		} else if(s == selected)
			setSelected(null);
	}

	/** Refresh the update status of the device */
	public void refreshUpdate() {
		DMS proxy = selectedSign;	// Avoid NPE race
		if(proxy != null) {
			txtId.setText(proxy.getId());
			txtLocation.setText(proxy.getDescription());
			txtCamera.setText(proxy.getCameraId());
			messageSelector.updateModel(proxy);
		}
	}

	/** Refresh the status of the device */
	public void refreshStatus() {
		DMS dms = selectedSign;	// Avoid NPE race
		if(dms == null)
			return;
		SignMessage m = dms.getMessage();
		pnlSign.setMessage(m);
		messageSelector.setMessage(dms);
		txtOperation.setText(dms.getOperation());
		if(dms.isFailed()) {
			txtOperation.setForeground(Color.WHITE);
			txtOperation.setBackground(Color.GRAY);
		} else {
			txtOperation.setForeground(null);
			txtOperation.setBackground(null);
		}
		txtBrightness.setText("" + Math.round(
			dms.getLightOutput() / 655.35f));

		// optional controller status field
		if(m_useControllerStatusField)
			txtControllerStatus.setText(dms.getControllerStatus());
	}

	/** get the currently selected DMS */
	public DMS getSelectedDms() {
		return selectedSign;
	}

	/** get the currently selected DMS id, e.g. "V1" */
	public String getSelectedDmsId() {
		if(selectedSign == null)
			return null;
		else
			return selectedSign.getId();
	}
}
