package org.rr.jeborker.gui.renderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import org.rr.common.swing.SwingUtils;
import org.rr.commons.collection.VolatileHashMap;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.layout.VerticalLayout;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.remote.metadata.MetadataDownloadEntry;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;

public class MetadataDownloadTableCellRenderer extends JPanel implements TableCellRenderer, Serializable  {

	private static final long serialVersionUID = -4684790158985895647L;
	
	private static final int LEFT_WIDTH = 80;
	
	private JLabel imagelabel;
	
	private JPanel mainPanel;
	
	private JCheckBox imageCheck = new JCheckBox();
	
	protected final Color selectedBgColor;
	
	protected final Color selectedFgColor;

	protected final Color bgColor;
	
	protected final Color fgColor;
	
	private HashMap<IMetadataReader.METADATA_TYPES, List<Map.Entry<JCheckBox, String>>> editingValues = new HashMap<IMetadataReader.METADATA_TYPES, List<Map.Entry<JCheckBox, String>>>();
	
	private MetadataDownloadEntry editingEntry;
	
	private static final VolatileHashMap<String, ImageIcon> thumbnailCache = new VolatileHashMap<String, ImageIcon>(20, 20);
	
	public MetadataDownloadTableCellRenderer() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{LEFT_WIDTH, 0, 0};
		gridBagLayout.rowHeights = new int[]{344, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		imagelabel = new JLabel();
		imagelabel.setOpaque(false);
		imagelabel.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_imagelabel = new GridBagConstraints();
		gbc_imagelabel.insets = new Insets(0, 0, 0, 5);
		gbc_imagelabel.fill = GridBagConstraints.BOTH;
		gbc_imagelabel.gridx = 0;
		gbc_imagelabel.gridy = 0;
		
		imagelabel.setBounds(0, 0, LEFT_WIDTH, 200);
		JLayeredPane layeredPane = new JLayeredPane();
		layeredPane.add(imagelabel, Integer.valueOf(0));
		
		imageCheck = new JCheckBox();
		imageCheck.setOpaque(false);
		imageCheck.setSelected(false);
		imageCheck.setBounds(0, 0, imageCheck.getPreferredSize().width, imageCheck.getPreferredSize().height);
		layeredPane.add(imageCheck, Integer.valueOf(1));
		
		add(layeredPane, gbc_imagelabel);
		
		mainPanel = new JPanel();
		mainPanel.setOpaque(false);
		GridBagConstraints gbc_mainPanel = new GridBagConstraints();
		gbc_mainPanel.fill = GridBagConstraints.BOTH;
		gbc_mainPanel.gridx = 1;
		gbc_mainPanel.gridy = 0;
		add(mainPanel, gbc_mainPanel);
		mainPanel.setLayout(new VerticalLayout(0, VerticalLayout.LEFT));
		
		Color color = SwingUtils.getSelectionBackgroundColor();
		selectedBgColor = SwingUtils.getBrighterColor(color, 20);		
		selectedFgColor = SwingUtils.getSelectionForegroundColor();
		bgColor = SwingUtils.getBackgroundColor();
		fgColor = SwingUtils.getForegroundColor();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		final Component tableCellComponent = this.getTableCellComponent(table, (MetadataDownloadEntry) value, isSelected, hasFocus, row, column);
		return tableCellComponent;
	}	

