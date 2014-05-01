package dk.statsbiblioteket.newspaper.domsenricher.component;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
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

    protected static final String RELS_EXT = "RELS-EXT";
    protected static final String APPLICATION_RDF_XML = "application/rdf+xml";
    protected static final String UUID = "uuid:";
    protected static final String DOMS_CONTENT_MODEL_DOMS = "doms:ContentModel_DOMS";
    protected static final String DOMS_CONTENT_MODEL_ROUND_TRIP = "doms:ContentModel_RoundTrip";
    protected static final String DOMS_CONTENT_MODEL_FILM = "doms:ContentModel_Film";
    protected static final String DOMS_NAMESPACE = "http://doms.statsbiblioteket.dk/relations/default/0/1/#";
    protected static final String COMMENT = "Modified by GenericNodeEnricher";
    protected static final String DOMS_CONTENT_MODEL_EDITION = "doms:ContentModel_Edition";
    protected static final String DOMS_CONTENT_MODEL_PAGE = "doms:ContentModel_Page";
    protected static final String DOMS_CONTENT_MODEL_EDITION_PAGE = "doms:ContentModel_EditionPage";
    protected static final String DOMS_CONTENT_MODEL_JPEG2000_FILE = "doms:ContentModel_Jpeg2000File";
    protected static final String JP2 = ".jp2";
    protected static final String HAS_FILM = "hasFilm";
    protected static final String HAS_EDITION = "hasEdition";
    protected static final String HAS_FILE = "hasFile";
    protected static final String COMMENT1 = "Modified by EditionPageEnricher";
    protected static final String HAS_PAGE = "hasPage";
    protected static final String COMMENT2 = "Modified by BarePageEnricher";
    protected static final String DOMS_CONTENT_MODEL_WORKSHIFT = "doms:ContentModel_Workshift";
    protected static final String DOMS_CONTENT_MODEL_UNMATCHED = "doms:ContentModel_Unmatched";
    protected static final String DOMS_CONTENT_MODEL_ISO_TARGET = "doms:ContentModel_IsoTarget";
    protected static final String HAS_ISO_TARGET = "hasIsoTarget";
    protected static final String HAS_WORKSHIFT = "hasWorkshift";
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

        when(fedora.getXMLDatastreamContents(eq(batchPid), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        batchPid, null, null, null)
                                                                                    );
        DomsEnricherTreeEventHandler enricher = new DomsEnricherTreeEventHandler(fedora, results);

        enricher.handleNodeBegin(new NodeBeginsParsingEvent(name, batchPid));
        enricher.handleNodeEnd(new NodeEndParsingEvent(name, batchPid));
        verify(fedora).modifyDatastreamByValue(
                batchPid,
                RELS_EXT,
                null,
                null,

                batchRelsExt(
                        batchPid,
                        null,
                        null,
                        Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_ROUND_TRIP)).getBytes(),
                new ArrayList<String>(),
                APPLICATION_RDF_XML,
                COMMENT,
                null
                                              );


    }


    @Test
    public void testRoundTripEnrichWithFilmChild() throws Exception {


        String filmPid = newPid();
        String filmName = name + "/" + batchID + "-1";
        when(fedora.getXMLDatastreamContents(eq(batchPid), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        batchPid, list(filmPid), null, null)
                                                                                    );
        when(fedora.getXMLDatastreamContents(eq(filmPid), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        filmPid, null, null, null)
                                                                                   );

        DomsEnricherTreeEventHandler enricher = new DomsEnricherTreeEventHandler(fedora, results);

        enricher.handleNodeBegin(new NodeBeginsParsingEvent(name, batchPid));
        enricher.handleNodeBegin(new NodeBeginsParsingEvent(filmName, filmPid));
        enricher.handleNodeEnd(new NodeEndParsingEvent(filmName, filmPid));
        enricher.handleNodeEnd(new NodeEndParsingEvent(name, batchPid));

        verify(fedora).modifyDatastreamByValue(
                filmPid,
                RELS_EXT,
                null,
                null,

                batchRelsExt(
                        filmPid,
                        null,
                        null,
                        Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_FILM)).getBytes(),
                new ArrayList<String>(),
                APPLICATION_RDF_XML,
                COMMENT,
                null
                                              );

        final String relsExtMigrated = batchRelsExt(
                batchPid, list(filmPid), list(
                        new Pair<Pair<String, String>, String>(
                                new Pair<String, String>(
                                        DOMS_NAMESPACE, HAS_FILM), filmPid
                        )
                                             ), Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_ROUND_TRIP)
                                                   );
        verify(fedora).modifyDatastreamByValue(
                batchPid, RELS_EXT, null, null,

                relsExtMigrated.getBytes(), new ArrayList<String>(), APPLICATION_RDF_XML, COMMENT, null);


    }


    @Test
    public void testFilmEnrichWithChildren() throws Exception {


        String filmPid = newPid();
        String edition1 = newPid();
        String edition2 = newPid();
        String filmName = name + "/" + batchID + "-1";
        String edition1Name = filmName + "/" + "1795-06-02-01";
        String edition2Name = filmName + "/" + "1795-06-03-01";
        when(fedora.getXMLDatastreamContents(eq(batchPid), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        batchPid, list(filmPid), null, null)
                                                                                    );
        when(fedora.getXMLDatastreamContents(eq(filmPid), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        filmPid, list(edition1, edition2), null, null)
                                                                                   );
        when(fedora.getXMLDatastreamContents(eq(edition1), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        edition1, null, null, null)
                                                                                    );

        when(fedora.getXMLDatastreamContents(eq(edition2), eq(RELS_EXT))).thenReturn(
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
                edition1,
                RELS_EXT,
                null,
                null,

                batchRelsExt(
                        edition1,
                        null,
                        null,
                        Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_EDITION)).getBytes(),
                new ArrayList<String>(),
                APPLICATION_RDF_XML,
                COMMENT,
                null
                                              );

        verify(fedora).modifyDatastreamByValue(
                edition2,
                RELS_EXT,
                null,
                null,

                batchRelsExt(
                        edition2,
                        null,
                        null,
                        Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_EDITION)).getBytes(),
                new ArrayList<String>(),
                APPLICATION_RDF_XML,
                COMMENT,
                null
                                              );


        verify(fedora).modifyDatastreamByValue(
                filmPid, RELS_EXT, null, null,

                batchRelsExt(
                        filmPid,
                        list(edition1, edition2),
                        list(
                                new Pair<>(
                                        new Pair<>(
                                                DOMS_NAMESPACE, HAS_EDITION), edition1
                                ), new Pair<>(
                                        new Pair<>(
                                                DOMS_NAMESPACE, HAS_EDITION), edition2
                                )
                            ),
                        Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_FILM)
                            ).getBytes(), new ArrayList<String>(), APPLICATION_RDF_XML, COMMENT, null
                                              );

        verify(fedora).modifyDatastreamByValue(
                batchPid, RELS_EXT, null, null,

                batchRelsExt(
                        batchPid,
                        list(filmPid),
                        list(
                                new Pair<>(
                                        new Pair<>(
                                                DOMS_NAMESPACE, HAS_FILM), filmPid
                                )
                            ),
                        Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_ROUND_TRIP)
                            ).getBytes(), new ArrayList<String>(), APPLICATION_RDF_XML, COMMENT, null
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
        String imageName = pageName + JP2;

        when(fedora.getXMLDatastreamContents(eq(batchPid), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        batchPid, list(filmPid), null, null)
                                                                                    );
        when(fedora.getXMLDatastreamContents(eq(filmPid), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        filmPid, list(edition1), null, null)
                                                                                   );


        when(fedora.getXMLDatastreamContents(eq(edition1), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        edition1, list(page1), null, null)
                                                                                    );

        when(fedora.getXMLDatastreamContents(eq(page1), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        page1, list(image1), null, null)
                                                                                 );

        when(fedora.getXMLDatastreamContents(eq(image1), eq(RELS_EXT))).thenReturn(
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
                image1,
                RELS_EXT,
                null,
                null,

                batchRelsExt(
                        image1,
                        null,
                        null,
                        Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_JPEG2000_FILE)).getBytes(),
                new ArrayList<String>(),
                APPLICATION_RDF_XML,
                COMMENT,
                null
                                              );

        final String hasFile = batchRelsExt(
                page1,
                list(image1),
                list(
                        new Pair<>(
                                new Pair<>(
                                        DOMS_NAMESPACE, HAS_FILE), image1
                        )
                    ),
                Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_PAGE, DOMS_CONTENT_MODEL_EDITION_PAGE)
                                           );
        verify(fedora).modifyDatastreamByValue(
                page1,
                RELS_EXT,
                null,
                null,

                hasFile.getBytes(),
                new ArrayList<String>(),
                APPLICATION_RDF_XML, COMMENT1,
                null);

        verify(fedora).modifyDatastreamByValue(
                edition1, RELS_EXT, null, null,

                batchRelsExt(
                        edition1, list(page1), list(
                                new Pair<>(
                                        new Pair<>(
                                                DOMS_NAMESPACE, HAS_PAGE), page1
                                )
                                                   ), Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_EDITION)
                            ).getBytes(), new ArrayList<String>(), APPLICATION_RDF_XML, COMMENT, null
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
        String workshiftIsoTargetImageName = workshiftIsoTargetName + JP2;

        String filmPid = newPid();
        String edition1 = newPid();
        String page1 = newPid();
        String image1 = newPid();

        String filmName = name + "/" + batchID + "-1";
        final String editionDate = "1795-06-02-01";
        String edition1Name = filmName + "/" + editionDate;

        String pageName = edition1Name + "/" + avisName + editionDate + "-0006";
        String imageName = pageName + JP2;


        String filmIsoTarget = newPid();
        String filmIsoTargetName = filmName + "/" + "FILM-ISO-target";
        String filmIsoTargetPage = newPid();
        String filmIsoTargetPageName = filmIsoTargetName + "/" + avisName + batchID + "-1-ISO-0001";
        String filmIsoTargetPageImage = newPid();
        String filmIsoTargetPageImageName = filmIsoTargetPageName + JP2;


        String unmatched = newPid();
        String unmatchedName = filmName + "/" + "UNMATCHED";
        String unmatchedPage = newPid();
        String unmatchedPageName = unmatchedName + "/" + avisName + batchID + "-1-0001";
        String unmatchedPageImage = newPid();
        String unmatchedPageImageName = unmatchedPageName + JP2;

        when(fedora.getXMLDatastreamContents(eq(batchPid), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        batchPid, list(workshiftIso, filmPid), null, null)
                                                                                    );
        when(fedora.getXMLDatastreamContents(eq(workshiftIso), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        workshiftIso, list(workshiftIsoTarget), null, null)
                                                                                        );
        when(fedora.getXMLDatastreamContents(eq(workshiftIsoTarget), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        workshiftIsoTarget, list(workshiftIsoTargetImage), null, null)
                                                                                              );
        when(fedora.getXMLDatastreamContents(eq(workshiftIsoTargetImage), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        workshiftIsoTargetImage, null, null, null)
                                                                                                   );


        when(fedora.getXMLDatastreamContents(eq(filmPid), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        filmPid, list(edition1, filmIsoTarget,unmatched), null, null)
                                                                                   );

        when(fedora.getXMLDatastreamContents(eq(unmatched), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        unmatched, list(unmatchedPage), null, null)
                                                                                         );

        when(fedora.getXMLDatastreamContents(eq(unmatchedPage), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        unmatchedPage, list(unmatchedPageImage), null, null)
                                                                                             );

        when(fedora.getXMLDatastreamContents(eq(unmatchedPageImage), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        unmatchedPageImage, null, null, null)
                                                                                                  );


        when(fedora.getXMLDatastreamContents(eq(filmIsoTarget), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        filmIsoTarget, list(filmIsoTargetPage), null, null)
                                                                                         );

        when(fedora.getXMLDatastreamContents(eq(filmIsoTargetPage), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        filmIsoTargetPage, list(filmIsoTargetPageImage), null, null)
                                                                                             );

        when(fedora.getXMLDatastreamContents(eq(filmIsoTargetPageImage), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        filmIsoTargetPageImage, null, null, null)
                                                                                                  );


        when(fedora.getXMLDatastreamContents(eq(edition1), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        edition1, list(page1), null, null)
                                                                                    );

        when(fedora.getXMLDatastreamContents(eq(page1), eq(RELS_EXT))).thenReturn(
                batchRelsExt(
                        page1, list(image1), null, null)
                                                                                 );

        when(fedora.getXMLDatastreamContents(eq(image1), eq(RELS_EXT))).thenReturn(
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
                workshiftIsoTargetImage,
                RELS_EXT,
                null,
                null,

                batchRelsExt(
                        workshiftIsoTargetImage,
                        null,
                        null,
                        Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_JPEG2000_FILE)).getBytes(),
                new ArrayList<String>(),
                APPLICATION_RDF_XML,
                COMMENT,
                null
                                              );



        verify(fedora).modifyDatastreamByValue(
                workshiftIsoTarget, RELS_EXT, null, null,

                batchRelsExt(
                        workshiftIsoTarget,
                        list(workshiftIsoTargetImage), list(
                                new Pair<>(
                                        new Pair<>(
                                                DOMS_NAMESPACE, HAS_FILE), workshiftIsoTargetImage
                                )
                                                      ),
                        Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_PAGE)
                            ).getBytes(), new ArrayList<String>(), APPLICATION_RDF_XML, COMMENT2, null
                                              );
        verify(fedora).modifyDatastreamByValue(
                workshiftIso, RELS_EXT, null, null,

                batchRelsExt(
                        workshiftIso,
                        list(workshiftIsoTarget), list(
                                new Pair<>(
                                        new Pair<>(
                                                DOMS_NAMESPACE, HAS_PAGE), workshiftIsoTarget
                                )
                                                      ),
                        Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_WORKSHIFT)
                            ).getBytes(), new ArrayList<String>(), APPLICATION_RDF_XML, COMMENT, null
                                              );


        verify(fedora).modifyDatastreamByValue(
                unmatchedPageImage,
                RELS_EXT,
                null,
                null,

                batchRelsExt(
                        unmatchedPageImage,
                        null,
                        null,
                        Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_JPEG2000_FILE)).getBytes(),
                new ArrayList<String>(),
                APPLICATION_RDF_XML,
                COMMENT,
                null
                                              );


        verify(fedora).modifyDatastreamByValue(
                unmatchedPage, RELS_EXT, null, null,

                batchRelsExt(
                        unmatchedPage,
                        list(unmatchedPageImage), list(
                                new Pair<>(
                                        new Pair<>(
                                                DOMS_NAMESPACE, HAS_FILE), unmatchedPageImage
                                )
                                                      ),
                        Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_PAGE, DOMS_CONTENT_MODEL_EDITION_PAGE)
                            ).getBytes(), new ArrayList<String>(), APPLICATION_RDF_XML, COMMENT1, null
                                              );




        verify(fedora).modifyDatastreamByValue(
                unmatched, RELS_EXT, null, null,

                batchRelsExt(
                        unmatched,
                        list(unmatchedPage), list(
                                new Pair<>(
                                        new Pair<>(
                                                DOMS_NAMESPACE, HAS_PAGE), unmatchedPage
                                )
                                                     ),
                        Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_UNMATCHED)
                            ).getBytes(), new ArrayList<String>(), APPLICATION_RDF_XML, COMMENT, null
                                              );



        verify(fedora).modifyDatastreamByValue(
                filmIsoTargetPageImage,
                RELS_EXT,
                null,
                null,

                batchRelsExt(
                        filmIsoTargetPageImage,
                        null,
                        null,
                        Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_JPEG2000_FILE)).getBytes(),
                new ArrayList<String>(),
                APPLICATION_RDF_XML,
                COMMENT,
                null
                                              );

        verify(fedora).modifyDatastreamByValue(
                filmIsoTargetPage, RELS_EXT, null, null,

                batchRelsExt(
                        filmIsoTargetPage,
                        list(filmIsoTargetPageImage), list(
                                new Pair<>(
                                        new Pair<>(
                                                DOMS_NAMESPACE, HAS_FILE), filmIsoTargetPageImage
                                )
                                                          ),
                        Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_PAGE)
                            ).getBytes(), new ArrayList<String>(), APPLICATION_RDF_XML, COMMENT2, null
                                              );


        verify(fedora).modifyDatastreamByValue(
                filmIsoTarget, RELS_EXT, null, null,

                batchRelsExt(
                        filmIsoTarget,
                        list(filmIsoTargetPage), list(
                                new Pair<>(
                                        new Pair<>(
                                                DOMS_NAMESPACE, HAS_PAGE), filmIsoTargetPage
                                )
                                                          ),
                        Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_ISO_TARGET)
                            ).getBytes(), new ArrayList<String>(), APPLICATION_RDF_XML, COMMENT, null
                                              );



        verify(fedora).modifyDatastreamByValue(
                image1,
                RELS_EXT,
                null,
                null,

                batchRelsExt(
                        image1,
                        null,
                        null,
                        Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_JPEG2000_FILE)).getBytes(),
                new ArrayList<String>(),
                APPLICATION_RDF_XML,
                COMMENT,
                null
                                              );

        verify(fedora).modifyDatastreamByValue(
                page1,
                RELS_EXT,
                null,
                null,

                batchRelsExt(
                        page1,
                        list(image1),
                        list(
                                new Pair<>(
                                        new Pair<>(
                                                DOMS_NAMESPACE, HAS_FILE), image1
                                )
                            ),
                        Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_PAGE, DOMS_CONTENT_MODEL_EDITION_PAGE)
                                                   ).getBytes(),
                new ArrayList<String>(),
                APPLICATION_RDF_XML, COMMENT1,
                null);

        verify(fedora).modifyDatastreamByValue(
                edition1, RELS_EXT, null, null,

                batchRelsExt(
                        edition1, list(page1), list(
                                new Pair<>(
                                        new Pair<>(
                                                DOMS_NAMESPACE, HAS_PAGE), page1
                                )
                                                   ), Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_EDITION)
                            ).getBytes(), new ArrayList<String>(), APPLICATION_RDF_XML, COMMENT, null
                                              );

        verify(fedora).modifyDatastreamByValue(
                filmPid,
                RELS_EXT,
                null,
                null,

                batchRelsExt(
                        filmPid,
                        list(edition1, filmIsoTarget, unmatched),
                        list(
                                new Pair<>(
                                        new Pair<>(
                                                DOMS_NAMESPACE, HAS_PAGE), unmatched
                                ), new Pair<>(
                                        new Pair<>(
                                                DOMS_NAMESPACE, HAS_ISO_TARGET), filmIsoTarget

                            ), new Pair<>(
                                new Pair<>(
                                        DOMS_NAMESPACE, HAS_EDITION), edition1
                        )
                            ),
                        Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_FILM)
                            ).getBytes(), new ArrayList<String>(), APPLICATION_RDF_XML, COMMENT, null
                                              );


        verify(fedora).modifyDatastreamByValue(
                batchPid, RELS_EXT, null, null,

                batchRelsExt(
                        batchPid,
                        list(workshiftIso, filmPid),
                        list(
                                new Pair<>(
                                        new Pair<>(
                                                DOMS_NAMESPACE, HAS_WORKSHIFT), workshiftIso
                                ), new Pair<>(
                                        new Pair<>(
                                                DOMS_NAMESPACE, HAS_FILM), filmPid
                                )
                            ),
                        Arrays.asList(DOMS_CONTENT_MODEL_DOMS, DOMS_CONTENT_MODEL_ROUND_TRIP)
                            ).getBytes(), new ArrayList<String>(), APPLICATION_RDF_XML, COMMENT, null
                                              );


    }


    private static <T> Iterable<T> list(T... entries) {
        return Arrays.asList(entries);
    }

    private static String newPid() {
        return UUID + java.util.UUID.randomUUID().toString();
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