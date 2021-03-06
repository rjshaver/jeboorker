package org.rr.jeborker.db.item;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.io.FilenameUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.ReflectionFailureException;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.db.IDBObject;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(daoClass = EbookPropertyItemDaoImpl.class)
public class EbookPropertyItem implements IDBObject, Serializable {
	
	private static final long serialVersionUID = -4301328577306625467L;
	
	@DatabaseField(index = true)
	@ViewField(name = "Created at", orderPriority = 0)
	@ProtectedField
	private Date createdAt;

	/**
	 * File name and path from the ebook file.
	 */
	@DatabaseField(id = true, index = true, width = Integer.MAX_VALUE)
	@ViewField(name = "file name", orderPriority = 0)
	@ProtectedField
	private String file;
	
	/**
	 * The file name without path
	 */
	@DatabaseField(index = true, width = 1024)
	private String fileName;
	
	/**
	 * The base path of the ebook file.
	 */
	@DatabaseField(index = true, width = Integer.MAX_VALUE)
	@ViewField(name = "Base Path", orderPriority = 0)
	@ProtectedField
	private String basePath;
	
	/**
	 * The mime type of the ebook file.
	 */
	@DatabaseField(index = true, width = 128)
	@ViewField(name = "Mime type", orderPriority = 0)
	@ProtectedField
	private String mimeType;
	
	/**
	 * Title of the ebook. This property is ready from the ebook meta data.
	 */
	@DatabaseField(index = true, width = Integer.MAX_VALUE)
	@ViewField(name = "Title", orderPriority = 99)
	private String title;
	
	/**
	 * Language of the ebook
	 */
	@DatabaseField(index = true, width = 256)
	@ViewField(name = "Language", orderPriority = 30)
	private String language;
	
	/**
	 * The publishing / release date of the ebook.
	 */
	@DatabaseField(index = true)
	@ViewField(name = "Publishing Date", orderPriority = 50)
	private Date publishingDate;
	
	/**
	 * The date when the ebook was created.
	 */
	@DatabaseField(index = true)
	@ViewField(name = "Creation Date", orderPriority = 50)
	private Date creationDate;	

	/**
	 * One of the authors of the ebook.
	 */
	@DatabaseField(index = true, width = Integer.MAX_VALUE)
	@ViewField(name = "Author", orderPriority = 101)
	private String author;
	
	/**
	 * The author's name in a good sortable manner (last name first if possible)
	 */
	@DatabaseField(index = true, width = Integer.MAX_VALUE)
	@ViewField(name = "Author Sort", orderPriority = 100)
	private String authorSort;	

	/**
	 * Epub Identifier. This is a UUID value
	 */
	@DatabaseField(index = true, width = 128)
	private String uuid;
	
	/**
	 * ISBN number of the ebook
	 */
	@DatabaseField(index = true, width = 52)
	@ViewField(name = "ISBN", orderPriority = 50)
	private String isbn;
	
	/**
	 * Description / summary of the book.
	 */
	@DatabaseField(index = true, width = Integer.MAX_VALUE)
	@ViewField(name = "Description", orderPriority = 20)
	private String description;
	
	/**
	 * Just some keywords for the book. Primary used with pdf.
	 */
	@ViewField(name = "Keywords", orderPriority = 0)
	@DatabaseField(width = Integer.MAX_VALUE)
	private String keywords;
	
	/**
	 * publisher of the ebook.
	 */
	@DatabaseField(index = true, width = Integer.MAX_VALUE)
	@ViewField(name = "Publisher", orderPriority = 80)
	private String publisher;
	
	/**
	 * The subject is for example "Belletristik/Krimis, Thriller, Spionage"
	 */
	@DatabaseField(index = true, width = Integer.MAX_VALUE)
	@ViewField(name = "Genre", orderPriority = 90)
	private String genre;
	
	/**
	 * If the ebook is part of a series like a trilogy, the name of the serie could be stored here.
	 */
	@DatabaseField(index = true, width = Integer.MAX_VALUE)
	@ViewField(name = "Series name", orderPriority = 90)
	private String seriesName;
	
	/**
	 * If the ebook is part of a series like a trilogy, the number of the serie could be stored here.
	 */
	@DatabaseField(index = true, width = 256)
	@ViewField(name = "Series index", orderPriority = 89)
	private String seriesIndex;
	
	/**
	 * The book rating. We use a 0.00 digit schema here. 
	 */
	@DatabaseField(index = true)
	@ViewField(name = "Rating", orderPriority = 95)
	private Integer rating;
	
	/**
	 * Something like "All rights reserved" 
	 */
	@DatabaseField(width = Integer.MAX_VALUE)
	@ViewField(name = "Rights", orderPriority = 10)
	private String rights;
	
	/**
	 * The release scope for the book. For example "Germany"
	 */
	@DatabaseField(index = true, width = Integer.MAX_VALUE)
	@ViewField(name = "Coverage", orderPriority = 10)
	private String coverage;
	
