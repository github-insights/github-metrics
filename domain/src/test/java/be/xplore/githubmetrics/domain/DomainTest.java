package be.xplore.githubmetrics.domain;

import be.xplore.githubmetrics.domain.domain.Repository;
import be.xplore.githubmetrics.domain.domain.WorkflowRun;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DomainTest {

    @Test
    void workflowRunTest() {
        WorkflowRun workflowRun = new WorkflowRun(
                12,
                "Test Workflow Run",
                WorkflowRun.RunStatus.DONE,
                new Repository(123L, "", "", new ArrayList<>())
        );

        assertEquals("Test Workflow Run", workflowRun.getName());
    }

}
