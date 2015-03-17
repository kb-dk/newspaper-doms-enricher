package dk.statsbiblioteket.newspaper.domsenricher.component;

import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeStateWithChildren;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeWithChildren;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.newspaper.domsenricher.component.enrichers.NodeEnricher;
import dk.statsbiblioteket.newspaper.domsenricher.component.enrichers.NodeEnricherFactory;
import dk.statsbiblioteket.newspaper.promptdomsingester.util.RdfManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A tree handler which enriches each node with the relevant content models and relations.
 */
public class DomsEnricherTreeEventHandler extends TreeNodeStateWithChildren {
    private static Logger logger = LoggerFactory.getLogger(DomsEnricherTreeEventHandler.class);

    private ResultCollector resultCollector;
    private NodeEnricherFactory nodeEnricherFactory;


    public DomsEnricherTreeEventHandler(EnhancedFedora fedora, ResultCollector resultCollector) {
        nodeEnricherFactory = new NodeEnricherFactory(fedora);
        this.resultCollector = resultCollector;
    }


    /**
     * Does the actual enrichment by adding all necessary relationships to child-nodes and content-models.
     * @param event the event identifying this node.
     */
    @Override
    public void processNodeEnd(NodeEndParsingEvent event) {
        NodeEnricher nodeEnricher = nodeEnricherFactory.getNodeEnricher((TreeNodeWithChildren) getCurrentNode());
        String relsExtXml = nodeEnricher.getRelsExt(event);
        RdfManipulator rdfManipulator = new RdfManipulator(relsExtXml);
        rdfManipulator.clearDomsRelations(NodeEnricher.HAS_BRIK, NodeEnricher.HAS_EDITION, NodeEnricher.HAS_EDITION_PAGE,
                                          NodeEnricher.HAS_FILE, NodeEnricher.HAS_FILM, NodeEnricher.HAS_ISO_TARGET,
                                          NodeEnricher.HAS_PAGE, NodeEnricher.HAS_WORKSHIFT);
        addParentChildRelations(event, rdfManipulator);
        rdfManipulator.clearOldContentModels();
        addContentModels(nodeEnricher, rdfManipulator);
        logger.debug("Writing rdf for {}:\n{}", event.getLocation(), rdfManipulator);
        //Finally write the new RELS-EXT back to DOMS.
        nodeEnricher.updateRelsExt(event, rdfManipulator.toString());
    }

    private void addContentModels(NodeEnricher nodeEnricher, RdfManipulator rdfManipulator) {
        for (String contentModel: nodeEnricher.getAllContentModels()) {
            rdfManipulator.addContentModel(contentModel);
        }
    }

    private void addParentChildRelations(NodeEndParsingEvent event, RdfManipulator rdfManipulator) {
        //Iterate through the child nodes, adding relevant relations.
        List<TreeNodeWithChildren> children = ((TreeNodeWithChildren) getCurrentNode()).getChildren();
        for (TreeNodeWithChildren childNode: children) {
            String pid = childNode.getLocation();
            String predicateName = null;
            switch (childNode.getType()) {
                case BATCH:
                    throw new IllegalStateException("Unexpectedly found a batch node" + childNode.getName() + "as the child of "
                            +  event.getName());
                case WORKSHIFT_ISO_TARGET:
                    predicateName = NodeEnricher.HAS_WORKSHIFT;
                    break;
                case WORKSHIFT_TARGET:
                    predicateName = NodeEnricher.HAS_PAGE;
                    break;
                case TARGET_IMAGE:
                    predicateName = NodeEnricher.HAS_FILE;
                    break;
                case FILM:
                    predicateName = NodeEnricher.HAS_FILM;
                    break;
                case FILM_ISO_TARGET:
                    predicateName = NodeEnricher.HAS_ISO_TARGET;
                    break;
                case FILM_TARGET:
                    predicateName = NodeEnricher.HAS_PAGE;
                    break;
                case ISO_TARGET_IMAGE:
                    predicateName = NodeEnricher.HAS_FILE;
                    break;
                case UNMATCHED:
                    predicateName = NodeEnricher.HAS_PAGE;
                    break;
                case EDITION:
                    predicateName = NodeEnricher.HAS_EDITION;
                    break;
                case PAGE:
                    if (getCurrentNode().getType().equals(NodeType.EDITION)) {
                        predicateName = NodeEnricher.HAS_EDITION_PAGE;
                    } else {
                        predicateName = NodeEnricher.HAS_PAGE;
                    }
                    break;
                case BRIK:
                    predicateName = NodeEnricher.HAS_BRIK;
                    break;
                case BRIK_IMAGE:
                    predicateName = NodeEnricher.HAS_FILE;
                    break;
                case PAGE_IMAGE:
                    predicateName = NodeEnricher.HAS_FILE;
                    break;
            }
            rdfManipulator.addDomsRelation(predicateName, pid);
        }
    }

}
