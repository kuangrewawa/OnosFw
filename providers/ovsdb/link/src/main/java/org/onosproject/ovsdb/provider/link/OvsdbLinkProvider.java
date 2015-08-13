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
package org.onosproject.ovsdb.provider.link;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.onlab.util.Tools.toHex;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.core.CoreService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.PortNumber;
import org.onosproject.net.link.DefaultLinkDescription;
import org.onosproject.net.link.LinkDescription;
import org.onosproject.net.link.LinkProvider;
import org.onosproject.net.link.LinkProviderRegistry;
import org.onosproject.net.link.LinkProviderService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.ovsdb.controller.EventSubject;
import org.onosproject.ovsdb.controller.OvsdbConstant;
import org.onosproject.ovsdb.controller.OvsdbController;
import org.onosproject.ovsdb.controller.OvsdbEvent;
import org.onosproject.ovsdb.controller.OvsdbEventListener;
import org.onosproject.ovsdb.controller.OvsdbEventSubject;
import org.slf4j.Logger;

/**
 * Provider which uses an ovsdb controller to detect link.
 */
@Component(immediate = true)
@Service
public class OvsdbLinkProvider extends AbstractProvider implements LinkProvider {
    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkProviderRegistry providerRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkService linkService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected OvsdbController controller;

    private LinkProviderService providerService;
    private OvsdbEventListener innerEventListener = new InnerOvsdbEventListener();

    @Activate
    public void activate() {
        providerService = providerRegistry.register(this);
        controller.addOvsdbEventListener(innerEventListener);
        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        providerRegistry.unregister(this);
        providerService = null;
        log.info("Stopped");
    }

    public OvsdbLinkProvider() {
        super(new ProviderId("ovsdb", "org.onosproject.ovsdb.provider.link"));
    }

    private class InnerOvsdbEventListener implements OvsdbEventListener {

        @Override
        public void handle(OvsdbEvent<EventSubject> event) {
            OvsdbEventSubject subject = null;
            if (event.subject() instanceof OvsdbEventSubject) {
                subject = (OvsdbEventSubject) event.subject();
            }
            checkNotNull(subject, "EventSubject is not null");
            if (!OvsdbConstant.TYPEVXLAN.equals(subject.portType().toString())) {
                return;
            }
            DeviceId deviceId = DeviceId.deviceId(uri(subject.dpid().value()));
            switch (event.type()) {
            case PORT_ADDED:
                PortNumber portNumber = PortNumber.portNumber(subject.portNumber().value(), subject
                        .portName().value());
                DeviceId dstdeviceId = DeviceId.deviceId(uri(subject.dstDpid().value()));
                PortNumber dstportNumber = PortNumber.portNumber(subject.dstPortNumber().value(),
                                                                 subject.dstPortName().value());
                ConnectPoint src = new ConnectPoint(deviceId, portNumber);
                ConnectPoint dst = new ConnectPoint(dstdeviceId, dstportNumber);
                LinkDescription linkDescription = new DefaultLinkDescription(src, dst,
                                                                             Link.Type.TUNNEL);
                providerService.linkDetected(linkDescription);
                break;
            case PORT_REMOVED:
                providerService.linksVanished(deviceId);
                break;
            default:
                break;
            }

        }

    }

    public URI uri(String value) {
        try {
            return new URI("of", toHex(Long.valueOf(value)), null);
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
