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
package org.onosproject.vtnrsc.cli.virtualport;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onlab.packet.MacAddress;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.DeviceId;
import org.onosproject.net.HostId;
import org.onosproject.vtnrsc.AllowedAddressPair;
import org.onosproject.vtnrsc.DefaultVirtualPort;
import org.onosproject.vtnrsc.FixedIp;
import org.onosproject.vtnrsc.SecurityGroup;
import org.onosproject.vtnrsc.TenantId;
import org.onosproject.vtnrsc.TenantNetworkId;
import org.onosproject.vtnrsc.VirtualPort;
import org.onosproject.vtnrsc.VirtualPortId;
import org.onosproject.vtnrsc.virtualport.VirtualPortService;

import com.google.common.collect.Maps;

/**
 * Supports for updating a virtualPort.
 */
@Command(scope = "onos", name = "virtualPort-update", description = "Supports for updating a virtualPort.")
public class VirtualPortUpdateCommand extends AbstractShellCommand {
    @Argument(index = 0, name = "id", description = "virtualPort id.", required = true, multiValued = false)
    String id = null;
    @Argument(index = 1, name = "networkId", description = "network id.", required = true, multiValued = false)
    String networkId = null;
    @Argument(index = 2, name = "adminStateUp", description = "administrative status of the virtualPort,which is"
            + " true or false.", required = true, multiValued = false)
    Boolean adminStateUp = false;
    @Argument(index = 3, name = "name", description = "virtualPort name.", required = true, multiValued = false)
    String name = null;
    @Argument(index = 4, name = "state", description = "virtualPort state.", required = true, multiValued = false)
    String state = null;
    @Argument(index = 5, name = "macAddress", description = "MAC address.", required = true, multiValued = false)
    String macAddress = null;
    @Argument(index = 6, name = "tenantId", description = "tenant id.", required = true, multiValued = false)
    String tenantId = null;
    @Argument(index = 7, name = "deviceOwner", description = "ID of the entity that uses this virtualPort.",
            required = true, multiValued = false)
    String deviceOwner = null;
    @Argument(index = 8, name = "deviceId", description = "device id.", required = true, multiValued = false)
    String deviceId = null;
    @Argument(index = 9, name = "fixedIp", description = " a IP address for the port,Include the IP address and "
            + "subnet identity..", required = true, multiValued = false)
    FixedIp fixedIp = null;
    @Argument(index = 10, name = "bindingHostId", description = "virtualPort bindingHostId.",
            required = true, multiValued = false)
    String bindingHostId = null;
    @Argument(index = 11, name = "bindingvnicType", description = "virtualPort bindingvnicType.",
            required = true, multiValued = false)
    String bindingvnicType = null;
    @Argument(index = 12, name = "bindingvifType", description = "virtualPort bindingvifType.",
            required = true, multiValued = false)
    String bindingvifType = null;
    @Argument(index = 13, name = "bindingvnicDetails", description = "virtualPort bindingvnicDetails.",
            required = true, multiValued = false)
    String bindingvnicDetails = null;
    @Argument(index = 14, name = "allowedAddress", description = "virtual allowedAddressPair.",
            required = true, multiValued = false)
    Collection<AllowedAddressPair> allowedAddressPairs = null;
    @Argument(index = 15, name = "securityGroups", description = "virtualPort securityGroups.",
            required = true, multiValued = false)
    Collection<SecurityGroup> securityGroups = null;

    @Override
    protected void execute() {
        VirtualPortService service = get(VirtualPortService.class);
        ConcurrentMap<String, String> strMap = Maps.newConcurrentMap();
        strMap.putIfAbsent("name", name);
        strMap.putIfAbsent("deviceOwner", deviceOwner);
        strMap.putIfAbsent("bindingvnicType", bindingvnicType);
        strMap.putIfAbsent("bindingvifType", bindingvifType);
        strMap.putIfAbsent("bindingvnicDetails", bindingvnicDetails);
        VirtualPort virtualPort = new DefaultVirtualPort(
                                                         VirtualPortId
                                                                 .portId(id),
                                                         TenantNetworkId
                                                                 .networkId(networkId),
                                                         false,
                                                         strMap,
                                                         VirtualPort.State.ACTIVE,
                                                         MacAddress
                                                                 .valueOf(macAddress),
                                                         TenantId.tenantId(tenantId),
                                                         DeviceId.deviceId(deviceId),
                                                         fixedIp,
                                                         HostId.hostId(bindingHostId),
                                                         allowedAddressPairs,
                                                         securityGroups);
        Set<VirtualPort> virtualPorts = new HashSet<VirtualPort>();
        virtualPorts.add(virtualPort);
        service.updatePorts(virtualPorts);
    }
}
