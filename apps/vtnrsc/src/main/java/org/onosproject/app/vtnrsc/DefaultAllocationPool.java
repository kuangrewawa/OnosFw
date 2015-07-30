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
package org.onosproject.app.vtnrsc;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;
import java.util.Objects;

import org.onlab.packet.IpAddress;

/**
 * The continuous IP address range between the start address and the end address
 * for the allocation pools.
 */
public final class DefaultAllocationPool implements AllocationPool {

    private final IpAddress startIP;
    private final IpAddress endIP;

    public DefaultAllocationPool(IpAddress startIP, IpAddress endIP) {
        checkNotNull(startIP, "StartIP cannot be null");
        checkNotNull(endIP, "EndIP cannot be null");
        this.startIP = startIP;
        this.endIP = endIP;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startIP, endIP);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DefaultAllocationPool) {
            final DefaultAllocationPool other = (DefaultAllocationPool) obj;
            return Objects.equals(this.startIP, other.startIP)
                    && Objects.equals(this.endIP, other.endIP);
        }
        return false;
    }

    @Override
    public String toString() {
        return toStringHelper(this).add("startIP", startIP).add("endIP", endIP)
                .toString();
    }

    @Override
    public IpAddress startIP() {
        return startIP;
    }

    @Override
    public IpAddress endIP() {
        return endIP;
    }
}
