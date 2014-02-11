package dk.statsbiblioteket.newspaper.domsenricher.component;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.medieplatform.autonomous.AbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.InitialisationException;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.RunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.fedora3.ConfigurableFilter;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.fedora3.IteratorForFedora3;
import dk.statsbiblioteket.newspaper.promptdomsingester.component.RunnablePromptDomsIngester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 *
 */
public class RunnableDomsEnricherTestIT {

    private static Logger logger = LoggerFactory.getLogger(RunnableDomsEnricherTestIT.class);
    private EnhancedFedoraImpl fedora;
    public static final String BATCH_ID = "400022028254";
    public static final int ROUNDTRIP_NO= 1;
    private Batch batch;
    private Properties props;;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception, JAXBException, PIDGeneratorException {
        logger.debug("Doing setUp.");
        props = new Properties();
        try {
            props.load(new FileReader(new File(System.getProperty("integration.test.newspaper.properties"))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Credentials creds = new Credentials(props.getProperty(ConfigConstants.DOMS_USERNAME), props.getProperty(ConfigConstants.DOMS_PASSWORD));
        String fedoraLocation = props.getProperty(ConfigConstants.DOMS_URL);
        fedora = new EnhancedFedoraImpl(creds, fedoraLocation , props.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL) , null);
        batch = new Batch(BATCH_ID, ROUNDTRIP_NO);
        cleanRoundtripFromDoms();
        reingestRundtripInDoms();
    }

    private void reingestRundtripInDoms() {
        props.setProperty(ConfigConstants.ITERATOR_USE_FILESYSTEM, "true");
        RunnablePromptDomsIngester ingester = new RunnablePromptDomsIngester(props, fedora);
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        ingester.doWorkOnBatch(batch, resultCollector);
        assertTrue(resultCollector.isSuccess(), resultCollector.toReport());
    }

    private void cleanRoundtripFromDoms() throws Exception {
        String label = "path:" + batch.getFullID();
        RecursiveFedoraCleaner.cleanFedora(fedora, label, true);
    }


    /**
     * Full recursive enrichment of a (medium) batch.
     * @throws Exception
     */
    @Test(groups = "integrationTest")
    public void testDoWorkOnBatch() throws Exception {
        props.setProperty(ConfigConstants.ITERATOR_USE_FILESYSTEM, "false");
        RunnableDomsEnricher enricher = new RunnableDomsEnricher(props, fedora);
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        enricher.doWorkOnBatch(batch, resultCollector);
        assertTrue(resultCollector.isSuccess(), resultCollector.toReport());
    }
}
