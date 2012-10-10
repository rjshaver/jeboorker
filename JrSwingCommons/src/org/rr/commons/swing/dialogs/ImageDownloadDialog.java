package org.rr.commons.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.paraj.prodcons.Consumer;
import org.paraj.prodcons.FeedEngine;
import org.paraj.prodcons.Producer;
import org.rr.common.swing.ShadowPanel;
import org.rr.common.swing.SwingUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.net.imagefetcher.IImageFetcher;
import org.rr.commons.net.imagefetcher.IImageFetcherEntry;
import org.rr.commons.net.imagefetcher.IImageFetcherFactory;
import org.rr.commons.net.imagefetcher.ImageFetcherFactory;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;

public class ImageDownloadDialog extends JDialog {
	
	private IResourceHandler selectedImage;
	
	private JTextField searchTextField;
	
	private JComboBox searchProviderComboBox;
	
	private JScrollPane scrollPane;
	
	private JButton okButton;
	
	private static Dimension cellSize = new Dimension(150, 250);
	
	private IImageFetcherFactory factory;
	
	/**
	 * Number of images to be loaded into the dialog.
	 */
	private int resultCount = 20;

	private Color selectedFgColor;

	private Color selectedBgColor;

	private Color bgColor;

	private Color fgColor;
	
	public ImageDownloadDialog(JFrame owner, IImageFetcherFactory factory) {
		super(owner);
		this.factory = factory;
		init(owner);
	}
	
	public ImageDownloadDialog(IImageFetcherFactory factory) {
		super();
		this.factory = factory;
		init(null);
	}

