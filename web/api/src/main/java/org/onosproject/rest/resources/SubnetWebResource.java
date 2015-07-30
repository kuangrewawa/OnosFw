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

import java.io.IOException;
import java.io.InputStream;
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
import org.onlab.packet.IpAddress.Version;
import org.onlab.packet.IpPrefix;
import org.onlab.util.ItemNotFoundException;
import org.onosproject.app.vtnrsc.AllocationPool;
import org.onosproject.app.vtnrsc.DefaultAllocationPool;
import org.onosproject.app.vtnrsc.DefaultHostRoute;
import org.onosproject.app.vtnrsc.DefaultSubnet;
import org.onosproject.app.vtnrsc.HostRoute;
import org.onosproject.app.vtnrsc.Subnet;
import org.onosproject.app.vtnrsc.Subnet.Mode;
import org.onosproject.app.vtnrsc.SubnetId;
import org.onosproject.app.vtnrsc.TenantId;
import org.onosproject.app.vtnrsc.TenantNetworkId;
import org.onosproject.app.vtnrsc.subnet.SubnetService;
import org.onosproject.app.vtnrsc.web.SubnetCodec;
import org.onosproject.rest.AbstractWebResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

@Path("subnets")
public class SubnetWebResource extends AbstractWebResource {
    private final Logger log = LoggerFactory.getLogger(SubnetWebResource.class);
    public static final String SUBNET_NOT_CREATE = "Subnets is failed to create!";
    public static final String SUBNET_NOT_FOUND = "Subnets is failed to update!";

    @GET
    public Response listSubnets() {
        Iterable<Subnet> subnets = get(SubnetService.class).getSubnets();
        ObjectNode result = new ObjectMapper().createObjectNode();
        result.set("subnets", new SubnetCodec().encode(subnets, this));
        return ok(result.toString()).build();
    }

    @GET
    @Path("{subnetUUID}")
    public Response getSubnet(@PathParam("subnetUUID") String id) {
        Subnet sub = nullIsNotFound(get(SubnetService.class)
                                            .getSubnet(SubnetId.subnetId(id)),
                                    SUBNET_NOT_FOUND);

        ObjectNode result = new ObjectMapper().createObjectNode();
        result.set("subnet", new SubnetCodec().encode(sub, this));
        return ok(result.toString()).build();
    }

