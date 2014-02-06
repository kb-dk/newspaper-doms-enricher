package dk.statsbiblioteket.newspaper.domsenricher.component;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.AbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventRunner;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.util.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    public void doWorkOnBatch(Batch batch, ResultCollector resultCollector) throws IOException {
        List<TreeEventHandler> handlers = new ArrayList<>();
        handlers.add(new DomsEnricherTreeEventHandler(eFedora, resultCollector));
        EventRunner eventRunner = new EventRunner(createIterator(batch));
        eventRunner.runEvents(handlers, resultCollector);
    }

}