	Component getTableCellComponent(JTable table, MetadataDownloadEntry entry, boolean isSelected, boolean hasFocus, int row, final int column) {
		final int addEntryLabels = this.addMetadataComponentEntries(entry, isSelected);
		final int rowHeight = Math.max(80, addEntryLabels * 25);
		
		table.setRowHeight(row, rowHeight);
		ImageIcon imageIconCover = getImageIconCover(table, entry, new Dimension(LEFT_WIDTH, rowHeight));
		imagelabel.setIcon(imageIconCover);
		if(imageIconCover == null) {
			imageCheck.setVisible(false);
		} else {
			imageCheck.setVisible(true);
		}
		imagelabel.setBounds(0, 0, LEFT_WIDTH, rowHeight);
		
		Component[] allComponents = SwingUtils.getAllComponents(null, this);
		for(Component c : allComponents) {
			if(isSelected) {
				c.setBackground(selectedBgColor);	
				c.setForeground(selectedFgColor);
			} else {		
				c.setBackground(bgColor);
				c.setForeground(fgColor);			
			}			
		}
		if(isSelected) {
			this.setBackground(selectedBgColor);	
			this.setForeground(selectedFgColor);
		} else {		
			this.setBackground(bgColor);
			this.setForeground(fgColor);			
		}		
		return this;
	}
	
	/**
	 * Add a component for each valid value in the {@link MetadataDownloadEntry}. In common author and title
	 * are two of the values to be present and created.
	 * @return The number of components that was created and added. 
	 */
	private int addMetadataComponentEntries(MetadataDownloadEntry entry, boolean isEditing) {
		int result = 0;
		mainPanel.removeAll();
		if(isEditing) {
			//create a new one because the old is used as result.
			editingValues = new HashMap<IMetadataReader.METADATA_TYPES, List<Map.Entry<JCheckBox, String>>>();
			editingEntry = entry;
		}
		
		for(String author : entry.getAuthors()) {
			JComponent metadataEntryAuthorView = getMetadataEntryViewPanel(Bundle.getString("MetadataDownloadTableCellRenderer.label.author"), author, IMetadataReader.METADATA_TYPES.AUTHOR);
			mainPanel.add(metadataEntryAuthorView);
			result++;
		}
		
		if(!StringUtils.isEmpty(entry.getTitle())) {
			JComponent metadataEntryTitleView = getMetadataEntryViewPanel(Bundle.getString("MetadataDownloadTableCellRenderer.label.title"), entry.getTitle(), IMetadataReader.METADATA_TYPES.TITLE);
			mainPanel.add(metadataEntryTitleView);
			result++;
		}
		
		if(!StringUtils.isEmpty(entry.getAgeSuggestion())) {
			JComponent metadataEntryAgeSuggestionView = getMetadataEntryViewPanel(Bundle.getString("MetadataDownloadTableCellRenderer.label.ageSuggestion"), entry.getAgeSuggestion(), IMetadataReader.METADATA_TYPES.AGE_SUGGESTION);
			mainPanel.add(metadataEntryAgeSuggestionView);
			result++;
		}
		
		//no isbn 10 if isbn 13 is available 
		if(!StringUtils.isEmpty(entry.getIsbn13())) {
			JComponent metadataEntryISBN13View = getMetadataEntryViewPanel(Bundle.getString("MetadataDownloadTableCellRenderer.label.isbn13"), entry.getIsbn13(), IMetadataReader.METADATA_TYPES.ISBN);
			mainPanel.add(metadataEntryISBN13View);
			result++;
		} else if(!StringUtils.isEmpty(entry.getIsbn10())) {
			JComponent metadataEntryISBN10View = getMetadataEntryViewPanel(Bundle.getString("MetadataDownloadTableCellRenderer.label.isbn10"), entry.getIsbn10(), IMetadataReader.METADATA_TYPES.ISBN);
			mainPanel.add(metadataEntryISBN10View);
			result++;
		}
		
		if(!StringUtils.isEmpty(entry.getLanguage())) {
			JComponent metadataEntryLanguageView = getMetadataEntryViewPanel(Bundle.getString("MetadataDownloadTableCellRenderer.label.language"), entry.getLanguage(), IMetadataReader.METADATA_TYPES.LANGUAGE);
			mainPanel.add(metadataEntryLanguageView);
			result++;
		}
		
		if(!StringUtils.isEmpty(entry.getDescription())) {
			JComponent metadataEntryDescriptionView = getMetadataEntryViewPanel(Bundle.getString("MetadataDownloadTableCellRenderer.label.description"), entry.getDescription(), IMetadataReader.METADATA_TYPES.DESCRIPTION);
			mainPanel.add(metadataEntryDescriptionView);
			result++;
		}
		
		return result;
	}
	
