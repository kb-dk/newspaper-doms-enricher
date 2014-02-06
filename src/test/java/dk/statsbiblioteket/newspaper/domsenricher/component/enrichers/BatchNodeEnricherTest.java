package dk.statsbiblioteket.newspaper.domsenricher.component.enrichers;

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
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class BatchNodeEnricherTest {

    private static Logger logger = LoggerFactory.getLogger(BatchNodeEnricherTest.class);

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        logger.debug("Doing setUp.");
    }

    @Test
    public void testEnrich() throws Exception {
        EnhancedFedora fedora = mock(EnhancedFedora.class);
        AbstractNodeEnricher enricher = new BatchNodeEnricher(fedora);
        final String pid = "uuid:foobar";
        ParsingEvent event = new NodeBeginsParsingEvent("B400022028241-RT1", pid);
        enricher.enrich(event);
        verify(fedora, times(2)).addRelation( eq(pid), (String) isNull(), anyString(), anyString(), eq(false), anyString());
    }
}
