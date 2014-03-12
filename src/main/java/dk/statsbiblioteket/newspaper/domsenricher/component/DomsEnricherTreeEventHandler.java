package dk.statsbiblioteket.newspaper.domsenricher.component;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbibliokeket.newspaper.treenode.TreeNodeStateWithChildren;
import dk.statsbibliokeket.newspaper.treenode.TreeNodeWithChildren;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.newspaper.domsenricher.component.enrichers.AbstractNodeEnricher;
import dk.statsbiblioteket.newspaper.domsenricher.component.enrichers.NodeEnricherFactory;
import dk.statsbiblioteket.newspaper.domsenricher.component.util.RdfManipulator;

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
        AbstractNodeEnricher nodeEnricher = nodeEnricherFactory.getNodeEnricher((TreeNodeWithChildren) getCurrentNode());
        String relsExtXml = nodeEnricher.getRelsExt(event);
        RdfManipulator rdfManipulator = new RdfManipulator(relsExtXml);
        addParentChildRelations(event, rdfManipulator);
        addContentModels(nodeEnricher, rdfManipulator);
        logger.debug("Writing rdf for {}:\n{}", event.getLocation(), rdfManipulator);
        //Finally write the new RELS-EXT back to DOMS.
        nodeEnricher.updateRelsExt(event, rdfManipulator.toString());
    }

    private void addContentModels(AbstractNodeEnricher nodeEnricher, RdfManipulator rdfManipulator) {
        for (String contentModel: nodeEnricher.getAllContentModels()) {
            rdfManipulator.addContentModel("doms:" + contentModel);
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
                    predicateName = "hasWorkshift";
                    break;
                case WORKSHIFT_TARGET:
                    predicateName = "hasPage";
                    break;
                case TARGET_IMAGE:
                    predicateName = "hasFile";
                    break;
                case FILM:
                    predicateName = "hasFilm";
                    break;
                case FILM_ISO_TARGET:
                    predicateName = "hasIsoTarget";
                    break;
                case FILM_TARGET:
                    predicateName = "hasPage";
                    break;
                case ISO_TARGET_IMAGE:
                    predicateName = "hasFile";
                    break;
                case UNMATCHED:
                    predicateName = "hasPage";
                    break;
                case EDITION:
                    predicateName = "hasEdition";
                    break;
                case PAGE:
                    predicateName = "hasPage";
                    break;
                case BRIK:
                    predicateName = "hasBrik";
                    break;
                case BRIK_IMAGE:
                    predicateName = "hasFile";
                    break;
                case PAGE_IMAGE:
                    predicateName = "hasFile";
                    break;
            }
            rdfManipulator.addExternalRelation(predicateName, pid);
        }
    }

}
