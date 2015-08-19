package org.onosproject.driver.ovsdb;

import java.util.HashSet;
import java.util.Set;

import org.onlab.packet.IpAddress;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.behaviour.PortConfig;
import org.onosproject.net.device.PortDescription;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.net.driver.DriverHandler;
import org.onosproject.ovsdb.controller.OvsdbClientService;
import org.onosproject.ovsdb.controller.OvsdbController;
import org.onosproject.ovsdb.controller.OvsdbNodeId;
import org.onosproject.ovsdb.controller.OvsdbPort;

import com.google.common.primitives.UnsignedInteger;

public class OvsdbPortConfig extends AbstractHandlerBehaviour
        implements PortConfig {

    @Override
    public void applyQoS(PortDescription port, UnsignedInteger queueId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeQoS(PortDescription port) {
        // TODO Auto-generated method stub

    }

    @Override
    public Set<PortNumber> getPorts() {
        Set<PortNumber> portNumberSet = new HashSet<>();
        DriverHandler handler = handler();
        OvsdbClientService clientService = getOvsdbNode(handler);
        Set<OvsdbPort> ports = clientService.getPorts();
        ports.forEach(p -> {
            portNumberSet.add(PortNumber.portNumber(p.portNumber().value(), p.portName().value()));
        });
        return portNumberSet;
    }

    // OvsdbNodeId(IP:port) is used in the adaptor while DeviceId(ovsdb:IP:port)
    // is used in the core. So DeviceId need be changed to OvsdbNodeId.
    private OvsdbNodeId changeDeviceIdToNodeId(DeviceId deviceId) {
        int lastColon = deviceId.toString().lastIndexOf(":");
        int fistColon = deviceId.toString().indexOf(":");
        String ip = deviceId.toString().substring(fistColon + 1, lastColon);
        String port = deviceId.toString().substring(lastColon + 1);
        IpAddress ipAddress = IpAddress.valueOf(ip);
        long portL = Long.valueOf(port).longValue();
        return new OvsdbNodeId(ipAddress, portL);
    }

    private OvsdbClientService getOvsdbNode(DriverHandler handler) {
        OvsdbController ovsController = handler.get(OvsdbController.class);
        DeviceId deviceId = handler.data().deviceId();
        OvsdbNodeId nodeId = changeDeviceIdToNodeId(deviceId);
        return ovsController.getOvsdbClient(nodeId);
    }


}
