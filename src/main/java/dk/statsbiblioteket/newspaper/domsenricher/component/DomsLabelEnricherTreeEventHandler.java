package dk.statsbiblioteket.newspaper.domsenricher.component;

import dk.statsbibliokeket.newspaper.treenode.TreeNodeStateWithChildren;
import dk.statsbibliokeket.newspaper.treenode.TreeNodeWithChildren;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.newspaper.domsenricher.component.enrichers.AbstractNodeEnricher;
import dk.statsbiblioteket.newspaper.domsenricher.component.enrichers.NodeEnricherFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class DomsLabelEnricherTreeEventHandler extends TreeNodeStateWithChildren {
    private static Logger logger = LoggerFactory.getLogger(DomsEnricherTreeEventHandler.class);

    private ResultCollector resultCollector;
    private NodeEnricherFactory nodeEnricherFactory;
    private EnhancedFedora fedora;

    public DomsLabelEnricherTreeEventHandler(EnhancedFedora fedora, ResultCollector resultCollector) {
        nodeEnricherFactory = new NodeEnricherFactory(fedora);
        this.resultCollector = resultCollector;
        this.fedora = fedora;
    }

    /**
     *
     * @param event the event identifying this node.
     */
    @Override
    public void processNodeEnd(NodeEndParsingEvent event) {
        AbstractNodeEnricher nodeEnricher = nodeEnricherFactory.getNodeEnricher((TreeNodeWithChildren)getCurrentNode());
        String relsExtXml = nodeEnricher.getRelsExt(event);

        String currentNodePid = getCurrentNode().getLocation();
        int lastSlashPosition = event.getName().lastIndexOf("/");
        String nameOfNodeOrAttribute = event.getName().substring(lastSlashPosition + 1);

        try {
            switch (getCurrentNode().getType()) {
                case BATCH:
                    fedora.modifyObjectLabel(currentNodePid, "batch-" + nameOfNodeOrAttribute, "");
                    break;
                case WORKSHIFT_ISO_TARGET:
                    fedora.modifyObjectLabel(currentNodePid, "workshift-iso-target", "");
                    break;
                case WORKSHIFT_TARGET:
                    fedora.modifyObjectLabel(currentNodePid, "workshift-target-" + nameOfNodeOrAttribute, "");
                    break;
                case TARGET_IMAGE:
                    fedora.modifyObjectLabel(currentNodePid, nameOfNodeOrAttribute, "");
                    break;
                case FILM:
                    fedora.modifyObjectLabel(currentNodePid, "film-" + nameOfNodeOrAttribute, "");
                    break;
                case FILM_ISO_TARGET:
                    fedora.modifyObjectLabel(currentNodePid, "film-iso-target", "");
                    break;
                case FILM_TARGET:
                    fedora.modifyObjectLabel(currentNodePid, "film-target-" + nameOfNodeOrAttribute, "");
                    break;
                case ISO_TARGET_IMAGE:
                    fedora.modifyObjectLabel(currentNodePid, "iso-target-image-" + nameOfNodeOrAttribute, "");
                    break;
                case UNMATCHED:
                    fedora.modifyObjectLabel(currentNodePid, "unmatched", "");
                    break;
                case EDITION:
                    fedora.modifyObjectLabel(currentNodePid, "edition-" + nameOfNodeOrAttribute, "");
                    break;
                case PAGE:
                    fedora.modifyObjectLabel(currentNodePid, "page-" + nameOfNodeOrAttribute, "");
                    break;
                case BRIK:
                    fedora.modifyObjectLabel(currentNodePid, nameOfNodeOrAttribute, "");
                    break;
                case BRIK_IMAGE:
                    fedora.modifyObjectLabel(currentNodePid, "brik-image-" + nameOfNodeOrAttribute, "");
                    break;
                case PAGE_IMAGE:
                    fedora.modifyObjectLabel(currentNodePid, "page-image-" + nameOfNodeOrAttribute, "");
                    break;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Call to enhanced Fedora's modifyObjectLabel() failed on node '"
                    + getCurrentNode().getName() + "' with PID '" + currentNodePid + "'");
        }
    }
}
