package dk.statsbiblioteket.newspaper.domsenricher.component.enrichers;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.FedoraRelation;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 *
 */
public class GenericNodeEnricherIT  {

    private static Logger logger = LoggerFactory.getLogger(GenericNodeEnricherTest.class);
    EnhancedFedora fedora;
    private final String HAS_MODEL = "info:fedora/fedora-system:def/model#hasModel";
    public static final String ROUNDTRIP_MODEL = "doms:" + "ContentModel_RoundTrip";
    String batchId = "B123321123-RT12";
    String pid;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        logger.debug("Doing setUp.");
        Properties props = new Properties();
        try {
            props.load(new FileReader(new File(System.getProperty("integration.test.newspaper.properties"))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Credentials creds = new Credentials(props.getProperty(ConfigConstants.DOMS_USERNAME), props.getProperty(ConfigConstants.DOMS_PASSWORD));
        String fedoraLocation = props.getProperty(ConfigConstants.DOMS_URL);
        fedora = new EnhancedFedoraImpl(creds, fedoraLocation, props.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL) , null);
        List<String> pids = fedora.findObjectFromDCIdentifier("path:" + batchId);
        pid = pids.get(0);
        logger.debug("Working on pid {}", pid);
        if (hasRelation(pid, HAS_MODEL, ROUNDTRIP_MODEL)) {
            fedora.deleteRelation(pid, null, HAS_MODEL, ROUNDTRIP_MODEL, false, "" );
        }
        assertFalse(hasRelation(pid, HAS_MODEL, ROUNDTRIP_MODEL));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() throws Exception {
        fedora.deleteRelation(pid, null, HAS_MODEL, ROUNDTRIP_MODEL, false, "" );
        assertFalse(hasRelation(pid, HAS_MODEL, ROUNDTRIP_MODEL));
    }

    @Test(groups = "integrationTest")
    public void testEnrich() throws Exception {
        NodeEnricherFactory factory = new NodeEnricherFactory(fedora);
        AbstractNodeEnricher enricher = factory.getNodeEnricher(NodeType.BATCH);
        NodeBeginsParsingEvent event = new NodeBeginsParsingEvent(batchId, pid);
        enricher.enrich(event);
        assertTrue(hasRelation(pid, HAS_MODEL, ROUNDTRIP_MODEL));
    }

    private boolean hasRelation(String pid, String predicate, String object) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        List<FedoraRelation> relations = fedora.getNamedRelations(pid, predicate, (new Date()).getTime()*1000L);
        for (FedoraRelation relation: relations) {
            logger.debug("Found relation {} {} ", relation.getPredicate(), relation.getObject());
            if (relation.getPredicate().equals(predicate) && relation.getObject().equals(object)) {
                return true;
            }
        }
        return false;
    }

}
