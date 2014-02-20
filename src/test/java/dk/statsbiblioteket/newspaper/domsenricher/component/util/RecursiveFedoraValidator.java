package dk.statsbiblioteket.newspaper.domsenricher.component.util;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.generated.Validation;

/**
 * Created by csr on 20/02/14.
 */
public class RecursiveFedoraValidator extends RecursiveFedoraVisitor<Validation> {

    public RecursiveFedoraValidator(EnhancedFedora fedora) {
        super(fedora);
    }

    @Override
    protected Validation visitObject(String pid, boolean doit) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return fedora.validate(pid);
    }
}
