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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.draytonwiser.DraytonWiserBindingConstants;
import org.openhab.binding.draytonwiser.internal.config.Room;
import org.openhab.binding.draytonwiser.internal.config.RoomStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RoomHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andrew Schofield - Initial contribution
 */
@NonNullByDefault
public class RoomHandler extends DraytonWiserThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RoomHandler.class);

    @Nullable
    private Room room;

    public RoomHandler(Thing thing) {
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
    protected void refresh() {
        try {
            boolean roomUpdated = updateRoomData();
            if (roomUpdated) {
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_CURRENT_TEMPERATURE),
                        getTemperature());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_CURRENT_HUMIDITY),
                        getHumidity());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_CURRENT_SETPOINT),
                        getSetPoint());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_CURRENT_DEMAND),
                        getDemand());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_HEAT_REQUEST),
                        getHeatRequest());
            }
        } catch (Exception e) {
            logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private boolean updateRoomData() {
        room = getBridgeHandler().getRoom(((BigDecimal) getThing().getConfiguration().get("internalID")).intValue());
        return room != null;
    }

    private State getSetPoint() {
        if (room != null) {
            return new DecimalType((float) room.getCurrentSetPoint() / 10);
        }

        return UnDefType.UNDEF;
    }

    private State getHumidity() {
        if (room != null && room.getRoomStatId() != null) {
            RoomStat roomStat = getRoomStat(room.getRoomStatId());
            if (roomStat != null) {
                return new DecimalType(roomStat.getMeasuredHumidity());
            }
        }
        return UnDefType.UNDEF;
    }

    private State getTemperature() {
        if (room != null) {
            return new DecimalType((float) room.getCalculatedTemperature() / 10);
        }

        return UnDefType.UNDEF;
    }

    private State getDemand() {
        if (room != null) {
            return new DecimalType(room.getPercentageDemand());
        }

        return UnDefType.UNDEF;
    }

    private State getHeatRequest() {
        if (room != null) {
            if (room.getControlOutputState().toUpperCase().equals("ON")) {
                return OnOffType.ON;
            }
        }

        return OnOffType.OFF;
    }

    @Nullable
    private RoomStat getRoomStat(int id) {
        return getBridgeHandler().getRoomStat(id);
    }
}
