package org.onosproject.app.vtn.impl;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.onlab.util.Tools.groupedThreads;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.util.KryoNamespace;
import org.onosproject.app.vtn.VTNService;
import org.onosproject.app.vtnrsc.SegmentationId;
import org.onosproject.app.vtnrsc.TenantNetwork;
import org.onosproject.app.vtnrsc.VirtualPort;
import org.onosproject.app.vtnrsc.VirtualPortId;
import org.onosproject.app.vtnrsc.tenantnetwork.TenantNetworkService;
import org.onosproject.app.vtnrsc.virtualport.VirtualPortService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Host;
import org.onosproject.net.HostId;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.behaviour.BridgeConfig;
import org.onosproject.net.behaviour.BridgeName;
import org.onosproject.net.behaviour.DefaultTunnelDescription;
import org.onosproject.net.behaviour.IpTunnelEndPoint;
import org.onosproject.net.behaviour.TunnelConfig;
import org.onosproject.net.behaviour.TunnelDescription;
import org.onosproject.net.behaviour.TunnelEndPoint;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.driver.DriverHandler;
import org.onosproject.net.driver.DriverService;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.criteria.Criteria;
import org.onosproject.net.flow.instructions.Instructions;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.flowobjective.ForwardingObjective.Flag;
import org.onosproject.net.flowobjective.Objective;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.EventuallyConsistentMap;
import org.onosproject.store.service.StorageService;
import org.onosproject.store.service.WallClockTimestamp;
import org.slf4j.Logger;

/**
 * Provides implementation of VTNService.
 */
@Component(immediate = true)
@Service
public class VTNManager implements VTNService {
    private final Logger log = getLogger(getClass());

    private static final String APP_ID = "org.onosproject.app.vtn";
    private ScheduledExecutorService backgroundService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StorageService storageService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TenantNetworkService tenantNetworkService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected VirtualPortService virtualPortService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DriverService driverService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowObjectiveService flowObjectiveService;
    private EventuallyConsistentMap<HostId, SegmentationId> binding;
    private ApplicationId appId;
    private HostListener hostListener = new InnerHostListener();
    private DeviceListener deviceListener = new InnerDeviceListener();
    private static final String IFACEID = "ifaceid";
    private static final String PORT_HEAD = "vxlan";
    private static final int MAC_TABLE = 40;
    private static final short ETH_TYPE_MAC = 40;
    private static final short ETH_TYPE_PORT = 0;

    @Activate
    public void activate() {
        KryoNamespace.Builder serializer = KryoNamespace.newBuilder()
                .register(KryoNamespaces.API);
        appId = coreService.registerApplication(APP_ID);
        deviceService.addListener(deviceListener);
        hostService.addListener(hostListener);
        backgroundService = newSingleThreadScheduledExecutor(groupedThreads("onos-apps/vtn",
                                                                            "manager-background"));
        binding = storageService
                .<HostId, SegmentationId>eventuallyConsistentMapBuilder()
                .withName("all_tunnel").withSerializer(serializer)
                .withTimestampProvider((k, v) -> new WallClockTimestamp())
                .build();
        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        backgroundService.shutdown();
        binding.destroy();
        log.info("Stopped");
    }

