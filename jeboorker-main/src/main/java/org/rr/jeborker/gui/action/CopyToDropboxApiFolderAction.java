package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.SwingUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.Jeboorker;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry.File;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;
import com.dropbox.core.DbxWriteMode;

public class CopyToDropboxApiFolderAction extends AbstractAction {

	private static final long serialVersionUID = 8728272292807726067L;

	/** Key to get the access token from the preferences. */
	private static final String DROPBOX_ACCESS_TOKEN_KEY = "dropboxAuthKey";

	/** source file to upload to dropbox */
	String source;

	CopyToDropboxApiFolderAction(String text) {
		this.source = text;
		putValue(Action.NAME, Bundle.getString("CopyToDropboxAction.name"));
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("copy_dropbox_16.png"));
		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("copy_dropbox_22.png"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		IResourceHandler resource = ResourceHandlerFactory.getResourceHandler(source);
		try {
			String message = Bundle.getFormattedString("CopyToDropboxAction.uploading", resource.getName());
			MainController.getController().getProgressMonitor().monitorProgressStart(message);

			doUpload(resource);
		} catch (Exception ex) {
			LoggerFactory.getLogger(this).log(Level.WARNING, "Upload failed", ex);
		} finally {
			MainController.getController().getProgressMonitor().monitorProgressStop();
		}
	}

	/**
	 * Uploads the given {@link IResourceHandler} to the dropbox folder of the application.
	 *
	 * @param resource The resource to be uploaded.
	 *
	 * @see https://www.dropbox.com/developers/core/start/java
	 */
	private void doUpload(IResourceHandler resource) throws MalformedURLException, IOException, URISyntaxException, DbxException {
		// https://www.dropbox.com/developers
		DbxAppInfo appInfo = new DbxAppInfo(StringUtils.rot13("m8zitanp9n1p5nq"), StringUtils.rot13("gz585xxj5gp98qe"));
		DbxRequestConfig config = new DbxRequestConfig("Jeboorker/" + Jeboorker.getVersion(), Locale.getDefault().toString());
		DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);

		APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		String accessToken = preferenceStore.getGenericEntryAsString(DROPBOX_ACCESS_TOKEN_KEY);
		if (StringUtils.isNotEmpty(accessToken)) {
			// re-auth specific stuff
			try {
				DbxClient client = createClient(config, accessToken);

				// throws the DbxException if auth was detracted.
				doUpload(resource, client);
			} catch (DbxException e) {
				// retry with an auth if the old auth are no longer be valid.
				DbxClient client = authToDropbox(config, webAuth);
				doUpload(resource, client);
			}
		} else {
			DbxClient client = authToDropbox(config, webAuth);
			doUpload(resource, client);
		}
	}

	private File doUpload(IResourceHandler resource, DbxClient client) throws DbxException, IOException {
		try (InputStream inputStream = resource.getContentInputStream()) {
			return client.uploadFile('/' + resource.getName(), DbxWriteMode.add(), resource.size(), inputStream);
		}
	}

	private DbxClient createClient(DbxRequestConfig config, String accessToken) {
		return new DbxClient(config, accessToken);
	}

	private DbxClient authToDropbox(DbxRequestConfig config, DbxWebAuthNoRedirect webAuth) throws IOException, URISyntaxException,
			MalformedURLException, DbxException {
		APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		String authorizeUrl = webAuth.start();
		SwingUtils.openURL(authorizeUrl);

		String code = JOptionPane.showInputDialog(MainController.getController().getMainWindow(), Bundle.getString("CopyToDropboxAction.auth"));

		DbxAuthFinish authFinish = webAuth.finish(code);
		String accessToken = authFinish.accessToken;

		preferenceStore.addGenericEntryAsString(DROPBOX_ACCESS_TOKEN_KEY, accessToken);

		return createClient(config, accessToken);
	}

}