    @POST
    public Response createSubnet(final InputStream input) throws IOException {

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode subnode = mapper.readTree(input);
            Iterable<Subnet> subnets = createOrUpdateByInputStream(subnode);
            Boolean result = nullIsNotFound((get(SubnetService.class)
                                                    .createSubnets(subnets)),
                                            SUBNET_NOT_CREATE);

            if (!result) {
                return Response.status(204).entity(SUBNET_NOT_CREATE).build();
            }
            return Response.status(202).entity(result.toString()).build();
        } catch (Exception e) {
            return Response.status(INTERNAL_SERVER_ERROR).entity(e.toString())
                    .build();
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateSubnet(final InputStream input) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode subnode = mapper.readTree(input);
            Iterable<Subnet> subnets = createOrUpdateByInputStream(subnode);
            Boolean result = nullIsNotFound(get(SubnetService.class)
                    .updateSubnets(subnets), SUBNET_NOT_FOUND);
            if (!result) {
                return Response.status(204).entity(SUBNET_NOT_FOUND).build();
            }
            return Response.status(203).entity(result.toString()).build();
        } catch (Exception e) {
            return Response.status(INTERNAL_SERVER_ERROR).entity(e.toString())
                    .build();
        }
    }

    @Path("{subnetUUID}")
    @DELETE
    public Response deleteSingleSubnet(@PathParam("subnetUUID") String id)
            throws IOException {
        try {
            SubnetId subId = SubnetId.subnetId(id);
            Set<SubnetId> subIds = new HashSet<SubnetId>();
            subIds.add(subId);
            get(SubnetService.class).removeSubnets(subIds);
            return Response.status(201).entity("SUCCESS").build();
        } catch (Exception e) {
            return Response.status(201).entity("SUCCESS").build();
        }
    }

    @DELETE
    public Response deleteSubnets(final InputStream input) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode subnode = mapper.readTree(input);
            Iterable<SubnetId> subnetIds = createOrUpdateIdsByInputStream(subnode);
            get(SubnetService.class).removeSubnets(subnetIds);
            return Response.status(201).entity("SUCCESS").build();
        } catch (Exception e) {
            return Response.status(201).entity("SUCCESS").build();
        }
    }

    /**
     * Returns the specified item if that items is null; otherwise throws not
     * found exception.
     *
     * @param item item to check
     * @param <T> item type
     * @param message not found message
     * @return item if not null
     * @throws org.onlab.util.ItemNotFoundException if item is null
     */
    protected <T> T nullIsNotFound(T item, String message) {
        if (item == null) {
            throw new ItemNotFoundException(message);
        }
        return item;
    }

    private Iterable<SubnetId> createOrUpdateIdsByInputStream(JsonNode subnode) {
        Iterable<SubnetId> subnetIds = null;
        Map<SubnetId, SubnetId> subMap = new HashMap<SubnetId, SubnetId>();
        try {
            JsonNode subnetNodes = subnode.get("subnets");
            if (subnetNodes == null) {
                subnetNodes = subnode.get("subnet");
            }
            log.info("subnetNodes is {}", subnetNodes.toString());
            if (subnetNodes.isArray()) {
                for (JsonNode subnetNode : subnetNodes) {
                    SubnetId id = SubnetId.subnetId(subnetNode.get("id")
                            .asText());
                    subMap.put(id, id);
                }
            } else {
                SubnetId id = SubnetId.subnetId(subnetNodes.get("id").asText());
                subMap.put(id, id);
            }
            subnetIds = Collections.unmodifiableCollection(subMap.values());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return subnetIds;
    }

    private Iterable<Subnet> createOrUpdateByInputStream(JsonNode subnode) {
        Iterable<Subnet> subnets = null;
        Map<SubnetId, Subnet> subMap = new HashMap<SubnetId, Subnet>();
        try {
            JsonNode subnetNodes = subnode.get("subnets");
            if (subnetNodes == null) {
                subnetNodes = subnode.get("subnet");
            }
            log.info("subnetNodes is {}", subnetNodes.toString());
            if (subnetNodes.isArray()) {
                for (JsonNode subnetNode : subnetNodes) {
                    SubnetId id = SubnetId.subnetId(subnetNode.get("id")
                            .asText());
                    String subnetName = subnetNode.get("name").asText();
                    TenantId tenantId = TenantId.tenantId(subnetNode
                            .get("tenant_id").asText());
                    TenantNetworkId networkId = TenantNetworkId
                            .networkId(subnetNode.get("network_id").asText());
                    Version ipVersion = Version.valueOf(subnetNode
                            .get("ip_version").asText());
                    IpPrefix cidr = IpPrefix.valueOf(subnetNode.get("cidr")
                            .asText());
                    // String cidrPrefix =
                    // subnetNode.get("cidrPrefix").asText();
                    IpAddress gatewayIp = IpAddress.valueOf(subnetNode
                            .get("gateway_ip").asText());
                    Boolean dhcpEnabled = subnetNode.get("enable_dhcp")
                            .asBoolean();
                    Boolean shared = subnetNode.get("shared").asBoolean();
                    JsonNode hostRoutes = subnetNode.get("host_routes");
                    Iterable<HostRoute> hostRoutesIt = jsonNodeToHostRoutes(hostRoutes);
                    JsonNode allocationPools = subnetNode
                            .get("allocation_pools");
                    Iterable<AllocationPool> allocationPoolsIt = jsonNodeToAllocationPools(allocationPools);
                    Mode ipV6AddressMode = Mode.valueOf(subnetNode
                            .get("ipv6_address_mode").asText());
                    Mode ipV6RaMode = Mode.valueOf(subnetNode
                            .get("ipv6_ra_mode").asText());
                    Subnet subnet = new DefaultSubnet(id, subnetName,
                                                      networkId, tenantId,
                                                      ipVersion, cidr,
                                                      gatewayIp, dhcpEnabled,
                                                      shared, hostRoutesIt,
                                                      ipV6AddressMode,
                                                      ipV6RaMode,
                                                      allocationPoolsIt);
                    subMap.put(id, subnet);
                }
            } else {
                SubnetId id = SubnetId.subnetId(subnetNodes.get("id").asText());
                String subnetName = subnetNodes.get("name").asText();
                TenantId tenantId = TenantId.tenantId(subnetNodes
                        .get("tenant_id").asText());
                TenantNetworkId networkId = TenantNetworkId
                        .networkId(subnetNodes.get("network_id").asText());
                Version ipVersion = Version.valueOf(subnetNodes
                        .get("ip_version").asText());
                IpPrefix cidr = IpPrefix.valueOf(subnetNodes.get("cidr")
                        .asText());
                // String cidrPrefix = subnetNode.get("cidrPrefix").asText();
                IpAddress gatewayIp = IpAddress.valueOf(subnetNodes
                        .get("gateway_ip").asText());
                Boolean dhcpEnabled = subnetNodes.get("enable_dhcp")
                        .asBoolean();
                Boolean shared = subnetNodes.get("shared").asBoolean();
                JsonNode hostRoutes = subnetNodes.get("host_routes");
                Iterable<HostRoute> hostRoutesIt = jsonNodeToHostRoutes(hostRoutes);
                JsonNode allocationPools = subnetNodes.get("allocation_pools");
                Iterable<AllocationPool> allocationPoolsIt = jsonNodeToAllocationPools(allocationPools);
                Mode ipV6AddressMode = Mode.valueOf(subnetNodes
                        .get("ipv6_address_mode").asText());
                Mode ipV6RaMode = Mode.valueOf(subnetNodes.get("ipv6_ra_mode")
                        .asText());
                Subnet subnet = new DefaultSubnet(id, subnetName, networkId,
                                                  tenantId, ipVersion, cidr,
                                                  gatewayIp, dhcpEnabled,
                                                  shared, hostRoutesIt,
                                                  ipV6AddressMode, ipV6RaMode,
                                                  allocationPoolsIt);
                subMap.put(id, subnet);
            }
            subnets = Collections.unmodifiableCollection(subMap.values());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return subnets;
    }

    /**
     * Changes JsonNode alocPools to a collection of the alocPools.
     *
     * @param JsonNode extralDHCPOptions
     * @return a iterable collection of AllocationPool
     */
    public Iterable<AllocationPool> jsonNodeToAllocationPools(JsonNode allocationPools) {
        ConcurrentMap<Integer, AllocationPool> alocplMaps = Maps
                .newConcurrentMap();
        Integer i = 0;
        for (JsonNode node : allocationPools) {
            IpAddress startIp = IpAddress.valueOf(node.get("start").asText());
            IpAddress endIp = IpAddress.valueOf(node.get("end").asText());
            AllocationPool alocPls = new DefaultAllocationPool(startIp, endIp);
            alocplMaps.putIfAbsent(i, alocPls);
            i++;
        }
        return Collections.unmodifiableCollection(alocplMaps.values());
    }

    public Iterable<HostRoute> jsonNodeToHostRoutes(JsonNode hostRoutes) {
        ConcurrentMap<Integer, HostRoute> hostRouteMaps = Maps
                .newConcurrentMap();
        Integer i = 0;
        for (JsonNode node : hostRoutes) {
            IpAddress nexthop = IpAddress.valueOf(node.get("nexthop").asText());
            IpPrefix destination = IpPrefix.valueOf(node.get("destination")
                    .asText());
            HostRoute hostRoute = new DefaultHostRoute(nexthop, destination);
            hostRouteMaps.putIfAbsent(i, hostRoute);
            i++;
        }
        return Collections.unmodifiableCollection(hostRouteMaps.values());
    }

}
