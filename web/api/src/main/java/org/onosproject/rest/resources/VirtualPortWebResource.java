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
package org.onosproject.rest.resources;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.util.ItemNotFoundException;
import org.onosproject.net.DeviceId;
import org.onosproject.net.HostId;
import org.onosproject.rest.AbstractWebResource;
import org.onosproject.vtnrsc.AllowedAddressPair;
import org.onosproject.vtnrsc.DefaultVirtualPort;
import org.onosproject.vtnrsc.FixedIp;
import org.onosproject.vtnrsc.SecurityGroup;
import org.onosproject.vtnrsc.SubnetId;
import org.onosproject.vtnrsc.TenantId;
import org.onosproject.vtnrsc.TenantNetworkId;
import org.onosproject.vtnrsc.VirtualPort;
import org.onosproject.vtnrsc.VirtualPortId;
import org.onosproject.vtnrsc.VirtualPort.State;
import org.onosproject.vtnrsc.virtualport.VirtualPortService;
import org.onosproject.vtnrsc.web.VirtualPortCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

/**
 * REST resource for interacting with the inventory of infrastructure
 * virtualPort.
 */
@Path("virtualports")
public class VirtualPortWebResource extends AbstractWebResource {
    public static final String VPORT_NOT_FOUND = "VirtualPort is not found";
    public static final String VPORT_ID_EXIST = "VirtualPort id is exist";
    public static final String VPORT_ID_NOT_EXIST = "VirtualPort id is not exist";
    protected static final Logger log = LoggerFactory
            .getLogger(VirtualPortService.class);

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getPorts() {
        Iterable<VirtualPort> virtualPorts = get(VirtualPortService.class)
                .getPorts();
        ObjectNode result = new ObjectMapper().createObjectNode();
        result.set("virtualports",
                   new VirtualPortCodec().encode(virtualPorts, this));
        return ok(result.toString()).build();
    }

    @GET
    @Path("{id}")
    public Response getportsById(@PathParam("id") String id) {
        VirtualPort virtualPort = nullIsNotFound(get(VirtualPortService.class)
                .getPort(VirtualPortId.portId(id)), VPORT_NOT_FOUND);
        ObjectNode result = new ObjectMapper().createObjectNode();
        result.set("virtualPort",
                   new VirtualPortCodec().encode(virtualPort, this));
        return ok(result.toString()).build();
    }

