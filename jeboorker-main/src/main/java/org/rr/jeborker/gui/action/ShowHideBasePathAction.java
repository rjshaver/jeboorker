package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;

/**
 * Add a folder action.
 */
class ShowHideBasePathAction extends AbstractAction {

	private static final long serialVersionUID = -9066575818229620987L;
	
	private static final String HIDE_ALL = "hideAll";
	
	private static final String SHOW_ALL = "showAll";
	
	private String path;
	
	ShowHideBasePathAction(String text) {
		super();
		if(text==null) {
			throw new RuntimeException("Need to specify path.");
		} else {
			if(SHOW_ALL.equals(text)) {
				path = text;
				putValue(Action.NAME, Bundle.getString("ShowHideBasePathAction.showAll.name"));
			} else if(HIDE_ALL.equals(text)) {
				path = text;
				putValue(Action.NAME, Bundle.getString("ShowHideBasePathAction.hideAll.name"));
			} else if(ResourceHandlerFactory.hasResourceHandler(text)) {
				path = text;
				putValue(Action.NAME, text);
			}
		}
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		final List<String> basePaths = preferenceStore.getBasePath();
		if(SHOW_ALL.equals(path)) {
			for (String basePath : basePaths) {
				ActionUtils.setBasePathVisibility(basePath, true);
			}
		} else if(HIDE_ALL.equals(path)) {
			for (String basePath : basePaths) {
				ActionUtils.setBasePathVisibility(basePath, false);
			}
		} else {
			ActionUtils.toggleBasePathVisibility(path);
		}
	}
	
}
