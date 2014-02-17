package dk.statsbiblioteket.newspaper.domsenricher.component;

import dk.statsbibliokeket.newspaper.treenode.NodeType;
import dk.statsbibliokeket.newspaper.treenode.TreeNode;
import dk.statsbibliokeket.newspaper.treenode.TreeNodeStateWithChildren;
import dk.statsbibliokeket.newspaper.treenode.TreeNodeWithChildren;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 *
 */
public class DomsRelationshipEnricherTreeEventHandler extends TreeNodeStateWithChildren {

    private static Logger logger = LoggerFactory.getLogger(DomsRelationshipEnricherTreeEventHandler.class);

    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        List<TreeNodeWithChildren> children = ((TreeNodeWithChildren) getCurrentNode()).getChildren();
        logger.debug("Node {} has {} children.", event.getName(), children.size());
        super.handleNodeEnd(event);
    }
}
