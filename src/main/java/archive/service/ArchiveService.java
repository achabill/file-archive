package archive.service;


import archive.dao.IDocumentDao;
import archive.dao.MultipartFileSender;
import archive.model.Document;
import archive.model.DocumentMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * A service to save, find and get documents from an archive.
 *
 * @author Acha Bill <achabill12[at]gmail[dot]com>
 */
@Service("archiveService")
public class ArchiveService implements IArchiveService, Serializable {

  private static final long serialVersionUID = 8119784722798361327L;

  @Autowired
  private IDocumentDao DocumentDao;

  /**
   * Saves a document in the archive.
   */
  @Override
  public DocumentMetadata save(Document document) {
    getDocumentDao().insert(document);
    return document.getMetadata();
  }

  /**
   * Finds document in the archive
   */
  @Override
  public List<DocumentMetadata> findDocuments(String personName, Date date, String contentType) {
    return getDocumentDao().findByPersonNameDateContentType(personName, date, contentType);
  }

  /**
   * Returns the document file from the archive
   */
  @Override
  public byte[] getDocumentFile(String id) {
    Document document = getDocumentDao().load(id);
    if (document != null) {
      return document.getFileData();
    } else {
      return null;
    }
  }

  /**
   * Returns the document from the archive with the given id in multipart download
   *  @param id       The id of a document
   * @param request
   * @param response
   */
  @Override
  public Object getDocumentMultipart(String id, HttpServletRequest request, HttpServletResponse response) throws Exception {
    Document document = getDocumentDao().loadWithPath(id);
    if(document != null)
      MultipartFileSender.fromPath(document.getPath()).with(request).with(response).serveResource();
    return null;
  }

  /**
   * Delests the document with the id
   *
   * @param id The id
   * @return The uuid of the deleted document
   */
  @Override
  public String deleteDocument(String id) throws IOException {
    return getDocumentDao().delete(id);
  }


  public IDocumentDao getDocumentDao() {
    return DocumentDao;
  }

  public void setDocumentDao(IDocumentDao documentDao) {
    DocumentDao = documentDao;
  }


}

