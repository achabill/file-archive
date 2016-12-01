package archive.dao;

import archive.model.Document;
import archive.model.DocumentMetadata;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

//import org.apache.log4j.Logger;

/**
 * Data access object to insert, find and load {@link Document}s.
 * <p>
 * FileSystemDocumentDao saves documents in the file system. No database in involved.
 * For each document a folder is created. The folder contains the document
 * and a properties files with the meta data of the document.
 * Each document in the archive has a Universally Unique Identifier (UUID).
 * The name of the documents folder is the UUID of the document.
 *
 * @author Acha Bill <achabill12[at]gmail[dot]com>
 */
@Service("documentDao")
public class FileSystemDocumentDao implements IDocumentDao {

  private static final Logger LOG = Logger.getLogger(FileSystemDocumentDao.class);

  public static String DIRECTORY = "file-archive";
  public static final String META_DATA_FILE_NAME = "metadata.properties";

  @PostConstruct
  public void init() {
    createDirectory(DIRECTORY);
  }

  /**
   * Inserts a document to the archive by creating a folder with the UUID
   * of the document. In the folder the document is saved and a properties file
   * with the meta data of the document.
   */
  @Override
  public void insert(Document document) {
    try {
      createDirectory(document);
      saveFileData(document);
      saveMetaData(document);
    } catch (IOException e) {
      String message = "Error while inserting document";
      LOG.error(message, e);
      throw new RuntimeException(message, e);
    }
  }


  /**
   * Returns the document from the data store with the given UUID.
   *
   * @see archive.dao.IDocumentDao#load(java.lang.String)
   */
  @Override
  public Document load(String uuid) {
    try {
      return loadFromFileSystem(uuid);
    } catch (IOException e) {
      String message = "Error while loading document with id: " + uuid;
      LOG.error(message, e);
      throw new RuntimeException(message, e);
    }

  }

  /**
   * Returns the document from the data store with the given id setting the path property
   * The document file and meta data is returned.
   * Returns null if no document was found.
   *
   * @param uuid The id of the document
   * @return A document inc. file and meta data
   */
  @Override
  public Document loadWithPath(String uuid) {
    try {
      return loadFromFileSystemWithPath(uuid);
    } catch (IOException e) {
      String message = "Error while loading document with id: " + uuid;
      LOG.error(message, e);
      throw new RuntimeException(message, e);
    }
  }

  /**
   * Sets the filename of the archive directory
   *
   * @param filename The filename of the dir.
   */
  @Override
  public void setArchiveDirectory(String filename) {
    DIRECTORY = filename;
  }

  /**
   * Gets the filename of the archive dir.
   *
   * @return The filename
   */
  @Override
  public String getArchiveDirectory() {
    return DIRECTORY;
  }

  /**
   * Deletes the document with the specified uuid
   *
   * @param uuid The uuid of the document
   * @return The deleted uuid
   */
  @Override
  public String delete(String uuid) throws IOException {

    File f = new File(getDirectoryPath(uuid));
    if(!f.exists()) {
      throw new FileNotFoundException("File not found");
    }else {
      if (FileSystemUtils.deleteRecursively(f)) ;
      return uuid;
    }
  }

  /**
   * Finds documents in the data store matching the given parameter.
   * A list of document meta data is returned which does not include the file data.
   * Use load and the id from the meta data to get the document file.
   * Returns an empty list if no document was found.
   *
   * @param personName  The name of a person, may be null
   * @param date        The date of a document, may be null
   * @param contentType Contenttype
   * @return A list of document meta data
   */
  @Override
  public List<DocumentMetadata> findByPersonNameDateContentType(String personName, Date date, String contentType) {
    try {
      return findInFileSystem(personName, date,contentType);
    } catch (IOException e) {
      String message = "Error while finding document, person name: " + personName + ", date:" + date;
      LOG.error(message, e);
      throw new RuntimeException(message, e);
    }
  }