    @Override
    public void onServerDetected(Device device) {
        Iterable<Device> devices = deviceService.getAvailableDevices();
        DriverHandler handler = driverService.createHandler(device.id());
        TunnelConfig config = handler.behaviour(TunnelConfig.class);
        BridgeConfig bridgeConfig = handler.behaviour(BridgeConfig.class);
        log.info("create a bridge");
        bridgeConfig.addBridge(BridgeName.bridgeName("br-int"));
        String ipAddress = device.annotations().value("ipaddress");
        IpAddress ip = IpAddress.valueOf(ipAddress);
        TunnelEndPoint tunnelAsSrcSrc = IpTunnelEndPoint.ipTunnelPoint(ip);
        TunnelEndPoint tunnelAsDstDst = IpTunnelEndPoint.ipTunnelPoint(ip);
        devices.forEach(d -> {
            if (!device.id().equals(d.id())) {
                String ipAddress1 = d.annotations().value("ipaddress");
                IpAddress ip1 = IpAddress.valueOf(ipAddress1);
                TunnelEndPoint tunnelAsSrcDst = IpTunnelEndPoint
                        .ipTunnelPoint(ip1);
                TunnelEndPoint tunnelAsDstSrc = IpTunnelEndPoint
                        .ipTunnelPoint(ip1);
                TunnelDescription tunnelAsSrc = new DefaultTunnelDescription(
                                                                             tunnelAsSrcSrc,
                                                                             tunnelAsSrcDst,
                                                                             TunnelDescription.Type.VXLAN,
                                                                             null);
                TunnelDescription tunnelAsDst = new DefaultTunnelDescription(
                                                                             tunnelAsDstDst,
                                                                             tunnelAsDstSrc,
                                                                             TunnelDescription.Type.VXLAN,
                                                                             null);
                log.info("create a vxlan");
                config.createTunnel(tunnelAsSrc);
                config.createTunnel(tunnelAsDst);
            }
        });
    }

    @Override
    public void onServerVanished(Device device) {
        Iterable<Device> devices = deviceService.getAvailableDevices();
        DriverHandler handler = driverService.createHandler(device.id());
        TunnelConfig config = handler.behaviour(TunnelConfig.class);
        BridgeConfig bridgeConfig = handler.behaviour(BridgeConfig.class);
        log.info("drop a bridge");
        bridgeConfig.deleteBridge(BridgeName.bridgeName("br-int"));
        String ipAddress = device.annotations().value("ipaddress");
        IpAddress ip = IpAddress.valueOf(ipAddress);
        TunnelEndPoint tunnelAsSrcSrc = IpTunnelEndPoint.ipTunnelPoint(ip);
        TunnelEndPoint tunnelAsDstDst = IpTunnelEndPoint.ipTunnelPoint(ip);
        devices.forEach(d -> {
            if (!device.id().equals(d.id())) {
                String ipAddress1 = d.annotations().value("ipaddress");
                IpAddress ip1 = IpAddress.valueOf(ipAddress1);
                TunnelEndPoint tunnelAsSrcDst = IpTunnelEndPoint
                        .ipTunnelPoint(ip1);
                TunnelEndPoint tunnelAsDstSrc = IpTunnelEndPoint
                        .ipTunnelPoint(ip1);
                TunnelDescription tunnelAsSrc = new DefaultTunnelDescription(
                                                                             tunnelAsSrcSrc,
                                                                             tunnelAsSrcDst,
                                                                             TunnelDescription.Type.VXLAN,
                                                                             null);
                TunnelDescription tunnelAsDst = new DefaultTunnelDescription(
                                                                             tunnelAsDstDst,
                                                                             tunnelAsDstSrc,
                                                                             TunnelDescription.Type.VXLAN,
                                                                             null);
                log.info("drop a bridge");
                config.removeTunnel(tunnelAsSrc);
                config.removeTunnel(tunnelAsDst);
            }
        });
    }

    @Override
    public void onOvsDetected(Device device) {
        programMacDefaultRules(device.id(), appId, Objective.Operation.ADD);
        programPortDefaultRules(device.id(), appId, Objective.Operation.ADD);
    }

    @Override
    public void onOvsVanished(Device device) {
        programMacDefaultRules(device.id(), appId, Objective.Operation.REMOVE);
        programPortDefaultRules(device.id(), appId, Objective.Operation.REMOVE);
    }

