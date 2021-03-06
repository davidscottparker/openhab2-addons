/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.draytonwiser.handler;

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
            return;
        }

        if (channelUID.getId().equals(DraytonWiserBindingConstants.CHANNEL_CURRENT_SETPOINT)) {
            int newSetPoint = Math.round((Float.parseFloat(command.toString()) * 10));
            setSetPoint(newSetPoint);
        }

        if (channelUID.getId().equals(DraytonWiserBindingConstants.CHANNEL_MANUAL_MODE_STATE)) {
            boolean manualMode = command.toString().toUpperCase().equals("ON");
            setManualMode(manualMode);
        }

        if (channelUID.getId().equals(DraytonWiserBindingConstants.CHANNEL_ROOM_BOOST_DURATION)) {
            int boostDuration = Math.round((Float.parseFloat(command.toString()) * 60));
            setBoostDuration(boostDuration);
        }
    }

    @Override
    protected void refresh() {
        try {
            boolean roomUpdated = updateRoomData();
            if (roomUpdated) {
                updateStatus(ThingStatus.ONLINE);
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
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_MANUAL_MODE_STATE),
                        getManualModeState());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_ROOM_BOOSTED),
                        getBoostedState());
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_ROOM_BOOST_REMAINING),
                        getBoostRemainingState());
            }
        } catch (Exception e) {
            logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private boolean updateRoomData() {
        if (bridgeHandler == null) {
            return false;
        }
        room = bridgeHandler.getRoom(getThing().getConfiguration().get("roomName").toString());
        return room != null;
    }

    private State getSetPoint() {
        if (room != null) {
            return new DecimalType((float) room.getCurrentSetPoint() / 10);
        }

        return UnDefType.UNDEF;
    }

    private void setSetPoint(Integer setPoint) {
        if (bridgeHandler != null) {
            bridgeHandler.setRoomSetPoint(getThing().getConfiguration().get("roomName").toString(), setPoint);
        }
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
            Integer fullScaleTemp = room.getCalculatedTemperature();
            if (fullScaleTemp.equals(DraytonWiserBindingConstants.OFFLINE_TEMPERATURE)) {
                return UnDefType.UNDEF;
            }
            return new DecimalType((float) fullScaleTemp / 10);
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
        if (bridgeHandler != null) {
            return bridgeHandler.getRoomStat(id);
        }
        return null;
    }

    private State getManualModeState() {
        if (room != null) {
            if (room.getMode().toUpperCase().equals("MANUAL")) {
                return OnOffType.ON;
            }
        }

        return OnOffType.OFF;
    }

    private void setManualMode(Boolean manualMode) {
        if (bridgeHandler != null) {
            bridgeHandler.setRoomManualMode(getThing().getConfiguration().get("roomName").toString(), manualMode);
        }
    }

    private State getBoostedState() {
        if (room != null) {
            if (room.getOverrideTimeoutUnixTime() != null && !room.getOverrideType().toUpperCase().equals("NONE")) {
                return OnOffType.ON;
            }
        }
        updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_ROOM_BOOST_DURATION),
                new DecimalType(0));

        return OnOffType.OFF;
    }

    private State getBoostRemainingState() {
        if (room != null) {
            if (room.getOverrideTimeoutUnixTime() != null && !room.getOverrideType().toUpperCase().equals("NONE")) {
                return new DecimalType((room.getOverrideTimeoutUnixTime() - (System.currentTimeMillis() / 1000L)) / 60);
            }
        }

        return new DecimalType(0);
    }

    private void setBoostDuration(Integer durationMinutes) {
        if (bridgeHandler != null && room != null) {
            if (durationMinutes > 0) {
                bridgeHandler.setRoomBoostActive(getThing().getConfiguration().get("roomName").toString(),
                        room.getCalculatedTemperature() + 20, durationMinutes);
            } else {
                bridgeHandler.setRoomBoostInactive(getThing().getConfiguration().get("roomName").toString());
            }
        }
    }
}
