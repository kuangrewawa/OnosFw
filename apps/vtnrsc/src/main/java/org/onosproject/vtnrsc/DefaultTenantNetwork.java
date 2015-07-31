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

import java.util.Objects;

/**
 * Default TenantNetwork model implementation.
 */
public final class DefaultTenantNetwork implements TenantNetwork {
    private final TenantNetworkId id;
    private final String name;
    private final boolean adminStateUp;
    private final State state;
    private final boolean shared;
    private final Type type;
    private final TenantId tenantID;
    private final boolean routerExternal;
    private final PhysicalNetwork physicalNetwork;
    private final SegmentationId segmentationID;

    /**
     * Creates a neutronNetwork element attributed to the specified provider.
     *
     * @param id identity of the NetworkId
     * @param name the network name
     * @param adminStateUp administrative state of the network
     * @param state the network state
     * @param shared Indicates whether this network is shared across all
     *            tenants,By default,only administrative user can change this
     *            value.
     * @param type the network type
     * @param tenantID tenant ID
     * @param routeExternal network routerExternal.
     * @param physicalNetwork physical network identity of the physicalNetwork.
     * @param segmentationID Segmentation id identity of the SegmentationID
     */
    public DefaultTenantNetwork(TenantNetworkId id, String name,
                                 boolean adminStateUp, State state,
                                 boolean shared, TenantId tenantID,
                                 boolean routerExternal, Type type,
                                 PhysicalNetwork physicalNetwork,
                                 SegmentationId segmentationID) {
        this.id = id;
        this.name = name;
        this.adminStateUp = adminStateUp;
        this.state = state;
        this.shared = shared;
        this.type = type;
        this.tenantID = tenantID;
        this.routerExternal = routerExternal;
        this.physicalNetwork = physicalNetwork;
        this.segmentationID = segmentationID;
    }

    @Override
    public TenantNetworkId id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean adminStateUp() {
        return adminStateUp;
    }

    @Override
    public State state() {
        return state;
    }

    @Override
    public boolean shared() {
        return shared;
    }

    @Override
    public TenantId tenantId() {
        return tenantID;
    }

    @Override
    public boolean routerExternal() {
        return routerExternal;
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public PhysicalNetwork physicalNetwork() {
        return physicalNetwork;
    }

    @Override
    public SegmentationId segmentationId() {
        return segmentationID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, adminStateUp, state, shared, tenantID,
                            routerExternal, type, physicalNetwork,
                            segmentationID);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DefaultTenantNetwork) {
            final DefaultTenantNetwork that = (DefaultTenantNetwork) obj;
            return Objects.equals(this.id, that.id)
                    && Objects.equals(this.name, that.name)
                    && Objects.equals(this.adminStateUp, that.adminStateUp)
                    && Objects.equals(this.state, that.state)
                    && Objects.equals(this.shared, that.shared)
                    && Objects.equals(this.tenantID, that.tenantID)
                    && Objects.equals(this.routerExternal, that.routerExternal)
                    && Objects.equals(this.type, that.type)
                    && Objects.equals(this.physicalNetwork,
                                      that.physicalNetwork)
                    && Objects.equals(this.segmentationID, that.segmentationID);
        }
        return false;
    }

    @Override
    public String toString() {
        return toStringHelper(this).add("id", id).add("name", name)
                .add("adminStateUp", adminStateUp).add("state", state)
                .add("shared", shared).add("tenantID", tenantID)
                .add("routeExternal", routerExternal).add("type", type)
                .add("physicalNetwork", physicalNetwork)
                .add("segmentationID", segmentationID).toString();
    }

}
