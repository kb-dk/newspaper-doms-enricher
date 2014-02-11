package dk.statsbiblioteket.newspaper.domsenricher.component.enrichers;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import sun.reflect.generics.tree.Tree;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class NodeEnricherFactory {

    private Map<NodeType, AbstractNodeEnricher> nodeEnricherMap;

    private EnhancedFedora fedora;

    public NodeEnricherFactory(EnhancedFedora fedora) {
        this.fedora = fedora;
        createEnrichers();
    }

    private void createEnrichers() {
        nodeEnricherMap = new HashMap<>();
        nodeEnricherMap.put(NodeType.BATCH, new GenericNodeEnricher(fedora, "ContentModel_RoundTrip"));
        nodeEnricherMap.put(NodeType.WORKSHIFT_ISO_TARGET, new GenericNodeEnricher(fedora, "ContentModel_Workshift"));
        nodeEnricherMap.put(NodeType.WORKSHIFT_TARGET, new GenericNodeEnricher(fedora, null));
        nodeEnricherMap.put(NodeType.TARGET_IMAGE, new GenericNodeEnricher(fedora, "ContentModel_Jpeg2000File"));
        nodeEnricherMap.put(NodeType.FILM, new GenericNodeEnricher(fedora, "ContentModel_Film"));
        nodeEnricherMap.put(NodeType.FILM_ISO_TARGET, new GenericNodeEnricher(fedora, "ContentModel_IsoTarget"));
        nodeEnricherMap.put(NodeType.FILM_TARGET, new GenericNodeEnricher(fedora, null));
        nodeEnricherMap.put(NodeType.ISO_TARGET_IMAGE, new GenericNodeEnricher(fedora, "ContentModel_Jpeg2000File"));
        nodeEnricherMap.put(NodeType.UNMATCHED, new GenericNodeEnricher(fedora, "ContentModel_Unmatched"));
        nodeEnricherMap.put(NodeType.EDITION, new GenericNodeEnricher(fedora, "ContentModel_Edition"));
        nodeEnricherMap.put(NodeType.PAGE, new GenericNodeEnricher(fedora, "ContentModel_Page"));
        nodeEnricherMap.put(NodeType.BRIK, new GenericNodeEnricher(fedora, "ContentModel_Brik"));
        nodeEnricherMap.put(NodeType.BRIK_IMAGE, new GenericNodeEnricher(fedora, "ContentModel_Jpeg2000File"));
        nodeEnricherMap.put(NodeType.PAGE_IMAGE, new GenericNodeEnricher(fedora, "ContentModel_Jpeg2000File"));
    }

    public AbstractNodeEnricher getNodeEnricher(NodeType nodeType) {
        return nodeEnricherMap.get(nodeType);
    }


}
