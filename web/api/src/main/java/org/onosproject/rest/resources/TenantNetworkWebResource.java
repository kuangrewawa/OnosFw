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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.onlab.util.ItemNotFoundException;
import org.onosproject.app.vtnrsc.DefaultTenantNetwork;
import org.onosproject.app.vtnrsc.PhysicalNetwork;
import org.onosproject.app.vtnrsc.SegmentationId;
import org.onosproject.app.vtnrsc.TenantId;
import org.onosproject.app.vtnrsc.TenantNetwork;
import org.onosproject.app.vtnrsc.TenantNetwork.State;
import org.onosproject.app.vtnrsc.TenantNetwork.Type;
import org.onosproject.app.vtnrsc.TenantNetworkId;
import org.onosproject.app.vtnrsc.tenantnetwork.TenantNetworkService;
import org.onosproject.app.vtnrsc.web.TenantNetworkCodec;
import org.onosproject.rest.AbstractWebResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

/**
 * REST resource for interacting with the inventory of networks.
 */
@Path("networks")
public class TenantNetworkWebResource extends AbstractWebResource {
    public static final String NETWORK_NOT_FOUND = "Network is not found";
    public static final String NETWORK_ID_EXIST = "Network id is existed";
    public static final String NETWORK_ID_NOT_EXIST = "Network id is not existed";
    public static final String CREATE_NETWORK = "create network";
    public static final String UPDATE_NETWORK = "update network";
    public static final String DELETE_NETWORK = "delete network";
    protected static final Logger log = LoggerFactory
            .getLogger(TenantNetworkWebResource.class);
    private final ConcurrentMap<TenantNetworkId, TenantNetwork> networksMap = Maps
            .newConcurrentMap();

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getNetworks(@QueryParam("id") String queryID,
                                @QueryParam("name") String queryName,
                                @QueryParam("admin_state_up") String queryadminStateUp,
                                @QueryParam("state") String querystate,
                                @QueryParam("shared") String queryshared,
                                @QueryParam("tenant_id") String querytenantID,
                                @QueryParam("routerExternal") String routerExternal,
                                @QueryParam("type") String type,
                                @QueryParam("physicalNetwork") String physicalNetwork,
                                @QueryParam("segmentationID") String segmentationID) {

        log.info("Starting query network.");
        Iterable<TenantNetwork> networks = get(TenantNetworkService.class)
                .getNetworks();
        Iterator<TenantNetwork> networkors = networks.iterator();
        while (networkors.hasNext()) {
            TenantNetwork network = networkors.next();
            if ((queryID == null || queryID.equals(network.id().toString()))
                    && (queryName == null || queryName.equals(network.name()
                            .toString()))
                    && (queryadminStateUp == null || queryadminStateUp
                            .equals(network.adminStateUp()))
                    && (querystate == null || querystate.equals(network.state()
                            .toString()))
                    && (queryshared == null || queryshared.equals(network
                            .shared()))
                    && (querytenantID == null || querytenantID.equals(network
                            .tenantId().toString()))
                    && (routerExternal == null || routerExternal.equals(network
                            .routerExternal()))
                    && (type == null || type.equals(network.type()))
                    && (physicalNetwork == null || physicalNetwork
                            .equals(network.physicalNetwork()))
                    && (segmentationID == null || segmentationID.equals(network
                            .segmentationId()))) {
                networksMap.putIfAbsent(network.id(), network);
            }
        }
        networks = Collections.unmodifiableCollection(networksMap.values());
        ObjectNode result = new ObjectMapper().createObjectNode();
        result.set("network", new TenantNetworkCodec().encode(networks, this));

        return ok(result.toString()).build();
    }

