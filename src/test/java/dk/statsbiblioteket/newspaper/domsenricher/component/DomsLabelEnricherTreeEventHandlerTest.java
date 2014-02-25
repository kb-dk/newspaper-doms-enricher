package dk.statsbiblioteket.newspaper.domsenricher.component;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;

/**
 *
 */
public class DomsLabelEnricherTreeEventHandlerTest {
    private ResultCollector resultCollector;
    private EnhancedFedora fedora;


    @BeforeMethod(alwaysRun = true)
    public void initialize() {
        fedora = mock(EnhancedFedora.class);
        resultCollector = new ResultCollector("blah", "blah");
    }


    @Test
    public void testprocessNodeEndPage() throws Exception {
        TreeEventHandler labelHandler = new DomsLabelEnricherTreeEventHandler(fedora);

        labelHandler.handleNodeBegin(new NodeBeginsParsingEvent("batch1"));
        labelHandler.handleNodeBegin(new NodeBeginsParsingEvent("film1"));
        labelHandler.handleNodeBegin(new NodeBeginsParsingEvent("edition1"));

        NodeBeginsParsingEvent page = new NodeBeginsParsingEvent("page1");

        labelHandler.handleNodeBegin(page);

        labelHandler.handleNodeEnd(createNodeEndParsingEvent("blabla/mypage"));
        String currentNodePid = page.getLocation();

        verify(fedora).modifyObjectLabel(currentNodePid, "page-" + "mypage", "");
        verifyNoMoreInteractions(fedora);
    }


    private NodeEndParsingEvent createNodeEndParsingEvent(String name) {
        return new NodeEndParsingEvent(name);
    }
}
