package dk.statsbiblioteket.newspaper.domsenricher.component.enrichers;

import dk.statsbibliokeket.newspaper.treenode.NodeType;
import dk.statsbibliokeket.newspaper.treenode.TreeNodeWithChildren;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertTrue;

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
        final String pid = "uuid:foobar";
        ParsingEvent event = new NodeBeginsParsingEvent("B400022028241-RT1", pid);
        TreeNodeWithChildren treeNodeWithChildren = new TreeNodeWithChildren(event.getName(), NodeType.BATCH, null, event.getLocation());
        AbstractNodeEnricher enricher = factory.getNodeEnricher(treeNodeWithChildren);
        List<String> contentModels = enricher.getAllContentModels();
        assertTrue(contentModels.contains("doms:ContentModel_RoundTrip"), contentModels.toString());
    }
}
