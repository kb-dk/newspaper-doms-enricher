package dk.statsbiblioteket.newspaper.domsenricher.component;

import dk.statsbibliokeket.newspaper.treenode.TreeNodeStateWithChildren;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
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
    private Logger log = LoggerFactory.getLogger(getClass());

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
                    modifyObjectLabel(currentNodePid, "batch-" + nameOfNode);
                    break;
                case WORKSHIFT_ISO_TARGET:
                    modifyObjectLabel(currentNodePid, "workshift-iso-target");
                    break;
                case WORKSHIFT_TARGET:
                    modifyObjectLabel(currentNodePid, "workshift-target-" + nameOfNode);
                    break;
                case TARGET_IMAGE:
                    modifyObjectLabel(currentNodePid, nameOfNode);
                    break;
                case FILM:
                    modifyObjectLabel(currentNodePid, "film-" + nameOfNode);
                    // Put aside AvisID for use later
                    Matcher m = AVIS_PATTERN.matcher(nameOfNode);
                    if (m.find()) {
                        avisID = m.group(1);
                    } else {
                        avisID = "";
                    }
                    break;
                case FILM_ISO_TARGET:
                    modifyObjectLabel(currentNodePid, "film-iso-target");
                    break;
                case FILM_TARGET:
                    modifyObjectLabel(currentNodePid, "film-target-" + nameOfNode);
                    break;
                case ISO_TARGET_IMAGE:
                    modifyObjectLabel(currentNodePid, "iso-target-image-" + nameOfNode);
                    break;
                case UNMATCHED:
                    modifyObjectLabel(currentNodePid, "unmatched");
                    break;
                case EDITION:
                    modifyObjectLabel(currentNodePid, "edition-" + avisID + "-" + nameOfNode);
                    break;
                case PAGE:
                    modifyObjectLabel(currentNodePid, "page-" + nameOfNode);
                    previousPageName = nameOfNode;
                    break;
                case BRIK:
                    modifyObjectLabel(currentNodePid, nameOfNode);
                    previousBrikName = nameOfNode;
                    break;
                case BRIK_IMAGE:
                    modifyObjectLabel(currentNodePid, "brik-image-" + previousBrikName);
                    break;
                case PAGE_IMAGE:
                    modifyObjectLabel(currentNodePid, "page-image-" + previousPageName);
                    break;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Call to enhanced Fedora's modifyObjectLabel() failed on node '"
                    + getCurrentNode().getName() + "' with PID '" + currentNodePid + "'");
        }
    }

    private void modifyObjectLabel(String pid, String label)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        fedora.modifyObjectLabel(pid, label, COMMENT);
        return;
    }
}
