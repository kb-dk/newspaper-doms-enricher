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

    protected final String domsNS = "doms:";
    private final String contentModelDoms = "ContentModel_DOMS";


    private EnhancedFedora fedora;

    /**
     * Constructor for this class.
     * @param fedora the location of the objects to be enriched,
     */
    protected AbstractNodeEnricher(EnhancedFedora fedora) {
        this.fedora = fedora;
    }

    /**
     * Get the current version of the RELS-EXT datastream corresponding to an event.
     * @param event the Event.
     * @return the datastream.
     */
    public String getRelsExt(ParsingEvent event) {
        try {
            return fedora.getXMLDatastreamContents(event.getLocation(), "RELS-EXT");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Update the RELS-EXT datastream corresponding to an event.
     * @param event the Event.
     * @param relsExtXml the new datastream contents.
     */
    public void updateRelsExt(ParsingEvent event, String relsExtXml) {
        try {
            fedora.modifyDatastreamByValue(event.getLocation(),
                    "RELS-EXT",
                    null,
                    null,
                    relsExtXml.getBytes(),
                    new ArrayList<String>(),
                    "application/rdf+xml",
                    "Modified by " + getClass().getSimpleName(),
                    null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return a list of all content models to be added to objects by this enricher, in addition to
     * "ContentModel_DOMS" which is added to all objects. The "doms:" prefix is not included in the
     * returned value.
     * @return
     */
    protected abstract List<String> getAdditionalContentModels();

     /**
     * Return a list of all content models to be added to objects by this enricher, including
     * "ContentModel_DOMS" which is added to all objects. The "doms:" prefix is not included in the
     * returned value.
     * @return
     */
    public List<String> getAllContentModels() {
        List<String> allContentModels = new ArrayList<>();
        allContentModels.add(contentModelDoms);
        allContentModels.addAll(getAdditionalContentModels());
        return allContentModels;
    }

}
