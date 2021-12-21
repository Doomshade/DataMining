package git.doomshade.datamining.io.xml.write;

import git.doomshade.datamining.data.infobox.InfoboxTemplate;
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
import java.util.Objects;

import static git.doomshade.datamining.io.xml.Constants.*;

public final class XMLWriter {
    private static final Logger L = LogManager.getLogger();

    public static void saveInfoboxData(File file, InfoboxTemplate... infoboxTemplates) throws XMLStreamException, FileNotFoundException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(infoboxTemplates);

        FileOutputStream fos = new FileOutputStream(file);
        XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter(
                new OutputStreamWriter(fos, StandardCharsets.UTF_8));
        out.writeStartDocument(StandardCharsets.UTF_8.displayName(), "1.0");
        out.writeStartElement(ROOT);

        for (InfoboxTemplate ib : infoboxTemplates) {
            out.writeStartElement(INFOBOX);
            out.writeAttribute(INFOBOX_NAME, ib.getName());

            writeAttributes(out, ib.getRequiredAttributes(), true);
            writeAttributes(out, ib.getOptionalAttributes(), false);

            out.writeEndElement(); // INFOBOX
        }

        out.writeEndElement(); // ROOT
        out.writeEndDocument();
        out.close();
        L.info(String.format("Saved contents to %s", file.getAbsolutePath()));
    }

    private static void writeAttributes(XMLStreamWriter out, Collection<String> attributes, boolean required) throws XMLStreamException {
        for (String attr : attributes) {
            out.writeEmptyElement(INFOBOX_ATTRIBUTE);
            // TODO try to tell if the attribute is required
            if (required) {
                out.writeAttribute(INFOBOX_ATTRIBUTE_REQUIRED, String.valueOf(true));
            }
            out.writeAttribute(INFOBOX_ATTRIBUTE_NAME, attr);
        }
    }
}
