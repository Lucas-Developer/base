/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

import java.util.Set;
import java.util.UUID;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandStatus;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.impl.commandrunner.CommandImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

/**
 *
 * @author dilshat
 */
public class CommandImplTest {

    private final String SOME_DUMMY_OUTPUT = "some dummy output";

    private final UUID agentUUID = UUID.randomUUID();
    private CommandImpl command;

    @Before
    public void beforeMethod() {
        Set<Agent> agents = MockUtils.getAgents(agentUUID);
        RequestBuilder requestBuilder = MockUtils.getRequestBuilder("pwd", 1, agents);
        command = new CommandImpl(requestBuilder, agents);
    }

    @Test(expected = NullPointerException.class)
    public void constructorShouldFailNullBuilder() {
        new CommandImpl(null, mock(Set.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorShouldFailNullAgents() {
        new CommandImpl(mock(RequestBuilder.class), null);
    }

    @Test
    public void shouldReturnSameNumberOfRequestAsAgents() {

        assertEquals(1, command.getRequests().size());
    }

    @Test
    public void shouldCompleteCommand() {

        command.appendResult(MockUtils.getTimedOutResponse(agentUUID, command.getCommandUUID()));

        assertTrue(command.hasCompleted());
    }

    @Test
    public void shouldSucceedCommandStatus() {

        command.appendResult(MockUtils.getSucceededResponse(agentUUID, command.getCommandUUID()));

        assertEquals(CommandStatus.SUCCEEDED, command.getCommandStatus());
    }

    @Test
    public void shouldFailCommandStatus() {

        command.appendResult(MockUtils.getFailedResponse(agentUUID, command.getCommandUUID()));

        assertEquals(CommandStatus.FAILED, command.getCommandStatus());
    }

    @Test
    public void shouldCollectCommandOutput() {

        Response response = MockUtils.getIntermediateResponse(agentUUID, command.getCommandUUID());
        when(response.getStdOut()).thenReturn(SOME_DUMMY_OUTPUT);
        
        command.appendResult(response);

        assertEquals(SOME_DUMMY_OUTPUT, command.getResults().get(agentUUID).getStdOut());
    }

    @Test
    public void shouldCollectAllCommandOutput() {

        Response response = MockUtils.getIntermediateResponse(agentUUID, command.getCommandUUID());
        when(response.getStdOut()).thenReturn(SOME_DUMMY_OUTPUT);
        
        command.appendResult(response);
        command.appendResult(response);

        assertEquals(SOME_DUMMY_OUTPUT + SOME_DUMMY_OUTPUT, command.getResults().get(agentUUID).getStdOut());
    }

}
