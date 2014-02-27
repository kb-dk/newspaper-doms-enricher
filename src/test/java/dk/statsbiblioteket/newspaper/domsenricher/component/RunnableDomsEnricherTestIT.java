package dk.statsbiblioteket.newspaper.domsenricher.component;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
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
import dk.statsbiblioteket.newspaper.domsenricher.component.util.RecursiveFedoraCleaner;
import dk.statsbiblioteket.newspaper.domsenricher.component.util.RecursiveFedoraValidator;
import dk.statsbiblioteket.newspaper.domsenricher.component.util.RecursiveFedoraVisitor;
import dk.statsbiblioteket.newspaper.promptdomsingester.component.RunnablePromptDomsIngester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.testng.Assert.*;

/**
 *
 */
public class RunnableDomsEnricherTestIT {

    private static Logger logger = LoggerFactory.getLogger(RunnableDomsEnricherTestIT.class);
    private EnhancedFedoraImpl fedora;
    public static String BATCH_ID;
    public static final int ROUNDTRIP_NO= 1;
    private Batch batch;
    private Properties props;;

    @BeforeMethod(alwaysRun = true, groups = "externalTest")
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
        generateTestBatch();
        batch = new Batch(BATCH_ID, ROUNDTRIP_NO);
        props.setProperty(ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER, "target");
        IngestRoundtripInDoms();
    }

    @AfterMethod(alwaysRun=true, groups = "externalTest")
    public void tearDown() throws Exception {
       //cleanRoundtripFromDoms();
    }

    private void generateTestBatch() throws IOException, InterruptedException {
        String testdataDir = System.getProperty("integration.test.newspaper.testdata");
        logger.debug("Reading testdata from " + testdataDir);
        String generateBatchScript = new File(testdataDir).getAbsolutePath() + "/generate-test-batch/bin/generateTestData.sh";
        BATCH_ID = new Date().getTime() + "";
        ProcessBuilder processBuilder = new ProcessBuilder(generateBatchScript);
        Map<String, String> env = processBuilder.environment();
        env.put("outputDir", "target");
        env.put("numberOfFilms", "2");
        env.put("filmNoOfPictures", "10");
        env.put("avisID", "colinstidende");
        env.put("batchID", BATCH_ID);
        env.put("roundtripID", ROUNDTRIP_NO + "");
        env.put("startDate", "1964-03-27");
        env.put("workshiftTargetSerialisedNumber", "000001");
        env.put("workshiftTargetPages", "2");
        env.put("isoTargetPages", "2");
        env.put("pagesPerEdition", "2");
        env.put("editionsPerDate", "2");
        env.put("pagesPerUnmatched", "1");
        env.put("probabilitySplit", "50");
        env.put("probabilityBrik", "20");
        logger.debug("Generating batch: B{}-RT{}", BATCH_ID, ROUNDTRIP_NO);
        Process process = processBuilder.start();
        process.waitFor();
        logger.debug("Batch generation finished.");
    }


    private void IngestRoundtripInDoms() {
        props.setProperty(ConfigConstants.ITERATOR_USE_FILESYSTEM, "true");
        RunnablePromptDomsIngester ingester = new RunnablePromptDomsIngester(props, fedora);
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        ingester.doWorkOnBatch(batch, resultCollector);
        assertTrue(resultCollector.isSuccess(), resultCollector.toReport());
    }

    private void cleanRoundtripFromDoms() throws Exception {
        String label = "path:" + batch.getFullID();
        logger.debug("Cleaning up from '" + label + "'");
        (new RecursiveFedoraCleaner(fedora)).visitTree(label, true);
    }


    /**
     * Full enrichment of a (medium) batch.
     * @throws Exception
     */
    @Test(groups = "externalTest")
    public void testDoWorkOnBatch() throws Exception {
        props.setProperty(ConfigConstants.ITERATOR_USE_FILESYSTEM, "false");
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
        cleanRoundtripFromDoms();
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