    @Override
    public void onHostDetected(Host host) {
        String ifaceId = host.annotations().value(IFACEID);
        VirtualPortId portId = VirtualPortId.portId(ifaceId);
        VirtualPort port = virtualPortService.getPort(portId);
        TenantNetwork network = tenantNetworkService.getNetwork(port
                .networkId());
        binding.put(host.id(), network.segmentationId());
        DeviceId deviceId = host.location().deviceId();
        List<Port> allPorts = deviceService.getPorts(deviceId);
        PortNumber inPort = host.location().port();
        Set<Port> localPorts = new HashSet<>();
        allPorts.forEach(p -> {
            if (!p.number().name().startsWith(PORT_HEAD)) {
                localPorts.add(p);
            }
        });
        programLocalBcastRules(deviceId, network.segmentationId(), inPort,
                               allPorts, appId, Objective.Operation.ADD);
        programLocalOut(deviceId, network.segmentationId(), inPort, host.mac(),
                        appId, Objective.Operation.ADD);
        programTunnelFloodOut(deviceId, network.segmentationId(), inPort,
                              localPorts, appId, Objective.Operation.ADD);
        programTunnelOut(deviceId, network.segmentationId(), inPort,
                         host.mac(), appId, Objective.Operation.ADD);
        programLocalIn(deviceId, network.segmentationId(), inPort, host.mac(),
                       appId, Objective.Operation.ADD);
        programTunnelIn(deviceId, network.segmentationId(), inPort, host.mac(),
                        appId, Objective.Operation.ADD);
    }

    @Override
    public void onHostVanished(Host host) {
        SegmentationId segId = binding.remove(host.id());
        DeviceId deviceId = host.location().deviceId();
        List<Port> allPorts = deviceService.getPorts(deviceId);
        PortNumber inPort = host.location().port();
        Set<Port> localPorts = new HashSet<>();
        allPorts.forEach(p -> {
            if (!p.number().name().startsWith(PORT_HEAD)) {
                localPorts.add(p);
            }
        });
        programLocalBcastRules(deviceId, segId, inPort, allPorts, appId,
                               Objective.Operation.REMOVE);
        programLocalOut(deviceId, segId, inPort, host.mac(), appId,
                        Objective.Operation.REMOVE);
        programTunnelFloodOut(deviceId, segId, inPort, localPorts, appId,
                              Objective.Operation.REMOVE);
        programTunnelOut(deviceId, segId, inPort, host.mac(), appId,
                         Objective.Operation.REMOVE);
        programLocalIn(deviceId, segId, inPort, host.mac(), appId,
                       Objective.Operation.REMOVE);
        programTunnelIn(deviceId, segId, inPort, host.mac(), appId,
                        Objective.Operation.REMOVE);
    }

    private class InnerDeviceListener implements DeviceListener {

        @Override
        public void event(DeviceEvent event) {
            Device device = event.subject();
            if (Device.Type.CONTROLLER == device.type()
                    && DeviceEvent.Type.DEVICE_ADDED == event.type()) {
                backgroundService.execute(() -> {
                    onServerDetected(device);
                });
            } else if (Device.Type.CONTROLLER == device.type()
                    && DeviceEvent.Type.DEVICE_REMOVED == event.type()) {
                backgroundService.execute(() -> {
                    onServerVanished(device);
                });
            } else if (Device.Type.SWITCH == device.type()
                    && DeviceEvent.Type.DEVICE_ADDED == event.type()) {
                backgroundService.execute(() -> {
                    onOvsDetected(device);
                });
            } else if (Device.Type.SWITCH == device.type()
                    && DeviceEvent.Type.DEVICE_REMOVED == event.type()) {
                backgroundService.execute(() -> {
                    onOvsVanished(device);
                });
            } else {
                log.info("do nothing for this device type");
            }
        }

    }

    private class InnerHostListener implements HostListener {

        @Override
        public void event(HostEvent event) {
            Host host = event.subject();
            if (HostEvent.Type.HOST_ADDED == event.type()) {
                backgroundService.execute(() -> {
                    onHostDetected(host);
                });
            } else if (HostEvent.Type.HOST_REMOVED == event.type()) {
                backgroundService.execute(() -> {
                    onHostVanished(host);
                });
            } else {
                log.info("unknow host");
            }
        }

    }

