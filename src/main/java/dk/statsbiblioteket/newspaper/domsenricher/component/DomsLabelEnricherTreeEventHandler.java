package dk.statsbiblioteket.newspaper.domsenricher.component;

import dk.statsbibliokeket.newspaper.treenode.TreeNodeStateWithChildren;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Attaches somewhat descriptive labels to different types of nodes.
 */
public class DomsLabelEnricherTreeEventHandler extends TreeNodeStateWithChildren {
    private EnhancedFedora fedora;
    private String previousAvisID;
    private String previousBrikName;
    private String previousPageName;


    public DomsLabelEnricherTreeEventHandler(EnhancedFedora fedora) {
        this.fedora = fedora;
        previousAvisID = "";
    }


    /**
     * Sets the object label for the object at the current node, by a call to Fedora.
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
                    fedora.modifyObjectLabel(currentNodePid, "edition-" + previousAvisID, "");
                    break;
                case PAGE:
                    fedora.modifyObjectLabel(currentNodePid, "page-" + nameOfNode, "");
                    previousPageName = nameOfNode;
                    // Put aside AvisID for use later
                    Pattern p = Pattern.compile("(.*)-[0-9]{4}(-[0-9]{2})?(-[0-9]{2})?-[0-9]{2}-[0-9]{4}[A-Za-z]?");
                    Matcher m = p.matcher(nameOfNode);
                    if (m.find()) {
                        previousAvisID = m.group(1);
                    } else {
                        previousAvisID = "";
                    }
                    break;
                case BRIK:
                    fedora.modifyObjectLabel(currentNodePid, nameOfNode, "");
                    previousBrikName = nameOfNode;
                    break;
                case BRIK_IMAGE:
                    fedora.modifyObjectLabel(currentNodePid, "brik-image-" + previousBrikName, "");
                    break;
                case PAGE_IMAGE:
                    fedora.modifyObjectLabel(currentNodePid, "page-image-" + previousPageName, "");
                    break;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Call to enhanced Fedora's modifyObjectLabel() failed on node '"
                    + getCurrentNode().getName() + "' with PID '" + currentNodePid + "'");
        }
    }
}
