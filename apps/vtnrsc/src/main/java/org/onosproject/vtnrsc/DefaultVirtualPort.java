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
package org.onosproject.vtnrsc;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.onlab.packet.MacAddress;
import org.onosproject.net.DeviceId;
import org.onosproject.net.HostId;

/**
 * Default infrastructure vPort model implementation.
 */
public final class DefaultVirtualPort implements VirtualPort {
    private final VirtualPortId id;
    private final TenantNetworkId networkId;
    private final boolean adminStateUp;
    private final String name;
    private final State state;
    private final MacAddress macAddress;
    private final TenantId tenantID;
    private final String deviceOwner;
    private final DeviceId deviceId;
    private final FixedIp fixedIp;
    private final HostId bindingHostId;
    private final String bindingvnicType;
    private final String bindingvifType;
    private final String bindingvifDetails;
    private final Collection<AllowedAddressPair> allowedAddressPairs;
    private final Collection<SecurityGroup> securityGroups;

    /**
     * Creates a vPort element attributed to the specified provider.
     *
     * @param providerId
     */
    public DefaultVirtualPort(VirtualPortId id,
                              TenantNetworkId networkId,
                              Boolean adminStateUp,
                              Map<String, String> strMap,
                              State state,
                              MacAddress macAddress,
                              TenantId tenantID,
                              DeviceId deviceId,
                              FixedIp fixedIp,
                              HostId bindingHostId,
                              Collection<AllowedAddressPair> allowedAddressPairs,
                              Collection<SecurityGroup> securityGroups) {
        this.id = id;
        this.networkId = networkId;
        this.adminStateUp = adminStateUp;
        this.name = strMap.get("name");
        this.state = state;
        this.macAddress = macAddress;
        this.tenantID = tenantID;
        this.deviceOwner = strMap.get("deviceOwner");
        this.deviceId = deviceId;
        this.fixedIp = fixedIp;
        this.bindingHostId = bindingHostId;
        this.bindingvnicType = strMap.get("bindingvnicType");
        this.bindingvifType = strMap.get("bindingvifType");
        this.bindingvifDetails = strMap.get("bindingvifDetails");
        this.allowedAddressPairs = allowedAddressPairs;
        this.securityGroups = securityGroups;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, networkId, adminStateUp, name, state,
                            macAddress, tenantID, deviceId, deviceOwner,
                            allowedAddressPairs, fixedIp, bindingHostId,
                            bindingvnicType, bindingvifType,
                            bindingvifDetails, securityGroups);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DefaultVirtualPort) {
            final DefaultVirtualPort that = (DefaultVirtualPort) obj;
            return Objects.equals(this.id, that.id)
                    && Objects.equals(this.networkId, that.networkId)
                    && Objects.equals(this.adminStateUp, that.adminStateUp)
                    && Objects.equals(this.state, that.state)
                    && Objects.equals(this.name, that.name)
                    && Objects.equals(this.tenantID, that.tenantID)
                    && Objects.equals(this.macAddress, that.macAddress)
                    && Objects.equals(this.deviceId, that.deviceId)
                    && Objects.equals(this.deviceOwner, that.deviceOwner)
                    && Objects.equals(this.allowedAddressPairs,
                                      that.allowedAddressPairs)
                    && Objects.equals(this.fixedIp, that.fixedIp)
                    && Objects.equals(this.bindingHostId, that.bindingHostId)
                    && Objects.equals(this.bindingvifDetails,
                                      that.bindingvifDetails)
                    && Objects.equals(this.bindingvifType, that.bindingvifType)
                    && Objects.equals(this.tenantID, that.tenantID)
                    && Objects.equals(this.securityGroups, that.securityGroups);
        }
        return false;
    }

    @Override
    public String toString() {
        return toStringHelper(this).add("id", id).add("network_id", networkId)
                .add("adminStateUp", adminStateUp).add("state", state)
                .add("name", name).add("state", state)
                .add("macAddress", macAddress).add("tenantID", tenantID)
                .add("deviced", deviceId).add("deviceOwner", deviceOwner)
                .add("allowedAddressPairs", allowedAddressPairs)
                .add("fixedIp", fixedIp).add("bindingHostId", bindingHostId)
                .add("bindingvnicType", bindingvnicType)
                .add("bindingvnicDetails", bindingvifDetails)
                .add("securityGroups", securityGroups).toString();
    }

    @Override
    public VirtualPortId portId() {
        return id;
    }

    @Override
    public TenantNetworkId networkId() {
        return networkId;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Boolean adminStateUp() {
        return adminStateUp;
    }

    @Override
    public State state() {
        return state;
    }

    @Override
    public MacAddress macAddress() {
        return macAddress;
    }

    @Override
    public TenantId tenantId() {
        return tenantID;
    }

    @Override
    public DeviceId deviceId() {
        return deviceId;
    }

    @Override
    public String deviceOwner() {
        return deviceOwner;
    }

    @Override
    public Collection<AllowedAddressPair> allowedAddressPairs() {
        return allowedAddressPairs;
    }

    @Override
    public FixedIp fixedIps() {
        return fixedIp;
    }

    @Override
    public HostId bindingHostId() {
        return bindingHostId;
    }

    @Override
    public String bindingVnicType() {
        return bindingvifType;
    }

    @Override
    public String bindingVifType() {
        return bindingvnicType;
    }

    @Override
    public String bindingvifDetails() {
        return bindingvifDetails;
    }

    @Override
    public Collection<SecurityGroup> securityGroups() {
        return securityGroups;
    }

}
