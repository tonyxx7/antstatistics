/*
 * TargetPerformance.java created on 27.07.2010
 *
 * Copyright (c) 2008 Lufthansa Systems
 * All rights reserved.
 *
 * This program and the accompanying materials are proprietary information
 * of Lufthansa Systems.
 * Use is subject to license terms.
 */
package de.pellepelster.ant.statistics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TargetPerformance {

    private String targetName;
    private final List<Long> timings = new ArrayList<Long>();
    private Date startDate;

    public TargetPerformance() {
        super();
    }

    public TargetPerformance(String targetName) {
        super();
        this.targetName = targetName;
    }

    public Long getDuration() {
        Long result = 0L;

        for (Long timing : timings) {
            result += timing;
        }

        return result;
    }

    public int getInvocationCount() {
        return timings.size();
    }

    public Date getStartDate() {
        return startDate;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public void startTimer() {
        if (startDate == null) {
            startDate = new Date();
        } else {
            throw new RuntimeException(String.format("target '%s' has already a running timer",
                    targetName));
        }
    }

    public void stopTimer() {
        if (startDate != null) {
            Date stopDate = new Date();
            timings.add(stopDate.getTime() - startDate.getTime());
            startDate = null;
        } else {
            throw new RuntimeException(String.format("target '%s' has no running timer", targetName));
        }
    }

}
