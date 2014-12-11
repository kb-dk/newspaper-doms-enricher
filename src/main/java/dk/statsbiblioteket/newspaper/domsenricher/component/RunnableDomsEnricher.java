package dk.statsbiblioteket.newspaper.domsenricher.component;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.TreeProcessorAbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventRunner;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * The runnable component for the DomsEnricher
 */
public class RunnableDomsEnricher extends TreeProcessorAbstractRunnableComponent {

    private static Logger logger = LoggerFactory.getLogger(RunnableDomsEnricher.class);

    private final EnhancedFedora eFedora;

    /**
     * Constructor for this class.
     * @param properties the properties to use.
     * @param eFedora the fedora instance containing the objects to work on.
     */
    public RunnableDomsEnricher(Properties properties, EnhancedFedora eFedora) {
        super(properties);
        this.eFedora = eFedora;
    }

    @Override
    public String getEventID() {
        return "Metadata_Enriched";
    }

    @Override
    public void doWorkOnItem(Batch batch, ResultCollector resultCollector) throws IOException {
        logger.debug("Beginning enrichment of " + batch.getFullID());
        List<TreeEventHandler> handlers = new ArrayList<>();

        int tries = Integer.parseInt(getProperties().getProperty(ConfigConstants.FEDORA_RETRIES, "3"));
        int maxThreads = Integer.parseInt(getProperties().getProperty(ConfigConstants.THREADS_PER_BATCH, "1"));
        handlers.add(new DomsEnricherTreeEventHandler(eFedora, resultCollector, tries));
        handlers.add(new DomsLabelEnricherTreeEventHandler(eFedora, tries));
        handlers.add(new MimetypeEnricher(Arrays.asList("FILM", "MIX", "MODS", "ALTO", "EDITION"),
                                                 new SpecializedFedora(getProperties().getProperty(ConfigConstants.DOMS_URL),
                                                                              new Credentials(getProperties().getProperty(ConfigConstants.DOMS_USERNAME),
                                                                                                     getProperties().getProperty(ConfigConstants.DOMS_PASSWORD)))));
        if (getProperties().getProperty(Constants.PUBLISH, "true").equalsIgnoreCase("true") ){
            handlers.add(new DomsPublisherEventHandler(eFedora,
                    resultCollector, maxThreads, tries));
        }

        EventRunner eventRunner = new EventRunner(createIterator(batch), handlers, resultCollector);
        eventRunner.run();
    }

}
