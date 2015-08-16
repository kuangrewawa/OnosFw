package org.onosproject.vtn.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.DeviceId;
import org.onosproject.vtn.VTNService;

/**
 * create label resource pool by specific device id.
 */
@Command(scope = "onos", name = "drop-bridge",
    description = "Drop bridge")
public class DropBridgeCommand extends AbstractShellCommand {
    @Argument(index = 0, name = "deviceId", description = "Device identity", required = true, multiValued = false)
    String deviceId = null;

    @Override
    protected void execute() {
        VTNService vs = get(VTNService.class);
        vs.dropBridge(DeviceId.deviceId(deviceId));
    }

}
