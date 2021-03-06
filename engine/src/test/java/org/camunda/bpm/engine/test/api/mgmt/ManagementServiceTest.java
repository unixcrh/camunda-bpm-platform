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

package org.camunda.bpm.engine.test.api.mgmt;

import java.util.Date;

import junit.framework.Assert;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cmd.AcquireJobsCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.TableMetaData;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;


/**
 * @author Frederik Heremans
 * @author Falko Menge
 * @author Saeid Mizaei
 * @author Joram Barrez
 */
public class ManagementServiceTest extends PluggableProcessEngineTestCase {

  public void testGetMetaDataForUnexistingTable() {
    TableMetaData metaData = managementService.getTableMetaData("unexistingtable");
    assertNull(metaData);
  }
  
  public void testGetMetaDataNullTableName() {
    try {
      managementService.getTableMetaData(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("tableName is null", re.getMessage());
    }
  }
  
  public void testExecuteJobNullJobId() {
    try {
      managementService.executeJob(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("jobId is null", re.getMessage());
    }
  }
  
  public void testExecuteJobUnexistingJob() {
    try {
      managementService.executeJob("unexistingjob");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("No job found with id", ae.getMessage());
    }
  }
  
  
  @Deployment
  public void testGetJobExceptionStacktrace() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exceptionInJobExecution");
    
    // The execution is waiting in the first usertask. This contains a boundry
    // timer event which we will execute manual for testing purposes.
    Job timerJob = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .singleResult();
    
    assertNotNull("No job found for process instance", timerJob);
    
    try {
      managementService.executeJob(timerJob.getId());
      fail("RuntimeException from within the script task expected");
    } catch(RuntimeException re) {
      assertTextPresent("This is an exception thrown from scriptTask", re.getMessage());
    }
    
    // Fetch the task to see that the exception that occurred is persisted
    timerJob = managementService.createJobQuery()
    .processInstanceId(processInstance.getId())
    .singleResult();
    
    Assert.assertNotNull(timerJob);
    Assert.assertNotNull(timerJob.getExceptionMessage());
    assertTextPresent("This is an exception thrown from scriptTask", timerJob.getExceptionMessage());
    
    // Get the full stacktrace using the managementService
    String exceptionStack = managementService.getJobExceptionStacktrace(timerJob.getId());
    Assert.assertNotNull(exceptionStack);
    assertTextPresent("This is an exception thrown from scriptTask", exceptionStack);    
  }
  
  public void testgetJobExceptionStacktraceUnexistingJobId() {
    try {
      managementService.getJobExceptionStacktrace("unexistingjob");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("No job found with id unexistingjob", re.getMessage());
    }
  }
  
  public void testgetJobExceptionStacktraceNullJobId() {
    try {
      managementService.getJobExceptionStacktrace(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("jobId is null", re.getMessage());
    }
  }
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  public void testSetJobRetries() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exceptionInJobExecution");

    // The execution is waiting in the first usertask. This contains a boundary
    // timer event.
    Job timerJob = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .singleResult();
    
    assertNotNull("No job found for process instance", timerJob);
    assertEquals(JobEntity.DEFAULT_RETRIES, timerJob.getRetries());

    managementService.setJobRetries(timerJob.getId(), 5);

    timerJob = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .singleResult();
    assertEquals(5, timerJob.getRetries());
  }
  
  public void testSetJobRetriesUnexistingJobId() {
    try {
      managementService.setJobRetries("unexistingjob", 5);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("No job found with id 'unexistingjob'.", re.getMessage());
    }
  }
  
  public void testSetJobRetriesEmptyJobId() {
    try {
      managementService.setJobRetries("", 5);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("The job id is mandatory, but '' has been provided.", re.getMessage());
    }
  }
  
  public void testSetJobRetriesJobIdNull() {
    try {
      managementService.setJobRetries(null, 5);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("The job id is mandatory, but 'null' has been provided.", re.getMessage());
    }
  }

  public void testSetJobRetriesNegativeNumberOfRetries() {
    try {
      managementService.setJobRetries("unexistingjob", -1);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("The number of job retries must be a non-negative Integer, but '-1' has been provided.", re.getMessage());
    }
  }

  public void testDeleteJobNullJobId() {
    try {
      managementService.deleteJob(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("jobId is null", re.getMessage());
    }
  }

  public void testDeleteJobUnexistingJob() {
    try {
      managementService.deleteJob("unexistingjob");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("No job found with id", ae.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/mgmt/timerOnTask.bpmn20.xml" })
  public void testDeleteJobDeletion() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerOnTask");
    Job timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();

    assertNotNull("Task timer should be there", timerJob);
    managementService.deleteJob(timerJob.getId());
    
    timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNull("There should be no job now. It was deleted", timerJob);
  }
  
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/mgmt/timerOnTask.bpmn20.xml" })
  public void testDeleteJobThatWasAlreadyAcquired() {
    ClockUtil.setCurrentTime(new Date());
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerOnTask");
    Job timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    
    // We need to move time at least one hour to make the timer executable
    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() + 7200000L));

    // Acquire job by running the acquire command manually
    ProcessEngineImpl processEngineImpl = (ProcessEngineImpl) processEngine;
    AcquireJobsCmd acquireJobsCmd = new AcquireJobsCmd(processEngineImpl.getProcessEngineConfiguration().getJobExecutor());
    CommandExecutor commandExecutor = processEngineImpl.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    commandExecutor.execute(acquireJobsCmd);
    
    // Try to delete the job. This should fail.
    try {
      managementService.deleteJob(timerJob.getId());
      fail();
    } catch (ProcessEngineException e) {
      // Exception is expected
    }
    
    // Clean up
    managementService.executeJob(timerJob.getId());
  }
}
