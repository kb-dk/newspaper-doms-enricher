package dk.statsbiblioteket.newspaper.domsenricher.component.enrichers;

import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNode;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeWithChildren;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;

import java.util.HashMap;
import java.util.Map;

/**
 * A Factory class for generating node enrichers.
 */
public class NodeEnricherFactory {

    private Map<NodeType, NodeEnricher> nodeEnricherMap;

    private EnhancedFedora fedora;
    private NodeEnricher unmatchedPage;
    private NodeEnricher editionPage;
    private NodeEnricher missingPage;

    /**
     * Create a factory from which we can obtain instances of the required node-enrichers.
     * @param fedora
     *
     */
    public NodeEnricherFactory(EnhancedFedora fedora) {
        this.fedora = fedora;
        createEnrichers();
    }

    private void createEnrichers() {
        nodeEnricherMap = new HashMap<>();
        nodeEnricherMap.put(NodeType.BATCH, new NodeEnricher(fedora, NodeEnricher.DOMS_CONTENT_MODEL_ITEM, NodeEnricher.DOMS_CONTENT_MODEL_ROUND_TRIP));
        nodeEnricherMap.put(NodeType.WORKSHIFT_ISO_TARGET, new NodeEnricher(fedora, NodeEnricher.DOMS_CONTENT_MODEL_WORKSHIFT));
        nodeEnricherMap.put(NodeType.WORKSHIFT_TARGET, new NodeEnricher(fedora, NodeEnricher.DOMS_CONTENT_MODEL_PAGE));
        nodeEnricherMap.put(NodeType.TARGET_IMAGE, new NodeEnricher(fedora, NodeEnricher.DOMS_CONTENT_MODEL_FILE, NodeEnricher.DOMS_CONTENT_MODEL_JPEG2000_FILE));
        nodeEnricherMap.put(NodeType.FILM, new NodeEnricher(fedora, NodeEnricher.DOMS_CONTENT_MODEL_FILM));
        nodeEnricherMap.put(NodeType.FILM_ISO_TARGET, new NodeEnricher(fedora, NodeEnricher.DOMS_CONTENT_MODEL_ISO_TARGET));
        nodeEnricherMap.put(NodeType.FILM_TARGET, new NodeEnricher(fedora, NodeEnricher.DOMS_CONTENT_MODEL_PAGE));
        nodeEnricherMap.put(NodeType.ISO_TARGET_IMAGE, new NodeEnricher(fedora, NodeEnricher.DOMS_CONTENT_MODEL_FILE, NodeEnricher.DOMS_CONTENT_MODEL_JPEG2000_FILE));
        nodeEnricherMap.put(NodeType.UNMATCHED, new NodeEnricher(fedora, NodeEnricher.DOMS_CONTENT_MODEL_UNMATCHED));
        nodeEnricherMap.put(NodeType.EDITION, new NodeEnricher(fedora, NodeEnricher.DOMS_CONTENT_MODEL_ITEM, NodeEnricher.DOMS_CONTENT_MODEL_EDITION));
        nodeEnricherMap.put(NodeType.PAGE, null);
        nodeEnricherMap.put(NodeType.BRIK, new NodeEnricher(fedora, NodeEnricher.DOMS_CONTENT_MODEL_BRIK));
        nodeEnricherMap.put(NodeType.BRIK_IMAGE, new NodeEnricher(fedora, NodeEnricher.DOMS_CONTENT_MODEL_FILE, NodeEnricher.DOMS_CONTENT_MODEL_JPEG2000_FILE));
        nodeEnricherMap.put(NodeType.PAGE_IMAGE, new NodeEnricher(fedora, NodeEnricher.DOMS_CONTENT_MODEL_FILE, NodeEnricher.DOMS_CONTENT_MODEL_JPEG2000_FILE));
        unmatchedPage = new NodeEnricher(
                fedora, NodeEnricher.DOMS_CONTENT_MODEL_PAGE);
        editionPage = new NodeEnricher(
                fedora, NodeEnricher.DOMS_CONTENT_MODEL_PAGE,
                NodeEnricher.DOMS_CONTENT_MODEL_EDITION_PAGE);
         missingPage = new NodeEnricher(
                fedora, NodeEnricher.DOMS_CONTENT_MODEL_EDITION_PAGE);
    }

    private NodeEnricher getNodeEnricher(NodeType nodeType) {
        return nodeEnricherMap.get(nodeType);
    }


    /**
     * Return an instance of a subclass of AbstractNodeEnricher which is appropriate for the given node.
     * @param treeNode the node to be enriched.
     * @return the enricher.
     */
    public NodeEnricher getNodeEnricher(TreeNodeWithChildren treeNode) {
        //GenericNodeEnricher is used for all non-Page nodes. For Page nodes
        //the logic is slightly more complex depending on which type of page
        //we are on (unmatched or edition) and whether the page actually has
        //a corresponding scan.
        if (!treeNode.getType().equals(NodeType.PAGE)) {
            return getNodeEnricher(treeNode.getType());
        } else {
            if (treeNode.getParent().getType().equals(NodeType.UNMATCHED)) {
                return unmatchedPage;
            }
            for (TreeNode child: treeNode.getChildren()) {
                if (child.getType().equals(NodeType.PAGE_IMAGE)) {
                    return editionPage;
                }
            }
            return missingPage;
        }
    }


}
