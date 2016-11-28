package archive.service;


import archive.model.Document;
import archive.model.DocumentMetadata;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;


/**
 * A service to save, find and get documents from an archive.
 *
 * @author Acha Bill <achabill12[at]gmail[dot]com>
 */
public interface IArchiveService {

  /**
   * Saves a document in the archive.
   *
   * @param document A document
   * @return DocumentMetadata The meta data of the saved document
   */
  DocumentMetadata save(Document document);

  /**
   * Finds document in the archive matching the given parameter.
   * A list of document meta data which does not include the file data.
   * Use getDocumentFile and the id from the meta data to get the file.
   * Returns an empty list if no document was found.
   *
   * @param personName The name of a person, may be null
   * @param date       The date of a document, may be null
   * @return A list of document meta data
   */
  List<DocumentMetadata> findDocuments(String personName, Date date);


  /**
   * Returns the document file from the archive with the given id.
   * Returns null if no document was found.
   *
   * @param id The id of a document
   * @return A document file
   */
  byte[] getDocumentFile(String id);

  /**
   * Returns the document from the archive with the given id in multipart download
   * @param id The id of a document
   */
  Object getDocumentMultipart(String id, HttpServletRequest request, HttpServletResponse response) throws Exception;

  /**
   * Delests the document with the id
   * @param id The id
   * @return The uuid of the deleted document
   */
  String deleteDocument(String id) throws IOException;
}

