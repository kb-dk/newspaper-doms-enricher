package dk.statsbiblioteket.newspaper.domsenricher.component.enrichers;

import dk.statsbibliokeket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 */
public class GenericNodeEnricherTest {

    private static Logger logger = LoggerFactory.getLogger(GenericNodeEnricherTest.class);
    EnhancedFedora fedora;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        logger.debug("Doing setUp.");
        fedora = mock(EnhancedFedora.class);
    }

    @Test
    public void testEnrich() throws Exception {
        NodeEnricherFactory factory = new NodeEnricherFactory(fedora);
        AbstractNodeEnricher enricher = factory.getNodeEnricher(NodeType.BATCH);
        final String pid = "uuid:foobar";
        ParsingEvent event = new NodeBeginsParsingEvent("B400022028241-RT1", pid);
        enricher.enrich(event);
        verify(fedora, times(2)).addRelation( eq(pid), (String) isNull(), anyString(), anyString(), eq(false), anyString());
    }
}