    /**
     * Returns a Object of the currently known infrastructure network.
     *
     * @param NetworkId network identifier
     * @param JsonNode node the network json
     * @return Object of network
     */
    public Iterable<TenantNetwork> changeJson2obj(String flag,
                                                   TenantNetworkId networkId,
                                                   JsonNode node) {
        TenantNetwork network = null;
        ConcurrentMap<TenantNetworkId, TenantNetwork> networksMap = Maps
                .newConcurrentMap();
        if (node != null) {
            String name = node.get("name").asText();
            boolean adminStateUp = node.get("admin_state_up").asBoolean();
            String state = node.get("state").asText();
            boolean shared = node.get("shared").asBoolean();
            String tenantID = node.get("tenant_id").asText();
            boolean routerExternal = node.get("routerExternal").asBoolean();
            String type = node.get("type").asText();
            String physicalNetwork = node.get("physicalNetwork").asText();
            String segmentationID = node.get("segmentationID").asText();
            TenantNetworkId id = null;
            if (flag == CREATE_NETWORK) {
                id = TenantNetworkId.networkId(node.get("id").asText());
            } else if (flag == UPDATE_NETWORK) {
                id = networkId;
            }
            network = new DefaultTenantNetwork(
                                                id,
                                                name,
                                                adminStateUp,
                                                isState(state),
                                                shared,
                                                TenantId.tenantId(tenantID),
                                                routerExternal,
                                                isType(type),
                                                PhysicalNetwork
                                                        .physicalNetwork(physicalNetwork),
                                                SegmentationId
                                                        .segmentationID(segmentationID));
            networksMap.putIfAbsent(id, network);
        }
        return Collections.unmodifiableCollection(networksMap.values());
    }

    /**
     * Returns a Object of the currently known infrastructure network.
     *
     * @param NetworkId network identifier
     * @param JsonNode node the network json
     * @return Object of network
     */
    public Iterable<TenantNetworkId> changeJson2objId(String flag,
                                                       TenantNetworkId networkId,
                                                       JsonNode node) {
        TenantNetwork network = null;
        ConcurrentMap<TenantNetworkId, TenantNetwork> networksMap = Maps
                .newConcurrentMap();
        if (node != null) {
            String name = node.get("name").asText();
            boolean adminStateUp = node.get("admin_state_up").asBoolean();
            String state = node.get("state").asText();
            boolean shared = node.get("shared").asBoolean();
            String tenantID = node.get("tenant_id").asText();
            boolean routerExternal = node.get("routerExternal").asBoolean();
            String type = node.get("type").asText();
            String physicalNetwork = node.get("physicalNetwork").asText();
            String segmentationID = node.get("segmentationID").asText();
            TenantNetworkId id = null;
            if (flag == DELETE_NETWORK) {
                id = TenantNetworkId.networkId(node.get("id").asText());
            } else {
                id = networkId;
            }
            network = new DefaultTenantNetwork(
                                                id,
                                                name,
                                                adminStateUp,
                                                isState(state),
                                                shared,
                                                TenantId.tenantId(tenantID),
                                                routerExternal,
                                                isType(type),
                                                PhysicalNetwork
                                                        .physicalNetwork(physicalNetwork),
                                                SegmentationId
                                                        .segmentationID(segmentationID));
            networksMap.putIfAbsent(id, network);
        }
        return Collections.unmodifiableCollection(networksMap.keySet());
    }

    /**
     * Returns a Object of the currently known infrastructure network.
     *
     * @param JsonNode node the network json
     * @return Object of network
     */
    public Iterable<TenantNetwork> changeJson2objs(JsonNode nodes) {
        TenantNetwork network = null;
        ConcurrentMap<TenantNetworkId, TenantNetwork> networksMap = Maps
                .newConcurrentMap();
        if (nodes != null) {
            for (JsonNode node : nodes) {
                String id = node.get("id").asText();
                String name = node.get("name").asText();
                boolean adminStateUp = node.get("admin_state_up").asBoolean();
                String state = node.get("state").asText();
                boolean shared = node.get("shared").asBoolean();
                String tenantID = node.get("tenant_id").asText();
                boolean routerExternal = node.get("routerExternal").asBoolean();
                String type = node.get("type").asText();
                String physicalNetwork = node.get("physicalNetwork").asText();
                String segmentationID = node.get("segmentationID").asText();
                network = new DefaultTenantNetwork(
                                                    TenantNetworkId
                                                            .networkId(id),
                                                    name,
                                                    adminStateUp,
                                                    isState(state),
                                                    shared,
                                                    TenantId.tenantId(tenantID),
                                                    routerExternal,
                                                    isType(type),
                                                    PhysicalNetwork
                                                            .physicalNetwork(physicalNetwork),
                                                    SegmentationId
                                                            .segmentationID(segmentationID));
                networksMap
                        .putIfAbsent(TenantNetworkId.networkId(id), network);
            }
        }
        return Collections.unmodifiableCollection(networksMap.values());
    }

