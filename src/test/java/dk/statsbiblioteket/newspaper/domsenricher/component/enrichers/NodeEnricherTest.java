package dk.statsbiblioteket.newspaper.domsenricher.component.enrichers;

import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeWithChildren;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertTrue;

/**
 *
 */
public class NodeEnricherTest {

    private static Logger logger = LoggerFactory.getLogger(NodeEnricherTest.class);
    EnhancedFedora fedora;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        logger.debug("Doing setUp.");
        fedora = mock(EnhancedFedora.class);
    }

    @Test
    public void testEnrich() throws Exception {
        NodeEnricherFactory factory = new NodeEnricherFactory(fedora);
        final String pid = "uuid:foobar";
        ParsingEvent event = new NodeBeginsParsingEvent("B400022028241-RT1", pid);
        TreeNodeWithChildren treeNodeWithChildren = new TreeNodeWithChildren(event.getName(), NodeType.BATCH, null, event.getLocation());
        NodeEnricher enricher = factory.getNodeEnricher(treeNodeWithChildren);
        List<String> contentModels = enricher.getAllContentModels();
        assertTrue(contentModels.contains(NodeEnricher.DOMS_CONTENT_MODEL_ROUND_TRIP), contentModels.toString());
    }
}
