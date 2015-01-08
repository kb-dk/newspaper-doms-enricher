package dk.statsbiblioteket.newspaper.domsenricher.component;

import org.testng.annotations.Test;

import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.newspaper.domsenricher.component.enrichers.NodeEnricher;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Test of the publisher.
 */
public class DomsPublisherEventHandlerTest {

    /**
     * Test normal case. Three objects are found, all are published.
     *
     * @throws Exception
     */
    @Test
    public void testPublishing() throws Exception {
        EnhancedFedora enhancedFedoraMock = mock(EnhancedFedora.class);
        ResultCollector resultCollectorMock = mock(ResultCollector.class);
        DomsPublisherEventHandler domsPublisherEventHandler = new DomsPublisherEventHandler(enhancedFedoraMock,
                                                                                            resultCollectorMock, 1);
        domsPublisherEventHandler.handleNodeBegin(new NodeBeginsParsingEvent("B000000000000-RT1", "uuid:1"));
        domsPublisherEventHandler
                .handleNodeBegin(new NodeBeginsParsingEvent("B000000000000-RT1/000000000000-1", "uuid:2"));
        domsPublisherEventHandler
                .handleNodeBegin(new NodeBeginsParsingEvent("B000000000000-RT1/000000000000-1/1970-01", "uuid:3"));
        domsPublisherEventHandler.handleFinish();
        verify(enhancedFedoraMock).modifyObjectState("uuid:2", "A", NodeEnricher.COMMENT);
        verify(enhancedFedoraMock).modifyObjectState("uuid:3", "A", NodeEnricher.COMMENT);
        verifyNoMoreInteractions(enhancedFedoraMock);
        verifyNoMoreInteractions(resultCollectorMock);
    }

    /**
     * Exceptional case: publishing throws exceptions. Fails after that call, and result is collected.
     *
     * @throws Exception
     */
    @Test
    public void testPublishingWithNoRetries() throws Exception {
        EnhancedFedora enhancedFedoraMock = mock(EnhancedFedora.class);
        BackendMethodFailedException exception = new BackendMethodFailedException("exceptionMessage");
        doThrow(exception).when(enhancedFedoraMock)
                .modifyObjectState("uuid:2", "A", NodeEnricher.COMMENT);
        int tries = 1;
        ResultCollector resultCollectorMock = mock(ResultCollector.class);
        DomsPublisherEventHandler domsPublisherEventHandler = new DomsPublisherEventHandler(enhancedFedoraMock,
                                                                                            resultCollectorMock, 1);
        domsPublisherEventHandler.handleNodeBegin(new NodeBeginsParsingEvent("B000000000000-RT1", "uuid:1"));
        domsPublisherEventHandler
                .handleNodeBegin(new NodeBeginsParsingEvent("B000000000000-RT1/000000000000-1", "uuid:2"));
        domsPublisherEventHandler
                .handleNodeBegin(new NodeBeginsParsingEvent("B000000000000-RT1/000000000000-1/1970-01", "uuid:3"));
        domsPublisherEventHandler.handleFinish();
        verify(enhancedFedoraMock, times(tries)).modifyObjectState("uuid:2", "A", NodeEnricher.COMMENT);
        verify(enhancedFedoraMock).modifyObjectState("uuid:3", "A", NodeEnricher.COMMENT);
        verifyNoMoreInteractions(enhancedFedoraMock);
        verify(resultCollectorMock).addFailure("B000000000000-RT1/000000000000-1", "metadata",
                                               DomsPublisherEventHandler.class.getSimpleName(),
                                               "Could not publish doms object: " + exception.toString());
        verifyNoMoreInteractions(resultCollectorMock);
    }
}
