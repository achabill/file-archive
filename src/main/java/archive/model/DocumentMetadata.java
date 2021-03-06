package archive.model;


import archive.service.IArchiveService;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
//import org.apache.log4j.Logger;

/**
 * Meta data of a document from an archive managed by {@link IArchiveService}.
 *
 * @author Acha Bill <achabill12[at]gmail[dot]com>
 */
public class DocumentMetadata implements Serializable {

  public static final String PROP_UUID = "uuid";

  private static final Logger LOG = Logger.getLogger(DocumentMetadata.class);
  public static final String PROP_PERSON_NAME = "person-name";
  public static final String PROP_FILE_NAME = "file-name";
  public static final String PROP_DOCUMENT_DATE = "document-date";
  public static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";
  public static final String PROP_CONTENT_TYPE = "content-type";
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PATTERN);
  static final long serialVersionUID = 7283287076019483950L;
  protected String uuid;
  protected String fileName;
  protected Date documentDate;
  protected String personName;
  protected Path path;
  protected String contentType;


  public DocumentMetadata(String fileName, Date documentDate, String personName, String contentType) {
    this(UUID.randomUUID().toString(), fileName, documentDate, personName, contentType);
  }

  public DocumentMetadata(String uuid, String fileName, Date documentDate, String personName, String contentType) {
    super();
    this.uuid = uuid;
    this.fileName = fileName;
    this.documentDate = documentDate;
    this.personName = personName;
    this.contentType = contentType;
  }

  public DocumentMetadata(Properties properties) {
    this(properties.getProperty(PROP_UUID),
      properties.getProperty(PROP_FILE_NAME),
      null,
      properties.getProperty(PROP_PERSON_NAME),
      properties.getProperty(PROP_CONTENT_TYPE));
    String dateString = properties.getProperty(PROP_DOCUMENT_DATE);
    if (dateString != null) {
      try {
        this.documentDate = DATE_FORMAT.parse(dateString);
      } catch (ParseException e) {
        LOG.error("Error while parsing date string: " + dateString + ", format is: yyyy-MM-dd" , e);
      }
    }
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public Date getDocumentDate() {
    return documentDate;
  }

  public void setDocumentDate(Date documentDate) {
    this.documentDate = documentDate;
  }

  public String getPersonName() {
    return personName;
  }

  public void setPersonName(String personName) {
    this.personName = personName;
  }

  public Path getPath(){return path;}

  public void setPath(Path path){this.path = path;}

  public String getContentType(){return contentType;}

  public void setContentType(String contentType){this.contentType = contentType; }

  public Properties createProperties() {
    Properties props = new Properties();
    props.setProperty(PROP_UUID, getUuid());
    props.setProperty(PROP_FILE_NAME, getFileName());
    props.setProperty(PROP_PERSON_NAME, getPersonName());
    props.setProperty(PROP_PERSON_NAME, getPersonName());
    props.setProperty(PROP_CONTENT_TYPE,getContentType());
    props.setProperty(PROP_DOCUMENT_DATE, DATE_FORMAT.format(getDocumentDate()));
    return props;
  }
}