    private void programLocalOut(DeviceId dpid, SegmentationId segmentationId,
                                 PortNumber outPort, MacAddress sourceMac,
                                 ApplicationId appid, Objective.Operation type) {
        log.info("start programLocalOut");
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchEthDst(sourceMac).matchEthType(ETH_TYPE_MAC).build();
        TrafficTreatment treatment = DefaultTrafficTreatment
                .builder()
                .add(Instructions.modTunnelId(Long.parseLong(segmentationId
                             .toString()))).setOutput(outPort).build();
        ForwardingObjective.Builder objective = DefaultForwardingObjective
                .builder().withTreatment(treatment).withSelector(selector)
                .fromApp(appId).withFlag(Flag.SPECIFIC);
        if (type.equals(Objective.Operation.ADD)) {
            flowObjectiveService.forward(dpid, objective.add());
        } else {
            flowObjectiveService.forward(dpid, objective.remove());
        }
        log.info("end programLocalOut");
    }

    private void programTunnelOut(DeviceId dpid, SegmentationId segmentationId,
                                  PortNumber outPort, MacAddress sourceMac,
                                  ApplicationId appid, Objective.Operation type) {
        log.info("start programTunnelOut");
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchEthDst(sourceMac).matchEthType(ETH_TYPE_MAC).build();
        TrafficTreatment treatment = DefaultTrafficTreatment
                .builder()
                .add(Instructions.modTunnelId(Long.parseLong(segmentationId
                             .toString()))).setOutput(outPort).build();
        ForwardingObjective.Builder objective = DefaultForwardingObjective
                .builder().withTreatment(treatment).withSelector(selector)
                .fromApp(appId).makePermanent().withFlag(Flag.SPECIFIC);
        if (type.equals(Objective.Operation.ADD)) {
            flowObjectiveService.forward(dpid, objective.add());
        } else {
            flowObjectiveService.forward(dpid, objective.remove());
        }
        log.info("end programTunnelOut");
    }

    private void programTunnelFloodOut(DeviceId dpid,
                                       SegmentationId segmentationId,
                                       PortNumber ofPortOut,
                                       Iterable<Port> localports,
                                       ApplicationId appid,
                                       Objective.Operation type) {
        log.info("start programTunnelFloodOut");
        TrafficSelector selector = DefaultTrafficSelector
                .builder()
                .matchInPort(ofPortOut)
                .matchEthType(ETH_TYPE_MAC)
                .add(Criteria.matchTunnelId(Long.parseLong(segmentationId
                             .toString()))).matchEthDst(MacAddress.BROADCAST)
                .build();
        TrafficTreatment.Builder treatment = DefaultTrafficTreatment.builder();

        for (Port outport : localports) {
            treatment.setOutput(outport.number());
        }

        ForwardingObjective.Builder objective = DefaultForwardingObjective
                .builder().withTreatment(treatment.build())
                .withSelector(selector).fromApp(appId)

                .makePermanent().withFlag(Flag.SPECIFIC);
        if (type.equals(Objective.Operation.ADD)) {
            flowObjectiveService.forward(dpid, objective.add());
        } else {
            flowObjectiveService.forward(dpid, objective.remove());
        }
        log.info("end programTunnelFloodOut");
    }

    private void programMacDefaultRules(DeviceId dpid, ApplicationId appid,
                                        Objective.Operation type) {
        log.info("start programMacDefaultRules");
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchEthType(ETH_TYPE_MAC).build();
        TrafficTreatment treatment = DefaultTrafficTreatment.builder().drop()
                .build();

        ForwardingObjective.Builder objective = DefaultForwardingObjective
                .builder().withTreatment(treatment).withSelector(selector)
                .fromApp(appId).makePermanent().withFlag(Flag.SPECIFIC);
        if (type.equals(Objective.Operation.ADD)) {
            flowObjectiveService.forward(dpid, objective.add());
        } else {
            flowObjectiveService.forward(dpid, objective.remove());
        }
        log.info("end programMacDefaultRules");
    }

