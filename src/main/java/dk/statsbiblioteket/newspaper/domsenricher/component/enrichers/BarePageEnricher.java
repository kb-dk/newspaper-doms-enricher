package dk.statsbiblioteket.newspaper.domsenricher.component.enrichers;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;

import java.util.ArrayList;
import java.util.List;

/**
 * This enricher represents pages in Workshift-Iso-Target, File-Iso-Target and Unmatched that have just a MIX datastream.
 */
public class BarePageEnricher extends AbstractNodeEnricher {

    /**
     * Constructor for this class.
     *
     * @param fedora the location of the objects to be enriched,
     */
    protected BarePageEnricher(EnhancedFedora fedora) {
        super(fedora);
    }

    @Override
    protected List<String> getAdditionalContentModels() {
        List<String> additionalContentModels = new ArrayList<>();
        additionalContentModels.add("ContentModel_Page");
        return additionalContentModels;
    }
}
