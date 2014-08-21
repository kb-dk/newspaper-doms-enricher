package dk.statsbiblioteket.newspaper.domsenricher.component;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.generated.Datastream;
import dk.statsbiblioteket.doms.central.connectors.fedora.generated.DatastreamProblems;
import dk.statsbiblioteket.doms.central.connectors.fedora.generated.Problems;
import dk.statsbiblioteket.doms.central.connectors.fedora.generated.Validation;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.domsenricher.component.util.RecursiveFedoraValidator;
import dk.statsbiblioteket.newspaper.domsenricher.component.util.RecursiveFedoraVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * This was a test used solely to run on a real batch in our test server. It should only be run manually
 */
public class RunnableDomsEnricherTestForRealIT {

    private static Logger logger = LoggerFactory.getLogger(RunnableDomsEnricherTestForRealIT.class);
    private EnhancedFedoraImpl fedora;
    public static String BATCH_ID = "400026952016";
    public static final int ROUNDTRIP_NO= 1;
    private Batch batch;
    private Properties props;

    @BeforeMethod(groups = "integrationTest")
    public void setUp() throws Exception, JAXBException, PIDGeneratorException {
        logger.debug("Doing setUp.");
        props = new Properties();
        try {
            final String propsfile = System.getProperty("integration.test.newspaper.properties");
            logger.debug("Loading properties from {}.", propsfile);
            props.load(new FileReader(new File(propsfile)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Credentials creds = new Credentials(props.getProperty(ConfigConstants.DOMS_USERNAME), props.getProperty(ConfigConstants.DOMS_PASSWORD));
        String fedoraLocation = props.getProperty(ConfigConstants.DOMS_URL);
        fedora = new EnhancedFedoraImpl(creds, fedoraLocation , props.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL) , null);
        batch = new Batch(BATCH_ID, ROUNDTRIP_NO);
        props.setProperty(ConfigConstants.ITERATOR_USE_FILESYSTEM,"false");
        props.setProperty(ConfigConstants.THREADS_PER_BATCH,"4");
    }




    /**
     * Full enrichment of a (medium) batch.
     * @throws Exception
     */
    @Test(groups = "integrationTest", enabled = false)
    public void testDoWorkOnBatch() throws Exception {
        props.setProperty(ConfigConstants.ITERATOR_USE_FILESYSTEM, "" +
                "" +
                "" +
                "false");
        RunnableDomsEnricher enricher = new RunnableDomsEnricher(props, fedora);
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        enricher.doWorkOnBatch(batch, resultCollector);
        assertTrue(resultCollector.isSuccess(), resultCollector.toReport());
        RecursiveFedoraVisitor<Validation> validator = new RecursiveFedoraValidator(fedora);
        Map<String, Validation> validationMap = null;
        validationMap = validator.visitTree("path:" + batch.getFullID(), true);
        int unknownProblems = 0;
        for (Map.Entry<String, Validation> entry: validationMap.entrySet()) {
            String pid = entry.getKey();
            Validation validation = entry.getValue();
            Problems problems = validation.getProblems();
            for (String problem: problems.getProblem()) {
                if (!isKnown(problem)) {
                    unknownProblems++;
                    logger.debug("In {}: {}.", pid, problem);
                }
            }
            DatastreamProblems datastreamProblems = validation.getDatastreamProblems();
            List<Datastream> datastreams = datastreamProblems.getDatastream();
            for (Datastream datastream: datastreams) {
                for (String problem: datastream.getProblem() ) {
                    if (!isKnown(problem)) {
                        unknownProblems++;
                        logger.debug("In {}/datastreams/{}: {}", pid, datastream.getDatastreamID(), problem);
                    }
                }
            }
        }
        assertEquals(unknownProblems, 0, "Require that there should be no unknown problems.");
    }

    /**
     * Problems which are known and which are assumed to be either unimportant or being dealt with elsewhere.
     * @param problem
     * @return
     */
    private boolean isKnown(String problem) {
        return   (problem.contains("EVENTS") && problem.contains("RoundTrip")) ||
                       (problem.contains("FILM") && problem.contains("schema"));
    }

}
