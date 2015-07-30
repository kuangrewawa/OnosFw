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
package org.onosproject.app.vtnrsc.cli.subnet;

import java.util.HashSet;
import java.util.Set;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onlab.packet.IpAddress;
import org.onlab.packet.IpAddress.Version;
import org.onlab.packet.IpPrefix;
import org.onosproject.app.vtnrsc.AllocationPool;
import org.onosproject.app.vtnrsc.DefaultSubnet;
import org.onosproject.app.vtnrsc.HostRoute;
import org.onosproject.app.vtnrsc.TenantNetworkId;
import org.onosproject.app.vtnrsc.Subnet;
import org.onosproject.app.vtnrsc.Subnet.Mode;
import org.onosproject.app.vtnrsc.SubnetId;
import org.onosproject.app.vtnrsc.TenantId;
import org.onosproject.app.vtnrsc.subnet.SubnetService;
import org.onosproject.cli.AbstractShellCommand;

/**
 * Supports for creating subnets.
 */
@Command(scope = "onos", name = "subnet-create", description = "Supports for creating a subnet")
public class SubnetCreateCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "id", description = "Subnet SubnetId Id", required = true, multiValued = false)
    String id = null;
    @Argument(index = 1, name = "subnetName", description = "Subnet String name", required = true,
            multiValued = false)
    String subnetName = null;
    @Argument(index = 2, name = "networkId", description = "Subnet NetworkId Id", required = true,
            multiValued = false)
    String networkId = null;
    @Argument(index = 3, name = "tenantId", description = "Subnet tenantId Id", required = true,
            multiValued = false)
    String tenantId = null;
    @Argument(index = 4, name = "ipVersion", description = "Subnet Version ipVersion", required = true,
            multiValued = false)
    Version ipVersion = null;
    @Argument(index = 5, name = "cidr", description = "Subnet IpPrefix cidr", required = true,
            multiValued = false)
    String cidr = null;
    @Argument(index = 6, name = "gatewayIp", description = "Subnet IpAddress gatewayIp", required = true,
            multiValued = false)
    String gatewayIp = null;
    @Argument(index = 7, name = "dhcpEnabled", description = "Subnet boolean dhcpEnabled", required = true,
            multiValued = false)
    boolean dhcpEnabled = false;
    @Argument(index = 8, name = "shared", description = "Subnet boolean shared", required = true,
            multiValued = false)
    boolean shared = false;
    @Argument(index = 9, name = "ipV6AddressMode", description = "Subnet Mode ipV6AddressMode",
            required = true, multiValued = false)
    String ipV6AddressMode = null;
    @Argument(index = 10, name = "ipV6RaMode", description = "Subnet Mode ipV6RaMode", required = true,
            multiValued = false)
    String ipV6RaMode = null;
    @Argument(index = 11, name = "hostRoutes", description = "Subnet jsonnode hostRoutes", required = true,
            multiValued = false)
    Iterable<HostRoute> hostRoutes = null;
    @Argument(index = 12, name = "allocationPools", description = "Subnet jsonnode allocationPools",
            required = true, multiValued = false)
    Iterable<AllocationPool> allocationPools = null;

    @Override
    protected void execute() {
        SubnetService service = get(SubnetService.class);
        Subnet subnet = new DefaultSubnet(
                                          SubnetId.subnetId(id),
                                          subnetName,
                                          TenantNetworkId.networkId(networkId),
                                          TenantId.tenantId(tenantId),
                                          ipVersion, IpPrefix.valueOf(cidr),
                                          IpAddress.valueOf(gatewayIp),
                                          dhcpEnabled, shared, hostRoutes, Mode
                                                  .valueOf(ipV6AddressMode),
                                          Mode.valueOf(ipV6RaMode),
                                          allocationPools);

        Set<Subnet> subnetsSet = new HashSet<Subnet>();
        subnetsSet.add(subnet);
        service.createSubnets(subnetsSet);
    }

}
