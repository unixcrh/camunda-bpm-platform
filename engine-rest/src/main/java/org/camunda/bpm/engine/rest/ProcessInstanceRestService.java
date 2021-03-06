/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest;

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.VariableListDto;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

@Path(ProcessInstanceRestService.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface ProcessInstanceRestService {

  public static final String PATH = "/process-instance";
  
  @GET
  @Path("/{id}")
  ProcessInstanceDto getProcessInstance(@PathParam("id") String processInstanceId);
  
  /**
   * Exposes the {@link ProcessInstanceQuery} interface as a REST service.
   * 
   * @param query
   * @param firstResult
   * @param maxResults
   * @return
   */
  @GET
  List<ProcessInstanceDto> getProcessInstances(@Context UriInfo uriInfo,
      @QueryParam("firstResult") Integer firstResult,
      @QueryParam("maxResults") Integer maxResults);

  /**
   * Expects the same parameters as
   * {@link ProcessInstanceRestService#getProcessInstances(ProcessInstanceQueryDto, Integer, Integer)} (as a JSON message body)
   * and allows for any number of variable checks.
   * 
   * @param query
   * @param firstResult
   * @param maxResults
   * @return
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  List<ProcessInstanceDto> queryProcessInstances(ProcessInstanceQueryDto query,
      @QueryParam("firstResult") Integer firstResult,
      @QueryParam("maxResults") Integer maxResults);

  @GET
  @Path("/count")
  CountResultDto getProcessInstancesCount(@Context UriInfo uriInfo);
  
  @POST
  @Path("/count")
  @Consumes(MediaType.APPLICATION_JSON)
  CountResultDto queryProcessInstancesCount(ProcessInstanceQueryDto query);

  @GET
  @Path("/{id}/variables")
  VariableListDto getVariables(@PathParam("id") String processInstanceId);
}
