package dk.statsbiblioteket.newspaper.domsenricher.component;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.newspaper.domsenricher.component.enrichers.NodeEnricher;
import dk.statsbiblioteket.util.Pair;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DomsEnricherTreeEventHandlerTest {

    EnhancedFedora fedora;
    ResultCollector results;
    String name;
    private String batchPid;
    private String batchID;

    @BeforeMethod
    public void setUp() throws Exception {

        fedora = mock(EnhancedFedora.class);
        results = new ResultCollector("bla", "bla");
        batchID = "5000";
        name = "B" + batchID + "-RT1";
        batchPid = newPid();
    }

    @Test
    public void testRoundTripEnrichNoChildren() throws Exception {

        when(fedora.getXMLDatastreamContents(eq(batchPid), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        batchPid, null, null, null)
                                                                                    );
        DomsEnricherTreeEventHandler enricher = new DomsEnricherTreeEventHandler(fedora, results);

        enricher.handleNodeBegin(new NodeBeginsParsingEvent(name, batchPid));
        enricher.handleNodeEnd(new NodeEndParsingEvent(name, batchPid));
        verify(fedora).modifyDatastreamByValue(
                batchPid, NodeEnricher.RELS_EXT,
                null,
                null,

                batchRelsExt(
                        batchPid,
                        null,
                        null,
                        Arrays.asList(
                                NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                                NodeEnricher.DOMS_CONTENT_MODEL_ROUND_TRIP)).getBytes(),
                new ArrayList<String>(), NodeEnricher.APPLICATION_RDF_XML, NodeEnricher.COMMENT,
                null
                                              );


    }


    @Test
    public void testRoundTripEnrichWithFilmChild() throws Exception {


        String filmPid = newPid();
        String filmName = name + "/" + batchID + "-1";
        when(fedora.getXMLDatastreamContents(eq(batchPid), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        batchPid, list(filmPid), null, null)
                                                                                    );
        when(fedora.getXMLDatastreamContents(eq(filmPid), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        filmPid, null, null, null)
                                                                                   );

        DomsEnricherTreeEventHandler enricher = new DomsEnricherTreeEventHandler(fedora, results);

        enricher.handleNodeBegin(new NodeBeginsParsingEvent(name, batchPid));
        enricher.handleNodeBegin(new NodeBeginsParsingEvent(filmName, filmPid));
        enricher.handleNodeEnd(new NodeEndParsingEvent(filmName, filmPid));
        enricher.handleNodeEnd(new NodeEndParsingEvent(name, batchPid));

        verify(fedora).modifyDatastreamByValue(
                filmPid, NodeEnricher.RELS_EXT,
                null,
                null,

                batchRelsExt(
                        filmPid,
                        null,
                        null,
                        Arrays.asList(
                                NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                                NodeEnricher.DOMS_CONTENT_MODEL_FILM)).getBytes(),
                new ArrayList<String>(), NodeEnricher.APPLICATION_RDF_XML, NodeEnricher.COMMENT,
                null
                                              );

        final String relsExtMigrated = batchRelsExt(
                batchPid, list(filmPid), list(
                        new Pair<Pair<String, String>, String>(
                                new Pair<String, String>(
                                        NodeEnricher.DOMS_NAMESPACE, NodeEnricher.HAS_FILM), filmPid
                        )
                                             ), Arrays.asList(
                        NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                        NodeEnricher.DOMS_CONTENT_MODEL_ROUND_TRIP)
                                                   );
        verify(fedora).modifyDatastreamByValue(
                batchPid,
                NodeEnricher.RELS_EXT, null, null,

                relsExtMigrated.getBytes(), new ArrayList<String>(),
                NodeEnricher.APPLICATION_RDF_XML,
                NodeEnricher.COMMENT, null);


    }


    @Test
    public void testFilmEnrichWithChildren() throws Exception {


        String filmPid = newPid();
        String edition1 = newPid();
        String edition2 = newPid();
        String filmName = name + "/" + batchID + "-1";
        String edition1Name = filmName + "/" + "1795-06-02-01";
        String edition2Name = filmName + "/" + "1795-06-03-01";
        when(fedora.getXMLDatastreamContents(eq(batchPid), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        batchPid, list(filmPid), null, null)
                                                                                    );
        when(fedora.getXMLDatastreamContents(eq(filmPid), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        filmPid, list(edition1, edition2), null, null)
                                                                                   );
        when(fedora.getXMLDatastreamContents(eq(edition1), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        edition1, null, null, null)
                                                                                    );

        when(fedora.getXMLDatastreamContents(eq(edition2), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        edition2, null, null, null)
                                                                                    );

        DomsEnricherTreeEventHandler enricher = new DomsEnricherTreeEventHandler(fedora, results);

        enricher.handleNodeBegin(new NodeBeginsParsingEvent(name, batchPid));
        enricher.handleNodeBegin(new NodeBeginsParsingEvent(filmName, filmPid));
        enricher.handleNodeBegin(new NodeBeginsParsingEvent(edition1Name, edition1));
        enricher.handleNodeEnd(new NodeEndParsingEvent(edition1Name, edition1));
        enricher.handleNodeBegin(new NodeBeginsParsingEvent(edition2Name, edition2));
        enricher.handleNodeEnd(new NodeEndParsingEvent(edition2Name, edition2));
        enricher.handleNodeEnd(new NodeEndParsingEvent(filmName, filmPid));
        enricher.handleNodeEnd(new NodeEndParsingEvent(name, batchPid));

        verify(fedora).modifyDatastreamByValue(
                edition1, NodeEnricher.RELS_EXT,
                null,
                null,

                batchRelsExt(
                        edition1,
                        null,
                        null,
                        Arrays.asList(
                                NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                                NodeEnricher.DOMS_CONTENT_MODEL_EDITION)).getBytes(),
                new ArrayList<String>(), NodeEnricher.APPLICATION_RDF_XML, NodeEnricher.COMMENT,
                null
                                              );

        verify(fedora).modifyDatastreamByValue(
                edition2, NodeEnricher.RELS_EXT,
                null,
                null,

                batchRelsExt(
                        edition2,
                        null,
                        null,
                        Arrays.asList(
                                NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                                NodeEnricher.DOMS_CONTENT_MODEL_EDITION)).getBytes(),
                new ArrayList<String>(), NodeEnricher.APPLICATION_RDF_XML, NodeEnricher.COMMENT,
                null
                                              );


        verify(fedora).modifyDatastreamByValue(
                filmPid,
                NodeEnricher.RELS_EXT, null, null,

                batchRelsExt(
                        filmPid,
                        list(edition1, edition2),
                        list(
                                new Pair<>(
                                        new Pair<>(
                                                NodeEnricher.DOMS_NAMESPACE, NodeEnricher.HAS_EDITION), edition1
                                ), new Pair<>(
                                        new Pair<>(
                                                NodeEnricher.DOMS_NAMESPACE, NodeEnricher.HAS_EDITION), edition2
                                )
                            ),
                        Arrays.asList(
                                NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                                NodeEnricher.DOMS_CONTENT_MODEL_FILM)
                            ).getBytes(), new ArrayList<String>(),
                NodeEnricher.APPLICATION_RDF_XML,
                NodeEnricher.COMMENT, null
                                              );

        verify(fedora).modifyDatastreamByValue(
                batchPid,
                NodeEnricher.RELS_EXT, null, null,

                batchRelsExt(
                        batchPid,
                        list(filmPid),
                        list(
                                new Pair<>(
                                        new Pair<>(
                                                NodeEnricher.DOMS_NAMESPACE, NodeEnricher.HAS_FILM), filmPid
                                )
                            ),
                        Arrays.asList(
                                NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                                NodeEnricher.DOMS_CONTENT_MODEL_ROUND_TRIP)
                            ).getBytes(), new ArrayList<String>(),
                NodeEnricher.APPLICATION_RDF_XML,
                NodeEnricher.COMMENT, null
                                              );


    }


    @Test
    public void testEditionEnrichWithChildren() throws Exception {

        String filmPid = newPid();
        String edition1 = newPid();
        String page1 = newPid();
        String image1 = newPid();

        String filmName = name + "/" + batchID + "-1";
        String edition1Name = filmName + "/" + "1795-06-02-01";
        String pageName = edition1Name + "/" + "adresseavisen1759-1795-06-02-01-0006";
        String imageName = pageName + NodeEnricher.JP2;

        when(fedora.getXMLDatastreamContents(eq(batchPid), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        batchPid, list(filmPid), null, null)
                                                                                    );
        when(fedora.getXMLDatastreamContents(eq(filmPid), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        filmPid, list(edition1), null, null)
                                                                                   );


        when(fedora.getXMLDatastreamContents(eq(edition1), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        edition1, list(page1), null, null)
                                                                                    );

        when(fedora.getXMLDatastreamContents(eq(page1), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        page1, list(image1), null, null)
                                                                                 );

        when(fedora.getXMLDatastreamContents(eq(image1), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        image1, null, null, null)
                                                                                  );


        DomsEnricherTreeEventHandler enricher = new DomsEnricherTreeEventHandler(fedora, results);

        enricher.handleNodeBegin(new NodeBeginsParsingEvent(name, batchPid));
        enricher.handleNodeBegin(new NodeBeginsParsingEvent(filmName, filmPid));
        enricher.handleNodeBegin(new NodeBeginsParsingEvent(edition1Name, edition1));
        enricher.handleNodeBegin(new NodeBeginsParsingEvent(pageName, page1));
        enricher.handleNodeBegin(new NodeBeginsParsingEvent(imageName, image1));
        enricher.handleNodeEnd(new NodeEndParsingEvent(imageName, image1));

        enricher.handleNodeEnd(new NodeEndParsingEvent(pageName, page1));
        enricher.handleNodeEnd(new NodeEndParsingEvent(edition1Name, edition1));
        enricher.handleNodeEnd(new NodeEndParsingEvent(filmName, filmPid));
        enricher.handleNodeEnd(new NodeEndParsingEvent(name, batchPid));


        verify(fedora).modifyDatastreamByValue(
                image1, NodeEnricher.RELS_EXT,
                null,
                null,

                batchRelsExt(
                        image1,
                        null,
                        null,
                        Arrays.asList(
                                NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                                NodeEnricher.DOMS_CONTENT_MODEL_JPEG2000_FILE)).getBytes(),
                new ArrayList<String>(), NodeEnricher.APPLICATION_RDF_XML, NodeEnricher.COMMENT,
                null
                                              );

        final String hasFile = batchRelsExt(
                page1,
                list(image1),
                list(
                        new Pair<>(
                                new Pair<>(
                                        NodeEnricher.DOMS_NAMESPACE, NodeEnricher.HAS_FILE), image1
                        )
                    ),
                Arrays.asList(
                        NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                        NodeEnricher.DOMS_CONTENT_MODEL_PAGE,
                        NodeEnricher.DOMS_CONTENT_MODEL_EDITION_PAGE)
                                           );
        verify(fedora).modifyDatastreamByValue(
                page1, NodeEnricher.RELS_EXT,
                null,
                null,

                hasFile.getBytes(),
                new ArrayList<String>(), NodeEnricher.APPLICATION_RDF_XML, NodeEnricher.COMMENT,
                null);

        verify(fedora).modifyDatastreamByValue(
                edition1,
                NodeEnricher.RELS_EXT, null, null,

                batchRelsExt(
                        edition1, list(page1), list(
                                new Pair<>(
                                        new Pair<>(
                                                NodeEnricher.DOMS_NAMESPACE, NodeEnricher.HAS_PAGE), page1
                                )
                                                   ), Arrays.asList(
                                NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                                NodeEnricher.DOMS_CONTENT_MODEL_EDITION)
                            ).getBytes(), new ArrayList<String>(),
                NodeEnricher.APPLICATION_RDF_XML,
                NodeEnricher.COMMENT, null
                                              );


    }


    @Test
    public void testAll() throws Exception {

        final String avisName = "adresseavisen1759-";

        String workshiftIso = newPid();
        String workshiftIsoTarget = newPid();
        String workshiftIsoTargetImage = newPid();
        String workshiftIsoName = name + "/" + "WORKSHIFT-ISO-TARGET";
        String workshiftIsoTargetName = workshiftIsoName + "/" + "Target-000001-0001";
        String workshiftIsoTargetImageName = workshiftIsoTargetName + NodeEnricher.JP2;

        String filmPid = newPid();
        String edition1 = newPid();
        String page1 = newPid();
        String image1 = newPid();

        String filmName = name + "/" + batchID + "-1";
        final String editionDate = "1795-06-02-01";
        String edition1Name = filmName + "/" + editionDate;

        String pageName = edition1Name + "/" + avisName + editionDate + "-0006";
        String imageName = pageName + NodeEnricher.JP2;


        String filmIsoTarget = newPid();
        String filmIsoTargetName = filmName + "/" + "FILM-ISO-target";
        String filmIsoTargetPage = newPid();
        String filmIsoTargetPageName = filmIsoTargetName + "/" + avisName + batchID + "-1-ISO-0001";
        String filmIsoTargetPageImage = newPid();
        String filmIsoTargetPageImageName = filmIsoTargetPageName + NodeEnricher.JP2;


        String unmatched = newPid();
        String unmatchedName = filmName + "/" + "UNMATCHED";
        String unmatchedPage = newPid();
        String unmatchedPageName = unmatchedName + "/" + avisName + batchID + "-1-0001";
        String unmatchedPageImage = newPid();
        String unmatchedPageImageName = unmatchedPageName + NodeEnricher.JP2;

        when(fedora.getXMLDatastreamContents(eq(batchPid), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        batchPid, list(workshiftIso, filmPid), null, null)
                                                                                    );
        when(fedora.getXMLDatastreamContents(eq(workshiftIso), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        workshiftIso, list(workshiftIsoTarget), null, null)
                                                                                        );
        when(fedora.getXMLDatastreamContents(eq(workshiftIsoTarget), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        workshiftIsoTarget, list(workshiftIsoTargetImage), null, null)
                                                                                              );
        when(fedora.getXMLDatastreamContents(eq(workshiftIsoTargetImage), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        workshiftIsoTargetImage, null, null, null)
                                                                                                   );


        when(fedora.getXMLDatastreamContents(eq(filmPid), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        filmPid, list(edition1, filmIsoTarget,unmatched), null, null)
                                                                                   );

        when(fedora.getXMLDatastreamContents(eq(unmatched), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        unmatched, list(unmatchedPage), null, null)
                                                                                         );

        when(fedora.getXMLDatastreamContents(eq(unmatchedPage), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        unmatchedPage, list(unmatchedPageImage), null, null)
                                                                                             );

        when(fedora.getXMLDatastreamContents(eq(unmatchedPageImage), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        unmatchedPageImage, null, null, null)
                                                                                                  );


        when(fedora.getXMLDatastreamContents(eq(filmIsoTarget), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        filmIsoTarget, list(filmIsoTargetPage), null, null)
                                                                                         );

        when(fedora.getXMLDatastreamContents(eq(filmIsoTargetPage), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        filmIsoTargetPage, list(filmIsoTargetPageImage), null, null)
                                                                                             );

        when(fedora.getXMLDatastreamContents(eq(filmIsoTargetPageImage), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        filmIsoTargetPageImage, null, null, null)
                                                                                                  );


        when(fedora.getXMLDatastreamContents(eq(edition1), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        edition1, list(page1), null, null)
                                                                                    );

        when(fedora.getXMLDatastreamContents(eq(page1), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        page1, list(image1), null, null)
                                                                                 );

        when(fedora.getXMLDatastreamContents(eq(image1), eq(NodeEnricher.RELS_EXT))).thenReturn(
                batchRelsExt(
                        image1, null, null, null)
                                                                                  );


        DomsEnricherTreeEventHandler enricher = new DomsEnricherTreeEventHandler(fedora, results);

        enricher.handleNodeBegin(new NodeBeginsParsingEvent(name, batchPid));

            enricher.handleNodeBegin(new NodeBeginsParsingEvent(workshiftIsoName, workshiftIso));
                enricher.handleNodeBegin(new NodeBeginsParsingEvent(workshiftIsoTargetName, workshiftIsoTarget));
                    enricher.handleNodeBegin(new NodeBeginsParsingEvent(workshiftIsoTargetImageName, workshiftIsoTargetImage));
                    enricher.handleNodeEnd(new NodeEndParsingEvent(workshiftIsoTargetImageName, workshiftIsoTargetImage));
                enricher.handleNodeEnd(new NodeEndParsingEvent(workshiftIsoTargetName, workshiftIsoTarget));
            enricher.handleNodeEnd(new NodeEndParsingEvent(workshiftIsoName, workshiftIso));

            enricher.handleNodeBegin(new NodeBeginsParsingEvent(filmName, filmPid));

                enricher.handleNodeBegin(new NodeBeginsParsingEvent(unmatchedName, unmatched));
                    enricher.handleNodeBegin(new NodeBeginsParsingEvent(unmatchedPageName, unmatchedPage));
                        enricher.handleNodeBegin(new NodeBeginsParsingEvent(unmatchedPageImageName, unmatchedPageImage));
                        enricher.handleNodeEnd(new NodeEndParsingEvent(unmatchedPageImageName, unmatchedPageImage));
                    enricher.handleNodeEnd(new NodeEndParsingEvent(unmatchedPageName, unmatchedPage));
                enricher.handleNodeEnd(new NodeEndParsingEvent(unmatchedName, unmatched));


                enricher.handleNodeBegin(new NodeBeginsParsingEvent(filmIsoTargetName, filmIsoTarget));
                    enricher.handleNodeBegin(new NodeBeginsParsingEvent(filmIsoTargetName, filmIsoTargetPage));
                        enricher.handleNodeBegin(new NodeBeginsParsingEvent(filmIsoTargetPageImageName, filmIsoTargetPageImage));
                        enricher.handleNodeEnd(new NodeEndParsingEvent(filmIsoTargetPageImageName, filmIsoTargetPageImage));
                    enricher.handleNodeEnd(new NodeEndParsingEvent(filmIsoTargetPageName, filmIsoTargetPage));
                enricher.handleNodeEnd(new NodeEndParsingEvent(filmIsoTargetName, filmIsoTarget));

                enricher.handleNodeBegin(new NodeBeginsParsingEvent(edition1Name, edition1));

                    enricher.handleNodeBegin(new NodeBeginsParsingEvent(pageName, page1));
                        enricher.handleNodeBegin(new NodeBeginsParsingEvent(imageName, image1));
                        enricher.handleNodeEnd(new NodeEndParsingEvent(imageName, image1));
                    enricher.handleNodeEnd(new NodeEndParsingEvent(pageName, page1));

                enricher.handleNodeEnd(new NodeEndParsingEvent(edition1Name, edition1));


        enricher.handleNodeEnd(new NodeEndParsingEvent(filmName, filmPid));
        enricher.handleNodeEnd(new NodeEndParsingEvent(name, batchPid));

        verify(fedora).modifyDatastreamByValue(
                workshiftIsoTargetImage, NodeEnricher.RELS_EXT,
                null,
                null,

                batchRelsExt(
                        workshiftIsoTargetImage,
                        null,
                        null,
                        Arrays.asList(
                                NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                                NodeEnricher.DOMS_CONTENT_MODEL_JPEG2000_FILE)).getBytes(),
                new ArrayList<String>(), NodeEnricher.APPLICATION_RDF_XML, NodeEnricher.COMMENT,
                null
                                              );



        verify(fedora).modifyDatastreamByValue(
                workshiftIsoTarget,
                NodeEnricher.RELS_EXT, null, null,

                batchRelsExt(
                        workshiftIsoTarget,
                        list(workshiftIsoTargetImage), list(
                                new Pair<>(
                                        new Pair<>(
                                                NodeEnricher.DOMS_NAMESPACE, NodeEnricher.HAS_FILE), workshiftIsoTargetImage
                                )
                                                      ),
                        Arrays.asList(
                                NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                                NodeEnricher.DOMS_CONTENT_MODEL_PAGE)
                            ).getBytes(), new ArrayList<String>(),
                NodeEnricher.APPLICATION_RDF_XML,
                NodeEnricher.COMMENT, null
                                              );
        verify(fedora).modifyDatastreamByValue(
                workshiftIso,
                NodeEnricher.RELS_EXT, null, null,

                batchRelsExt(
                        workshiftIso,
                        list(workshiftIsoTarget), list(
                                new Pair<>(
                                        new Pair<>(
                                                NodeEnricher.DOMS_NAMESPACE, NodeEnricher.HAS_PAGE), workshiftIsoTarget
                                )
                                                      ),
                        Arrays.asList(
                                NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                                NodeEnricher.DOMS_CONTENT_MODEL_WORKSHIFT)
                            ).getBytes(), new ArrayList<String>(),
                NodeEnricher.APPLICATION_RDF_XML,
                NodeEnricher.COMMENT, null
                                              );


        verify(fedora).modifyDatastreamByValue(
                unmatchedPageImage, NodeEnricher.RELS_EXT,
                null,
                null,

                batchRelsExt(
                        unmatchedPageImage,
                        null,
                        null,
                        Arrays.asList(
                                NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                                NodeEnricher.DOMS_CONTENT_MODEL_JPEG2000_FILE)).getBytes(),
                new ArrayList<String>(), NodeEnricher.APPLICATION_RDF_XML, NodeEnricher.COMMENT,
                null
                                              );


        verify(fedora).modifyDatastreamByValue(
                unmatchedPage,
                NodeEnricher.RELS_EXT, null, null,

                batchRelsExt(
                        unmatchedPage,
                        list(unmatchedPageImage), list(
                                new Pair<>(
                                        new Pair<>(
                                                NodeEnricher.DOMS_NAMESPACE, NodeEnricher.HAS_FILE), unmatchedPageImage
                                )
                                                      ),
                        Arrays.asList(
                                NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                                NodeEnricher.DOMS_CONTENT_MODEL_PAGE,
                                NodeEnricher.DOMS_CONTENT_MODEL_EDITION_PAGE)
                            ).getBytes(), new ArrayList<String>(),
                NodeEnricher.APPLICATION_RDF_XML,
                NodeEnricher.COMMENT, null
                                              );




        verify(fedora).modifyDatastreamByValue(
                unmatched,
                NodeEnricher.RELS_EXT, null, null,

                batchRelsExt(
                        unmatched,
                        list(unmatchedPage), list(
                                new Pair<>(
                                        new Pair<>(
                                                NodeEnricher.DOMS_NAMESPACE, NodeEnricher.HAS_PAGE), unmatchedPage
                                )
                                                     ),
                        Arrays.asList(
                                NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                                NodeEnricher.DOMS_CONTENT_MODEL_UNMATCHED)
                            ).getBytes(), new ArrayList<String>(),
                NodeEnricher.APPLICATION_RDF_XML,
                NodeEnricher.COMMENT, null
                                              );



        verify(fedora).modifyDatastreamByValue(
                filmIsoTargetPageImage, NodeEnricher.RELS_EXT,
                null,
                null,

                batchRelsExt(
                        filmIsoTargetPageImage,
                        null,
                        null,
                        Arrays.asList(
                                NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                                NodeEnricher.DOMS_CONTENT_MODEL_JPEG2000_FILE)).getBytes(),
                new ArrayList<String>(), NodeEnricher.APPLICATION_RDF_XML, NodeEnricher.COMMENT,
                null
                                              );

        verify(fedora).modifyDatastreamByValue(
                filmIsoTargetPage,
                NodeEnricher.RELS_EXT, null, null,

                batchRelsExt(
                        filmIsoTargetPage,
                        list(filmIsoTargetPageImage), list(
                                new Pair<>(
                                        new Pair<>(
                                                NodeEnricher.DOMS_NAMESPACE, NodeEnricher.HAS_FILE), filmIsoTargetPageImage
                                )
                                                          ),
                        Arrays.asList(
                                NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                                NodeEnricher.DOMS_CONTENT_MODEL_PAGE)
                            ).getBytes(), new ArrayList<String>(),
                NodeEnricher.APPLICATION_RDF_XML,
                NodeEnricher.COMMENT, null
                                              );


        verify(fedora).modifyDatastreamByValue(
                filmIsoTarget,
                NodeEnricher.RELS_EXT, null, null,

                batchRelsExt(
                        filmIsoTarget,
                        list(filmIsoTargetPage), list(
                                new Pair<>(
                                        new Pair<>(
                                                NodeEnricher.DOMS_NAMESPACE, NodeEnricher.HAS_PAGE), filmIsoTargetPage
                                )
                                                          ),
                        Arrays.asList(
                                NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                                NodeEnricher.DOMS_CONTENT_MODEL_ISO_TARGET)
                            ).getBytes(), new ArrayList<String>(),
                NodeEnricher.APPLICATION_RDF_XML,
                NodeEnricher.COMMENT, null
                                              );



        verify(fedora).modifyDatastreamByValue(
                image1, NodeEnricher.RELS_EXT,
                null,
                null,

                batchRelsExt(
                        image1,
                        null,
                        null,
                        Arrays.asList(
                                NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                                NodeEnricher.DOMS_CONTENT_MODEL_JPEG2000_FILE)).getBytes(),
                new ArrayList<String>(), NodeEnricher.APPLICATION_RDF_XML, NodeEnricher.COMMENT,
                null
                                              );

        verify(fedora).modifyDatastreamByValue(
                page1, NodeEnricher.RELS_EXT,
                null,
                null,

                batchRelsExt(
                        page1,
                        list(image1),
                        list(
                                new Pair<>(
                                        new Pair<>(
                                                NodeEnricher.DOMS_NAMESPACE, NodeEnricher.HAS_FILE), image1
                                )
                            ),
                        Arrays.asList(
                                NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                                NodeEnricher.DOMS_CONTENT_MODEL_PAGE,
                                NodeEnricher.DOMS_CONTENT_MODEL_EDITION_PAGE)
                                                   ).getBytes(),
                new ArrayList<String>(), NodeEnricher.APPLICATION_RDF_XML, NodeEnricher.COMMENT,
                null);

        verify(fedora).modifyDatastreamByValue(
                edition1,
                NodeEnricher.RELS_EXT, null, null,

                batchRelsExt(
                        edition1, list(page1), list(
                                new Pair<>(
                                        new Pair<>(
                                                NodeEnricher.DOMS_NAMESPACE, NodeEnricher.HAS_PAGE), page1
                                )
                                                   ), Arrays.asList(
                                NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                                NodeEnricher.DOMS_CONTENT_MODEL_EDITION)
                            ).getBytes(), new ArrayList<String>(),
                NodeEnricher.APPLICATION_RDF_XML,
                NodeEnricher.COMMENT, null
                                              );

        verify(fedora).modifyDatastreamByValue(
                filmPid,
                NodeEnricher.RELS_EXT,
                null,
                null,

                batchRelsExt(
                        filmPid,
                        list(edition1, filmIsoTarget, unmatched),
                        list(
                                new Pair<>(
                                        new Pair<>(
                                                NodeEnricher.DOMS_NAMESPACE, NodeEnricher.HAS_PAGE), unmatched
                                ), new Pair<>(
                                        new Pair<>(
                                                NodeEnricher.DOMS_NAMESPACE, NodeEnricher.HAS_ISO_TARGET), filmIsoTarget

                            ), new Pair<>(
                                new Pair<>(
                                        NodeEnricher.DOMS_NAMESPACE, NodeEnricher.HAS_EDITION), edition1
                        )
                            ),
                        Arrays.asList(
                                NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                                NodeEnricher.DOMS_CONTENT_MODEL_FILM)
                            ).getBytes(), new ArrayList<String>(),
                NodeEnricher.APPLICATION_RDF_XML,
                NodeEnricher.COMMENT, null
                                              );


        verify(fedora).modifyDatastreamByValue(
                batchPid,
                NodeEnricher.RELS_EXT, null, null,

                batchRelsExt(
                        batchPid,
                        list(workshiftIso, filmPid),
                        list(
                                new Pair<>(
                                        new Pair<>(
                                                NodeEnricher.DOMS_NAMESPACE, NodeEnricher.HAS_WORKSHIFT), workshiftIso
                                ), new Pair<>(
                                        new Pair<>(
                                                NodeEnricher.DOMS_NAMESPACE, NodeEnricher.HAS_FILM), filmPid
                                )
                            ),
                        Arrays.asList(
                                NodeEnricher.DOMS_CONTENT_MODEL_DOMS,
                                NodeEnricher.DOMS_CONTENT_MODEL_ROUND_TRIP)
                            ).getBytes(), new ArrayList<String>(),
                NodeEnricher.APPLICATION_RDF_XML,
                NodeEnricher.COMMENT, null
                                              );


    }


    private static <T> Iterable<T> list(T... entries) {
        return Arrays.asList(entries);
    }

    private static String newPid() {
        return NodeEnricher.UUID + java.util.UUID.randomUUID().toString();
    }


    private static String batchRelsExt(String pid, Iterable<String> childrenBeforeEnrich,
                                       Iterable<Pair<Pair<String, String>, String>> childrenAfterEnrich,
                                       Iterable<String> contentModels) {
        String result
                = "<rdf:RDF xmlns:doms=\"http://doms.statsbiblioteket.dk/relations/default/0/1/#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                  "  <rdf:Description rdf:about=\"info:fedora/" + pid + "\">\n";
        result += "    <doms:isPartOfCollection rdf:resource=\"info:fedora/doms:Newspaper_Collection\"/>\n";
        if (childrenBeforeEnrich != null) {
            for (String child : childrenBeforeEnrich) {
                result
                        += "    <hasPart xmlns=\"info:fedora/fedora-system:def/relations-external#\" rdf:resource=\"info:fedora/" + child + "\"/>\n";
            }
        }


        if (childrenAfterEnrich != null) {
            for (Pair<Pair<String, String>, String> child : childrenAfterEnrich) {
                result += "<" + child.getLeft().getRight() + " xmlns=\"" + child.getLeft()
                                                                                .getLeft() + "\" rdf:resource=\"info:fedora/" + child
                                  .getRight() + "\"/>";
            }
        }
        if (contentModels != null) {
            for (String contentModel : contentModels) {
                result
                        += "<hasModel xmlns=\"info:fedora/fedora-system:def/model#\" rdf:resource=\"info:fedora/" + contentModel + "\"/>";
            }
        }

        result += "</rdf:Description>\n" + "</rdf:RDF>";
        return result;
    }
}