    /**
     * Returns a Object of the currently known infrastructure network.
     *
     * @param JsonNode node the network json
     * @return Object of network
     */
    public Iterable<TenantNetworkId> changeJson2objsId(JsonNode nodes) {
        TenantNetwork network = null;
        ConcurrentMap<TenantNetworkId, TenantNetwork> networksMap = Maps
                .newConcurrentMap();
        if (nodes != null) {
            for (JsonNode node : nodes) {
                String id = node.get("id").asText();
                String name = node.get("name").asText();
                boolean adminStateUp = node.get("admin_state_up").asBoolean();
                String state = node.get("state").asText();
                boolean shared = node.get("shared").asBoolean();
                String tenantID = node.get("tenant_id").asText();
                boolean routerExternal = node.get("routerExternal").asBoolean();
                String type = node.get("type").asText();
                String physicalNetwork = node.get("physicalNetwork").asText();
                String segmentationID = node.get("segmentationID").asText();
                network = new DefaultTenantNetwork(
                                                    TenantNetworkId
                                                            .networkId(id),
                                                    name,
                                                    adminStateUp,
                                                    isState(state),
                                                    shared,
                                                    TenantId.tenantId(tenantID),
                                                    routerExternal,
                                                    isType(type),
                                                    PhysicalNetwork
                                                            .physicalNetwork(physicalNetwork),
                                                    SegmentationId
                                                            .segmentationID(segmentationID));
                networksMap
                        .putIfAbsent(TenantNetworkId.networkId(id), network);
            }
        }
        return Collections.unmodifiableCollection(networksMap.keySet());
    }

    private State isState(String state) {
        if (state.equals("ACTIVE")) {
            return TenantNetwork.State.ACTIVE;
        } else if (state.equals("BUILD")) {
            return TenantNetwork.State.BUILD;
        } else if (state.equals("DOWN")) {
            return TenantNetwork.State.DOWN;
        } else if (state.equals("ERROE")) {
            return TenantNetwork.State.ERROR;
        } else {
            return null;
        }
    }

    private Type isType(String type) {
        if (type.equals("LOCAL")) {
            return TenantNetwork.Type.LOCAL;
        } else {
            return null;
        }
    }

    @GET
    @Path("{id}")
    public Response getNetwork(@PathParam("id") String id) {
        TenantNetwork network = nullIsNotFound(get(TenantNetworkService.class)
                                                        .getNetwork(TenantNetworkId
                                                                            .networkId(id)),
                                                NETWORK_NOT_FOUND);
        ObjectNode result = new ObjectMapper().createObjectNode();
        result.set("network", new TenantNetworkCodec().encode(network, this));

        return ok(result.toString()).build();

    }

