package dk.statsbiblioteket.newspaper.domsenricher.component.enrichers;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.newspaper.domsenricher.component.DomsEnricherComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class NodeEnricher {

    public static final String COMMENT = "Modified by " + DomsEnricherComponent.class.getSimpleName();
    public static final String RELS_EXT = "RELS-EXT";
    public static final String APPLICATION_RDF_XML = "application/rdf+xml";
    public static final String UUID = "uuid:";
    public static final String DOMS_CONTENT_MODEL_DOMS = "doms:ContentModel_DOMS";
    public static final String DOMS_CONTENT_MODEL_ROUND_TRIP = "doms:ContentModel_RoundTrip";
    public static final String DOMS_CONTENT_MODEL_ITEM = "doms:ContentModel_Item";
    public static final String DOMS_CONTENT_MODEL_FILM = "doms:ContentModel_Film";
    public static final String DOMS_NAMESPACE = "http://doms.statsbiblioteket.dk/relations/default/0/1/#";
    public static final String DOMS_CONTENT_MODEL_EDITION = "doms:ContentModel_Edition";
    public static final String DOMS_CONTENT_MODEL_PAGE = "doms:ContentModel_Page";
    public static final String DOMS_CONTENT_MODEL_EDITION_PAGE = "doms:ContentModel_EditionPage";
    public static final String DOMS_CONTENT_MODEL_JPEG2000_FILE = "doms:ContentModel_Jpeg2000File";
    public static final String JP2 = ".jp2";
    public static final String HAS_FILM = "hasFilm";
    public static final String HAS_EDITION = "hasEdition";
    public static final String HAS_FILE = "hasFile";
    public static final String HAS_PAGE = "hasPage";
    public static final String HAS_EDITION_PAGE = "hasEditionPage";
    public static final String DOMS_CONTENT_MODEL_WORKSHIFT = "doms:ContentModel_Workshift";
    public static final String DOMS_CONTENT_MODEL_UNMATCHED = "doms:ContentModel_Unmatched";
    public static final String DOMS_CONTENT_MODEL_ISO_TARGET = "doms:ContentModel_IsoTarget";
    public static final String HAS_ISO_TARGET = "hasIsoTarget";
    public static final String HAS_WORKSHIFT = "hasWorkshift";
    public static final String DOMS_CONTENT_MODEL_BRIK = "doms:ContentModel_Brik";
    public static final String HAS_BRIK = "hasBrik";
    public static final String DOMS_CONTENT_MODEL_FILE = "doms:ContentModel_File";
    public static final int RETRY_DELAY = 100;
    static Logger logger = LoggerFactory.getLogger(NodeEnricher.class);
    private final String contentModelDoms = "doms:ContentModel_DOMS";
    ArrayList<String> contentModels;
    private EnhancedFedora fedora;

    /**
     * Creates a a node enricher which adds (up to) one additional model as well as ContentModel_Doms
     * to an object.
     * @param fedora
     * @param contentModelNames May be null, in which case only ContenModel_Doms is added.
     */
    protected NodeEnricher(EnhancedFedora fedora, String... contentModelNames) {
        this.fedora = fedora;
        contentModels = new ArrayList<>();

        if (contentModelNames != null) {
            for (String contentModelName : contentModelNames) {
                contentModels.add(contentModelName);
            }
        }
    }

    protected List<String> getAdditionalContentModels() {
        return contentModels;
    }

    /**
     * Get the current version of the RELS-EXT datastream corresponding to an event.
     * @param event the Event.
     * @return the datastream.
     */
    public String getRelsExt(ParsingEvent event) {
        try {
            return fedora.getXMLDatastreamContents(event.getLocation(), RELS_EXT);
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
            fedora.modifyDatastreamByValue(event.getLocation(), RELS_EXT, null, null, relsExtXml.getBytes(), new ArrayList<String>(), APPLICATION_RDF_XML, COMMENT,
                                           null);
        } catch (BackendInvalidCredsException e) {
            try {
                fedora.modifyObjectState(event.getLocation(), "I", COMMENT);
                fedora.modifyDatastreamByValue(event.getLocation(), RELS_EXT, null, null, relsExtXml.getBytes(), new ArrayList<String>(),
                                               APPLICATION_RDF_XML, COMMENT, null);
            } catch (BackendInvalidCredsException | BackendMethodFailedException | BackendInvalidResourceException e1) {
                throw new RuntimeException(e1);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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
