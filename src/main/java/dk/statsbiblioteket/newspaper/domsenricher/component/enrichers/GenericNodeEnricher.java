package dk.statsbiblioteket.newspaper.domsenricher.component.enrichers;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class GenericNodeEnricher extends AbstractNodeEnricher {

    ArrayList<String> contentModels;

    /**
     * Creates a a node enricher which adds (up to) one additional model as well as ContentModel_Doms
     * to an object.
     * @param fedora
     * @param contentModelName May be null, in which case only ContenModel_Doms is added.
     */
    protected GenericNodeEnricher(EnhancedFedora fedora, String contentModelName) {
        super(fedora);
        contentModels = new ArrayList<>();
        if (contentModelName != null) {
            contentModels.add(domsNS + contentModelName);
        }
    }

    @Override
    protected List<String> getAdditionalContentModels() {
        return contentModels;
    }
}
