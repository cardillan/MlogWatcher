package mlogwatcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import mlogwatcher.websocket.api.LogicProcessor;
import mlogwatcher.websocket.api.ProcessorExtractResults;
import mlogwatcher.websocket.api.ProcessorUpdateResults;
import mlogwatcher.websocket.api.ProgramId;
import mlogwatcher.websocket.api.PutSchematicInLibraryParams;
import mlogwatcher.websocket.api.Request;
import mlogwatcher.websocket.api.Response;
import mlogwatcher.websocket.api.TextResult;
import mlogwatcher.websocket.api.UpdateProcessorsOnMapParams;
import mlogwatcher.websocket.api.UpdateSelectedProcessorParams;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonDocGenerator {
    SchemaGenerator generator = createGenerator();
    StringBuilder sb = new StringBuilder();

    public static final String FILE = "CLASSES.md";

    private static final String HEADER = """
            # JSON Schema
            
            This document describes the structure of classes used by the WebSocket API.
            
            """;

    private static final String REQUESTS = """
            ## Requests
            
            These classes are used when sending requests to the server.
            
            """;

    private static final String RESPONSES = """
            ## Responses
            
            These classes are used by responses sent back from the server.
            
            """;

    private static final String COMMON = """
            ## Common
            
            These classes hold other objects used by the interface.
            
            """;

    private static final String CLASS = """
            ### Class %s
            
            Structure of the %<s class:
            
            ```json
            %s
            ```
            
            """;

    private void documentClass(Class<?> clazz) {
        JsonNode jsonSchema = generator.generateSchema(clazz);
        String string = jsonSchema.toPrettyString();
        sb.append(String.format(CLASS, clazz.getSimpleName(), string));
    }

    private void generate() {
        sb.append(HEADER);

        sb.append(REQUESTS);
        documentClass(Request.class);
        documentClass(UpdateSelectedProcessorParams.class);
        documentClass(UpdateProcessorsOnMapParams.class);
        documentClass(PutSchematicInLibraryParams.class);

        sb.append(RESPONSES);
        documentClass(Response.class);
        documentClass(TextResult.class);
        documentClass(ProcessorUpdateResults.class);
        documentClass(ProcessorExtractResults.class);

        sb.append(COMMON);
        documentClass(LogicProcessor.class);
        documentClass(ProgramId.class);



        try {
            Files.write(Paths.get(FILE), sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new JsonDocGenerator().generate();
    }

    private static SchemaGenerator createGenerator() {
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(
                SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON);

        SchemaGeneratorConfig config = configBuilder.build();
        return new SchemaGenerator(config);
    }
}
