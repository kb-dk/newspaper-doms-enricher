package dk.statsbiblioteket.newspaper.domsenricher.component.util;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.generated.Datastream;
import dk.statsbiblioteket.doms.central.connectors.fedora.generated.DatastreamProblems;
import dk.statsbiblioteket.doms.central.connectors.fedora.generated.Problems;
import dk.statsbiblioteket.doms.central.connectors.fedora.generated.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Class for validating all fedora objects in a tree navigable via "hasPart" relations,
 */
public class RecursiveFedoraValidator extends RecursiveFedoraVisitor<Validation> {

    private static Logger logger = LoggerFactory.getLogger(RecursiveFedoraValidator.class);


    public RecursiveFedoraValidator(EnhancedFedora fedora) {
        super(fedora);
    }

    /**
     * Validate an object.
     *
     * @param pid  the object to validate.
     * @param doit ignored.
     *
     * @return the result of the validation.
     * @throws BackendInvalidCredsException
     * @throws BackendMethodFailedException
     * @throws BackendInvalidResourceException
     */
    @Override
    protected Validation visitObject(String pid, boolean doit) throws
                                                               BackendInvalidCredsException,
                                                               BackendMethodFailedException,
                                                               BackendInvalidResourceException {
        Validation validation = fedora.validate(pid);
        if (!validation.isValid()) {
            Problems problems = validation.getProblems();
            for (String problem : problems.getProblem()) {

                logger.debug("In {}: {}.", pid, problem);

            }
            DatastreamProblems datastreamProblems = validation.getDatastreamProblems();
            List<Datastream> datastreams = datastreamProblems.getDatastream();
            for (Datastream datastream : datastreams) {
                for (String problem : datastream.getProblem()) {

                    logger.debug("In {}/datastreams/{}: {}", pid, datastream.getDatastreamID(), problem);
                }
            }
        } else {
            logger.debug("Pid {} is valid in regards to contents models {}", pid,validation.getContentModels().getModel());
        }


        return validation;
    }
}
