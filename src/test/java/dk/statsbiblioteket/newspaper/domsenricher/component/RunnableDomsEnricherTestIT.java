package dk.statsbiblioteket.newspaper.domsenricher.component;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
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
import java.util.Map;
import java.util.Properties;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

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
        generateTestBatch();
        batch = new Batch(BATCH_ID, ROUNDTRIP_NO);
        props.setProperty(ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER, "target");
        IngestRoundtripInDoms();
    }

    @AfterMethod(alwaysRun=true)
    public void tearDown() throws Exception {
        cleanRoundtripFromDoms();
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
        Process process = processBuilder.start();
        process.waitFor();
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
