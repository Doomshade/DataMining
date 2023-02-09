package cz.zcu.jsmahy.datamining.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import cz.zcu.jsmahy.datamining.api.*;

import java.io.InputStream;
import java.io.OutputStream;

import static java.util.Objects.requireNonNull;

public class FialaBPSerializer implements DataNodeSerializer {
    public static final String PREFIX = "define([],function(){return";
    public static final String SUFFIX = ";});\r\n";
    // this creates a copy of the object mapper
    public static final ObjectMapper OBJECT_MAPPER = JSONDataNodeSerializer.getJsonObjectMapper();

    private final DataNodeFactory dataNodeFactory;

    @Inject
    public FialaBPSerializer(final DataNodeFactory dataNodeFactory) {
        this.dataNodeFactory = dataNodeFactory;
    }

    @Override
    public DataNodeSerializerTask createSerializerTask(final OutputStream out, final DataNode root) {
        return new FialaBPSerializerTask(out, root);
    }

    @Override
    public DataNodeDeserializerTask createDeserializerTask(final InputStream in) {
        return new FialaBPDeserializerTask(requireNonNull(in), dataNodeFactory);
    }

    @Override
    public String getFileExtension() {
        return "js";
    }


}
