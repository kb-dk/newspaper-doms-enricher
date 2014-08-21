package dk.statsbiblioteket.newspaper.domsenricher.component;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.generated.Validation;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.newspaper.domsenricher.component.enrichers.NodeEnricher;
import dk.statsbiblioteket.util.Pair;
import dk.statsbiblioteket.util.Strings;

import dk.statsbibliokeket.newspaper.treenode.NodeType;
import dk.statsbibliokeket.newspaper.treenode.TreeNodeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DomsPublisherEventHandler extends TreeNodeState {

    private static Logger logger = LoggerFactory.getLogger(DomsEnricherTreeEventHandler.class);


    private final ResultCollector resultCollector;
    private final int maxThreads;
    public List<Pair<String,String>> pids = new ArrayList<>();

    public boolean shouldPublish = true;
    private static final String comment = NodeEnricher.COMMENT;
    private EnhancedFedora enhancedFedora;

    public DomsPublisherEventHandler(EnhancedFedora enhancedFedora, ResultCollector resultCollector, int maxThreads) {
        this.enhancedFedora = enhancedFedora;
        this.resultCollector = resultCollector;
        this.maxThreads = maxThreads;
    }

    @Override
    protected void processNodeBegin(NodeBeginsParsingEvent event) {
        if (getCurrentNode().getType() != NodeType.BATCH) {
            pids.add(new Pair<>(event.getLocation(), event.getName()));

        }
    }

    @Override
    public void handleFinish() {
        if (!shouldPublish){
            return;
        }

        logger.debug("Starting publishing of {} objects with {} threads", pids.size(),maxThreads);
        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(maxThreads);

        List<Publisher> publishers = asPublishers(pids);

        List<Future<String>> results;
        try {
            results = executorService.invokeAll(publishers);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executorService.shutdown();
        for (int i = 0; i < results.size(); i++) {
            Future<String> result = results.get(i);
            try {
                logger.trace("Published object {}", result.get());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                addFailure(pids.get(i),e.getCause());
            }
        }
        long end = System.currentTimeMillis() - start;

        logger.debug("Spent {} ms publishing {} objects",end,results.size());

    }

    private void addFailure(Pair<String, String> stringStringPair, Throwable e) {
        resultCollector.addFailure(stringStringPair.getRight(),"Exception",
                getClass().getSimpleName(),e.getMessage(), Strings.getStackTrace(e));
    }

    private List<Publisher> asPublishers(Collection<Pair<String,String>> pids) {
        List<Publisher> publishers = new ArrayList<>(pids.size());
        for (Pair<String, String> pid : pids) {
            publishers.add(new Publisher(enhancedFedora,pid.getLeft()));
        }
        return publishers;
    }

    public class Publisher implements Callable<String> {


        EnhancedFedora enhancedFedora;
        private final String pid;


        public Publisher(EnhancedFedora enhancedFedora, String pid) {
            this.enhancedFedora = enhancedFedora;
            this.pid = pid;
        }

        @Override
        public String call() throws Exception {
            enhancedFedora.modifyObjectState(pid,"A",comment);
            logger.debug("Published {}",pid);
            return pid;
        }

        private String toString(Validation result) throws JAXBException {
            StringWriter writer = new StringWriter();
            final Marshaller marshaller = JAXBContext.newInstance(Validation.class).createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(result, writer);
            return writer.toString();
        }
    }


}
