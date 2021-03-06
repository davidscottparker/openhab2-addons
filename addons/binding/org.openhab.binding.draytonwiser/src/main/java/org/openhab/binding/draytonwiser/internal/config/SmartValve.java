/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.draytonwiser.internal.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class SmartValve {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("SetPoint")
    @Expose
    private Integer setPoint;
    @SerializedName("MeasuredTemperature")
    @Expose
    private Integer measuredTemperature;
    @SerializedName("PercentageDemand")
    @Expose
    private Integer percentageDemand;
    @SerializedName("WindowState")
    @Expose
    private String windowState;
    @SerializedName("ExternalRoomStatTemperature")
    @Expose
    private Integer externalRoomStatTemperature;
    @SerializedName("MountingOrientation")
    @Expose
    private String mountingOrientation;

    public Integer getId() {
        return id;
    }

    public Integer getSetPoint() {
        return setPoint < 0 ? 0 : setPoint;
    }

    public void setSetPoint(Integer setPoint) {
        this.setPoint = setPoint;
    }

    public Integer getMeasuredTemperature() {
        return measuredTemperature;
    }

    public Integer getPercentageDemand() {
        return percentageDemand;
    }

    public String getWindowState() {
        return windowState;
    }

    public Integer getExternalRoomStatTemperature() {
        return externalRoomStatTemperature;
    }

}