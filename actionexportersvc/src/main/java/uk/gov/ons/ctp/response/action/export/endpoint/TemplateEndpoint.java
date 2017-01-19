package uk.gov.ons.ctp.response.action.export.endpoint;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.export.domain.TemplateExpression;
import uk.gov.ons.ctp.response.action.export.representation.TemplateExpressionDTO;
import uk.gov.ons.ctp.response.action.export.service.TemplateService;

/**
 * The REST endpoint controller for Templates.
 */
@Path("/templates")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class TemplateEndpoint {
  @Inject
  private TemplateService templateService;

  @Inject
  private MapperFacade mapperFacade;

  @Context
  private UriInfo uriInfo;

  /**
   * To retrieve all Templates
   * 
   * @return a list of Templates
   */
  @GET
  @Path("/")
  public List<TemplateExpressionDTO> findAllTemplates() {
    log.debug("Entering findAllTemplates ...");
    List<TemplateExpression> templates = templateService.retrieveAllTemplates();
    List<TemplateExpressionDTO> results = mapperFacade.mapAsList(templates, TemplateExpressionDTO.class);
    return CollectionUtils.isEmpty(results) ? null : results;
  }

  /**
   * To retrieve a specific Template
   * 
   * @param templateName for the specific Template to retrieve
   * @return the specific Template
   * @throws CTPException if no Template found
   */
  @GET
  @Path("/{templateName}")
  public TemplateExpressionDTO findTemplate(@PathParam("templateName") final String templateName) throws CTPException {
    log.debug("Entering findTemplate with {}", templateName);
    TemplateExpression result = templateService.retrieveTemplate(templateName);
    return mapperFacade.map(result, TemplateExpressionDTO.class);
  }

  /**
   * To store a Template
   * 
   * @param templateName the Template name
   * @param fileContents the Template content
   * @return 201 if created
   * @throws CTPException if the Template can't be stored
   */
  @POST
  @Path("/{templateName}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response storeTemplate(@PathParam("templateName") final String templateName,
      @FormDataParam("file") InputStream fileContents) throws CTPException {
    log.debug("Entering storeTemplate with templateName {}", templateName);
    TemplateExpression template = templateService.storeTemplate(templateName, fileContents);

    UriBuilder ub = uriInfo.getAbsolutePathBuilder();
    URI templateUri = ub.build();
    TemplateExpressionDTO templateDTO = mapperFacade.map(template, TemplateExpressionDTO.class);
    return Response.created(templateUri).entity(templateDTO).build();
  }
}