    @DELETE
    @Path("{id}")
    public Response deleteNetworks(@PathParam("id") String id) {
        log.info("Delete network by identifier {}.", id);
        Set<TenantNetworkId> networkSet = new HashSet<TenantNetworkId>();
        networkSet.add(TenantNetworkId.networkId(id));
        Boolean issuccess = nullIsNotFound(get(TenantNetworkService.class)
                .removeNetworks(networkSet), NETWORK_NOT_FOUND);
        if (!issuccess) {
            log.info("Network identifier {} is not existed", id);
            return Response.status(INTERNAL_SERVER_ERROR)
                    .entity(NETWORK_ID_NOT_EXIST).build();
        }
        return Response.status(OK).entity(issuccess.toString()).build();
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteNetworks(InputStream input)
            throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode cfg = mapper.readTree(input);
            JsonNode nodes = null;
            Iterable<TenantNetworkId> networksId = null;
            if (cfg.get("network") != null) {
                nodes = cfg.get("network");
                if (nodes.isArray()) {
                    networksId = changeJson2objsId(nodes);
                } else {
                    networksId = changeJson2objId(DELETE_NETWORK, null, nodes);
                }
            } else if (cfg.get("networks") != null) {
                nodes = cfg.get("networks");
                networksId = changeJson2objsId(nodes);
            }
            Boolean issuccess = nullIsNotFound((get(TenantNetworkService.class)
                                                       .removeNetworks(networksId)),
                                               NETWORK_NOT_FOUND);

            if (!issuccess) {
                return Response.status(INTERNAL_SERVER_ERROR)
                        .entity(NETWORK_ID_NOT_EXIST).build();
            }
            return Response.status(OK).entity(issuccess.toString()).build();
        } catch (Exception e) {
            log.info("delete network exception {}.", e.toString());
            return Response.status(INTERNAL_SERVER_ERROR).entity(e.toString())
                    .build();
        }
    }

    @POST
    public Response createNetworks(InputStream input) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode cfg = mapper.readTree(input);
            JsonNode nodes = null;
            Iterable<TenantNetwork> networks = null;
            if (cfg.get("network") != null) {
                nodes = cfg.get("network");
                if (nodes.isArray()) {
                    networks = changeJson2objs(nodes);
                } else {
                    networks = changeJson2obj(CREATE_NETWORK, null, nodes);
                }
            } else if (cfg.get("networks") != null) {
                nodes = cfg.get("networks");
                networks = changeJson2objs(nodes);
            }
            Boolean issuccess = nullIsNotFound((get(TenantNetworkService.class)
                                                       .createNetworks(networks)),
                                               NETWORK_NOT_FOUND);

            if (!issuccess) {
                return Response.status(INTERNAL_SERVER_ERROR)
                        .entity(NETWORK_ID_EXIST).build();
            }
            return Response.status(OK).entity(issuccess.toString()).build();
        } catch (Exception e) {
            log.info("create network exception {}.", e.toString());
            return Response.status(INTERNAL_SERVER_ERROR).entity(e.toString())
                    .build();
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateNetworks(@PathParam("id") String id, InputStream input)
            throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode cfg = mapper.readTree(input);
            JsonNode nodes = null;
            Iterable<TenantNetwork> networks = null;
            if (cfg.get("network") != null) {
                nodes = cfg.get("network");
                if (nodes.isArray()) {
                    networks = changeJson2objs(nodes);
                } else {
                    networks = changeJson2obj(UPDATE_NETWORK, null, nodes);
                }
            } else if (cfg.get("networks") != null) {
                nodes = cfg.get("networks");
                networks = changeJson2objs(nodes);
            }
            Boolean issuccess = nullIsNotFound((get(TenantNetworkService.class)
                                                       .updateNetworks(networks)),
                                               NETWORK_NOT_FOUND);

            if (!issuccess) {
                return Response.status(INTERNAL_SERVER_ERROR)
                        .entity(NETWORK_ID_NOT_EXIST).build();
            }
            return Response.status(OK).entity(issuccess.toString()).build();
        } catch (Exception e) {
            log.info("update network exception {}.", e.toString());
            return Response.status(INTERNAL_SERVER_ERROR).entity(e.toString())
                    .build();
        }
    }

    protected <T> T nullIsNotFound(T item, String message) {
        if (item == null) {
            throw new ItemNotFoundException(message);
        }
        return item;
    }
}
