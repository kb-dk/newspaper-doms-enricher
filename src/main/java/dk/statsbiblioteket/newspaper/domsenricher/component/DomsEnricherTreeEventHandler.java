package dk.statsbiblioteket.newspaper.domsenricher.component;

import dk.statsbibliokeket.newspaper.treenode.NodeType;
import dk.statsbibliokeket.newspaper.treenode.TreeNode;
import dk.statsbibliokeket.newspaper.treenode.TreeNodeStateWithChildren;
import dk.statsbibliokeket.newspaper.treenode.TreeNodeWithChildren;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.newspaper.domsenricher.component.enrichers.AbstractNodeEnricher;
import dk.statsbiblioteket.newspaper.domsenricher.component.enrichers.EditionPageEnricher;
import dk.statsbiblioteket.newspaper.domsenricher.component.enrichers.MissingPageEnricher;
import dk.statsbiblioteket.newspaper.domsenricher.component.enrichers.NodeEnricherFactory;
import dk.statsbiblioteket.newspaper.domsenricher.component.util.RdfManipulator;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.List;

/**
 * A tree handler which enriches each node with the relevant content models and relations.
 */
public class DomsEnricherTreeEventHandler extends TreeNodeStateWithChildren {
    private static Logger logger = LoggerFactory.getLogger(DomsEnricherTreeEventHandler.class);

    private ResultCollector resultCollector;
    private NodeEnricherFactory nodeEnricherFactory;
    private EnhancedFedora fedora;


    public DomsEnricherTreeEventHandler(EnhancedFedora fedora, ResultCollector resultCollector) {
        nodeEnricherFactory = new NodeEnricherFactory(fedora);
        this.resultCollector = resultCollector;
        this.fedora = fedora;
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
            String elementName = null;
            switch (childNode.getType()) {
                case BATCH:
                    throw new IllegalStateException("Unexpectedly found a batch node" + childNode.getName() + "as the child of "
                            +  event.getName());
                case WORKSHIFT_ISO_TARGET:
                    elementName = "hasWorkshift";
                    break;
                case WORKSHIFT_TARGET:
                    elementName = "hasPage";
                    break;
                case TARGET_IMAGE:
                    elementName = "hasFile";
                    break;
                case FILM:
                    elementName = "hasFilm";
                    break;
                case FILM_ISO_TARGET:
                    elementName = "hasIsoTarget";
                    break;
                case FILM_TARGET:
                    elementName = "hasPage";
                    break;
                case ISO_TARGET_IMAGE:
                    elementName = "hasFile";
                    break;
                case UNMATCHED:
                    elementName = "hasPage";
                    break;
                case EDITION:
                    elementName = "hasEdition";
                    break;
                case PAGE:
                    elementName = "hasPage";
                    break;
                case BRIK:
                    elementName = "hasBrik";
                    break;
                case BRIK_IMAGE:
                    elementName = "hasFile";
                    break;
                case PAGE_IMAGE:
                    elementName = "hasFile";
                    break;
            }
            rdfManipulator.addExternalRelation(elementName, pid);
        }
    }

}
