package dk.statsbiblioteket.newspaper.domsenricher.component.enrichers;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import sun.reflect.generics.tree.Tree;

/**
 *
 */
public class NodeEnricherFactory {

    private EnhancedFedora fedora;

    public NodeEnricherFactory(EnhancedFedora fedora) {
        this.fedora = fedora;
    }

    public AbstractNodeEnricher getNodeEnricher(NodeType nodeType) {
        switch (nodeType) {
            case BATCH:
                return new BatchNodeEnricher(fedora);
            case WORKSHIFT_ISO_TARGET:
                throw new RuntimeException("not implemented");
            case WORKSHIFT_TARGET:
                throw new RuntimeException("not implemented");
                
            case TARGET_IMAGE:
                throw new RuntimeException("not implemented");
                
            case FILM:
                throw new RuntimeException("not implemented");
                
            case FILM_ISO_TARGET:
                throw new RuntimeException("not implemented");
                
            case FILM_TARGET:
                throw new RuntimeException("not implemented");
                
            case ISO_TARGET_IMAGE:
                throw new RuntimeException("not implemented");
                
            case UNMATCHED:
                throw new RuntimeException("not implemented");
                
            case EDITION:
                throw new RuntimeException("not implemented");
                
            case PAGE:
                throw new RuntimeException("not implemented");
                
            case BRIK:
                throw new RuntimeException("not implemented");
                
            case BRIK_IMAGE:
                throw new RuntimeException("not implemented");
                
            case PAGE_IMAGE:
                throw new RuntimeException("not implemented");
                
        }        
        throw new RuntimeException("not implemented");
    }

    
    
}
