/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.draytonwiser.handler;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.draytonwiser.DraytonWiserBindingConstants;
import org.openhab.binding.draytonwiser.internal.config.SmartValve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TRVHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andrew Schofield - Initial contribution
 */
@NonNullByDefault
public class TRVHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TRVHandler.class);

    @Nullable
    private SmartValve smartValve;

    @Nullable
    private ScheduledFuture<?> refreshJob;

    public TRVHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refresh();
        }
        // if (channelUID.getId().equals(CHANNEL_1)) {
        // TODO: handle command

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");
        // }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);

        startAutomaticRefresh();
        refresh();
    }

    @Override
    public void dispose() {
        refreshJob.cancel(true);
    }

    private void startAutomaticRefresh() {
        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            refresh();
        }, 0, ((java.math.BigDecimal) getBridge().getConfiguration().get(DraytonWiserBindingConstants.REFRESH_INTERVAL))
                .intValue(), TimeUnit.SECONDS);
    }

    private void refresh() {
        try {
            boolean smartValveUpdated = updateSmartValveData();
            if (smartValveUpdated) {
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_CURRENT_TEMPERATURE),
                        getTemperature());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_CURRENT_DEMAND),
                        getDemand());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_CURRENT_SETPOINT),
                        getSetPoint());
            }
        } catch (Exception e) {
            logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private boolean updateSmartValveData() {
        smartValve = ((HeatHubHandler) getBridge().getHandler())
                .getSmartValve(((BigDecimal) getThing().getConfiguration().get("internalID")).intValue());
        return smartValve != null;
    }

    private State getSetPoint() {
        if (smartValve != null) {
            return new DecimalType((float) smartValve.getSetPoint() / 10);
        }

        return UnDefType.UNDEF;
    }

    private State getDemand() {
        if (smartValve != null) {
            return new DecimalType(smartValve.getPercentageDemand());
        }
        return UnDefType.UNDEF;
    }

    private State getTemperature() {
        if (smartValve != null) {
            return new DecimalType((float) smartValve.getMeasuredTemperature() / 10);
        }

        return UnDefType.UNDEF;
    }
}
