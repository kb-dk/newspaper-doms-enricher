package dk.statsbiblioteket.newspaper.domsenricher.component;

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
import dk.statsbiblioteket.newspaper.domsenricher.component.enrichers.NodeEnricherFactory;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;

/**
 *
 */
public class DomsEnricherTreeEventHandler extends TreeNodeState {

    private ResultCollector resultCollector;
    private NodeEnricherFactory nodeEnricherFactory;

    public DomsEnricherTreeEventHandler(EnhancedFedora fedora, ResultCollector resultCollector) {
        nodeEnricherFactory = new NodeEnricherFactory(fedora);
        this.resultCollector = resultCollector;
    }

    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        super.handleNodeBegin(event);
        try {
            nodeEnricherFactory.getNodeEnricher(getCurrentNode().getType()).enrich(event);
        } catch (Exception e) {
            //resultCollector.addFailure();
           //Add message to result collector
        }
    }


}
