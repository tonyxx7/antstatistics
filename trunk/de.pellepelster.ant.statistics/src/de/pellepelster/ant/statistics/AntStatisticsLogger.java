package de.pellepelster.ant.statistics;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.DefaultLogger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class AntStatisticsLogger extends DefaultLogger {

	private static final String CHART_TITLE = "Target Performance";
	private static final String CHART_YAXIS_LABEL = "Seconds";
	private static final String CHART_XAXIS_LABEL = "Targets";
	private final static String TABLE_HEADER_TARGET = "Target";
	private final static String TABLE_HEADER_DURATION = "Duration";

	private ProjectPerformance rootProject = null;
	private ProjectPerformance currentProject = null;

	private int historyExpire;
	private static final int HISTORY_EXPIRE_DEFAULT = 5;
	private static final String HISTORY_EXPIRE_PROPERTY_NAME = "antstatistics.history.expire";

	private int chartImageWidth;
	private static final int CHART_IMAGE_WIDTH_DEFAULT = 800;
	private static final String IMAGE_WIDTH_PROPERTY_NAME = "antstatistics.chart.width";

	private int chartImageHeight;
	private static final int CHART_IMAGE_HEIGHT_DEFAULT = 600;
	private static final String CHART_IMAGE_HEIGHT_PROPERTY_NAME = "antstatistics.chart.height";

	private int targetThreshold;
	private static final int TARGET_THRESHOLD_DEFAULT = 200;
	private static final String TARGET_THRESHOLD_PROPERTY_NAME = "antstatistics.target.threshold";

	private String dataDirectory;
	private static final String DATA_DIRECTORY_DEFAULT = "antstatistics";
	private static final String DATA_DIRECTORY_PROPERTY_NAME = "antstatistics.directory";

	private final static Comparator<ProjectPerformance> PROJECT_PERFORMANCE_DATE_COMPERATOR = new Comparator<ProjectPerformance>() {

		/** {@inheritDoc} */
		@Override
		public int compare(
				ProjectPerformance project1, ProjectPerformance project2) {

			if (project1 == null || project2 == null
					|| project1.getStartDate() == null
					|| project2.getStartDate() == null) {
				return 0;
			} else {
				return project1.getStartDate().compareTo(project2.getStartDate());
			}
		}
	};

	private final static Comparator<TargetPerformance> TARGET_DURATION_COMPERATOR = new Comparator<TargetPerformance>() {

		/** {@inheritDoc} */
		@Override
		public int compare(TargetPerformance target1, TargetPerformance target2) {

			if (target1 == null || target2 == null
					|| target1.getDuration() == null
					|| target2.getDuration() == null) {
				return 0;
			} else {
				return -1
						* target1.getDuration().compareTo(target2.getDuration());
			}
		}
	};

	/** {@inheritDoc} */
	@Override
	public void buildFinished(BuildEvent buildEvent) {
		super.buildFinished(buildEvent);

		getProject(buildEvent).stopTimer();

		ProjectPerformances projects = readProjectPerformances();
		projects.getProjects().add(rootProject);

		Collections.sort(projects.getProjects(), PROJECT_PERFORMANCE_DATE_COMPERATOR);

		if (historyExpire > 0 && projects.getProjects().size() > historyExpire) {

			List<ProjectPerformance> retain = new ArrayList<ProjectPerformance>(projects.getProjects().subList(0, projects.getProjects().size()
					- historyExpire));
			projects.getProjects().removeAll(retain);
		}

		writeProjectPerformances(projects);

		LinkedHashMap<Date, Map<String, Long>> data = createChartData(projects);
		JFreeChart chart = createChart(data);
		saveChartImage(chart);

		createStatisticsTable();
	}

	private JFreeChart createChart(Map<Date, Map<String, Long>> data) {

		SimpleDateFormat format = new SimpleDateFormat("dd.MM. HH:mm");

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (Map.Entry<Date, Map<String, Long>> column : data.entrySet()) {
			for (Map.Entry<String, Long> row : column.getValue().entrySet()) {
				dataset.addValue(row.getValue(), row.getKey(), format.format(column.getKey()));
			}
		}

		JFreeChart chart = ChartFactory.createStackedBarChart(CHART_TITLE, CHART_XAXIS_LABEL, CHART_YAXIS_LABEL, dataset, PlotOrientation.VERTICAL, true, true, false);
		chart.setBackgroundPaint(Color.white);

		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setForegroundAlpha(0.5f);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setLowerMargin(0.0);
		domainAxis.setUpperMargin(0.0);

		// change the auto tick unit selection to integer units only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		return chart;
	}

	private LinkedHashMap<Date, Map<String, Long>> createChartData(
			ProjectPerformances projects) {

		List<TargetPerformance> targets = getTargets(projects, true);
		List<String> targetNames = new ArrayList<String>();

		for (TargetPerformance target : targets) {
			if (!targetNames.contains(target.getTargetName())) {
				targetNames.add(target.getTargetName());
			}
		}

		LinkedHashMap<Date, Map<String, Long>> data = new LinkedHashMap<Date, Map<String, Long>>();
		for (ProjectPerformance project : projects.getProjects()) {

			List<TargetPerformance> columnTargets = getTargets(project, true);
			Map<String, Long> columns = new HashMap<String, Long>();

			for (TargetPerformance target : columnTargets) {
				columns.put(target.getTargetName(), getTime(target.getDuration()));
			}

			for (String targetName : targetNames) {
				if (!columns.containsKey(targetName)) {
					columns.put(targetName, 0l);
				}
			}

			data.put(project.getStartDate(), columns);
		}

		return data;
	}

	private void createStatisticsTable() {

		List<TargetPerformance> targets = getTargets(rootProject, false);
		Collections.sort(targets, TARGET_DURATION_COMPERATOR);

		String[] header = new String[] { TABLE_HEADER_TARGET,
				TABLE_HEADER_DURATION + " (s)", TABLE_HEADER_DURATION + " %" };
		AsciiTable asciiTable = new AsciiTable(rootProject.getProjectName(), header);

		long totalProjectDuration = rootProject.getDuration();
		long totalTargetDuration = getTotalTargetDuration(targets);

		for (TargetPerformance target : targets) {
			String[] row = new String[header.length];

			row[0] = target.getTargetName();
			row[1] = String.format("%d", getTime(target.getDuration()));
			double percentage = target.getDuration()
					/ (totalTargetDuration / 100);
			row[2] = String.format("%.2f%%", percentage);

			asciiTable.addRow(row);
		}

		asciiTable.setFooter(String.format("total time (s): %d", getTime(totalProjectDuration)));
		System.out.println(asciiTable.toString());
	}

	private ProjectPerformance getAndCreateProject(BuildEvent buildEvent) {

		String projectName = buildEvent.getProject().getName();

		if (rootProject == null) {
			initializeProperties(buildEvent);

			rootProject = new ProjectPerformance(projectName);
			rootProject.startTimer();
			currentProject = rootProject;
		} else {
			ProjectPerformance project = getProject(projectName);

			if (project == null) {

				project = new ProjectPerformance(projectName);
				project.startTimer();
				currentProject.getSubProjects().add(project);
			}

			currentProject = project;
		}

		return currentProject;
	}

	private TargetPerformance getAndCreateTarget(BuildEvent buildEvent) {

		ProjectPerformance project = getAndCreateProject(buildEvent);
		TargetPerformance target = getTarget(project, buildEvent);

		if (target == null) {

			String targetName = buildEvent.getTarget().getName();

			target = new TargetPerformance(targetName);
			project.getTargets().add(target);

		}

		return target;
	}

	private ProjectPerformance getProject(BuildEvent buildEvent) {
		String projectName = buildEvent.getProject().getName();
		return getProject(projectName);
	}

	private ProjectPerformance getProject(
			List<ProjectPerformance> projects, String projectName) {

		ProjectPerformance result = null;

		for (ProjectPerformance project : projects) {
			if (project.getProjectName().equals(projectName)) {
				return project;
			}

			result = getProject(project.getSubProjects(), projectName);
		}

		return result;
	}

	private ProjectPerformance getProject(String projectName) {

		if (rootProject.getProjectName().equals(projectName)) {
			return rootProject;
		} else {
			return getProject(rootProject.getSubProjects(), projectName);
		}
	}

	private String getProperty(
			BuildEvent buildEvent, String propertyName, String propertyDefault) {

		String propertyValue = buildEvent.getProject().getProperty(propertyName);

		if (propertyValue == null || propertyValue.isEmpty()) {
			return propertyDefault;
		} else {
			return propertyValue;
		}
	}

	private int getPropertyAsInteger(
			BuildEvent buildEvent, String propertyName, int propertyDefault) {

		String propertyValue = getProperty(buildEvent, propertyName, null);

		if (propertyValue == null || propertyValue.isEmpty()) {
			return propertyDefault;
		} else {
			try {
				return Integer.parseInt(propertyValue);
			} catch (NumberFormatException e) {
				log(String.format("'%s' is not a valid integer for property '%s'", propertyValue, propertyName));
			}

			return propertyDefault;
		}

	}

	private TargetPerformance getTarget(BuildEvent buildEvent) {

		String projectName = buildEvent.getProject().getName();
		ProjectPerformance project = getProject(projectName);

		return getTarget(project, buildEvent);
	}

	private TargetPerformance getTarget(
			ProjectPerformance project, BuildEvent buildEvent) {

		String targetName = buildEvent.getTarget().getName();

		for (TargetPerformance target : project.getTargets()) {
			if (target.getTargetName().equals(targetName)) {
				return target;
			}
		}

		return null;
	}

	private List<TargetPerformance> getTargets(
			ProjectPerformance project, boolean applyFilter) {

		List<TargetPerformance> targets = new ArrayList<TargetPerformance>();
		getTargets(project, targets, applyFilter);

		return targets;
	}

	private void getTargets(
			ProjectPerformance project, List<TargetPerformance> targets,
			boolean applyFilter) {

		for (TargetPerformance target : project.getTargets()) {
			if (!applyFilter || target.getDuration() > targetThreshold) {
				targets.add(target);
			}
		}

		for (ProjectPerformance subProject : project.getSubProjects()) {
			getTargets(subProject, targets, applyFilter);
		}
	}

	private List<TargetPerformance> getTargets(
			ProjectPerformances projects, boolean applyFilter) {

		List<TargetPerformance> targets = new ArrayList<TargetPerformance>();
		for (ProjectPerformance project : projects.getProjects()) {
			getTargets(project, targets, applyFilter);
		}

		return targets;
	}

	private long getTime(long time) {
		return time / 1000;
	}

	private long getTotalTargetDuration(List<TargetPerformance> targets) {

		long totalTargetDuration = 0;

		for (TargetPerformance target : targets) {
			totalTargetDuration += target.getDuration();
		}

		return totalTargetDuration;
	}

	private XStream getXStream() {

		XStream xstream = new XStream(new DomDriver());
		xstream.alias("projectperformances", ProjectPerformances.class);
		xstream.alias("projectperformance", ProjectPerformance.class);
		xstream.alias("targetperformance", TargetPerformance.class);

		return xstream;
	}

	private void initializeProperties(BuildEvent buildEvent) {
		dataDirectory = getProperty(buildEvent, DATA_DIRECTORY_PROPERTY_NAME, DATA_DIRECTORY_DEFAULT);
		chartImageHeight = getPropertyAsInteger(buildEvent, CHART_IMAGE_HEIGHT_PROPERTY_NAME, CHART_IMAGE_HEIGHT_DEFAULT);
		chartImageWidth = getPropertyAsInteger(buildEvent, IMAGE_WIDTH_PROPERTY_NAME, CHART_IMAGE_WIDTH_DEFAULT);
		historyExpire = getPropertyAsInteger(buildEvent, HISTORY_EXPIRE_PROPERTY_NAME, HISTORY_EXPIRE_DEFAULT);
		targetThreshold = getPropertyAsInteger(buildEvent, TARGET_THRESHOLD_PROPERTY_NAME, TARGET_THRESHOLD_DEFAULT);
	}

	private ProjectPerformances readProjectPerformances() {

		File file = new File(dataDirectory, String.format("%s.xml", rootProject.getProjectName()));

		if (file.exists()) {
			try {
				XStream xstream = getXStream();
				FileReader reader = new FileReader(file);
				ProjectPerformances projects = (ProjectPerformances) xstream.fromXML(reader);

				return projects;
			} catch (Exception e) {
				log(String.format("could not read performance history (%s)", e.getMessage()));
				return new ProjectPerformances();
			}
		} else {
			return new ProjectPerformances();
		}
	}

	private void saveChartImage(JFreeChart chart) {
		File imageFile = new File(dataDirectory, String.format("%s.png", rootProject.getProjectName()));

		try {
			ChartUtilities.saveChartAsPNG(imageFile, chart, chartImageWidth, chartImageHeight);
		} catch (Exception e) {
			log(String.format("could not save image '%s' (%s)", imageFile.toURI().toString(), e.getMessage()));
		}
	}

	/** {@inheritDoc} */
	@Override
	public void targetFinished(BuildEvent buildEvent) {
		getTarget(buildEvent).stopTimer();
	}

	/** {@inheritDoc} */
	@Override
	public void targetStarted(BuildEvent buildEvent) {
		getAndCreateTarget(buildEvent).startTimer();
	}

	private void writeProjectPerformances(ProjectPerformances projects) {

		File directory = new File(dataDirectory);
		File file = new File(dataDirectory, String.format("%s.xml", rootProject.getProjectName()));

		try {

			if (!directory.exists()) {
				directory.mkdir();
			}

			XStream xstream = getXStream();
			FileWriter writer = new FileWriter(file);
			xstream.toXML(projects, writer);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
