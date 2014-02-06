package dk.statsbiblioteket.newspaper.domsenricher.component.enrichers;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class BatchNodeEnricher extends AbstractNodeEnricher {

    protected BatchNodeEnricher(EnhancedFedora fedora) {
        super(fedora);
    }

    @Override
    protected List<String> getAdditionalContentModels() {
        ArrayList<String> contentModels = new ArrayList<>();
        contentModels.add(domsNS + "ContentModel_Batch");
        return contentModels;
    }
}
