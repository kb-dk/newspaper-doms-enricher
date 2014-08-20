package dk.statsbiblioteket.newspaper.domsenricher.component;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
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

    /*
    * TODO Testing here can be improved by doing a test on a known structure (as it is done on a number of xml files in the mods
    * metadata checker of newspaper-batch-metadata checker) - checking that it gives the expected labels on all levels.
    * At the same time, this would work as a kind of documentation of how labels will end up looking.
    */

    @Test
    public void testprocessNodeEndPage() throws Exception {
        DefaultTreeEventHandler labelHandler = new DomsLabelEnricherTreeEventHandler(fedora);

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
