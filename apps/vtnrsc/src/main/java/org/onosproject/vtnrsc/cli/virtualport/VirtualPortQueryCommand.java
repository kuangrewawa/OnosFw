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

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.DeviceId;
import org.onosproject.vtnrsc.TenantId;
import org.onosproject.vtnrsc.TenantNetworkId;
import org.onosproject.vtnrsc.VirtualPortId;
import org.onosproject.vtnrsc.virtualport.VirtualPortService;

/**
 * Supports for querying virtualPorts.
 */
@Command(scope = "onos", name = "virtualPorts", description = "Supports for querying virtualPort.")
public class VirtualPortQueryCommand extends AbstractShellCommand {
    @Option(name = "-v", aliases = "--vPortId", description = "virtualPort ID.", required = false, multiValued = false)
    String vPortId;
    @Option(name = "-n", aliases = "--networkId", description = "network ID.", required = false, multiValued = false)
    String networkId;
    @Option(name = "-d", aliases = "--deviceId", description = "device ID.", required = false, multiValued = false)
    String deviceId;
    @Option(name = "-t", aliases = "--tenantId", description = "tenant ID.", required = false, multiValued = false)
    String tenantId;

    @Override
    protected void execute() {
        VirtualPortService service = get(VirtualPortService.class);
        service.getPort(VirtualPortId.portId(vPortId));
        if (networkId != null) {
            service.getPorts(TenantNetworkId.networkId(networkId));
        } else {
            service.getPorts();
        }
        if (deviceId != null) {
            service.getPorts(DeviceId.deviceId(deviceId));
        } else {
            service.getPorts();
        }
        if (tenantId != null) {
            service.getPorts(TenantId.tenantId(tenantId));
        } else {
            service.getPorts();
        }
    }

}
