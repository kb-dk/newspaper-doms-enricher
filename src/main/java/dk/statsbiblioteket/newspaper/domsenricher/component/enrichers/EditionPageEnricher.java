package dk.statsbiblioteket.newspaper.domsenricher.component.enrichers;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;

import java.util.ArrayList;
import java.util.List;

/**
 * This enricher represents complete pages as found under edition nodes.These have optional ALTO and MODS.
 */
public class EditionPageEnricher extends AbstractNodeEnricher {

    /**
     * Constructor for this class.
     *
     * @param fedora the location of the objects to be enriched,
     */
    public EditionPageEnricher(EnhancedFedora fedora) {
        super(fedora);
    }

    @Override
    protected List<String> getAdditionalContentModels() {
        List<String> additionalContentModels = new ArrayList<>();
        additionalContentModels.add("ContentModel_Page");
        additionalContentModels.add("ContentModel_EditionPage");
        return additionalContentModels;
    }
}
