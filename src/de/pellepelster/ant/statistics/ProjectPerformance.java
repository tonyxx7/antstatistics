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

public class ProjectPerformance {

    private String projectName;
    private List<ProjectPerformance> subProjects = new ArrayList<ProjectPerformance>();
    private List<TargetPerformance> targets = new ArrayList<TargetPerformance>();
    private Date startDate;
    private Date stopDate;

    public ProjectPerformance(String projectName) {
        super();
        setProjectName(projectName);
    }

    public ProjectPerformance() {
        super();
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStopDate() {
        return stopDate;
    }

    public void setStopDate(Date stopDate) {
        this.stopDate = stopDate;
    }

    public void startTimer() {
        startDate = new Date();
    }

    public void stopTimer() {
        stopDate = new Date();
    }

    public long getDuration() {
        return stopDate.getTime() - startDate.getTime();
    }

    public void setTargets(List<TargetPerformance> targets) {
        this.targets = targets;
    }

    public List<TargetPerformance> getTargets() {
        return targets;
    }

    public void setSubProjects(List<ProjectPerformance> subProjects) {
        this.subProjects = subProjects;
    }

    public List<ProjectPerformance> getSubProjects() {
        return subProjects;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectName() {
        return projectName;
    }

}