    @POST
    public Response createPorts(InputStream input) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode cfg = mapper.readTree(input);
            Iterable<VirtualPort> vPorts = createOrUpdateByInputStream(cfg);
            Boolean issuccess = nullIsNotFound(get(VirtualPortService.class)
                    .createPorts(vPorts), VPORT_NOT_FOUND);
            if (!issuccess) {
                return Response.status(INTERNAL_SERVER_ERROR)
                        .entity(VPORT_ID_NOT_EXIST).build();
            }
            return Response.status(OK).entity(issuccess.toString()).build();
        } catch (Exception e) {
            return Response.status(INTERNAL_SERVER_ERROR).entity(e.toString())
                    .build();
        }
    }

    @Path("{portUUID}")
    @DELETE
    public Response deletePorts(@PathParam("subnetUUID") String id)
            throws IOException {
        Set<VirtualPortId> vPortIds = new HashSet<VirtualPortId>();
        try {
            if (id != null) {
                vPortIds.add(VirtualPortId.portId(id));
            }
            Boolean issuccess = nullIsNotFound(get(VirtualPortService.class)
                    .removePorts(vPortIds), VPORT_NOT_FOUND);
            if (!issuccess) {
                return Response.status(INTERNAL_SERVER_ERROR)
                        .entity(VPORT_ID_NOT_EXIST).build();
            }
            return Response.status(OK).entity(issuccess.toString()).build();
        } catch (Exception e) {
            return Response.status(INTERNAL_SERVER_ERROR).entity(e.toString())
                    .build();
        }
    }

    @DELETE
    public Response deletePorts(InputStream input) throws IOException {
        Set<VirtualPortId> vPortIds = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode cfg = mapper.readTree(input);
            vPortIds = removeByInputStream(cfg);
            Boolean issuccess = nullIsNotFound(get(VirtualPortService.class)
                    .removePorts(vPortIds), VPORT_NOT_FOUND);
            if (!issuccess) {
                return Response.status(INTERNAL_SERVER_ERROR)
                        .entity(VPORT_ID_NOT_EXIST).build();
            }
            return Response.status(OK).entity(issuccess.toString()).build();
        } catch (Exception e) {
            return Response.status(INTERNAL_SERVER_ERROR).entity(e.toString())
                    .build();
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePorts(@PathParam("id") String id, InputStream input)
            throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode cfg = mapper.readTree(input);
            Iterable<VirtualPort> vPorts = createOrUpdateByInputStream(cfg);
            Boolean issuccess = nullIsNotFound(get(VirtualPortService.class)
                    .updatePorts(vPorts), VPORT_NOT_FOUND);
            if (!issuccess) {
                return Response.status(INTERNAL_SERVER_ERROR)
                        .entity(VPORT_ID_NOT_EXIST).build();
            }
            return Response.status(OK).entity(issuccess.toString()).build();
        } catch (Exception e) {
            return Response.status(INTERNAL_SERVER_ERROR).entity(e.toString())
                    .build();
        }
    }

    /**
     * Returns a Object of the currently known infrastructure virtualPort.
     *
     * @param JsonNode node the virtualPort json
     * @return Object of virtualPort
     */
    public Iterable<VirtualPort> createOrUpdateByInputStream(JsonNode vPortNode) {
        Map<VirtualPortId, VirtualPort> vPortsMap = new HashMap<VirtualPortId, VirtualPort>();
        Map<String, String> strMap = new HashMap<String, String>();
        try {
            JsonNode vPortNodes = vPortNode.get("virtualports");
            if (vPortNodes == null) {
                vPortNodes = vPortNode.get("virtualport");
            }
            log.info("vPortNode is {}");
            if (vPortNodes.isArray()) {
                for (JsonNode vPortnode : vPortNodes) {
                    VirtualPortId id = VirtualPortId.portId(vPortnode.get("id")
                            .asText());
                    String name = vPortnode.get("name").asText();
                    TenantId tenantId = TenantId.tenantId(vPortnode
                            .get("tenantId").asText());
                    TenantNetworkId networkId = TenantNetworkId
                            .networkId(vPortnode.get("networkId").asText());
                    Boolean adminStateUp = vPortnode.get("adminStateUp")
                            .asBoolean();
                    String state = vPortnode.get("state").asText();
                    MacAddress macAddress = MacAddress.valueOf(vPortnode
                            .get("macAddress").asText());
                    DeviceId deviceId = DeviceId.deviceId(vPortnode
                            .get("deviceId").asText());
                    String deviceOwner = vPortnode.get("deviceOwner").asText();
                    JsonNode fixedIpNode = vPortnode.get("fixedIp");
                    FixedIp fixedIp = jsonNodeToFixedIps(fixedIpNode);
                    HostId bindingHostId = HostId.hostId(MacAddress
                            .valueOf(vPortnode.get("bindingHostId").asText()));
                    String bindingvnicType = vPortnode.get("bindingvnicType")
                            .asText();
                    String bindingvifType = vPortnode.get("bindingvifType")
                            .asText();
                    String bindingvifDetails = vPortnode
                            .get("bindingvifDetails").asText();
                    JsonNode allowedAddressPairJsonNode = vPortnode
                            .get("allowedAddressPairs");
                    Collection<AllowedAddressPair> allowedAddressPairs =
                            jsonNodeToAllowedAddressPair(allowedAddressPairJsonNode);
                    JsonNode securityGroupNode = vPortnode
                            .get("securityGroups");
                    Collection<SecurityGroup> securityGroups = jsonNodeToSecurityGroup(securityGroupNode);
                    strMap.put("name", name);
                    strMap.put("deviceOwner", deviceOwner);
                    strMap.put("bindingvnicType", bindingvnicType);
                    strMap.put("bindingvifType", bindingvifType);
                    strMap.put("bindingvifDetails", bindingvifDetails);
                    VirtualPort vPort = new DefaultVirtualPort(
                                                               id,
                                                               networkId,
                                                               adminStateUp,
                                                               strMap,
                                                               isState(state),
                                                               macAddress,
                                                               tenantId,
                                                               deviceId,
                                                               fixedIp,
                                                               bindingHostId,
                                                               allowedAddressPairs,
                                                               securityGroups);
                    vPortsMap.put(id, vPort);
                }
            } else {
                VirtualPortId id = VirtualPortId.portId(vPortNodes.get("id")
                        .asText());
                String name = vPortNodes.get("name").asText();
                TenantId tenantId = TenantId.tenantId(vPortNodes
                        .get("tenantId").asText());
                TenantNetworkId networkId = TenantNetworkId
                        .networkId(vPortNodes.get("networkId").asText());
                Boolean adminStateUp = vPortNodes.get("adminStateUp")
                        .asBoolean();
                String state = vPortNodes.get("state").asText();
                MacAddress macAddress = MacAddress.valueOf(vPortNodes
                        .get("macAddress").asText());
                DeviceId deviceId = DeviceId.deviceId(vPortNodes
                        .get("deviceId").asText());
                String deviceOwner = vPortNodes.get("deviceOwner").asText();
                JsonNode fixedIpNode = vPortNodes.get("fixedIp");
                FixedIp fixedIp = jsonNodeToFixedIps(fixedIpNode);
                // HostId bindingHostId = HostId.hostId(vPortnode
                // .get("bindingHostId").asText());
                HostId bindingHostId = HostId.hostId(MacAddress
                        .valueOf(vPortNodes.get("bindingHostId").asText()));
                String bindingvnicType = vPortNodes.get("bindingvnicType")
                        .asText();
                String bindingvifType = vPortNodes.get("bindingvifType")
                        .asText();
                String bindingvifDetails = vPortNodes.get("bindingvifDetails")
                        .asText();
                JsonNode allowedAddressPairJsonNode = vPortNodes
                        .get("allowedAddressPairs");
                Collection<AllowedAddressPair> allowedAddressPairs =
                        jsonNodeToAllowedAddressPair(allowedAddressPairJsonNode);
                JsonNode securityGroupNode = vPortNodes.get("securityGroups");
                Collection<SecurityGroup> securityGroups = jsonNodeToSecurityGroup(securityGroupNode);
                strMap.putIfAbsent("name", name);
                strMap.putIfAbsent("deviceOwner", deviceOwner);
                strMap.putIfAbsent("bindingvnicType", bindingvnicType);
                strMap.putIfAbsent("bindingvifType", bindingvifType);
                strMap.putIfAbsent("bindingvifDetails", bindingvifDetails);
                VirtualPort vPort = new DefaultVirtualPort(id, networkId,
                                                           adminStateUp,
                                                           strMap,
                                                           isState(state),
                                                           macAddress,
                                                           tenantId, deviceId,
                                                           fixedIp,
                                                           bindingHostId,
                                                           allowedAddressPairs,
                                                           securityGroups);
                vPortsMap.put(id, vPort);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.unmodifiableCollection(vPortsMap.values());
    }

    /**
     * Returns a Collection of the currently known infrastructure virtualPort.
     *
     * @param vPortNode node the virtualPort json
     * @return Collection of virtualPort
     */
    public Set<VirtualPortId> removeByInputStream(JsonNode vPortNode) {
        Set<VirtualPortId> vPortIds = new HashSet<VirtualPortId>();
        try {
            JsonNode vPortNodes = vPortNode.get("virtualports");
            if (vPortNodes == null) {
                vPortNodes = vPortNode.get("virtualport");
            }
            log.info("vPortNode is {}");
            if (vPortNodes.isArray()) {
                for (JsonNode vPortnode : vPortNodes) {
                    VirtualPortId id = VirtualPortId.portId(vPortnode.get("id")
                            .asText());
                    vPortIds.add(id);
                }
            } else {
                VirtualPortId id = VirtualPortId.portId(vPortNodes.get("id")
                        .asText());
                vPortIds.add(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vPortIds;
    }

    private State isState(String state) {
        if (state.equals("ACTIVE")) {
            return VirtualPort.State.ACTIVE;
        } else {
            return VirtualPort.State.DOWN;
        }

    }

    /**
     * Returns a Object of the currently known infrastructure virtualPort.
     *
     * @param JsonNode node the virtualPort json
     * @return Object of virtualPort
     */
    public Collection<AllowedAddressPair> jsonNodeToAllowedAddressPair(JsonNode allowedAddressPairs) {
        ConcurrentMap<Integer, AllowedAddressPair> allowMaps = Maps
                .newConcurrentMap();
        Integer i = 0;
        for (JsonNode node : allowedAddressPairs) {
            IpAddress ip = IpAddress.valueOf(node.get("ipAddress").asText());
            MacAddress mac = MacAddress
                    .valueOf(node.get("macAddress").asText());
            AllowedAddressPair allows = AllowedAddressPair
                    .allowedAddressPair(ip, mac);
            allowMaps.put(i, allows);
            i++;
        }
        return Collections.unmodifiableCollection(allowMaps.values());
    }

    /**
     * Returns a Object of the currently known infrastructure virtualPort.
     *
     * @param JsonNode node the virtualPort json
     * @return Object of virtualPort
     */
    public Collection<SecurityGroup> jsonNodeToSecurityGroup(JsonNode securityGroups) {
        ConcurrentMap<Integer, SecurityGroup> securMaps = Maps
                .newConcurrentMap();
        Integer i = 0;
        log.info("securityGroups is {}", securityGroups.toString());
        for (JsonNode node : securityGroups) {
            log.info("securityGroup is {}", node.get("securityGroup").asText());
            SecurityGroup securityGroup = SecurityGroup.securityGroup(node
                    .get("securityGroup").asText());
            log.info("securityGroup class is  {}", securityGroup.toString());
            securMaps.put(i, securityGroup);
            log.info("securMaps.size() is {}", securMaps.size());
            i++;
        }
        return Collections.unmodifiableCollection(securMaps.values());
    }

    public FixedIp jsonNodeToFixedIps(JsonNode fixedIpNode) {
        SubnetId subnetId = SubnetId.subnetId(fixedIpNode.get("subnetId")
                .asText());
        IpAddress ipAddress = IpAddress.valueOf(fixedIpNode.get("ipAddress")
                .asText());
        FixedIp fixedIps = FixedIp.fixedIp(subnetId, ipAddress);
        return fixedIps;
    }

    protected <T> T nullIsNotFound(T item, String message) {
        if (item == null) {
            throw new ItemNotFoundException(message);
        }
        return item;
    }
}