  private List<DocumentMetadata> findInFileSystem(String personName, Date date, String contentType) throws IOException {
    List<String> uuidList = getUuidList();
    List<DocumentMetadata> metadataList = new ArrayList<DocumentMetadata>(uuidList.size());
    for (String uuid : uuidList) {
      DocumentMetadata metadata = loadMetadataFromFileSystem(uuid);
      if (isMatched(metadata, personName, date,contentType)) {
        metadataList.add(metadata);
      }
    }
    return metadataList;
  }

  private boolean isMatched(DocumentMetadata metadata, String personName, Date date, String contentType) {
    if (metadata == null) {
      return false;
    }
    boolean match = true;
    if (personName != null) {
      match = (personName.equals(metadata.getPersonName()));
    }
    if (match && date != null) {
      match = (date.equals(metadata.getDocumentDate()));
    }
    if(match && contentType != null){
      match = metadata.getContentType().contains(contentType.toLowerCase());
    }
    return match;
  }

  private DocumentMetadata loadMetadataFromFileSystem(String uuid) throws IOException {
    DocumentMetadata document = null;
    String dirPath = getDirectoryPath(uuid);
    File file = new File(dirPath);
    if (file.exists()) {
      Properties properties = readProperties(uuid);
      document = new DocumentMetadata(properties);

    }
    return document;
  }

  private Document loadFromFileSystem(String uuid) throws IOException {
    DocumentMetadata metadata = loadMetadataFromFileSystem(uuid);
    if (metadata == null) {
      return null;
    }
    Path path = Paths.get(getFilePath(metadata));
    Document document = new Document(metadata);
    document.setFileData(Files.readAllBytes(path));
    return document;
  }

  private Document loadFromFileSystemWithPath(String uuid) throws IOException {
    DocumentMetadata metadata = loadMetadataFromFileSystem(uuid);
    if (metadata == null) {
      return null;
    }
    Path path = Paths.get(getFilePath(metadata));
    Document document = new Document(metadata);
    document.setPath(path);
    return document;
  }

  private String getFilePath(DocumentMetadata metadata) {
    String dirPath = getDirectoryPath(metadata.getUuid());
    StringBuilder sb = new StringBuilder();
    sb.append(dirPath).append(File.separator).append(metadata.getFileName());
    return sb.toString();
  }

  private void saveFileData(Document document) throws IOException {
    String path = getDirectoryPath(document);
    BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(new File(path), document.getFileName())));
    stream.write(document.getFileData());
    stream.close();
  }

  public void saveMetaData(Document document) throws IOException {
    String path = getDirectoryPath(document);
    Properties props = document.createProperties();
    File f = new File(new File(path), META_DATA_FILE_NAME);
    OutputStream out = new FileOutputStream(f);
    props.store(out, "Document meta data");
  }

  private List<String> getUuidList() {
    File file = new File(DIRECTORY);
    String[] directories = file.list(new FilenameFilter() {
      @Override
      public boolean accept(File current, String name) {
        return new File(current, name).isDirectory();
      }
    });
    return Arrays.asList(directories);
  }

  private Properties readProperties(String uuid) throws IOException {
    Properties prop = new Properties();
    try (InputStream input = new FileInputStream(new File(getDirectoryPath(uuid), META_DATA_FILE_NAME))){
      prop.load(input);
    }catch(FileNotFoundException e){
      e.printStackTrace();
    }catch (IOException e){
      e.printStackTrace();
    }
    return prop;
  }

  private String createDirectory(Document document) {
    String path = getDirectoryPath(document);
    createDirectory(path);
    return path;
  }

  private String getDirectoryPath(Document document) {
    return getDirectoryPath(document.getUuid());
  }

  private String getDirectoryPath(String uuid) {
    StringBuilder sb = new StringBuilder();
    sb.append(DIRECTORY).append(File.separator).append(uuid);
    String path = sb.toString();
    return path;
  }

  private void createDirectory(String path) {
    File file = new File(path);
    file.mkdirs();
  }

}
