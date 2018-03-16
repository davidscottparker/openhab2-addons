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

public class LocalDateAndTime {

    @SerializedName("Year")
    @Expose
    private Integer year;
    @SerializedName("Month")
    @Expose
    private String month;
    @SerializedName("Date")
    @Expose
    private Integer date;
    @SerializedName("Day")
    @Expose
    private String day;
    @SerializedName("Time")
    @Expose
    private Integer time;

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

}
