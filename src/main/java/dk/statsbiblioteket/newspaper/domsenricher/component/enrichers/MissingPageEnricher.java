package dk.statsbiblioteket.newspaper.domsenricher.component.enrichers;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;

import java.util.ArrayList;
import java.util.List;

/**
 * This enricher represents missing pages.These have no MIX datastream because there is no scan. ALTO and MODS
 * are "optional", although obviously there will never be an ALTO stream in these cases.
 */
public class MissingPageEnricher extends AbstractNodeEnricher {

    /**
     * Constructor for this class.
     *
     * @param fedora the location of the objects to be enriched,
     */
    public MissingPageEnricher(EnhancedFedora fedora) {
        super(fedora);
    }

    @Override
    protected List<String> getAdditionalContentModels() {
        List<String> additionalContentModels = new ArrayList<>();
        additionalContentModels.add("ContentModel_EditionPage");
        return additionalContentModels;
    }
}
