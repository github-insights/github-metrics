package be.xplore.githubmetrics.domain;

import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRun;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRunStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DomainTest {

    @Test
    void workflowRunTest() {
        WorkflowRun workflowRun = new WorkflowRun(
                12,
                "Test Workflow Run",
                WorkflowRunStatus.DONE,
                new Repository(123L, "", "", new ArrayList<>())
        );

        assertEquals("Test Workflow Run", workflowRun.getName());
    }

}