	private JComponent getMetadataEntryViewPanel(final String labelText, final String value, final IMetadataReader.METADATA_TYPES type) {
		JPanel panel = new JPanel();
		panel.setOpaque(true);
		panel.setLayout(new BorderLayout(3, 0));
		
		JLabel lblValue = new JLabel("<html><b>" + labelText + "</b>: " + value + "</html>");
		lblValue.setOpaque(true);
		panel.add(lblValue, BorderLayout.CENTER);
		
		final JCheckBox check = new JCheckBox();
		check.setSelected(true);
		panel.add(check, BorderLayout.WEST);
		
		List<Entry<JCheckBox, String>> valuesList = editingValues.get(type);
		if(valuesList == null) {
			valuesList = new ArrayList<Map.Entry<JCheckBox,String>>();
		}
		valuesList.add(new Map.Entry<JCheckBox, String>() {
			
			@Override
			public String setValue(String value) {
				return null;
			}
			
			@Override
			public String getValue() {
				return value;
			}
			
			@Override
			public JCheckBox getKey() {
				return check;
			}
		});
		editingValues.put(type, valuesList);
		
		return panel;
	}
	

	/**
	 * Gets the thumbnail image to be displayed in the renderer.
	 * @param table The JTable instance.
	 * @param entry The item to be rendered.
	 * @return The thumbnail image to be displayed in the renderer.
	 */
	private ImageIcon getImageIconCover(final JTable table, final MetadataDownloadEntry entry, Dimension dim) {
		if(entry == null) {
			return null;
		}
		
		byte[] coverThumbnail = entry.getThumbnailImageBytes();
		if(entry != null && coverThumbnail != null && coverThumbnail.length > 0) {
			final String coverThumbnailCRC32 = String.valueOf(CommonUtils.calculateCrc(coverThumbnail));
			if(thumbnailCache.containsKey(coverThumbnailCRC32)) {
				return thumbnailCache.get(coverThumbnailCRC32);
			}
			
			try {
				final byte[] coverData = coverThumbnail;
				if(coverData!=null) {
					final IResourceHandler virtualImageResourceLoader = ResourceHandlerFactory.getVirtualResourceHandler("TableCellRendererImageData", coverData);
					final IImageProvider imageProvider = ImageProviderFactory.getImageProvider(virtualImageResourceLoader);
					final BufferedImage image = imageProvider.getImage();
					if(image != null) {
						BufferedImage croped = ImageUtils.crop(imageProvider.getImage());
						BufferedImage scaleToMatch = ImageUtils.scaleToMatch(croped, dim, true);
						ImageIcon imageIcon = new ImageIcon(scaleToMatch);
						thumbnailCache.put(coverThumbnailCRC32, imageIcon);
						return imageIcon;
					} else {
						return null;
					}
				}
			} catch (Exception e) {
				LoggerFactory.logInfo(this, "Could not render thumbnail", e);
			} 
		}
		return null;
	}
	
	/**
	 * Get the values for the editing component.
	 */
	HashMap<IMetadataReader.METADATA_TYPES, List<Map.Entry<JCheckBox, String>>> getEditingValues() {
		return editingValues;
	}
	
	/**
	 * Get the MetadataDownloadEntry for the editing component.
	 */
	MetadataDownloadEntry getEditingMetadataDownloadEntry() {
		return editingEntry;
	}
	
	/**
	 * Tells if the user has selected the cover image checkbox for set/replace
	 * the cover of the ebook file with the downloaded one. 
	 */
	boolean isCoverImageChecked() {
		return this.imageCheck.isSelected();
	}
	
}