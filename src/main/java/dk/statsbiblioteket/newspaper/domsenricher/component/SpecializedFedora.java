package dk.statsbiblioteket.newspaper.domsenricher.component;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import dk.statsbiblioteket.sbutil.webservices.authentication.Credentials;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;

public class SpecializedFedora {

    private final WebResource restApi;

    public SpecializedFedora(String location, Credentials creds) {
        Client client = Client.create();
        restApi = client.resource(location + "/objects/");
        restApi.addFilter(new HTTPBasicAuthFilter(creds.getUsername(), creds.getPassword()));
    }

    /**
     * Set the datastream mimetype to text/xml
     *
     * @param datastream name
     * @param pid the pid
     */
    public void fixDatastream(String datastream, String pid) {
        restApi.path(pid)
               .path("/datastreams/").path(datastream)
               .queryParam("mimeType", "text/xml")
               .put();
    }

    public String getDatastreamsDom(String pid) {
        return restApi.path(pid).path("/datastreams").queryParam("format", "xml").get(String.class);
    }
}
