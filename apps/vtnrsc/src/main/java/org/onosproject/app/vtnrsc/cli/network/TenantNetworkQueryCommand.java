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
package org.onosproject.app.vtnrsc.cli.network;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.onosproject.app.vtnrsc.TenantNetworkId;
import org.onosproject.app.vtnrsc.tenantnetwork.TenantNetworkService;
import org.onosproject.cli.AbstractShellCommand;

/**
 * Supports for querying TenantNetworks by networkid.
 */
@Command(scope = "onos", name = "tenantNetworks", description = "Supports for querying"
        + "tenantNetworks by networkid")
public class TenantNetworkQueryCommand extends AbstractShellCommand {

    @Option(name = "-i", aliases = "--id", description = "TenantNetworkId id", required = false,
            multiValued = false)
    String id = null;

    @Override
    protected void execute() {
        TenantNetworkService service = get(TenantNetworkService.class);
        if (id != null) {
            service.getNetwork(TenantNetworkId.networkId(id));
        } else {
            service.getNetworks();
        }
    }

}
