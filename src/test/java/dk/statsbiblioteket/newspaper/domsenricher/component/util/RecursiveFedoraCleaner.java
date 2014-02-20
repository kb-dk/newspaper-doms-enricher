package dk.statsbiblioteket.newspaper.domsenricher.component.util;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.FedoraRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 *
 */
public class RecursiveFedoraCleaner extends RecursiveFedoraVisitor<Boolean> {

    private static Logger log = LoggerFactory.getLogger(RecursiveFedoraCleaner.class);

    public RecursiveFedoraCleaner(EnhancedFedora fedora) {
        super(fedora);
    }


    public Boolean visitObject(String pid, boolean doit) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        log.info("About to delete object '" + pid + "'");
        if (doit) {
            try {
                deleteSingleObject(pid);
            } catch (Exception e) {
                log.warn("Could not delete " + pid, e);
                return false;
            }
        } else {
            log.info("Didn't actually delete object '" + pid + "'");
            return false;
        }
       return true;
    }

    public void deleteSingleObject(String pid) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        fedora.deleteObject(pid, "Deleted in integration test");
    }

}
