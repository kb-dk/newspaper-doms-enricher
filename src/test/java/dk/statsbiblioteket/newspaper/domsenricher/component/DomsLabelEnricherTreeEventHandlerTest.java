package dk.statsbiblioteket.newspaper.domsenricher.component;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventRunner;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;

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
        when(iterator.next()).thenReturn(new NodeBeginsParsingEvent("B4004-RT1"),
                                         new NodeBeginsParsingEvent("newspaper-4004-1"),
                                         new NodeBeginsParsingEvent("1970-01-01-01"),
                                         new NodeBeginsParsingEvent("newspaper-1970-01-01-01-0001A"),
                                         new NodeBeginsParsingEvent("newspaper-1970-01-01-01-0001A.jp2"),
                                         new NodeEndParsingEvent("newspaper-1970-01-01-01-0001A.jp2"),
                                         new NodeEndParsingEvent("newspaper-1970-01-01-01-0001A"),
                                         new NodeBeginsParsingEvent("newspaper-1970-01-01-01-0001B"),
                                         new NodeBeginsParsingEvent("newspaper-1970-01-01-01-0001B.jp2"),
                                         new NodeEndParsingEvent("newspaper-1970-01-01-01-0001B.jp2"),
                                         new NodeEndParsingEvent("newspaper-1970-01-01-01-0001B"),
                                         new NodeBeginsParsingEvent("newspaper-1970-01-01-01-0001-brik"),
                                         new NodeBeginsParsingEvent("newspaper-1970-01-01-01-0001-brik.jp2"),
                                         new NodeEndParsingEvent("newspaper-1970-01-01-01-0001-brik.jp2"),
                                         new NodeEndParsingEvent("newspaper-1970-01-01-01-0001-brik"),
                                         new NodeEndParsingEvent("1970-01-01-01"),
                                         new NodeBeginsParsingEvent("UNMATCHED"),
                                         new NodeBeginsParsingEvent("newspaper-4004-1-0002"),
                                         new NodeBeginsParsingEvent("newspaper-4004-1-0002.jp2"),
                                         new NodeEndParsingEvent("newspaper-4004-1-0002.jp2"),
                                         new NodeEndParsingEvent("newspaper-4004-1-0002"),
                                         new NodeEndParsingEvent("UNMATCHED"),
                                         new NodeBeginsParsingEvent("FILM-ISO-target"),
                                         new NodeBeginsParsingEvent("newspaper-4004-1-0003"),
                                         new NodeBeginsParsingEvent("newspaper-4004-1-0003.jp2"),
                                         new NodeEndParsingEvent("newspaper-4004-1-0003.jp2"),
                                         new NodeEndParsingEvent("newspaper-4004-1-0003"),
                                         new NodeEndParsingEvent("FILM-ISO-target"),
                                         new NodeEndParsingEvent("avisid-4004-1"),
                                         new NodeBeginsParsingEvent("WORKSHIFT-ISO-TARGET"),
                                         new NodeBeginsParsingEvent("Target-000001-0001"),
                                         new NodeBeginsParsingEvent("Target-000001-0001.jp2"),
                                         new NodeEndParsingEvent("Target-000001-0001.jp2"),
                                         new NodeEndParsingEvent("Target-000001-0001"),
                                         new NodeEndParsingEvent("WORKSHIFT-ISO-TARGET"),
                                         new NodeEndParsingEvent("B4004-RT1"),
                                         null);
        TreeEventHandler labelHandler = new DomsLabelEnricherTreeEventHandler(fedora);
        new EventRunner(iterator, Arrays.asList(labelHandler), resultCollector).run();
        verify(fedora).modifyObjectLabel(anyString(), eq("batch-B4004-RT1"), anyString());
        verify(fedora).modifyObjectLabel(anyString(), eq("film-newspaper-4004-1"), anyString());
        verify(fedora).modifyObjectLabel(anyString(), eq("edition-newspaper-1970-01-01-01"), anyString());
        verify(fedora).modifyObjectLabel(anyString(), eq("page-newspaper-1970-01-01-01-0001A"), anyString());
        verify(fedora).modifyObjectLabel(anyString(), eq("page-image-newspaper-1970-01-01-01-0001A"), anyString());
        verify(fedora).modifyObjectLabel(anyString(), eq("page-newspaper-1970-01-01-01-0001B"), anyString());
        verify(fedora).modifyObjectLabel(anyString(), eq("page-image-newspaper-1970-01-01-01-0001B"), anyString());
        verify(fedora).modifyObjectLabel(anyString(), eq("newspaper-1970-01-01-01-0001-brik"), anyString());
        verify(fedora).modifyObjectLabel(anyString(), eq("brik-image-newspaper-1970-01-01-01-0001-brik"), anyString());
        verify(fedora).modifyObjectLabel(anyString(), eq("unmatched"), anyString());
        verify(fedora).modifyObjectLabel(anyString(), eq("page-newspaper-4004-1-0002"), anyString());
        verify(fedora).modifyObjectLabel(anyString(), eq("page-image-newspaper-4004-1-0002"), anyString());
        verify(fedora).modifyObjectLabel(anyString(), eq("film-iso-target"), anyString());
        verify(fedora).modifyObjectLabel(anyString(), eq("film-target-newspaper-4004-1-0003"), anyString());
        verify(fedora).modifyObjectLabel(anyString(), eq("iso-target-image-newspaper-4004-1-0003.jp2"), anyString());
        verify(fedora).modifyObjectLabel(anyString(), eq("workshift-iso-target"), anyString());
        verify(fedora).modifyObjectLabel(anyString(), eq("workshift-target-Target-000001-0001"), anyString());
        verify(fedora).modifyObjectLabel(anyString(), eq("Target-000001-0001.jp2"), anyString());
        verifyNoMoreInteractions(fedora);
        assertTrue(resultCollector.isSuccess());
    }

    @Test
    public void testprocessNodeEndPage() throws Exception {
        DefaultTreeEventHandler labelHandler = new DomsLabelEnricherTreeEventHandler(fedora);

        labelHandler.handleNodeBegin(new NodeBeginsParsingEvent("batch1"));
        labelHandler.handleNodeBegin(new NodeBeginsParsingEvent("film1"));
        labelHandler.handleNodeBegin(new NodeBeginsParsingEvent("edition1"));

        NodeBeginsParsingEvent page = new NodeBeginsParsingEvent("page1");

        labelHandler.handleNodeBegin(page);

        labelHandler.handleNodeEnd(new NodeEndParsingEvent("blabla/mypage"));
        String currentNodePid = page.getLocation();

        verify(fedora).modifyObjectLabel(currentNodePid, "batch-" + "batch1", "");
        verify(fedora).modifyObjectLabel(currentNodePid, "film-" + "film1", "");
        verify(fedora).modifyObjectLabel(currentNodePid, "edition--" + "edition1", "");
        verify(fedora).modifyObjectLabel(currentNodePid, "page-" + "page1", "");
        verifyNoMoreInteractions(fedora);
    }


}
