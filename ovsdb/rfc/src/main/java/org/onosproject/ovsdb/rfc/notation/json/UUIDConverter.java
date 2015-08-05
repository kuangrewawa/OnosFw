package org.onosproject.ovsdb.rfc.notation.json;

import org.onosproject.ovsdb.rfc.notation.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.StdConverter;

public class UUIDConverter extends StdConverter<JsonNode, UUID> {

    @Override
    public UUID convert(JsonNode json) {
        return UUID.uuid(json.get(1).asText());
    }
}
