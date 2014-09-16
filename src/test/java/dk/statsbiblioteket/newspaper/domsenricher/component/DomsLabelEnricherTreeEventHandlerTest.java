package dk.statsbiblioteket.newspaper.domsenricher.component;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventRunner;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.newspaper.domsenricher.component.enrichers.NodeEnricher;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertTrue;

/**
 *
 */
public class DomsLabelEnricherTreeEventHandlerTest {
    private ResultCollector resultCollector;
    private EnhancedFedora fedora;

    @BeforeMethod(alwaysRun = true)
    public void initialize() {
        fedora = mock(EnhancedFedora.class);
        resultCollector = new ResultCollector("blah", "blah");
    }

    /*
    * TODO Testing here can be improved by doing a test on a known structure (as it is done on a number of xml files in the mods
    * metadata checker of newspaper-batch-metadata checker) - checking that it gives the expected labels on all levels.
    * At the same time, this would work as a kind of documentation of how labels will end up looking.
    */

    @Test
    public void testFullLabelling() throws Exception {
        TreeIterator iterator;
        iterator = mock(TreeIterator.class);
        when(iterator.hasNext()).thenReturn(true, true, true, true, true, true, true, true, true, true, true, true,
                                            true, true, true, true, true, true, true, true, true, true, true, true,
                                            true, true, true, true, true, true, true, true, true, true, true, true,
                                            false);
        when(iterator.next()).thenReturn(new NodeBeginsParsingEvent("B4004-RT1", "uuid:00000000-0000-0000-0000-000000000001"),
                                         new NodeBeginsParsingEvent("newspaper-4004-1", "uuid:00000000-0000-0000-0000-000000000002"),
                                         new NodeBeginsParsingEvent("1970-01-01-01", "uuid:00000000-0000-0000-0000-000000000003"),
                                         new NodeBeginsParsingEvent("newspaper-1970-01-01-01-0001A", "uuid:00000000-0000-0000-0000-000000000004"),
                                         new NodeBeginsParsingEvent("newspaper-1970-01-01-01-0001A.jp2", "uuid:00000000-0000-0000-0000-000000000005"),
                                         new NodeEndParsingEvent("newspaper-1970-01-01-01-0001A.jp2", "uuid:00000000-0000-0000-0000-000000000005"),
                                         new NodeEndParsingEvent("newspaper-1970-01-01-01-0001A", "uuid:00000000-0000-0000-0000-000000000004"),
                                         new NodeBeginsParsingEvent("newspaper-1970-01-01-01-0001B", "uuid:00000000-0000-0000-0000-000000000006"),
                                         new NodeBeginsParsingEvent("newspaper-1970-01-01-01-0001B.jp2", "uuid:00000000-0000-0000-0000-000000000007"),
                                         new NodeEndParsingEvent("newspaper-1970-01-01-01-0001B.jp2", "uuid:00000000-0000-0000-0000-000000000007"),
                                         new NodeEndParsingEvent("newspaper-1970-01-01-01-0001B", "uuid:00000000-0000-0000-0000-000000000006"),
                                         new NodeBeginsParsingEvent("newspaper-1970-01-01-01-0001-brik", "uuid:00000000-0000-0000-0000-000000000008"),
                                         new NodeBeginsParsingEvent("newspaper-1970-01-01-01-0001-brik.jp2", "uuid:00000000-0000-0000-0000-000000000009"),
                                         new NodeEndParsingEvent("newspaper-1970-01-01-01-0001-brik.jp2", "uuid:00000000-0000-0000-0000-000000000009"),
                                         new NodeEndParsingEvent("newspaper-1970-01-01-01-0001-brik", "uuid:00000000-0000-0000-0000-000000000008"),
                                         new NodeEndParsingEvent("1970-01-01-01", "uuid:00000000-0000-0000-0000-000000000003"),
                                         new NodeBeginsParsingEvent("UNMATCHED", "uuid:00000000-0000-0000-0000-000000000010"),
                                         new NodeBeginsParsingEvent("newspaper-4004-1-0002", "uuid:00000000-0000-0000-0000-000000000011"),
                                         new NodeBeginsParsingEvent("newspaper-4004-1-0002.jp2", "uuid:00000000-0000-0000-0000-000000000012"),
                                         new NodeEndParsingEvent("newspaper-4004-1-0002.jp2", "uuid:00000000-0000-0000-0000-000000000012"),
                                         new NodeEndParsingEvent("newspaper-4004-1-0002", "uuid:00000000-0000-0000-0000-000000000011"),
                                         new NodeEndParsingEvent("UNMATCHED", "uuid:00000000-0000-0000-0000-000000000010"),
                                         new NodeBeginsParsingEvent("FILM-ISO-target", "uuid:00000000-0000-0000-0000-000000000013"),
                                         new NodeBeginsParsingEvent("newspaper-4004-1-0003", "uuid:00000000-0000-0000-0000-000000000014"),
                                         new NodeBeginsParsingEvent("newspaper-4004-1-0003.jp2", "uuid:00000000-0000-0000-0000-000000000015"),
                                         new NodeEndParsingEvent("newspaper-4004-1-0003.jp2", "uuid:00000000-0000-0000-0000-000000000015"),
                                         new NodeEndParsingEvent("newspaper-4004-1-0003", "uuid:00000000-0000-0000-0000-000000000014"),
                                         new NodeEndParsingEvent("FILM-ISO-target", "uuid:00000000-0000-0000-0000-000000000013"),
                                         new NodeEndParsingEvent("newspaper-4004-1", "uuid:00000000-0000-0000-0000-000000000002"),
                                         new NodeBeginsParsingEvent("WORKSHIFT-ISO-TARGET", "uuid:00000000-0000-0000-0000-000000000016"),
                                         new NodeBeginsParsingEvent("Target-000001-0001", "uuid:00000000-0000-0000-0000-000000000017"),
                                         new NodeBeginsParsingEvent("Target-000001-0001.jp2", "uuid:00000000-0000-0000-0000-000000000018"),
                                         new NodeEndParsingEvent("Target-000001-0001.jp2", "uuid:00000000-0000-0000-0000-000000000018"),
                                         new NodeEndParsingEvent("Target-000001-0001", "uuid:00000000-0000-0000-0000-000000000017"),
                                         new NodeEndParsingEvent("WORKSHIFT-ISO-TARGET", "uuid:00000000-0000-0000-0000-000000000016"),
                                         new NodeEndParsingEvent("B4004-RT1", "uuid:00000000-0000-0000-0000-000000000001"),
                                         null);
        TreeEventHandler labelHandler = new DomsLabelEnricherTreeEventHandler(fedora, 3);
        new EventRunner(iterator, Arrays.asList(labelHandler), resultCollector).run();
        verify(fedora).modifyObjectLabel(eq("uuid:00000000-0000-0000-0000-000000000001"), eq("batch-B4004-RT1"), eq(NodeEnricher.COMMENT));
        verify(fedora).modifyObjectLabel(eq("uuid:00000000-0000-0000-0000-000000000002"), eq("film-newspaper-4004-1"), eq(NodeEnricher.COMMENT));
        verify(fedora).modifyObjectLabel(eq("uuid:00000000-0000-0000-0000-000000000003"), eq("edition-newspaper-1970-01-01-01"), eq(NodeEnricher.COMMENT));
        verify(fedora).modifyObjectLabel(eq("uuid:00000000-0000-0000-0000-000000000004"), eq("page-newspaper-1970-01-01-01-0001A"), eq(NodeEnricher.COMMENT));
        verify(fedora).modifyObjectLabel(eq("uuid:00000000-0000-0000-0000-000000000005"), eq("page-image-newspaper-1970-01-01-01-0001A"), eq(NodeEnricher.COMMENT));
        verify(fedora).modifyObjectLabel(eq("uuid:00000000-0000-0000-0000-000000000006"), eq("page-newspaper-1970-01-01-01-0001B"), eq(NodeEnricher.COMMENT));
        verify(fedora).modifyObjectLabel(eq("uuid:00000000-0000-0000-0000-000000000007"), eq("page-image-newspaper-1970-01-01-01-0001B"), eq(NodeEnricher.COMMENT));
        verify(fedora).modifyObjectLabel(eq("uuid:00000000-0000-0000-0000-000000000008"), eq("newspaper-1970-01-01-01-0001-brik"), eq(NodeEnricher.COMMENT));
        verify(fedora).modifyObjectLabel(eq("uuid:00000000-0000-0000-0000-000000000009"), eq("brik-image-newspaper-1970-01-01-01-0001-brik"), eq(NodeEnricher.COMMENT));
        verify(fedora).modifyObjectLabel(eq("uuid:00000000-0000-0000-0000-000000000010"), eq("unmatched"), eq(NodeEnricher.COMMENT));
        verify(fedora).modifyObjectLabel(eq("uuid:00000000-0000-0000-0000-000000000011"), eq("page-newspaper-4004-1-0002"), eq(NodeEnricher.COMMENT));
        verify(fedora).modifyObjectLabel(eq("uuid:00000000-0000-0000-0000-000000000012"), eq("page-image-newspaper-4004-1-0002"), eq(NodeEnricher.COMMENT));
        verify(fedora).modifyObjectLabel(eq("uuid:00000000-0000-0000-0000-000000000013"), eq("film-iso-target"), eq(NodeEnricher.COMMENT));
        verify(fedora).modifyObjectLabel(eq("uuid:00000000-0000-0000-0000-000000000014"), eq("film-target-newspaper-4004-1-0003"), eq(NodeEnricher.COMMENT));
        verify(fedora).modifyObjectLabel(eq("uuid:00000000-0000-0000-0000-000000000015"), eq("iso-target-image-newspaper-4004-1-0003.jp2"), eq(NodeEnricher.COMMENT));
        verify(fedora).modifyObjectLabel(eq("uuid:00000000-0000-0000-0000-000000000016"), eq("workshift-iso-target"), eq(NodeEnricher.COMMENT));
        verify(fedora).modifyObjectLabel(eq("uuid:00000000-0000-0000-0000-000000000017"), eq("workshift-target-Target-000001-0001"), eq(NodeEnricher.COMMENT));
        verify(fedora).modifyObjectLabel(eq("uuid:00000000-0000-0000-0000-000000000018"), eq("Target-000001-0001.jp2"), eq(NodeEnricher.COMMENT));
        verifyNoMoreInteractions(fedora);
        assertTrue(resultCollector.isSuccess());
    }

    @Test
    public void testprocessNodeBeginPage() throws Exception {
        DefaultTreeEventHandler labelHandler = new DomsLabelEnricherTreeEventHandler(fedora, 3);

        NodeBeginsParsingEvent page = new NodeBeginsParsingEvent("blabla/mypage");
        labelHandler.handleNodeBegin(page);
        String currentNodePid = page.getLocation();

        verify(fedora).modifyObjectLabel(currentNodePid, "batch-" + "mypage", NodeEnricher.COMMENT);
        verifyNoMoreInteractions(fedora);
    }


}
