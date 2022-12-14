package cz.zcu.jsmahy.datamining.io.xml.write;

import cz.zcu.jsmahy.datamining.Validate;
import cz.zcu.jsmahy.datamining.io.xml.Constants;
import cz.zcu.jsmahy.datamining.query.infobox.InfoboxTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public final class XMLWriter {
    private static final Logger L = LogManager.getLogger();

    public static void saveInfoboxData(File file, InfoboxTemplate... infoboxTemplates) throws
                                                                                       XMLStreamException,
                                                                                       FileNotFoundException,
                                                                                       IllegalArgumentException {
        Validate.validateArguments(new String[] {"file", "infobox template"}, file, infoboxTemplates);
        FileOutputStream fos = new FileOutputStream(file);
        XMLStreamWriter out = XMLOutputFactory.newInstance()
                                              .createXMLStreamWriter(new OutputStreamWriter(fos,
                                                                                            StandardCharsets.UTF_8
                                              ));
        out.writeStartDocument(StandardCharsets.UTF_8.displayName(), "1.0");
        out.writeStartElement(Constants.ROOT);

        for (InfoboxTemplate ib : infoboxTemplates) {
            out.writeStartElement(Constants.INFOBOX);
            out.writeAttribute(Constants.INFOBOX_NAME, ib.getName());

            writeAttributes(out, ib.getRequiredAttributes(), true);
            writeAttributes(out, ib.getOptionalAttributes(), false);

            out.writeEndElement(); // INFOBOX
        }

        out.writeEndElement(); // ROOT
        out.writeEndDocument();
        out.close();
        L.info(String.format("Saved contents to %s", file.getAbsolutePath()));
    }

    private static void writeAttributes(XMLStreamWriter out, Collection<String> attributes, boolean required) throws
                                                                                                              XMLStreamException {
        for (String attr : attributes) {
            out.writeEmptyElement(Constants.INFOBOX_ATTRIBUTE);
            // TODO try to tell if the attribute is required
            if (required) {
                out.writeAttribute(Constants.INFOBOX_ATTRIBUTE_REQUIRED, String.valueOf(true));
            }
            out.writeAttribute(Constants.INFOBOX_ATTRIBUTE_NAME, attr);
        }
    }
}
