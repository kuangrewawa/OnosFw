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
package org.onosproject.vtnrsc.subnet.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collections;

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
import org.onosproject.vtnrsc.Subnet;
import org.onosproject.vtnrsc.SubnetId;
import org.onosproject.vtnrsc.subnet.SubnetService;
import org.onosproject.vtnrsc.tenantnetwork.TenantNetworkService;
import org.slf4j.Logger;

/**
 * Provides implementation of the subnet service.
 */
@Component(immediate = true)
@Service
public class SubnetManager implements SubnetService {

    private static final String SUBNET_ID_NULL = "Subnet ID cannot be null";

    private final Logger log = getLogger(getClass());

    private EventuallyConsistentMap<SubnetId, Subnet> subnetStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StorageService storageService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TenantNetworkService tenantNetworkService;

    @Activate
    public void activate() {

        KryoNamespace.Builder serializer = KryoNamespace.newBuilder()
                .register(MultiValuedTimestamp.class);
        subnetStore = storageService
                .<SubnetId, Subnet>eventuallyConsistentMapBuilder()
                .withName("all_subnet").withSerializer(serializer)
                .withTimestampProvider((k, v) -> new WallClockTimestamp())
                .build();

        log.info("SubnetManager  started");
    }

    @Deactivate
    public void deactivate() {
        subnetStore.destroy();
        log.info("SubnetManager stopped");
    }

    @Override
    public Iterable<Subnet> getSubnets() {
        return Collections.unmodifiableCollection(subnetStore.values());
    }

    @Override
    public Subnet getSubnet(SubnetId subnetId) {
        checkNotNull(subnetId, SUBNET_ID_NULL);
        return subnetStore.get(subnetId);
    }

    @Override
    public boolean exists(SubnetId subnetId) {
        checkNotNull(subnetId, SUBNET_ID_NULL);
        return subnetStore.containsKey(subnetId);
    }

    @Override
    public boolean createSubnets(Iterable<Subnet> subnets) {
        for (Subnet subnet : subnets) {
            if (!tenantNetworkService.exists(subnet.networkId())) {
                return false;
            }
            subnetStore.put(subnet.id(), subnet);
        }
        return true;
    }

    @Override
    public boolean updateSubnets(Iterable<Subnet> subnets) {
        if (subnets != null) {
            for (Subnet subnet : subnets) {
                subnetStore.put(subnet.id(), subnet);
            }
        }
        return true;
    }

    @Override
    public boolean removeSubnets(Iterable<SubnetId> subnetIds) {
        if (subnetIds != null) {
            for (SubnetId subnetId : subnetIds) {
                subnetStore.remove(subnetId);
            }
        }
        return true;
    }

}
