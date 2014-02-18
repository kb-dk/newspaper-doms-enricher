package dk.statsbiblioteket.newspaper.domsenricher.component.enrichers;

import dk.statsbibliokeket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import sun.reflect.generics.tree.Tree;

import java.util.HashMap;
import java.util.Map;

/**
 * A Factory class for generating node enrichers.
 */
public class NodeEnricherFactory {

    private Map<NodeType, AbstractNodeEnricher> nodeEnricherMap;

    private EnhancedFedora fedora;

    /**
     * Create a factory from which we can obtain instances of the required node-enrichers.
     * @param fedora
     */
    public NodeEnricherFactory(EnhancedFedora fedora) {
        this.fedora = fedora;
        createEnrichers();
    }

    //TODO consider how to distinguish between page-images and other images
    private void createEnrichers() {
        nodeEnricherMap = new HashMap<>();
        nodeEnricherMap.put(NodeType.BATCH, new GenericNodeEnricher(fedora, "ContentModel_RoundTrip"));
        nodeEnricherMap.put(NodeType.WORKSHIFT_ISO_TARGET, new GenericNodeEnricher(fedora, "ContentModel_Workshift"));
        //TODO create content model for these nodes - identical content to PAGE
        nodeEnricherMap.put(NodeType.WORKSHIFT_TARGET, new GenericNodeEnricher(fedora, null));
        nodeEnricherMap.put(NodeType.TARGET_IMAGE, new GenericNodeEnricher(fedora, "ContentModel_Jpeg2000File"));
        nodeEnricherMap.put(NodeType.FILM, new GenericNodeEnricher(fedora, "ContentModel_Film"));
        nodeEnricherMap.put(NodeType.FILM_ISO_TARGET, new GenericNodeEnricher(fedora, "ContentModel_IsoTarget"));
        //TODO create content model for these nodes - identical content to PAGE
        nodeEnricherMap.put(NodeType.FILM_TARGET, new GenericNodeEnricher(fedora, null));
        nodeEnricherMap.put(NodeType.ISO_TARGET_IMAGE, new GenericNodeEnricher(fedora, "ContentModel_Jpeg2000File"));
        nodeEnricherMap.put(NodeType.UNMATCHED, new GenericNodeEnricher(fedora, "ContentModel_Unmatched"));
        nodeEnricherMap.put(NodeType.EDITION, new GenericNodeEnricher(fedora, "ContentModel_Edition"));
        nodeEnricherMap.put(NodeType.PAGE, new GenericNodeEnricher(fedora, "ContentModel_Page"));
        nodeEnricherMap.put(NodeType.BRIK, new GenericNodeEnricher(fedora, "ContentModel_Brik"));
        nodeEnricherMap.put(NodeType.BRIK_IMAGE, new GenericNodeEnricher(fedora, "ContentModel_Jpeg2000File"));
        nodeEnricherMap.put(NodeType.PAGE_IMAGE, new GenericNodeEnricher(fedora, "ContentModel_Jpeg2000File"));
    }

    /**
     * Return an instance of a subclass of AbstractNodeEnricher which is appropriate for the given node.
     * @param nodeType the type of the node to be enriched.
     * @return the enricher.
     */
    public AbstractNodeEnricher getNodeEnricher(NodeType nodeType) {
        return nodeEnricherMap.get(nodeType);
    }


}