	/**
	 * age suggestion. Something like '12-13' or simple '12'.
	 */
	@DatabaseField(index = true, width = Integer.MAX_VALUE)
	@ViewField(name = "Age suggestion", orderPriority = 80)
	private String ageSuggestion;
	
	/**
	 * Timestamp of the ebook file. 
	 */
	@DatabaseField(index = true)
	private long timestamp = 0l;
	
	public EbookPropertyItem() {
		super();
	}
	
	@Override
	public String toString() {
		return this.getFile();
	}
	
	/**
	 * Creates an {@link IResourceHandler} for the ebook file. This is just 
	 * a convenience method for getting a IResourceHandler for this {@link EbookPropertyItem} instance.
	 * @return The desired {@link IResourceHandler}.
	 */
	public IResourceHandler getResourceHandler() {
		return ResourceHandlerFactory.getResourceHandler(this.getFile());
	}
	
	/**
	 * Get the file name without the path. The file name is created from the
	 * {@link #getFile()} property.
	 * 
	 * @return the file name without path statement.
	 */
	public String getFileName() {
		String file = this.getFile();
		if(file!=null && file.indexOf('/')!=-1) {
			return file.substring(file.lastIndexOf('/')+1);
		} else if(file!=null && file.indexOf('\\')!=-1) {
			return file.substring(file.lastIndexOf('\\')+1);
		} else if(file!=null){
			return file;
		} else {
			return EMPTY;
		}
	}	
	
    public boolean equals(Object obj) {
    	if(this.getFile() == null) {
    		return false;
    	}
    	
    	if(obj instanceof EbookPropertyItem) {
    		return this.getFile().equals(((EbookPropertyItem)obj).getFile());
    	}
    	return false;
    }	
    
	/**
	 * Clears all metadata excepting this ones which have a {@link ProtectedField} annotation.
	 */
	public void clearMetadata() {
		List<Field> dbViewFields = ReflectionUtils.getFieldsByAnnotation(ViewField.class, EbookPropertyItem.class);
		for (Field field : dbViewFields) {
			try {
				if(field.getAnnotation(ProtectedField.class) == null) {
					String setter = "set" + StringUtil.capitalize(field.getName());
					try {
						ReflectionUtils.invokeMethod(this, setter, new Object[] { null });
					} catch (Exception e) {
						LoggerFactory.logWarning(this, "Clear for field " + field.getName() + " with setter " + setter + " for " + file + " has failed." , e);
						
						if(e instanceof ReflectionFailureException) {
							Throwable cause = ((ReflectionFailureException)e).getCause();
							if(cause instanceof java.lang.reflect.InvocationTargetException) {
								LoggerFactory.logWarning(this, "Cause" , ((java.lang.reflect.InvocationTargetException)cause).getCause());
							}
						}

					}
				}
			} catch (Exception e) {
				LoggerFactory.log(Level.SEVERE, this, "could not clear EbookPropertyItem field " + field.getName(), e);
			}
		}
	}  

	public String getFile() {
		return this.file;
	}
	
	public void setFile(String file) {
		this.file = StringUtil.replace(file, "/", File.separator);
		this.file = StringUtil.replace(this.file, "\\", File.separator);
		this.fileName = file != null ? FilenameUtils.getName(file) : null;
	}

	public String getTitle() {
		return title != null ? title.trim() : title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
	
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}		
	
	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getIsbn() {
		//urn:isbn:
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSeriesName() {
		return seriesName;
	}

	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;
	}

	public String getSeriesIndex() {
		return seriesIndex;
	}

	public void setSeriesIndex(String seriesIndex) {
		this.seriesIndex = seriesIndex;
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public String getRights() {
		return rights;
	}

	public void setRights(String rights) {
		this.rights = rights;
	}

	public String getCoverage() {
		return coverage;
	}

	public void setCoverage(String coverage) {
		this.coverage = coverage;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public List<String> getKeywords() {
		if(keywords != null) {
			return ListUtils.split(keywords, ",");
		}
		return Collections.emptyList();
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = ListUtils.join(keywords, ",");
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getAgeSuggestion() {
		return ageSuggestion;
	}

	public void setAgeSuggestion(String ageSuggestion) {
		this.ageSuggestion = ageSuggestion;
	}

	public Date getPublishingDate() {
		return publishingDate;
	}

	public void setPublishingDate(Date publishingDate) {
		this.publishingDate = publishingDate;
	}

	public String getAuthorSort() {
		return authorSort;
	}

	public void setAuthorSort(String authorSort) {
		if(StringUtil.isNotEmpty(authorSort)) {
			authorSort = authorSort.trim();
			int lastSpaceIdx = authorSort.lastIndexOf(' ');
			if(lastSpaceIdx != -1) {
				this.authorSort = authorSort.substring(lastSpaceIdx).trim();
				return; //done
			}
		}
		this.authorSort = authorSort;
	}
	
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}	

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
