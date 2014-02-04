package dk.statsbiblioteket.newspaper.domsenricher.component;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.AbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.util.Strings;

import java.util.Properties;

/**
 * The runnable component for the DomsEnricher
 */
public class RunnableDomsEnricher extends AbstractRunnableComponent{
    private final EnhancedFedora eFedora;

    public RunnableDomsEnricher(Properties properties, EnhancedFedora eFedora) {
        super(properties);
        this.eFedora = eFedora;
    }

    @Override
    public String getEventID() {
        return "Metadata_Enriched";
    }

    @Override
    public void doWorkOnBatch(Batch batch, ResultCollector resultCollector) {
        /*IngesterInterface ingester = SimpleFedoraIngester.getNewspaperInstance(eFedora,
                new String[]{getProperties().getProperty(
                ConfigConstants.DOMS_COLLECTION, "doms:Newspaper_Collection")});*/
        try {
            //ingester.ingest(createIterator(batch));
        } catch (Exception e) {
            resultCollector.addFailure(batch.getFullID(),
                                       "exception",
                                       e.getClass().getSimpleName(),
                                       "Exception during ingest: " + e.toString(),
                                       Strings.getStackTrace(e));
        }
    }

}