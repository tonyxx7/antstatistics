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
import java.util.List;

public class ProjectPerformances {

    private List<ProjectPerformance> projects = new ArrayList<ProjectPerformance>();

    public ProjectPerformances() {
        super();
    }

    public List<ProjectPerformance> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectPerformance> projects) {
        this.projects = projects;
    }

}
