package dk.statsbiblioteket.newspaper.domsenricher.component;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.newspaper.domsenricher.component.enrichers.NodeEnricher;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class MimetypeEnricherTest {

    /**
     * Test normal case. Three objects are found, all are published.
     *
     * @throws Exception
     */
    @Test
    public void testPublishing() throws Exception {
        SpecializedFedora fedora = mock(SpecializedFedora.class);
        MimetypeEnricher enricher = new MimetypeEnricher(Arrays.asList("MIX","MODS"),fedora);
        when(fedora.getDatastreamsDom("uuid:test1")).thenReturn("<objectDatastreams  xmlns=\"http://www.fedora.info/definitions/1/0/access/\" pid=\"doms:ContentModel_DOMS\"><datastream dsid=\"MIX\" label=\"Dublin Core Record for this object\" mimeType=\"text/xml2\" /></objectDatastreams>");
        when(fedora.getDatastreamsDom("uuid:test2")).thenReturn("<objectDatastreams  xmlns=\"http://www.fedora.info/definitions/1/0/access/\" pid=\"doms:ContentModel_DOMS\"><datastream dsid=\"MODS\" label=\"Dublin Core Record for this object\" mimeType=\"text/xml2\" /></objectDatastreams>");

        enricher.handleNodeBegin(new NodeBeginsParsingEvent("dsfds", "uuid:test2"));
        enricher.handleNodeBegin(new NodeBeginsParsingEvent("dsfds", "uuid:test1"));
        enricher.handleNodeEnd(new NodeEndParsingEvent("dsfds", "uuid:test1"));
        enricher.handleNodeEnd(new NodeEndParsingEvent("dsfds", "uuid:test2"));

        verify(fedora).fixDatastream("MIX","uuid:test1");
        verify(fedora).fixDatastream("MODS", "uuid:test2");
   }

}