package dk.statsbiblioteket.newspaper.domsenricher.component.enrichers;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A class with a method for enriching a DOMS object with a list of content models.
 */
public abstract class AbstractNodeEnricher {

    static Logger logger = LoggerFactory.getLogger(AbstractNodeEnricher.class);

    private final String HAS_MODEL = "info:fedora/fedora-system:def/model#hasModel";

    protected final String domsNS = "doms:";
    private final String contentModelDoms = domsNS + "ContentModel_DOMS";


    private EnhancedFedora fedora;

    /**
     * Constructor for this class.
     * @param fedora the location of the objects to be enriched,
     */
    protected AbstractNodeEnricher(EnhancedFedora fedora) {
        this.fedora = fedora;
    }

    /**
     * Enrich the object corresponding to the given event.
     * @param event the event corresponding to a DOMS object to be enriched.
     * @throws BackendInvalidCredsException
     * @throws BackendMethodFailedException
     * @throws BackendInvalidResourceException
     */
    public void enrich(ParsingEvent event) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        logger.debug("Enriching " + event.getName());
        List<String> contentModels = new ArrayList<>();
        contentModels.addAll(getAdditionalContentModels());
        contentModels.add(contentModelDoms);
        logger.debug("Adding {} content models to {} at {}.", contentModels.size(), event.getName(), event.getLocation());
        for (String contentModel: contentModels) {
            String message = "Added by " + this.getClass().getSimpleName();
            logger.debug("Adding triple ({}, {}, {})", event.getLocation(), HAS_MODEL, contentModel);
            fedora.addRelation(event.getLocation(), null, HAS_MODEL, contentModel, false, message);
        }
    }

    /**
     * Return a list of all content models to be added to objects by this enricher, in addition to
     * "ContentModel_DOMS" which is added to all objects.
     * @return
     */
    protected abstract List<String> getAdditionalContentModels();

}