	protected void init(Frame owner) {
		this.setSize(800, 430);
		if(owner != null) {
			//center over the owner frame
			this.setLocation(owner.getBounds().x + owner.getBounds().width/2 - this.getSize().width/2, owner.getBounds().y + 50);
		}
		this.setTitle(Bundle.getString("ImageDownloadDialog.title"));
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setGlassPane(new ShadowPanel());	
		getGlassPane().setVisible(false);
		
		//workaround for a swing bug. The first time, the editor is used, the 
		//ui color instance draws the wrong color but have the right rgb values.
		Color color;
		color = SwingUtils.getSelectionForegroundColor();
		selectedFgColor = new Color(color.getRed(), color.getGreen(), color.getBlue());		
		
		color = SwingUtils.getSelectionBackgroundColor();
		selectedBgColor = new Color(color.getRed(), color.getGreen(), color.getBlue());		
		
		color = SwingUtils.getBackgroundColor();
		bgColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
		
		color = SwingUtils.getForegroundColor();
		fgColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel borderPanel = new JPanel();
		borderPanel.setBorder(new TitledBorder(null, Bundle.getString("ImageDownloadDialog.title"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_borderPanel = new GridBagConstraints();
		gbc_borderPanel.fill = GridBagConstraints.BOTH;
		gbc_borderPanel.gridx = 0;
		gbc_borderPanel.gridy = 0;
		getContentPane().add(borderPanel, gbc_borderPanel);
		GridBagLayout gbl_borderPanel = new GridBagLayout();
		gbl_borderPanel.columnWidths = new int[]{0, 340, 0, 0};
		gbl_borderPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_borderPanel.columnWeights = new double[]{1.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_borderPanel.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		borderPanel.setLayout(gbl_borderPanel);
		
		searchProviderComboBox = new JComboBox();
		GridBagConstraints gbc_searchProviderComboBox = new GridBagConstraints();
		gbc_searchProviderComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_searchProviderComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_searchProviderComboBox.gridx = 0;
		gbc_searchProviderComboBox.gridy = 0;
		borderPanel.add(searchProviderComboBox, gbc_searchProviderComboBox);
		searchProviderComboBox.setModel(new DefaultComboBoxModel(factory.getFetcherNames().toArray()));
		
		searchTextField = new JTextField();
		GridBagConstraints gbc_searchTextField = new GridBagConstraints();
		gbc_searchTextField.fill = GridBagConstraints.BOTH;
		gbc_searchTextField.insets = new Insets(0, 0, 5, 5);
		gbc_searchTextField.gridx = 1;
		gbc_searchTextField.gridy = 0;
		borderPanel.add(searchTextField, gbc_searchTextField);
		searchTextField.setColumns(10);
		searchTextField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					startSearch();
				}
			}
		});		
		
		JButton searchButton = new JButton(new SearchAction());
		searchButton.setMargin(new Insets(0, 8, 0, 8));
		GridBagConstraints gbc_searchButton = new GridBagConstraints();
		gbc_searchButton.fill = GridBagConstraints.VERTICAL;
		gbc_searchButton.insets = new Insets(0, 0, 5, 0);
		gbc_searchButton.gridx = 2;
		gbc_searchButton.gridy = 0;
		borderPanel.add(searchButton, gbc_searchButton);
		
		scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		borderPanel.add(scrollPane, gbc_scrollPane);
		scrollPane.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				if(!e.getValueIsAdjusting()) {
					scrollPane.repaint();
				}
			}
		});

		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 3;
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		borderPanel.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{287, 69, 0, 0};
		gbl_panel.rowHeights = new int[]{25, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JButton abortButton = new JButton(Bundle.getString("ImageDownloadDialog.Action.Cancel"));
		abortButton.setMargin(new Insets(2, 8, 2, 8));
		GridBagConstraints gbc_abortButton = new GridBagConstraints();
		gbc_abortButton.anchor = GridBagConstraints.NORTHEAST;
		gbc_abortButton.insets = new Insets(0, 0, 0, 5);
		gbc_abortButton.gridx = 1;
		gbc_abortButton.gridy = 0;
		panel.add(abortButton, gbc_abortButton);
		abortButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				closeDialog(null);
			}
		});
		
		okButton = new JButton(Bundle.getString("ImageDownloadDialog.Action.OK"));
		GridBagConstraints gbc_okButton = new GridBagConstraints();
		gbc_okButton.anchor = GridBagConstraints.EAST;
		gbc_okButton.gridx = 2;
		gbc_okButton.gridy = 0;
		panel.add(okButton, gbc_okButton);
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				storeSelectionAndCloseDialog();
			}
		});
	}
	
	/**
	 * Stores the selection for providing it for the {@link #getSelectedImage()} method
	 * and closes this {@link ImageDownloadDialog} instance.
	 * @see #closeDialog(IResourceHandler)
	 */
	private void storeSelectionAndCloseDialog() {
		SearchResultPanel view = (SearchResultPanel) scrollPane.getViewport().getView();
		if(view != null) {
			final int selectedColumn = view.getSelectedColumn();
			final IImageFetcherEntry imageFetcher = (IImageFetcherEntry) view.getModel().getValueAt(0, selectedColumn);
			if(imageFetcher != null) {
				try {
					URL imageURL = imageFetcher.getImageURL();
					IResourceHandler image = ResourceHandlerFactory.getResourceLoader(imageURL);
					closeDialog(image);
				} catch (Exception e1) {
					LoggerFactory.getLogger(this).log(Level.WARNING, "Could not fetch image from " + imageFetcher.getImageURL());
				}
			}
		}
	}
	
	/**
	 * Closes and disposes this {@link ImageDownloadDialog} instance. 
	 * @param selectedImage The image to be set as result.
	 */
	private void closeDialog(IResourceHandler selectedImage) {
		this.selectedImage = selectedImage;
		setVisible(false);
		dispose();			
	}
	
	private void startSearch() {
		getGlassPane().setVisible(true);
		new SwingWorker<SearchResultPanel, SearchResultPanel>() {
			
			@Override
			protected SearchResultPanel doInBackground() throws Exception {
				SearchResultPanel searchResultPanel = new SearchResultPanel(searchTextField.getText(), searchProviderComboBox.getSelectedItem().toString());
				return searchResultPanel;
			}
	
			@Override
			protected void done() {
				try {
					SearchResultPanel searchResultPanel = get();
					scrollPane.setViewportView(searchResultPanel);
					getGlassPane().setVisible(false);
				} catch (Exception e) {
					LoggerFactory.getLogger().log(Level.WARNING, "Error while setting search reuslts.", e);
				}
			}
		}.execute();
	}

	private class SearchAction extends AbstractAction {

		private SearchAction() {
			final URL resource = ImageDownloadDialog.class.getResource("resources/play_16.gif");
			putValue(Action.SMALL_ICON, new ImageIcon(resource));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			startSearch();
		}
		
	}
	
	private class SearchResultPanel extends JTable {
		
		private Map<URL, ImageIcon> imageCache = Collections.synchronizedMap(new HashMap<URL, ImageIcon>(30));
		
		private TableCellRenderer renderer = new SearchResultTableRenderer();
		
		private SearchResultPanel(String searchTerm, String searchProviderName) {
			this.setLayout(new FlowLayout(FlowLayout.LEFT));
			this.setRowHeight(cellSize.height);
			this.setTableHeader(null);
			this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			this.setShowGrid(false);
		    this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		    
			this.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						storeSelectionAndCloseDialog();
					}
				}
			});
			
			this.setModel(new SearchResultTableModel(searchTerm, searchProviderName));
			this.setDefaultRenderer(Object.class, renderer);
			
			int columnCount = this.getModel().getColumnCount();
			for (int i = 0; i < columnCount; i++) {
				TableColumn column = this.getColumnModel().getColumn(i);
				column.setPreferredWidth(cellSize.width);
			}
		}
		
		/**
		 * Creates an {@link ImageIcon} from the given {@link IImageFetcherEntry}.
		 * @param entry The {@link IImageFetcherEntry} containing the url for the thumbnail image.
		 * @param thumbnailURL
		 * @return
		 * @throws IOException
		 */
		private ImageIcon createThumbnail(IImageFetcherEntry entry) throws IOException {
			final URL thumbnailURL = entry.getThumbnailURL();

			ImageIcon imageIcon = imageCache.get(thumbnailURL);
			if(imageIcon == null) {
				byte[] thumbnailImageBytes = entry.getThumbnailImageBytes();
				IImageProvider imageProvider = ImageProviderFactory.getImageProvider(ResourceHandlerFactory.getResourceLoader(new ByteArrayInputStream(thumbnailImageBytes)));
				BufferedImage image = imageProvider.getImage();
				BufferedImage scaleToMatch = ImageUtils.scaleToMatch(image, cellSize, true);
				imageIcon = new ImageIcon(scaleToMatch);
				imageCache.put(thumbnailURL, imageIcon);			
			}
			return imageIcon;
		}
		
		private class SearchResultTableRenderer extends JPanel implements TableCellRenderer {
			private final short[] invertTable;
			{
				invertTable = new short[256];
				for (int i = 0; i < 256; i++) {
					invertTable[i] = (short) (255 - i);
				}
			}		
			
			private JLabel sizeLabel;
			
			private JLabel imageLabel;
			
			private SearchResultTableRenderer() {
				//init
				this.setLayout(new BorderLayout());
				this.setBorder(new EmptyBorder(0, 3, 0, 3));
				
				sizeLabel = new JLabel();
				sizeLabel.setOpaque(false);
				sizeLabel.setHorizontalAlignment(SwingConstants.CENTER);
				this.add(sizeLabel, BorderLayout.SOUTH);
				
				imageLabel = new JLabel();
				imageLabel.setOpaque(false);
				imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
				this.add(imageLabel, BorderLayout.CENTER);
			}

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				if(table.getSelectedColumn() == column) {
					setBackground(selectedBgColor);
					sizeLabel.setForeground(selectedFgColor);
					imageLabel.setForeground(selectedFgColor);					
				} else {
					setBackground(bgColor);
					sizeLabel.setForeground(fgColor);
					imageLabel.setForeground(fgColor);
				}
				
				int imageHeight = ((IImageFetcherEntry)value).getImageHeight();
				int imageWidth = ((IImageFetcherEntry)value).getImageWidth();
				
				sizeLabel.setText(imageWidth + "x" + imageHeight);
				try {
					ImageIcon imageIcon = createThumbnail((IImageFetcherEntry) value);
					if(table.getSelectedColumn() == column) {
						BufferedImage invertedThumbnail = invertImage((BufferedImage) imageIcon.getImage());
						imageIcon = new ImageIcon(invertedThumbnail);
					}
					imageLabel.setIcon(imageIcon);
				} catch (IOException e) {
					LoggerFactory.getLogger(this).log(Level.WARNING, "Images could not be retrieved.", e);
				}
				return this;
			}
			
			private BufferedImage invertImage(final BufferedImage src) {
				final int w = src.getWidth();
				final int h = src.getHeight();
				final BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
				 
				final BufferedImageOp invertOp = new LookupOp(new ShortLookupTable(0, invertTable), null);
				return invertOp.filter(src, dst);
				}			
		}
		
		/**
		 * Model loading and providing the images from the selected search provider.
		 */
		private class SearchResultTableModel extends AbstractTableModel {
			
			final List<IImageFetcherEntry> thumbnailEntries = Collections.synchronizedList(new ArrayList<IImageFetcherEntry>());
			
			private SearchResultTableModel(String searchTerm, String searchProviderName) {
				try { //init
					final IImageFetcher imageFetcher = factory.getImageFetcher(searchProviderName, searchTerm);
					final FeedEngine<List<IImageFetcherEntry>> engine = new FeedEngine<List<IImageFetcherEntry>>();
					final Producer<List<IImageFetcherEntry>> producer = new Producer<List<IImageFetcherEntry>>() {
						
						private int invoked = 0;
						
						@Override
						public List<IImageFetcherEntry> produce() throws Exception {
							try {
								synchronized(this) {
									//Calculate the number of needs to fetch the desired number of thumbnails.
									invoked++;
									if(imageFetcher.getPageSize() < getMaxDisplayedThumbnails()) {
										if((invoked - 1) * imageFetcher.getPageSize() >= getMaxDisplayedThumbnails() + (getMaxDisplayedThumbnails() % imageFetcher.getPageSize())) {
											return null;
										}										
									} else {
										if(invoked > 1) {
											return null;
										}
									}
								}
							
								List<IImageFetcherEntry> imageFetcherEntries = imageFetcher.getNextEntries();
								return imageFetcherEntries;
							} catch (Exception e) {
								LoggerFactory.getLogger().log(Level.WARNING, "Page fetching failed.", e);
							}
							return null;
						}
					};
					engine.addProducer(producer);
					engine.addProducer(producer);
					engine.addProducer(producer);
					
					final Consumer<List<IImageFetcherEntry>> consumer = new Consumer<List<IImageFetcherEntry>>() {

						@Override
						public void close() {
						}

						@Override
						public void consume(List<IImageFetcherEntry> imageFetcherEntries) throws Exception {
							thumbnailEntries.addAll(imageFetcherEntries);
							for (IImageFetcherEntry entry : imageFetcherEntries) {
								createThumbnail(entry);
							}
						}
					};
					engine.addConsumer(consumer);
					engine.addConsumer(consumer);
					engine.addConsumer(consumer);
					
					engine.runUntilAllProcessed();
				} catch (Exception e) {
					LoggerFactory.getLogger(this).log(Level.WARNING, "Images could not be retrieved.", e);
				}
			}

			@Override
			public int getRowCount() {
				return 1;
			}

			@Override
			public int getColumnCount() {
				return thumbnailEntries.size();
			}


			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				return thumbnailEntries.get(columnIndex);
			}
		}
	}

	public String getSearchPhrase() {
		return this.searchTextField.getText();
	}

	public void setSearchPhrase(String searchPhrase) {
		this.searchTextField.setText(searchPhrase);
	}
	
	public IResourceHandler getSelectedImage() {
		return selectedImage;
	}

	public int getMaxDisplayedThumbnails() {
		return resultCount;
	}

	public void setResultCount(int resultCount) {
		this.resultCount = resultCount;
	}

	public static void main(String[] args) {
		ImageDownloadDialog imageDownloadDialog = new ImageDownloadDialog(ImageFetcherFactory.getInstance());
		imageDownloadDialog.setVisible(true);
		System.out.println(imageDownloadDialog.getSelectedImage());
		System.exit(0);
	}
}