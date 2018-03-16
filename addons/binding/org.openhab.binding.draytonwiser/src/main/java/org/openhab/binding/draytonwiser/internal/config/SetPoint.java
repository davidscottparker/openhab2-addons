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
public class SetPoint {

    @SerializedName("Time")
    @Expose
    private Integer time;
    @SerializedName("DegreesC")
    @Expose
    private Integer degreesC;

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Integer getDegreesC() {
        return degreesC;
    }

    public void setDegreesC(Integer degreesC) {
        this.degreesC = degreesC;
    }

}
