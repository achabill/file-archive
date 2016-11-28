package archive.controller;


import archive.model.Document;
import archive.model.DocumentMetadata;
import archive.service.IArchiveService;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

//import org.apache.log4j.Logger;

/**
 * REST web service for archive service {@link IArchiveService}.
 * <p>
 * /archive/upload?file={file}&person={person}&date={date}  Add a document  POST
 * file: A file posted in a multipart request
 * person: The name of the uploading person
 * date: The date of the document
 * <p>
 * /archive/documents?person={person}&date={date}           Find documents  GET
 * person: The name of the uploading person
 * date: The date of the document
 * <p>
 * /archive/document/{id}                                   Get a document  GET
 * id: The UUID of a document
 * <p>
 * All service calls are delegated to instances of {@link IArchiveService}.
 *
 * @author Acha Bill <achabill12[at]gmail[dot]com>
 */
@Controller
@RequestMapping(value = "/archive")
public class ArchiveController {

  private static final Logger LOG = Logger.getLogger(ArchiveController.class);

  @Autowired
  IArchiveService archiveService;

  /**
   * Adds a document to the archive.
   * <p>
   * Url: /archive/upload?file={file}&person={person}&date={date} [POST]
   *
   * @param file   A file posted in a multipart request
   * @param person The name of the uploading person
   * @param date   The date of the document
   * @return The meta data of the added document
   */
  @RequestMapping(value = "/upload", method = RequestMethod.POST)
  @ApiOperation(value = "Upload a document", notes = "Adds a document to the archive")
  public
  @ResponseBody
  DocumentMetadata handleFileUpload(
    @RequestParam(value = "file", required = true) MultipartFile file,
    @RequestParam(value = "person", required = true) String person,
    @RequestParam(value = "date", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {

    try {
      Document document = new Document(file.getBytes(), file.getOriginalFilename(), date, person);
      getArchiveService().save(document);
      return document.getMetadata();
    } catch (RuntimeException e) {
      LOG.error("Error while uploading.", e);
      throw e;
    } catch (Exception e) {
      LOG.error("Error while uploading.", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Finds document in the archive. Returns a list of document meta data
   * which does not include the file data. Use getDocument to get the file.
   * Returns an empty list if no document was found.
   * <p>
   * Url: /archive/documents?person={person}&date={date} [GET]
   *
   * @param person The name of the uploading person
   * @param date   The date of the document
   * @return A list of document meta data
   */
  @RequestMapping(value = "/documents", method = RequestMethod.GET)
  @ApiOperation(value = "Find documents in archive", notes = " Returns a list of document meta data\n" +
    "   * which does not include the file data. Use getDocument to get the file.\n" +
    "   * Returns an empty list if no document was found.")
  public HttpEntity<List<DocumentMetadata>> findDocument(
    @RequestParam(value = "person", required = false) String person,
    @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
    HttpHeaders httpHeaders = new HttpHeaders();
    return new ResponseEntity<List<DocumentMetadata>>(getArchiveService().findDocuments(person, date), httpHeaders, HttpStatus.OK);
  }

  /**
   * Returns the document file from the archive with the given UUID.
   * <p>
   * Url: /archive/document/{id} [GET]
   *
   * @param id The UUID of a document
   * @param full Set for full file download. Else, multipart download of the file.
   * @return The document file
   */
  @RequestMapping(value = "/document/{id}", method = RequestMethod.GET)
  @ApiOperation(value = "Get document file", notes = "Returns the document file from the archive with the given UUID.")
  public HttpEntity<?> getDocument(@PathVariable String id,
                                        @RequestParam (value = "full", required = false) String full,
                                        HttpServletRequest request, HttpServletResponse response) throws Exception {
    if(full != null){
      return new ResponseEntity<byte[]>(getArchiveService().getDocumentFile(id), HttpStatus.OK);
    }else{
      getArchiveService().getDocumentMultipart(id,request,response);
      return new ResponseEntity<String>("multipart", HttpStatus.OK);
    }
  }

  /**
   * Deletes the document from the archive with the given UUID.
   * <p>
   *     Url: /archive/document/{id} [DELETE]
   * </p>
   *
   * @param id The UUID of a document
   * @return The UUID of the deleted file.
   * @throws IOException Exception
   */
  @RequestMapping(value = "/document/{id}", method = RequestMethod.DELETE)
  @ApiOperation(value = "Delete document", notes = "Deletes the document with the specified UUID")
  public HttpEntity<String> deleteDocument(@PathVariable String id) throws IOException {
    getArchiveService().deleteDocument(id);
    return new ResponseEntity<String>(id,HttpStatus.ACCEPTED);
  }

  public IArchiveService getArchiveService() {
    return archiveService;
  }

  public void setArchiveService(IArchiveService archiveService) {
    this.archiveService = archiveService;
  }

}
