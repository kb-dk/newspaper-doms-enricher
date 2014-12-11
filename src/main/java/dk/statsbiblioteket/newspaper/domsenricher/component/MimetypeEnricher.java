package dk.statsbiblioteket.newspaper.domsenricher.component;

import com.sun.jersey.api.client.WebResource;
import dk.statsbibliokeket.newspaper.treenode.TreeNodeState;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MimetypeEnricher extends TreeNodeState{

    protected static final String FEDORA_NAMESPACE = "http://www.fedora.info/definitions/1/0/access/";
    protected static final String DATASTREAM_NAME = "dsid";
    protected static final String MIME_TYPE = "mimeType";
    private final XPathSelector xpath;
    private Collection<String> xmlDatastreams;
    private final SpecializedFedora fedora;

    public MimetypeEnricher(Collection<String> xmlDatastreams, SpecializedFedora fedora) {
        this.xmlDatastreams = xmlDatastreams;
        this.fedora = fedora;
        xpath = DOM.createXPathSelector("f", FEDORA_NAMESPACE);

    }

    @Override
    protected void processNodeEnd(NodeEndParsingEvent event) {
        fixMimeTypes(event);
    }

    /**
     * fix mimetypes for all datastreams in the xmlDatastreams list
     * @param event the object
     */
    public void fixMimeTypes(NodeEndParsingEvent event) {
        Map<String, String> datastreamMimetypes = getDatastreamMimetypes(event);
        for (Map.Entry<String, String> datastreamAndMimetype : datastreamMimetypes.entrySet()) {
            if (xmlDatastreams.contains(datastreamAndMimetype.getKey())) {
                if (!datastreamAndMimetype.getValue().equals("text/xml")) {
                    fedora.fixDatastream(datastreamAndMimetype.getKey(), event.getLocation());
                }
            }
        }
    }



    /**
     * Get a map of datastream ID to datastream mimetype
     * @param event the event, ie. the object
     * @return a map of datastreams and mimetypes
     */
    private Map<String, String> getDatastreamMimetypes(NodeEndParsingEvent event) {
        String datastreamsXml = fedora.getDatastreamsDom(event.getLocation());
        Document dom = DOM.stringToDOM(datastreamsXml, true);
        NodeList datastreams = xpath.selectNodeList(dom, "/f:objectDatastreams/f:datastream");
        HashMap<String, String> result = new HashMap<>();
        for (int i = 0; i < datastreams.getLength(); i++) {
            Node datastream = datastreams.item(i);
            final String dsid = datastream.getAttributes().getNamedItem(DATASTREAM_NAME).getTextContent();
            final String mimetype = datastream.getAttributes()
                                              .getNamedItem(MIME_TYPE)
                                              .getTextContent();
            result.put(dsid, mimetype);
        }
        return result;
    }

}
