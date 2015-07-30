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
package org.onosproject.app.vtnrsc.web;

import static com.google.common.base.Preconditions.checkNotNull;

import org.onosproject.app.vtnrsc.VirtualPort;
import org.onosproject.codec.CodecContext;
import org.onosproject.codec.JsonCodec;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * VirtualPort JSON codec.
 */
public final class VirtualPortCodec extends JsonCodec<VirtualPort> {
    @Override
    public ObjectNode encode(VirtualPort vPort, CodecContext context) {
        checkNotNull(vPort, "VPort cannot be null");
        ObjectNode result = context
                .mapper()
                .createObjectNode()
                .put("id", vPort.portId().toString())
                .put("networkId", vPort.networkId().toString())
                .put("adminStateUp", vPort.adminStateUp().toString())
                .put("name", vPort.name().toString())
                .put("state", vPort.state().toString())
                .put("macAddress", vPort.macAddress().toString())
                .put("tenantId", vPort.tenantId().toString())
                .put("deviceId", vPort.deviceId().toString())
                .put("deviceOwner", vPort.deviceOwner().toString())
                .put("bindingVnicType", vPort.bindingVnicType().toString())
                .put("bindingVifType", vPort.bindingVifType().toString())
                .put("bindingHostId", vPort.bindingHostId().toString())
                .put("bindingvifDetails", vPort.bindingvifDetails().toString());
        result.set("allowedAddressPairs", new AllowedAddressPairCodec().encode(
                                                                               vPort.allowedAddressPairs(), context));
        result.set("fixedIp", new FixedIpCodec().encode(
                                                        vPort.fixedIps(), context));
        result.set("securityGroups", new SecurityGroupCodec().encode(
                                                        vPort.securityGroups(), context));
        return result;
    }
}
