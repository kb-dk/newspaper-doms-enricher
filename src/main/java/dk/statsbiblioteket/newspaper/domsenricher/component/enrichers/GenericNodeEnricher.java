package dk.statsbiblioteket.newspaper.domsenricher.component.enrichers;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * To change this template use File | Settings | File Templates.
 */
public class GenericNodeEnricher extends AbstractNodeEnricher {

    ArrayList<String> contentModels;

    protected GenericNodeEnricher(EnhancedFedora fedora, String contentModelName) {
        super(fedora);
        contentModels = new ArrayList<>();
        contentModels.add(domsNS + contentModelName);
    }

    @Override
    protected List<String> getAdditionalContentModels() {
        return contentModels;
    }
}
