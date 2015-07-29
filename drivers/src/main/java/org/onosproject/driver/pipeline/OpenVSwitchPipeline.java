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
package org.onosproject.driver.pipeline;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.Collections;

import org.onlab.osgi.ServiceDirectory;
import org.onosproject.core.CoreService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.behaviour.Pipeliner;
import org.onosproject.net.behaviour.PipelinerContext;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleOperations;
import org.onosproject.net.flow.FlowRuleOperationsContext;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.EthTypeCriterion;
import org.onosproject.net.flowobjective.FilteringObjective;
import org.onosproject.net.flowobjective.FlowObjectiveStore;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.flowobjective.NextObjective;
import org.onosproject.net.flowobjective.Objective;
import org.onosproject.net.flowobjective.ObjectiveError;
import org.slf4j.Logger;
/**
 * Driver for OpenVSwitch.
 */
public class OpenVSwitchPipeline extends AbstractHandlerBehaviour
        implements Pipeliner {

    private final Logger log = getLogger(getClass());
    private CoreService coreService;
    private ServiceDirectory serviceDirectory;
    protected FlowObjectiveStore flowObjectiveStore;
    protected DeviceId deviceId;
    protected FlowRuleService flowRuleService;
    protected DeviceService deviceService;
    private static final int MAC_TABLE_PRIORITY = 0xffff;
    private static final int PORT_TABLE_PRIORITY = 0xffff;
    private static final int TIME_OUT = 0;
    private static final int MAC_TABLE = 40;
    private static final int PORT_TABLE = 0;
    private static final short ETH_TYPE_MAC = 40;
    private static final short ETH_TYPE_PORT = 0;

    @Override
    public void init(DeviceId deviceId, PipelinerContext context) {
        this.serviceDirectory = context.directory();
        this.deviceId = deviceId;

        coreService = serviceDirectory.get(CoreService.class);
        flowRuleService = serviceDirectory.get(FlowRuleService.class);
        flowObjectiveStore = context.store();
        coreService.registerApplication("org.onosproject.driver.OpenVSwitchPipeline");

    }

    @Override
    public void filter(FilteringObjective filteringObjective) {
        // TODO Auto-generated method stub
    }

    @Override
    public void forward(ForwardingObjective fwd) {
        log.info("process-------------flowrule---------------");
        Collection<FlowRule> rules;
        FlowRuleOperations.Builder flowOpsBuilder = FlowRuleOperations
                .builder();

        rules = processForward(fwd);
        switch (fwd.op()) {
        case ADD:
            rules.stream().filter(rule -> rule != null)
                    .forEach(flowOpsBuilder::add);
            break;
        case REMOVE:
            rules.stream().filter(rule -> rule != null)
                    .forEach(flowOpsBuilder::remove);
            break;
        default:
            fail(fwd, ObjectiveError.UNKNOWN);
            log.warn("Unknown forwarding type {}", fwd.op());
        }
        log.info("----------------call apply---------------------");
        flowRuleService.apply(flowOpsBuilder
                .build(new FlowRuleOperationsContext() {
                    @Override
                    public void onSuccess(FlowRuleOperations ops) {
                        pass(fwd);
                    }

                    @Override
                    public void onError(FlowRuleOperations ops) {
                        fail(fwd, ObjectiveError.FLOWINSTALLATIONFAILED);
                    }
                }));
    }

    @Override
    public void next(NextObjective nextObjective) {
        // TODO Auto-generated method stub

    }

    private Collection<FlowRule> processForward(ForwardingObjective fwd) {
        log.info("----------------" + fwd.flag() + "---------------------");
        switch (fwd.flag()) {
        case SPECIFIC:
            return processSpecific(fwd);
        case VERSATILE:
            return processVersatile(fwd);
        default:
            fail(fwd, ObjectiveError.UNKNOWN);
            log.warn("Unknown forwarding flag {}", fwd.flag());
        }
        return Collections.emptySet();
    }

    private Collection<FlowRule> processVersatile(ForwardingObjective fwd) {
        log.info("Processing versatile forwarding objective");
        return Collections.emptySet();
    }

    private Collection<FlowRule> processSpecific(ForwardingObjective fwd) {
        log.debug("Processing specific forwarding objective");
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
        TrafficTreatment tb = fwd.treatment();
        EthTypeCriterion criterion = null;
        for (Criterion c : fwd.selector().criteria()) {
            if (c.type().equals(Criterion.Type.ETH_TYPE)) {
                criterion = (EthTypeCriterion) c;
            } else {
                selector.add(c);
            }
        }
        FlowRule.Builder ruleBuilder = DefaultFlowRule.builder()
                .fromApp(fwd.appId()).withPriority(fwd.priority())
                .forDevice(deviceId).withSelector(selector.build())
                .withTreatment(tb).makeTemporary(TIME_OUT).forTable(PORT_TABLE);

        if (fwd.permanent()) {
            ruleBuilder.makePermanent();
        }
        if (ETH_TYPE_PORT == criterion.ethType().toShort()) {
            ruleBuilder.withPriority(PORT_TABLE_PRIORITY);
            ruleBuilder.forTable(PORT_TABLE);
        } else if (ETH_TYPE_MAC == criterion.ethType().toShort()) {
            ruleBuilder.withPriority(MAC_TABLE_PRIORITY);
            ruleBuilder.forTable(MAC_TABLE);
        } else {
            log.info("Unknown table");
        }
        return Collections.singletonList(ruleBuilder.build());
    }

    private void fail(Objective obj, ObjectiveError error) {
        if (obj.context().isPresent()) {
            obj.context().get().onError(obj, error);
        }
    }

    private void pass(Objective obj) {
        if (obj.context().isPresent()) {
            obj.context().get().onSuccess(obj);
        }
    }
}