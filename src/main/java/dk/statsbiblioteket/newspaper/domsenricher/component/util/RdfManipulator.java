package dk.statsbiblioteket.newspaper.domsenricher.component.util;

import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import javax.xml.transform.TransformerException;

/**
 *
 */
public class RdfManipulator {
    Document document;
    Node rdfDescriptionNode;

    public static final String MODEL_TEMPLATE =  "<hasModel xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" "
            + "xmlns=\"info:fedora/fedora-system:def/model#\" "
            + "rdf:resource=\"info:fedora/PID\"/>";

    public static final String EXTERNAL_RELATION_TEMPLATE = "<NAME xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" "
            + "xmlns=\"info:fedora/fedora-system:def/relations-external#\" "
            + "rdf:resource=\"info:fedora/PID\"/>";

    public static final XPathSelector X_PATH_SELECTOR = DOM.createXPathSelector(
            "rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
            "doms", "http://doms.statsbiblioteket.dk/relations/default/0/1/#",
            "relsExt", "info:fedora/fedora-system:def/relations-external#"
    );

    public RdfManipulator(String xml) {
        document = DOM.stringToDOM(xml, true);
        rdfDescriptionNode = X_PATH_SELECTOR.selectNode(document, "//rdf:Description");
    }

    @Override
    public String toString() {
        try {
            return DOM.domToString(document);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Incorporates the given fragment of rdf/xml as a rdf:description node of the given document
     * @param fragment The rdf/xml fragment to be added
     */
    private void addFragmentToDescription(String fragment) {
        Document fragmentNode = DOM.stringToDOM(fragment, true);
        Node importedNode = document.importNode(fragmentNode.getDocumentElement(), true);
        rdfDescriptionNode.appendChild(importedNode);
    }

    /**
     * Adds a content model.
     * @param modelPid the full pid of the model, including prefix e.g. "doms:ContentModel_DOMS".
     */
    public void addContentModel(String modelPid) {
        String fragment = MODEL_TEMPLATE.replace("PID", modelPid);
        addFragmentToDescription(fragment);
    }

    /**
     * Adds a new named relation from this object to another object, e.g.
     * <hasPart xmlns="info:fedora/fedora-system:def/relations-external#" rdf:resource="info:fedora/uuid:05d840bf-8bb6-48e5-b214-2ab39f6259f8"/>
     * @param predicateName the name of the relation, e.g. "hasPart"
     * @param objectPid the full doms pid of the object of the relation, e.g. "uuid:05d840bf-8bb6-48e5-b214-2ab39f6259f8"
     */
    public void addExternalRelation(String predicateName, String objectPid) {
        String fragment = EXTERNAL_RELATION_TEMPLATE.replace("NAME", predicateName);
        fragment = fragment.replace("PID", objectPid);
        addFragmentToDescription(fragment);
    }
}
