/*
 * Copyright 2015 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.ovsdb.lib.notation.json;

import java.io.IOException;

import org.onosproject.ovsdb.lib.notation.UUID;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * UUID Serializer.
 */
public class UUIDSerializer extends JsonSerializer<UUID> {
    @Override
    public void serialize(UUID value, JsonGenerator generator,
                          SerializerProvider provider)
            throws IOException, JsonProcessingException {
        generator.writeStartArray();
        try {
            java.util.UUID.fromString(value.value());
            generator.writeString("uuid");
        } catch (IllegalArgumentException ex) {
            generator.writeString("named-uuid");
        }
        generator.writeString(value.value());
        generator.writeEndArray();
    }
}
