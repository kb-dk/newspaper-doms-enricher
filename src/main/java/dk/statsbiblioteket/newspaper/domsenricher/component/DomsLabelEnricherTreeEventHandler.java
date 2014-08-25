package dk.statsbiblioteket.newspaper.domsenricher.component;

import dk.statsbibliokeket.newspaper.treenode.TreeNodeStateWithChildren;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.newspaper.domsenricher.component.enrichers.NodeEnricher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Attaches somewhat descriptive labels to different types of nodes.
 */
public class DomsLabelEnricherTreeEventHandler extends TreeNodeStateWithChildren {
    private static final String COMMENT = NodeEnricher.COMMENT;
    private EnhancedFedora fedora;
    private String avisID;
    private String previousBrikName;
    private String previousPageName;
    private static final Pattern AVIS_PATTERN = Pattern.compile("(.*)-[0-9]*-[0-9]*$");

    public DomsLabelEnricherTreeEventHandler(EnhancedFedora fedora) {
        this.fedora = fedora;
        avisID = "";
    }


    /**
     * Sets the object label for the object at the current node, by a call to Fedora.
     *
     * @param event the event identifying this node.
     */
    @Override
    public void processNodeBegin(NodeBeginsParsingEvent event) {
        String currentNodePid = getCurrentNode().getLocation();
        int lastSlashPosition = event.getName().lastIndexOf("/");
        String nameOfNode = event.getName().substring(lastSlashPosition + 1);

        try {
            switch (getCurrentNode().getType()) {
                case BATCH:
                    fedora.modifyObjectLabel(currentNodePid, "batch-" + nameOfNode, COMMENT);
                    break;
                case WORKSHIFT_ISO_TARGET:
                    fedora.modifyObjectLabel(currentNodePid, "workshift-iso-target", COMMENT);
                    break;
                case WORKSHIFT_TARGET:
                    fedora.modifyObjectLabel(currentNodePid, "workshift-target-" + nameOfNode, COMMENT);
                    break;
                case TARGET_IMAGE:
                    fedora.modifyObjectLabel(currentNodePid, nameOfNode, COMMENT);
                    break;
                case FILM:
                    fedora.modifyObjectLabel(currentNodePid, "film-" + nameOfNode, COMMENT);
                    // Put aside AvisID for use later
                    Matcher m = AVIS_PATTERN.matcher(nameOfNode);
                    if (m.find()) {
                        avisID = m.group(1);
                    } else {
                        avisID = "";
                    }
                    break;
                case FILM_ISO_TARGET:
                    fedora.modifyObjectLabel(currentNodePid, "film-iso-target", COMMENT);
                    break;
                case FILM_TARGET:
                    fedora.modifyObjectLabel(currentNodePid, "film-target-" + nameOfNode, COMMENT);
                    break;
                case ISO_TARGET_IMAGE:
                    fedora.modifyObjectLabel(currentNodePid, "iso-target-image-" + nameOfNode, COMMENT);
                    break;
                case UNMATCHED:
                    fedora.modifyObjectLabel(currentNodePid, "unmatched", COMMENT);
                    break;
                case EDITION:
                    fedora.modifyObjectLabel(currentNodePid, "edition-" + avisID + "-" + nameOfNode, COMMENT);
                    break;
                case PAGE:
                    fedora.modifyObjectLabel(currentNodePid, "page-" + nameOfNode, COMMENT);
                    previousPageName = nameOfNode;
                    break;
                case BRIK:
                    fedora.modifyObjectLabel(currentNodePid, nameOfNode, COMMENT);
                    previousBrikName = nameOfNode;
                    break;
                case BRIK_IMAGE:
                    fedora.modifyObjectLabel(currentNodePid, "brik-image-" + previousBrikName, COMMENT);
                    break;
                case PAGE_IMAGE:
                    fedora.modifyObjectLabel(currentNodePid, "page-image-" + previousPageName, COMMENT);
                    break;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Call to enhanced Fedora's modifyObjectLabel() failed on node '"
                    + getCurrentNode().getName() + "' with PID '" + currentNodePid + "'");
        }
    }
}