    private void programLocalBcastRules(DeviceId dpid,
                                        SegmentationId segmentationId,
                                        PortNumber inPort, List<Port> allports,
                                        ApplicationId appid,
                                        Objective.Operation type) {
        log.info("start programLocalBcastRules");
        TrafficSelector selector = DefaultTrafficSelector
                .builder()
                .matchInPort(inPort)
                .matchEthType(ETH_TYPE_MAC)
                .matchEthDst(MacAddress.BROADCAST)
                .add(Criteria.matchTunnelId(Long.parseLong(segmentationId
                             .toString()))).build();
        TrafficTreatment.Builder treatment = DefaultTrafficTreatment.builder();

        for (Port outport : allports) {
            if (inPort != outport.number()) {
                treatment.setOutput(outport.number());
            }
        }
        ForwardingObjective.Builder objective = DefaultForwardingObjective
                .builder().withTreatment(treatment.build())
                .withSelector(selector).fromApp(appId).makePermanent()
                .withFlag(Flag.SPECIFIC);
        if (type.equals(Objective.Operation.ADD)) {
            flowObjectiveService.forward(dpid, objective.add());
        } else {
            flowObjectiveService.forward(dpid, objective.remove());
        }
        log.info("end programLocalBcastRules");
    }

    private void programLocalIn(DeviceId dpid, SegmentationId segmentationId,
                                PortNumber inPort, MacAddress srcMac,
                                ApplicationId appid, Objective.Operation type) {
        log.info("start programLocalIn");
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchInPort(inPort).matchEthSrc(srcMac)
                .matchEthType(ETH_TYPE_PORT).build();
        TrafficTreatment.Builder treatment = DefaultTrafficTreatment.builder();

        treatment.add(Instructions.modTunnelId(Long.parseLong(segmentationId
                .toString())));
        treatment.transition(MAC_TABLE);
        ForwardingObjective.Builder objective = DefaultForwardingObjective
                .builder().withTreatment(treatment.build())
                .withSelector(selector).fromApp(appId).makePermanent()
                .withFlag(Flag.SPECIFIC);
        if (type.equals(Objective.Operation.ADD)) {
            flowObjectiveService.forward(dpid, objective.add());
        } else {
            flowObjectiveService.forward(dpid, objective.remove());
        }
        log.info("end programLocalIn");
    }

    private void programTunnelIn(DeviceId dpid, SegmentationId segmentationId,
                                 PortNumber inPort, MacAddress sourceMac,
                                 ApplicationId appid, Objective.Operation type) {
        log.info("start programTunnelIn");
        TrafficSelector selector = DefaultTrafficSelector
                .builder()
                .matchInPort(inPort)
                .matchEthType(ETH_TYPE_PORT)
                .add(Criteria.matchTunnelId(Long.parseLong(segmentationId
                             .toString()))).build();
        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .transition(MAC_TABLE).build();

        ForwardingObjective.Builder objective = DefaultForwardingObjective
                .builder().withTreatment(treatment).withSelector(selector)
                .fromApp(appId).makePermanent().withFlag(Flag.SPECIFIC);
        if (type.equals(Objective.Operation.ADD)) {
            flowObjectiveService.forward(dpid, objective.add());
        } else {
            flowObjectiveService.forward(dpid, objective.remove());
        }
        log.info("end programTunnelIn");
    }

    private void programPortDefaultRules(DeviceId dpid, ApplicationId appid,
                                         Objective.Operation type) {
        log.info("start programPortDefaultRules");
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchEthType(ETH_TYPE_PORT).build();
        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .transition(MAC_TABLE).build();
        ForwardingObjective.Builder objective = DefaultForwardingObjective
                .builder().withTreatment(treatment).withSelector(selector)
                .fromApp(appId).makePermanent().withFlag(Flag.SPECIFIC);
        if (type.equals(Objective.Operation.ADD)) {
            flowObjectiveService.forward(dpid, objective.add());
        } else {
            flowObjectiveService.forward(dpid, objective.remove());
        }
        log.info("end programPortDefaultRules");
    }
}
