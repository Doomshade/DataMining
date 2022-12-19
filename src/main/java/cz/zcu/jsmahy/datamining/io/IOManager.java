package cz.zcu.jsmahy.datamining.io;

import cz.zcu.jsmahy.datamining.io.xml.write.XMLWriter;
import cz.zcu.jsmahy.datamining.query.infobox.InfoboxTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static cz.zcu.jsmahy.datamining.io.IOValidator.validateFileName;
import static java.util.Objects.requireNonNull;

public final class IOManager {
    public static final String INFOBOX_EXT = ".xml";
    private static final Logger L = LogManager.getRootLogger();
    private static final File DATA_FOLDER = new File("data");
    private static final File INFOBOX_FOLDER = new File(DATA_FOLDER, "infobox");


    public static void saveInfoboxData(String filename, Collection<InfoboxTemplate> infoboxTemplates)
            throws IOException, XMLStreamException {
        saveInfoboxData(filename, infoboxTemplates.toArray(new InfoboxTemplate[0]));
    }


    public static void saveInfoboxData(String fileName, InfoboxTemplate... infoboxTemplates) throws IOException, XMLStreamException {
        validateFileName(fileName);
        requireNonNull(infoboxTemplates);

        if (!fileName.endsWith(INFOBOX_EXT)) {
            fileName = fileName.concat(INFOBOX_EXT);
        }

        File file = getFile(INFOBOX_FOLDER, fileName);
        L.info(String.format("Saving infobox data to %s...", file.getAbsolutePath()));
        XMLWriter.saveInfoboxData(file, infoboxTemplates);
    }

    private static File getFile(File folder, String fileName) throws IOException {
        validateFileName(fileName);
        File file = new File(getFolder(folder), fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    private static File getFolder(File folder) throws IOException {
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new IOException(
                        String.format("Could not create folder %s", folder.getAbsolutePath()));
            }
        } else if (!folder.isDirectory()) {
            throw new IllegalArgumentException(
                    String.format("%s is not a folder!", folder.getAbsolutePath()));
        }
        return folder;
    }
}
