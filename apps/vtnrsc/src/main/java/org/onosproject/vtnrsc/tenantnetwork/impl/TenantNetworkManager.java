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
package org.onosproject.vtnrsc.tenantnetwork.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collections;
import java.util.Iterator;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.util.KryoNamespace;
import org.onosproject.store.service.EventuallyConsistentMap;
import org.onosproject.store.service.MultiValuedTimestamp;
import org.onosproject.store.service.StorageService;
import org.onosproject.store.service.WallClockTimestamp;
import org.onosproject.vtnrsc.TenantNetwork;
import org.onosproject.vtnrsc.TenantNetworkId;
import org.onosproject.vtnrsc.tenantnetwork.TenantNetworkService;
import org.slf4j.Logger;

/**
 * Provides implementation of the tenantNetworkService.
 */
@Component(immediate = true)
@Service
public class TenantNetworkManager implements TenantNetworkService {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StorageService storageService;
    private EventuallyConsistentMap<TenantNetworkId, TenantNetwork> networkIdAsKeyStore;
    private final Logger log = getLogger(getClass());
    private static final String NETWORK_ID_NULL = "Network ID cannot be null";

    @Activate
    public void activate() {
        KryoNamespace.Builder serializer = KryoNamespace.newBuilder()
                .register(MultiValuedTimestamp.class);
        networkIdAsKeyStore = storageService
                .<TenantNetworkId, TenantNetwork>eventuallyConsistentMapBuilder()
                .withName("all_network").withSerializer(serializer)
                .withTimestampProvider((k, v) -> new WallClockTimestamp())
                .build();
        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        networkIdAsKeyStore.destroy();
        log.info("Stopped");
    }

    @Override
    public boolean exists(TenantNetworkId networkId) {
        return networkIdAsKeyStore.containsKey(networkId);
    }

    @Override
    public int getNetworkCount() {
        return networkIdAsKeyStore.size();
    }

    @Override
    public Iterable<TenantNetwork> getNetworks() {
        return Collections.unmodifiableCollection(networkIdAsKeyStore.values());
    }

    @Override
    public TenantNetwork getNetwork(TenantNetworkId networkId) {
        checkNotNull(networkId, NETWORK_ID_NULL);
        return networkIdAsKeyStore.get(networkId);
    }

    @Override
    public boolean createNetworks(Iterable<TenantNetwork> networks) {
        Iterator<TenantNetwork> networkors = networks.iterator();
        while (networkors.hasNext()) {
            TenantNetwork network = networkors.next();
            networkIdAsKeyStore.put(network.id(), network);
        }
        return true;
    }

    @Override
    public boolean updateNetworks(Iterable<TenantNetwork> networks) {
        Iterator<TenantNetwork> networkors = networks.iterator();
        while (networkors.hasNext()) {
            TenantNetwork network = networkors.next();
            networkIdAsKeyStore.put(network.id(), network);
        }
        return true;
    }

    @Override
    public boolean removeNetworks(Iterable<TenantNetworkId> networks) {
        Iterator<TenantNetworkId> networkors = networks.iterator();
        while (networkors.hasNext()) {
            TenantNetworkId network = networkors.next();
            networkIdAsKeyStore.remove(network);
        }
        return true;
    }
}
