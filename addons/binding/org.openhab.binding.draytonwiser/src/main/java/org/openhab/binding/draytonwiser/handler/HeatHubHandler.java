/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.draytonwiser.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.draytonwiser.DraytonWiserBindingConstants;
import org.openhab.binding.draytonwiser.internal.config.Device;
import org.openhab.binding.draytonwiser.internal.config.Domain;
import org.openhab.binding.draytonwiser.internal.config.HeatingChannel;
import org.openhab.binding.draytonwiser.internal.config.Room;
import org.openhab.binding.draytonwiser.internal.config.RoomStat;
import org.openhab.binding.draytonwiser.internal.config.SmartValve;
import org.openhab.binding.draytonwiser.internal.config.Station;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link HeatHubHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andrew Schofield - Initial contribution
 */
@NonNullByDefault
public class HeatHubHandler extends BaseBridgeHandler {

    @Nullable
    protected ScheduledFuture<?> refreshJob;

    private final Logger logger = LoggerFactory.getLogger(HeatHubHandler.class);
    private HttpClient httpClient;
    private Gson gson;

    @Nullable
    private Domain domain;

    public HeatHubHandler(Bridge thing) {
        super(thing);
        httpClient = new HttpClient();
        gson = new Gson();

        try {
            httpClient.start();
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
        }
    }

    @Override
    public void dispose() {
        if (httpClient != null) {
            httpClient.destroy();
        }

        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
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
        logger.debug("Initializing Drayton Wiser Heat Hub handler");
        Device device = getExtendedDeviceProperties(0);
        if (device != null) {
            Map<String, String> properties = new HashMap<>();
            properties.put("Device Type", device.getProductIdentifier());
            properties.put("Firmware Version", device.getActiveFirmwareVersion());
            properties.put("Manufacturer", device.getManufacturer());
            properties.put("Model", device.getModelIdentifier());
            getThing().setProperties(properties);
        }

        startAutomaticRefresh();
        refresh();
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

    private void refresh() {
        try {
            domain = getDomain();
        } catch (Exception e) {
            logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    public @Nullable Domain getDomain() {
        ContentResponse response = sendMessageToHeatHub(DraytonWiserBindingConstants.DOMAIN_ENDPOINT, HttpMethod.GET,
                "");

        if (response == null) {
            return null;
        }

        Domain domain = gson.fromJson(response.getContentAsString(), Domain.class);
        return domain;
    }

    public List<RoomStat> getRoomStats() {
        if (domain == null) {
            return new ArrayList<RoomStat>();
        }

        return domain.getRoomStat();
    }

    public List<SmartValve> getSmartValves() {
        if (domain == null) {
            return new ArrayList<SmartValve>();
        }

        return domain.getSmartValve();
    }

    public List<Room> getRooms() {
        if (domain == null) {
            return new ArrayList<Room>();
        }

        return domain.getRoom();
    }

    public @Nullable Room getRoom(Integer id) {
        if (domain == null) {
            return null;
        }

        for (Room room : domain.getRoom()) {
            if (room.getId().equals(id)) {
                return room;
            }
        }

        return null;
    }

    public @Nullable RoomStat getRoomStat(Integer id) {
        if (domain == null) {
            return null;
        }

        for (RoomStat roomStat : domain.getRoomStat()) {
            if (roomStat.getId().equals(id)) {
                return roomStat;
            }
        }

        return null;
    }

    public @Nullable SmartValve getSmartValve(Integer id) {
        if (domain == null) {
            return null;
        }

        for (SmartValve smartValve : domain.getSmartValve()) {
            if (smartValve.getId().equals(id)) {
                return smartValve;
            }
        }

        return null;
    }

    public @Nullable Device getExtendedDeviceProperties(int id) {
        if (domain == null) {
            return null;
        }

        for (Device device : domain.getDevice()) {
            if (device.getId().equals(id)) {
                return device;
            }
        }

        return null;
    }

    public org.openhab.binding.draytonwiser.internal.config.@Nullable System getSystem() {
        if (domain == null) {
            return null;
        }

        return domain.getSystem();
    }

    public @Nullable Station getStation() {
        Station station = null;
        ContentResponse response = sendMessageToHeatHub(DraytonWiserBindingConstants.STATION_ENDPOINT, HttpMethod.GET,
                "");

        if (response == null) {
            return null;
        }

        station = gson.fromJson(response.getContentAsString(), Station.class);
        return station;
    }

    public List<HeatingChannel> getHeatingChannels() {
        if (domain == null) {
            return new ArrayList<HeatingChannel>();
        }

        return domain.getHeatingChannel();
    }

    private @Nullable ContentResponse sendMessageToHeatHub(String path, HttpMethod method, String content) {
        try {
            String address = (String) getConfig().get(DraytonWiserBindingConstants.ADDRESS);
            String authtoken = (String) getConfig().get(DraytonWiserBindingConstants.AUTH_TOKEN);
            StringContentProvider contentProvider = new StringContentProvider(content);
            ContentResponse response = httpClient.newRequest("http://" + address + "/" + path).method(HttpMethod.GET)
                    .header("SECRET", authtoken).content(contentProvider).send();
            if (response.getStatus() == 200) {
                updateStatus(ThingStatus.ONLINE);
                return response;
            } else if (response.getStatus() == 401) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid authorization token");
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Incorrect Heat Hub address");
        } catch (Exception e) {
            e.printStackTrace();
            updateStatus(ThingStatus.OFFLINE);
        }
        return null;
    }
}
