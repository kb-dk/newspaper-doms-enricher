package dk.statsbiblioteket.newspaper.domsenricher.component;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import dk.statsbibliokeket.newspaper.treenode.TreeNodeState;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
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
    private final WebResource restApi;
    private final XPathSelector xpath;
    private Collection<String> xmlDatastreams;

    public MimetypeEnricher(Collection<String> xmlDatastreams, String location, Credentials creds) {
        this.xmlDatastreams = xmlDatastreams;
        xpath = DOM.createXPathSelector("f", FEDORA_NAMESPACE);

        Client client = Client.create();
        restApi = client.resource(location + "/objects/");
        restApi.addFilter(new HTTPBasicAuthFilter(creds.getUsername(), creds.getPassword()));
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
        for (Map.Entry<String, String> stringStringEntry : datastreamMimetypes.entrySet()) {
            if (xmlDatastreams.contains(stringStringEntry.getKey())) {
                if (!stringStringEntry.getValue().equals("text/xml")) {
                    fixDatastream(stringStringEntry.getKey(), event);
                }
            }
        }
    }

    /**
     * Set the datastream mimetype to text/xml
     * @param datastream name
     * @param event the event, ie. the pid
     */
    private void fixDatastream(String datastream, NodeEndParsingEvent event) {
        restApi.path(event.getLocation())
                                       .path("/datastreams/").path(datastream)
                                       .queryParam("mimeType", "text/xml")
                                       .put();
    }

    /**
     * Get a map of datastream ID to datastream mimetype
     * @param event the event, ie. the object
     * @return a map of datastreams and mimetypes
     */
    private Map<String, String> getDatastreamMimetypes(NodeEndParsingEvent event) {
        String datastreamsXml = restApi.path(event.getLocation())
                                       .path("/datastreams")
                                       .queryParam("format", "xml")
                                       .get(String.class);
        Document dom = DOM.stringToDOM(datastreamsXml, true);
        NodeList datastreams = xpath.selectNodeList(dom, "/f:objectDatastreams/f:datastream");
        HashMap<String, String> result = new HashMap<>();
        for (int i = 0; i < datastreams.getLength(); i++) {
            Node datastream = datastreams.item(i);
            final String dsid = datastream.getAttributes().getNamedItem("dsid").getTextContent();
            final String mimetype = datastream.getAttributes()
                                              .getNamedItem("mimeType")
                                              .getTextContent();
            result.put(dsid, mimetype);
        }
        return result;
    }
}
