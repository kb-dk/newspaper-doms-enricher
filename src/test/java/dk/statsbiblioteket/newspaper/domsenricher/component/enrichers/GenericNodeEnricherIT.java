package dk.statsbiblioteket.newspaper.domsenricher.component.enrichers;

import dk.statsbibliokeket.newspaper.treenode.NodeType;
import dk.statsbibliokeket.newspaper.treenode.TreeNodeWithChildren;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.Fedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.FedoraRest;
import dk.statsbiblioteket.doms.central.connectors.fedora.generated.Datastream;
import dk.statsbiblioteket.doms.central.connectors.fedora.generated.DatastreamProblems;
import dk.statsbiblioteket.doms.central.connectors.fedora.generated.DatastreamProfile;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.FedoraRelation;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.newspaper.domsenricher.component.util.RdfManipulator;
import dk.statsbiblioteket.newspaper.domsenricher.component.util.RecursiveFedoraCleaner;
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

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;

/**
 *
 */
public class GenericNodeEnricherIT  {

    private static Logger logger = LoggerFactory.getLogger(GenericNodeEnricherTest.class);

    private EnhancedFedoraImpl enhancedFedora;
    private FedoraRest fedora;
    public static String BATCH_ID;
    public static final int ROUNDTRIP_NO= 1;
    private Batch batch;
    private Properties props;
    private final String HAS_MODEL = "info:enhancedFedora/enhancedFedora-system:def/model#hasModel";
    public static final String ROUNDTRIP_MODEL = "doms:" + "ContentModel_RoundTrip";

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
           enhancedFedora = new EnhancedFedoraImpl(creds, fedoraLocation , props.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL) , null);
           fedora = new FedoraRest(creds, fedoraLocation);
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
           env.put("numberOfFilms", "1");
           env.put("filmNoOfPictures", "2");
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
           RunnablePromptDomsIngester ingester = new RunnablePromptDomsIngester(props, enhancedFedora);
           ResultCollector resultCollector = new ResultCollector("foo", "bar");
           ingester.doWorkOnBatch(batch, resultCollector);
           assertTrue(resultCollector.isSuccess(), resultCollector.toReport());
       }

       private void cleanRoundtripFromDoms() throws Exception {
           String label = "path:" + batch.getFullID();
           logger.debug("Cleaning up from '" + label + "'");
           (new RecursiveFedoraCleaner(enhancedFedora)).visitTree(label, true);
       }


    /**
     * Test that enriches a single node and ensures that it validates correctly afterwards.
     * @throws Exception
     */
    @Test(groups = "integrationTest")
    public void testEnrich() throws Exception {
        String label = "path:" + batch.getFullID();
        List<String> pids = enhancedFedora.findObjectFromDCIdentifier(label);
        String pid = pids.get(0);
        dk.statsbiblioteket.doms.central.connectors.fedora.structures.DatastreamProfile datastreamProfile =  fedora.getDatastreamProfile(pid, "RELS-EXT", (new Date()).getTime());
        assertEquals(datastreamProfile.getMimeType(), "application/rdf+xml");
        NodeEnricherFactory factory = new NodeEnricherFactory(enhancedFedora);
        NodeBeginsParsingEvent event = new NodeBeginsParsingEvent(BATCH_ID, pid);
        TreeNodeWithChildren node = new TreeNodeWithChildren(event.getName(), NodeType.BATCH, null, event.getLocation());
        AbstractNodeEnricher enricher = factory.getNodeEnricher(node);
        List<String> contentModels = enricher.getAllContentModels();
        assertTrue(contentModels.contains("ContentModel_RoundTrip"), contentModels.toString());
        String relsExtXml = enricher.getRelsExt(event);
        assertTrue(relsExtXml.contains("hasPart"));
        RdfManipulator manipulator = new RdfManipulator(relsExtXml);
        enricher.updateRelsExt(event, manipulator.toString());
        dk.statsbiblioteket.doms.central.connectors.fedora.generated.Validation validator =  enhancedFedora.validate(pid);
        if (!validator.isValid()) {
            DatastreamProblems datastreamProblems = validator.getDatastreamProblems();
            List<String> problems = validator.getProblems().getProblem();
            for (Datastream datastream: datastreamProblems.getDatastream()) {
                problems.addAll(datastream.getProblem());
            }
            for (String problem: problems) {
                logger.debug(problem);
            }
            fail();
        }
    }

}
