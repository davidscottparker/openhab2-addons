/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.draytonwiser.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.draytonwiser.DraytonWiserBindingConstants;

public abstract class DraytonWiserThingHandler extends BaseThingHandler {

    @Nullable
    protected ScheduledFuture<?> refreshJob;

    protected DraytonWiserThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);

        startAutomaticRefresh();
        refresh();
    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
    }

    private void startAutomaticRefresh() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            refreshJob = scheduler.scheduleWithFixedDelay(() -> {
                refresh();
            }, 0, ((java.math.BigDecimal) bridge.getConfiguration().get(DraytonWiserBindingConstants.REFRESH_INTERVAL))
                    .intValue(), TimeUnit.SECONDS);
        }
    }

    protected abstract void refresh();

    protected HeatHubHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            return ((HeatHubHandler) bridge.getHandler());
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return null;
        }
    }

}
