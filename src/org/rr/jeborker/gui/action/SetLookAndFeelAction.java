package org.rr.jeborker.gui.action;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.rr.commons.utils.CommonUtils;
import org.rr.jeborker.JeboorkerPreferences;

public class SetLookAndFeelAction extends AbstractAction {

	private static final long serialVersionUID = -2884898180881622573L;
	
	private String lookAndFeelName;
	
	SetLookAndFeelAction(String lookAndFeelName) {
		this.lookAndFeelName = lookAndFeelName;
		putValue(Action.NAME, lookAndFeelName);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		setLookAndFeel(this.lookAndFeelName);
	}

	public static void setLookAndFeel(String lookAndFeelName) {
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if(CommonUtils.compareTo(lookAndFeelName, info.getName()) == 0) {
				try {
					setLookAndFeel(info);
					JeboorkerPreferences.addEntryString(SetLookAndFeelAction.class.getName(), lookAndFeelName);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				break;
			}
		}
	}
	
	public static boolean restoreLookAndFeel() {
		String lookAndFeelName = JeboorkerPreferences.getEntryString(SetLookAndFeelAction.class.getName());
		if(lookAndFeelName != null && lookAndFeelName.length() > 0) {
			setLookAndFeel(lookAndFeelName);
			return true;
		}
		return false;
	}

	private static void setLookAndFeel(LookAndFeelInfo info) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(info.getClassName());
		
		Window[] windows = Window.getWindows();
		for (Window window : windows) {
			for(int i=0; i < window.getComponentCount(); i++) {
				Component component = window.getComponent(i);
				SwingUtilities.updateComponentTreeUI(component);
			}
		}
	}

}