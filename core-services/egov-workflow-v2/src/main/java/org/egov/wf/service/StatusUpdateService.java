package org.egov.wf.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.wf.config.WorkflowConfig;
import org.egov.wf.producer.Producer;
import org.egov.wf.util.WorkflowUtil;
import org.egov.wf.web.models.AuditDetails;
import org.egov.wf.web.models.ProcessInstance;
import org.egov.wf.web.models.ProcessInstanceRequest;
import org.egov.wf.web.models.ProcessStateAndAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;


@Service
public class StatusUpdateService {

    private Producer producer;

    private WorkflowConfig config;

    private WorkflowUtil util;


    @Autowired
    public StatusUpdateService(Producer producer, WorkflowConfig config, WorkflowUtil util) {
        this.producer = producer;
        this.config = config;
        this.util = util;
    }


    /**
     * Updates the status and pushes the request on kafka to persist
      * @param requestInfo
     * @param processStateAndActions
     */
    public void updateStatus(RequestInfo requestInfo,List<ProcessStateAndAction> processStateAndActions){

        for(ProcessStateAndAction processStateAndAction : processStateAndActions){
            if(processStateAndAction.getProcessInstanceFromRequest().getState()!=null){
                String prevStatus = processStateAndAction.getProcessInstanceFromRequest().getState().getUuid();
                processStateAndAction.getProcessInstanceFromRequest().setPreviousStatus(prevStatus);
            }
            processStateAndAction.getProcessInstanceFromRequest().setState(processStateAndAction.getResultantState());
        }
        List<ProcessInstance> processInstances = new LinkedList<>();
        processStateAndActions.forEach(processStateAndAction -> {
            processInstances.add(processStateAndAction.getProcessInstanceFromRequest());
        });
        ProcessInstanceRequest processInstanceRequest = new ProcessInstanceRequest(requestInfo,processInstances);
        producer.push(config.getSaveTransitionTopic(),processInstanceRequest);
    }

    /**
     * Updates the assignee for process instances without creating a new transition entry
     * Pushes the update request to Kafka topic for asynchronous processing by persister
     * 
     * @param requestInfo The RequestInfo of the request
     * @param processStateAndActions List of ProcessStateAndAction containing ProcessInstance to be updated
     */
    public void updateAssignee(RequestInfo requestInfo, List<ProcessStateAndAction> processStateAndActions) {
        List<ProcessInstance> processInstances = new LinkedList<>();
        
        for(ProcessStateAndAction processStateAndAction : processStateAndActions) {
            ProcessInstance processInstanceFromDb = processStateAndAction.getProcessInstanceFromDb();
            ProcessInstance processInstanceFromRequest = processStateAndAction.getProcessInstanceFromRequest();
            
            if(processInstanceFromDb == null) {
                continue;
            }
            
            // Create auditDetails for the update operation, preserving createdBy and createdTime from DB
            AuditDetails auditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), false);
            if(processInstanceFromDb.getAuditDetails() != null) {
                auditDetails.setCreatedBy(processInstanceFromDb.getAuditDetails().getCreatedBy());
                auditDetails.setCreatedTime(processInstanceFromDb.getAuditDetails().getCreatedTime());
            }
            
            // Set the process instance ID from DB and enrich with new assignees
            ProcessInstance updateInstance = ProcessInstance.builder()
                    .id(processInstanceFromDb.getId())
                    .tenantId(processInstanceFromDb.getTenantId())
                    .businessId(processInstanceFromDb.getBusinessId())
                    .businessService(processInstanceFromDb.getBusinessService())
                    .assignes(processInstanceFromRequest.getAssignes())
                    .auditDetails(auditDetails)
                    .build();
            
            processInstances.add(updateInstance);
        }
        
        if(!processInstances.isEmpty()) {
            ProcessInstanceRequest processInstanceRequest = new ProcessInstanceRequest(requestInfo, processInstances);
            producer.push(config.getUpdateAssigneeTopic(), processInstanceRequest);
        }
    }




}
