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
package org.onosproject.vtnrsc.cli.network;

import java.util.HashSet;
import java.util.Set;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.vtnrsc.DefaultTenantNetwork;
import org.onosproject.vtnrsc.PhysicalNetwork;
import org.onosproject.vtnrsc.SegmentationId;
import org.onosproject.vtnrsc.TenantId;
import org.onosproject.vtnrsc.TenantNetwork;
import org.onosproject.vtnrsc.TenantNetworkId;
import org.onosproject.vtnrsc.tenantnetwork.TenantNetworkService;

/**
 * Supports for updating a TenantNetworks.
 */
@Command(scope = "onos", name = "tenantNetwork-update",
        description = "Supports for updating TenantNetworks")
public class TenantNetworkUpdateCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "id", description = "TenantNetwork network id", required = true,
            multiValued = false)
    String id = null;

    @Argument(index = 1, name = "name", description = "TenantNetwork name", required = true,
            multiValued = false)
    String name = null;

    @Argument(index = 2, name = "adminStateUp", description = "TenantNetwork adminStateUp is true or false",
            required = true, multiValued = false)
    boolean adminStateUp = false;

    @Argument(index = 3, name = "state", description = "The state of TenantNetwork",
            required = true, multiValued = false)
    String state = null;

    @Argument(index = 4, name = "shared", description = "TenantNetwork is shared or not",
            required = true, multiValued = false)
    boolean shared = false;

    @Argument(index = 5, name = "tenantID", description = "The tenant id of TenantNetwork",
            required = true, multiValued = false)
    String tenantID = null;

    @Argument(index = 6, name = "routerExternal",
            description = "TenantNetwork is routerExternal or not", required = true,
            multiValued = false)
    boolean routerExternal = false;

    @Argument(index = 7, name = "type", description = "The type of TenantNetwork", required = true,
            multiValued = false)
    String type = null;

    @Argument(index = 8, name = "physicalNetwork", description = "The physical network of Tenant",
            required = true, multiValued = false)
    String physicalNetwork = null;

    @Argument(index = 9, name = "segmentationID", description = "The segmentation id of TenantNetwork",
            required = true, multiValued = false)
    String segmentationID = null;

    @Override
    protected void execute() {
        TenantNetworkService service = get(TenantNetworkService.class);
        TenantNetwork network = new DefaultTenantNetwork(TenantNetworkId.networkId(id), name,
                                                         adminStateUp,
                                                         TenantNetwork.State.valueOf(state),
                                                         shared, TenantId.tenantId(tenantID),
                                                         routerExternal,
                                                         TenantNetwork.Type.valueOf(type),
                                                         PhysicalNetwork
                                                                 .physicalNetwork(physicalNetwork),
                                                         SegmentationId
                                                                 .segmentationID(segmentationID));

        Set<TenantNetwork> networksSet = new HashSet<TenantNetwork>();
        networksSet.add(network);
        service.updateNetworks(networksSet);
    }

}
