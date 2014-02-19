package dk.statsbiblioteket.newspaper.domsenricher.component.enrichers;

import dk.statsbibliokeket.newspaper.treenode.NodeType;
import dk.statsbibliokeket.newspaper.treenode.TreeNodeWithChildren;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.FedoraRelation;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.newspaper.domsenricher.component.RecursiveFedoraCleaner;
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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 *
 */
public class GenericNodeEnricherIT  {

    private static Logger logger = LoggerFactory.getLogger(GenericNodeEnricherTest.class);

    private EnhancedFedoraImpl fedora;
    public static String BATCH_ID;
    public static final int ROUNDTRIP_NO= 1;
    private Batch batch;
    private Properties props;
    private final String HAS_MODEL = "info:fedora/fedora-system:def/model#hasModel";
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
           RunnablePromptDomsIngester ingester = new RunnablePromptDomsIngester(props, fedora);
           ResultCollector resultCollector = new ResultCollector("foo", "bar");
           ingester.doWorkOnBatch(batch, resultCollector);
           assertTrue(resultCollector.isSuccess(), resultCollector.toReport());
       }

       private void cleanRoundtripFromDoms() throws Exception {
           String label = "path:" + batch.getFullID();
           logger.debug("Cleaning up from '" + label + "'");
           RecursiveFedoraCleaner.cleanFedora(fedora, label, true);
       }



    @Test(groups = "integrationTest")
    public void testEnrich() throws Exception {
        String label = "path:" + batch.getFullID();
        List<String> pids = fedora.findObjectFromDCIdentifier(label);
        String pid = pids.get(0);
        NodeEnricherFactory factory = new NodeEnricherFactory(fedora);
        NodeBeginsParsingEvent event = new NodeBeginsParsingEvent(BATCH_ID, pid);
        TreeNodeWithChildren node = new TreeNodeWithChildren(event.getName(), NodeType.BATCH, null, event.getLocation());
        AbstractNodeEnricher enricher = factory.getNodeEnricher(node);
        List<String> contentModels = enricher.getAllContentModels();
        assertTrue(contentModels.contains("ContentModel_RoundTrip"), contentModels.toString());
        String relsExtXml = enricher.getRelsExt(event);
        assertTrue(relsExtXml.contains("hasPart"));
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
