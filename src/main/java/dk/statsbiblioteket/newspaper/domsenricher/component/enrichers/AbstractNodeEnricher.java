package dk.statsbiblioteket.newspaper.domsenricher.component.enrichers;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;

import java.util.List;

/**
 *
 */
public abstract class AbstractNodeEnricher {

    private final String HAS_MODEL = "info:fedora/fedora-system:def/model#hasModel";

    protected final String domsNS = "http://doms.statsbiblioteket.dk/relations/default/0/1/#";
    private final String contentModelDoms = domsNS + "ContentModel_DOMS";


    private EnhancedFedora fedora;

    protected AbstractNodeEnricher(EnhancedFedora fedora) {
        this.fedora = fedora;
    }

    public void enrich(ParsingEvent event) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        List<String> contentModels = getAdditionalContentModels();
        contentModels.add(contentModelDoms);
        for (String contentModel: contentModels) {
            String message = "Added by " + this.getClass().getSimpleName();
            fedora.addRelation(event.getLocation(), null, HAS_MODEL, contentModel, false, message);
        }
    }

    protected abstract List<String> getAdditionalContentModels();

}
