package dk.statsbiblioteket.newspaper.domsenricher.component;

import dk.statsbibliokeket.newspaper.treenode.TreeNodeStateWithChildren;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;


/**
 *
 */
public class DomsLabelEnricherTreeEventHandler extends TreeNodeStateWithChildren {
    private EnhancedFedora fedora;


    public DomsLabelEnricherTreeEventHandler(EnhancedFedora fedora) {
        this.fedora = fedora;
    }


    /**
     *
     * @param event the event identifying this node.
     */
    @Override
    public void processNodeEnd(NodeEndParsingEvent event) {
        String currentNodePid = getCurrentNode().getLocation();
        int lastSlashPosition = event.getName().lastIndexOf("/");
        String nameOfNode = event.getName().substring(lastSlashPosition + 1);

        try {
            switch (getCurrentNode().getType()) {
                case BATCH:
                    fedora.modifyObjectLabel(currentNodePid, "batch-" + nameOfNode, "");
                    break;
                case WORKSHIFT_ISO_TARGET:
                    fedora.modifyObjectLabel(currentNodePid, "workshift-iso-target", "");
                    break;
                case WORKSHIFT_TARGET:
                    fedora.modifyObjectLabel(currentNodePid, "workshift-target-" + nameOfNode, "");
                    break;
                case TARGET_IMAGE:
                    fedora.modifyObjectLabel(currentNodePid, nameOfNode, "");
                    break;
                case FILM:
                    fedora.modifyObjectLabel(currentNodePid, "film-" + nameOfNode, "");
                    break;
                case FILM_ISO_TARGET:
                    fedora.modifyObjectLabel(currentNodePid, "film-iso-target", "");
                    break;
                case FILM_TARGET:
                    fedora.modifyObjectLabel(currentNodePid, "film-target-" + nameOfNode, "");
                    break;
                case ISO_TARGET_IMAGE:
                    fedora.modifyObjectLabel(currentNodePid, "iso-target-image-" + nameOfNode, "");
                    break;
                case UNMATCHED:
                    fedora.modifyObjectLabel(currentNodePid, "unmatched", "");
                    break;
                case EDITION:
                    fedora.modifyObjectLabel(currentNodePid, "edition-" + nameOfNode, "");
                    break;
                case PAGE:
                    fedora.modifyObjectLabel(currentNodePid, "page-" + nameOfNode, "");
                    break;
                case BRIK:
                    fedora.modifyObjectLabel(currentNodePid, nameOfNode, "");
                    break;
                case BRIK_IMAGE:
                    fedora.modifyObjectLabel(currentNodePid, "brik-image-" + nameOfNode, "");
                    break;
                case PAGE_IMAGE:
                    fedora.modifyObjectLabel(currentNodePid, "page-image-" + nameOfNode, "");
                    break;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Call to enhanced Fedora's modifyObjectLabel() failed on node '"
                    + getCurrentNode().getName() + "' with PID '" + currentNodePid + "'");
        }
    }
}
