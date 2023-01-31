package cz.zcu.jsmahy.datamining.util;

import cz.zcu.jsmahy.datamining.api.DataNode;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class RDFNodeUtil {
    public static final String SPECIAL_CHARACTERS = "_";
    private static final Logger LOGGER = LogManager.getLogger(RDFNodeUtil.class);

    /**
     * <p>Formats the {@link RDFNode} to be "pretty" on output.</p>
     * <p>This method will strip any domain off the {@link RDFNode} if it's a {@link Resource}. If it's a {@link Literal}, this will
     * simply return {@link Literal#toString()}. If it's neither, it will return {@link RDFNode#toString()}.</p>
     *
     * @param node the node to format. if null, {@code "null"} is returned.
     *
     * @return {@link RDFNode} in a simple {@link String} representation
     */
    public static String formatRDFNode(RDFNode node) {
        if (node == null) {
            return "null";
        }
        final Marker marker = MarkerManager.getMarker("node-type");
        if (node.isLiteral()) {
            String str = node.asLiteral()
                             .toString();
            final int languageIndex = str.lastIndexOf('@');
            if (languageIndex > 0) {
                str = str.substring(0, languageIndex);
            }
            LOGGER.trace(marker, "Literal \"{}\"", str);
            return str;
        }
        if (node.isURIResource()) {
            final Resource resource = node.asResource();
            final String uri = resource.getURI();
            final int lastPartIndex = uri.lastIndexOf('/') + 1;

            final String localName = uri.substring(lastPartIndex);
            LOGGER.trace(marker, "Resource \"{}\"", localName);
            return localName;
        }

        LOGGER.debug(marker, "RDFNode \"{}\" was neither literal or resource, using default toString method.", node);
        return node.toString();
    }

    public static void setDataNodeNameFromRDFNode(DataNode dataNode, RDFNode rdfNode) {
        if (dataNode == null || rdfNode == null) {
            return;
        }
        dataNode.addMetadata("name", formatRDFNode(rdfNode).replaceAll(SPECIAL_CHARACTERS, " "));
        if (rdfNode.isURIResource()) {
            final String uri = rdfNode.asResource()
                                      .getURI();
            dataNode.addMetadata("uri", uri);
        }
    }
}
