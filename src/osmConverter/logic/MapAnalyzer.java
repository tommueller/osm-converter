package osmConverter.logic;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import osmConverter.data.Link;
import osmConverter.data.StreetMap;
import osmConverter.io.IsLogging;

public class MapAnalyzer implements IsLogging {

    private static JFreeChart createPieChart(PieDataset dataset) {

	JFreeChart chart = ChartFactory.createPieChart(
		"Verteilung der Straﬂentypen", // chart title
		dataset, // data
		true, // include legend
		true, false);

	PiePlot plot = (PiePlot) chart.getPlot();
	plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
	plot.setNoDataMessage("No data available");
	plot.setCircular(false);
	plot.setLabelGap(0.02);
	return chart;

    }

    Logger logger;

    private List<String> sortOut = new ArrayList<String>();

    public MapAnalyzer(String[] logs) {
	logger = Logger.getLogger(logs[0]);

	// this are irrelevant streetcategories

	sortOut.add("crossing");
	sortOut.add("road");
	sortOut.add("elevator");
	sortOut.add("virtual_connection");
	sortOut.add("bridleway");
	sortOut.add("platform");
	sortOut.add("steps");
	sortOut.add("escalator");
	sortOut.add("cycleway");
	sortOut.add("escalator");
	sortOut.add("turning_circle");
	sortOut.add("track");
	sortOut.add("unsurfaced");
	sortOut.add("pedestrian");
	sortOut.add("path");
	sortOut.add("mini_roundabout");
	sortOut.add("footway");
	sortOut.add("gate");
	sortOut.add("historic");
	sortOut.add("cycleroad");
	sortOut.add("residential;tertiary");
	sortOut.add("service; footway");
    }

    /**
     * Analyze the longest and shortest link of a map as well as its average
     * length.
     * 
     * @param streetMap
     *            The streetmap which's links shall be analyzed.
     */
    public void analyzeLinkLength(StreetMap streetMap) {
	double maxLength = 0;
	double minLength = 1000;
	double averageLength = 0;

	for (Link link : streetMap.getLinks().values()) {
	    double length = link.getLength();

	    if (maxLength < length)
		maxLength = length;
	    if (minLength > length)
		minLength = length;

	    averageLength += length;
	}

	averageLength /= streetMap.getLinks().size();

	logInfo("max. link-length: " + maxLength);
	logInfo("min. link-length: " + minLength);
	logInfo("avr. link-length: " + averageLength);
    }

    /**
     * Analyzes the existence of name-tags by highway-types.
     * 
     * @param namedHighways
     *            Map containing all named highway.
     * @param unnamedHighways
     *            Map containing all unnamed highways.
     * @param file
     *            Word to add to the output-files-name.
     */
    public void analyzeNameTag(
	    HashMap<String, HashMap<String, Integer>> namedHighways,
	    Map<String, Integer> unnamedHighways, String file) {

	try {

	    FileWriter fw = new FileWriter("output/stats/names (" + file
		    + ").csv");
	    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	    HashMap<String, Double> all = new HashMap<String, Double>();
	    HashMap<String, Double> allRelevant = new HashMap<String, Double>();
	    double allSum = 0;
	    double allRelevantSum = 0;

	    for (Entry<String, HashMap<String, Integer>> entry : namedHighways
		    .entrySet()) {

		double sum = 0;

		for (Entry<String, Integer> mymy : entry.getValue().entrySet()) {
		    sum += mymy.getValue();

		    if (!sortOut.contains(entry.getKey())
			    && !entry.getKey().equals("service")) {
			if (allRelevant.containsKey(mymy.getKey())) {
			    double temp = allRelevant.get(mymy.getKey());
			    temp += mymy.getValue();
			    allRelevant.put(mymy.getKey(), temp);
			} else {
			    allRelevant.put(mymy.getKey(),
				    (double) mymy.getValue());
			}
		    }

		    if (all.containsKey(mymy.getKey())) {
			double temp = all.get(mymy.getKey());
			temp += mymy.getValue();
			all.put(mymy.getKey(), temp);
		    } else {
			all.put(mymy.getKey(), (double) mymy.getValue());
		    }

		    if (!sortOut.contains(entry.getKey())
			    && !entry.getKey().equals("service")) {
			allRelevantSum += mymy.getValue();
		    }

		    allSum += mymy.getValue();
		}

		if (unnamedHighways.containsKey(entry.getKey())) {
		    sum += unnamedHighways.get(entry.getKey());
		    allSum += unnamedHighways.get(entry.getKey());
		    if (!sortOut.contains(entry.getKey())
			    && !entry.getKey().equals("service")) {
			allRelevantSum += unnamedHighways.get(entry.getKey());
		    }
		}

		for (Entry<String, Integer> mymy : entry.getValue().entrySet()) {

		    if (sortOut.contains(entry.getKey()))
			continue;

		    double val = (double) mymy.getValue() / sum;

		    dataset.addValue(val, mymy.getKey(), entry.getKey());
		    fw.write(entry.getKey() + ";" + mymy.getKey() + ";"
			    + String.format("%11.8f", val) + "\r\n");
		}
	    }

	    for (Entry<String, Double> entry : all.entrySet()) {
		dataset.addValue(entry.getValue() / allSum, entry.getKey(),
			"all");
		fw.write("all;" + entry.getKey() + ";"
			+ String.format("%11.8f", (entry.getValue() / allSum))
			+ "\r\n");
	    }

	    for (Entry<String, Double> entry : allRelevant.entrySet()) {
		if (entry.getKey().equals(Scorer.DEF))
		    continue;
		dataset.addValue(entry.getValue() / allRelevantSum,
			entry.getKey(), "allRelevant");
		fw.write("allRelevant;"
			+ entry.getKey()
			+ ";"
			+ String.format("%11.8f",
				(entry.getValue() / allRelevantSum)) + "\r\n");
	    }

	    Set<String> streets = namedHighways.keySet();
	    List<String> cats = new ArrayList<String>();

	    for (HashMap<String, Integer> map : namedHighways.values()) {
		for (String str : map.keySet()) {
		    cats.add(str);
		}
	    }

	    fw.flush();
	    fw.close();

	    JFreeChart chart = createStackedChart(dataset, cats, streets,
		    "Setzrate des Namenstags nach Straﬂentypen (" + file + ")",
		    "Typ des Namens-Tags",
		    "Anteil der Straﬂen mit gesetzem Tag", true);
	    BufferedImage bi = chart.createBufferedImage(1440, 900);
	    File f = new File("output/charts/streetTypeNameSet(" + file
		    + ").png");

	    try {
		ImageIO.write(bi, "png", f);
	    } catch (IOException e) {
		logError(e.getMessage());
	    }
	} catch (Exception e) {

	}
    }

    /**
     * Analyzes the existence of maxspeed-tags by highway-types.
     * 
     * @param highwaySpeeds
     *            Map containing all highways and there speed-set-rate.
     * @param file
     *            Word to add to the output-files-name.
     */
    public void analyzeSpeedTag(
	    HashMap<String, HashMap<String, Double>> highwaySpeeds, String file) {

	try {
	    FileWriter fw = new FileWriter("output/stats/speed (" + file
		    + ").csv");

	    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	    HashMap<String, Double> all = new HashMap<String, Double>();
	    HashMap<String, Double> allRelevant = new HashMap<String, Double>();
	    double allSum = 0;
	    double allRelevantSum = 0;

	    for (Entry<String, HashMap<String, Double>> entry : highwaySpeeds
		    .entrySet()) {

		double sum = 0;

		for (Entry<String, Double> mymy : entry.getValue().entrySet()) {

		    sum += mymy.getValue();

		    if (!sortOut.contains(entry.getKey())
			    && !entry.getKey().equals("service")) {
			if (allRelevant.containsKey(mymy.getKey())) {
			    double temp = allRelevant.get(mymy.getKey());
			    temp += mymy.getValue();
			    allRelevant.put(mymy.getKey(), temp);
			} else {
			    allRelevant.put(mymy.getKey(), mymy.getValue());
			}
		    }

		    if (all.containsKey(mymy.getKey())) {
			double temp = all.get(mymy.getKey());
			temp += mymy.getValue();
			all.put(mymy.getKey(), temp);
		    } else {
			all.put(mymy.getKey(), mymy.getValue());
		    }

		    if (!sortOut.contains(entry.getKey())
			    && !entry.getKey().equals("service")) {
			allRelevantSum += mymy.getValue();
		    }

		    allSum += mymy.getValue();
		}

		for (Entry<String, Double> mymy : entry.getValue().entrySet()) {

		    if (mymy.getKey().equals(Scorer.DEF)
			    || sortOut.contains(entry.getKey()))
			continue;

		    double val = (double) mymy.getValue() / sum;

		    dataset.addValue(val, mymy.getKey(), entry.getKey());
		    fw.write(entry.getKey() + ";" + mymy.getKey() + ";"
			    + String.format("%11.8f", val) + "\r\n");
		}
	    }

	    for (Entry<String, Double> entry : all.entrySet()) {
		if (entry.getKey().equals(Scorer.DEF))
		    continue;
		dataset.addValue(entry.getValue() / allSum, entry.getKey(),
			"all");
		fw.write("all;" + entry.getKey() + ";"
			+ String.format("%11.8f", (entry.getValue() / allSum))
			+ "\r\n");
	    }

	    for (Entry<String, Double> entry : allRelevant.entrySet()) {
		if (entry.getKey().equals(Scorer.DEF))
		    continue;
		dataset.addValue(entry.getValue() / allRelevantSum,
			entry.getKey(), "allRelevant");
		fw.write("allRelevant;"
			+ entry.getKey()
			+ ";"
			+ String.format("%11.8f",
				(entry.getValue() / allRelevantSum)) + "\r\n");
	    }

	    Set<String> streets = highwaySpeeds.keySet();
	    List<String> cats = new ArrayList<String>();

	    for (HashMap<String, Double> map : highwaySpeeds.values()) {
		for (String str : map.keySet()) {
		    cats.add(str);
		}
	    }

	    fw.flush();
	    fw.close();

	    JFreeChart chart = createStackedChart(dataset, cats, streets,
		    "Setzrate des maxspeed-Tags nach Straﬂentyp", "Straﬂentyp",
		    "Setzrate", true);
	    BufferedImage bi = chart.createBufferedImage(1440, 900);
	    File f = new File("output/charts/speedSet (" + file + ").png");

	    try {
		ImageIO.write(bi, "png", f);
	    } catch (IOException e) {
		logError(e.getMessage());
	    }
	} catch (Exception e) {

	}
    }

    // private JFreeChart createChart(final CategoryDataset dataset, String
    // title,
    // String xAxis, String yAxis) {

    // create the chart...
    // final JFreeChart chart = ChartFactory.createBarChart(title, // chart
    // // title
    // xAxis, // domain axis label
    // yAxis, // range axis label
    // dataset, // data
    // PlotOrientation.VERTICAL, // orientation
    // false, // include legend
    // false, // tooltips?
    // false // URLs?
    // );

    // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
    //
    // // set the background color for the chart...
    // chart.setBackgroundPaint(Color.white);
    //
    // // get a reference to the plot for further customisation...
    // final CategoryPlot plot = chart.getCategoryPlot();
    // plot.setBackgroundPaint(Color.lightGray);
    // plot.setDomainGridlinePaint(Color.white);
    // plot.setRangeGridlinePaint(Color.white);
    //
    // // set the range axis to display integers only...
    // final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    // rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    //
    // // disable bar outlines...
    // final BarRenderer renderer = (BarRenderer) plot.getRenderer();
    // renderer.setDrawBarOutline(false);
    //
    // // set up gradient paints for series...
    // final GradientPaint gp0 = new GradientPaint(0.0f, 0.0f, Color.blue,
    // 0.0f, 0.0f, Color.lightGray);
    // final GradientPaint gp1 = new GradientPaint(0.0f, 0.0f, Color.green,
    // 0.0f, 0.0f, Color.lightGray);
    // final GradientPaint gp2 = new GradientPaint(0.0f, 0.0f, Color.red,
    // 0.0f, 0.0f, Color.lightGray);
    // renderer.setSeriesPaint(0, gp0);
    // renderer.setSeriesPaint(1, gp1);
    // renderer.setSeriesPaint(2, gp2);
    //
    // final CategoryAxis domainAxis = plot.getDomainAxis();
    // domainAxis.setCategoryLabelPositions(CategoryLabelPositions
    // .createUpRotationLabelPositions(Math.PI / 6.0));
    // // OPTIONAL CUSTOMISATION COMPLETED.
    //
    // return chart;
    //
    // }

    
    /**
     * Analyzes the quantity of street-categories.
     * Creates a pie-chart visualizing the results.
     */
    public void analyzeCategorySizes(StreetMap streetMap, String file) {

	double streetsWithHighwayTag = 0;
	double type0 = 0;
	double type1 = 0;
	double type2 = 0;
	double type3 = 0;
	double type4 = 0;
	double type5 = 0;
	double typeRest = 0;

	for (Link link : streetMap.getLinks().values()) {
	    streetsWithHighwayTag++;
	    if (link.getStreetType() == 0) {
		type0++;
	    } else if (link.getStreetType() == 1) {
		type1++;
	    } else if (link.getStreetType() == 2) {
		type2++;
	    } else if (link.getStreetType() == 3) {
		type3++;
	    } else if (link.getStreetType() == 4) {
		type4++;
	    } else if (link.getStreetType() == 5) {
		type5++;
	    } else {
		typeRest++;
	    }
	}

	// logInfo("highwaycount: highways=" + streetsWithHighwayTag + "\tcat0="
	// + type0 + "\tcat1=" + type1 + "\tcat2=" + type2 + "\t cat3="
	// + type3 + "\tcat4=" + type4 + "\tcat5=" + type5 + "\tother="
	// + typeRest);

	DefaultPieDataset dataset = new DefaultPieDataset();

	dataset.setValue(
		"Kategorie 0 - "
			+ Math.round(((type0 / streetsWithHighwayTag) * 100) * 100)
			/ 100. + "% (" + (int) type0 + ")", type0);
	dataset.setValue(
		"Kategorie 1 - "
			+ Math.round(((type1 / streetsWithHighwayTag) * 100) * 100)
			/ 100. + "% (" + (int) type1 + ")", type1);
	dataset.setValue(
		"Kategorie 2 - "
			+ Math.round(((type2 / streetsWithHighwayTag) * 100) * 100)
			/ 100. + "% (" + (int) type2 + ")", type2);
	dataset.setValue(
		"Kategorie 3 - "
			+ Math.round(((type3 / streetsWithHighwayTag) * 100) * 100)
			/ 100. + "% (" + (int) type3 + ")", type3);
	dataset.setValue(
		"Kategorie 4 - "
			+ Math.round(((type4 / streetsWithHighwayTag) * 100) * 100)
			/ 100. + "% (" + (int) type4 + ")", type4);
	dataset.setValue(
		"Kategorie 5 - "
			+ Math.round(((type5 / streetsWithHighwayTag) * 100) * 100)
			/ 100. + "% (" + (int) type5 + ")", type5);
	dataset.setValue(
		"Andere - "
			+ Math.round(((typeRest / streetsWithHighwayTag) * 100) * 100)
			/ 100. + "% (" + (int) typeRest + ")", typeRest);

	JFreeChart chart = createPieChart(dataset);
	BufferedImage bi = chart.createBufferedImage(1024, 768);
	File f = new File("output/charts/highwayCount (" + file + ").png");

	try {
	    ImageIO.write(bi, "png", f);
	} catch (IOException e) {
	    logError(e.getMessage());
	    e.printStackTrace();
	}
    }

    private JFreeChart createStackedChart(final CategoryDataset dataset,
	    List<String> cats, Set<String> streetTypes, String title,
	    String xAchse, String yAchse, boolean legend) {

	final JFreeChart chart = ChartFactory.createStackedBarChart(title, // chart
									   // title
		xAchse, // domain axis label
		yAchse, // range axis label
		dataset, // data
		PlotOrientation.VERTICAL, // the plot orientation
		legend, // legend
		true, // tooltips
		false // urls
		);

	final CategoryPlot plot = chart.getCategoryPlot();

	final CategoryAxis domainAxis = plot.getDomainAxis();
	domainAxis.setCategoryLabelPositions(CategoryLabelPositions
		.createUpRotationLabelPositions(Math.PI / 6.0));

	return chart;

    }

    @Override
    public void logError(String message) {
	logger.log(Level.SEVERE, message);
    }

    @Override
    public void logInfo(String message) {
	logger.log(Level.INFO, message);
    }

    @Override
    public void logWarning(String message) {
	logger.log(Level.WARNING, message);
    }

    /**
     * Analyzes exisitence of lane-tags by highway-type.
     * @param lanes map containing all highways with lane-tag.
     * @param noLanes map containing all highways without lane-tag.
     * @param file Word to add to the output-files-name.
     */
    public void analyzeLaneTag(HashMap<String, Double> lanes,
	    HashMap<String, Double> noLanes, String file) {
	try {

	    FileWriter fw = new FileWriter("output/stats/lanes (" + file
		    + ").csv");
	    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	    double all = 0;
	    double allSum = 0;
	    double allRelevant = 0;
	    double allRelevantSum = 0;

	    for (Entry<String, Double> entry : lanes.entrySet()) {

		double val = 1;

		if (noLanes.containsKey(entry.getKey())) {
		    val = entry.getValue()
			    / (entry.getValue() + noLanes.get(entry.getKey()));

		    if (!sortOut.contains(entry.getKey())
			    && !entry.getKey().equals("service")) {
			allRelevantSum += noLanes.get(entry.getKey());
		    }

		    allSum += noLanes.get(entry.getKey());
		}

		all += entry.getValue();
		allSum += entry.getValue();

		if (!sortOut.contains(entry.getKey())
			&& !entry.getKey().equals("service")) {
		    allRelevantSum += entry.getValue();
		    allRelevant += entry.getValue();
		}

		if (sortOut.contains(entry.getKey()))
		    continue;

		dataset.addValue(val, entry.getKey(), entry.getKey());
		fw.write(entry.getKey() + ";" + String.format("%11.8f", val)
			+ "\r\n");
	    }

	    dataset.addValue(all / allSum, "all", "all");
	    fw.write("all;" + String.format("%11.8f", (all / allSum)) + "\r\n");

	    dataset.addValue(allRelevant / allRelevantSum, "allRelevant",
		    "allRelevant");
	    fw.write("allRelevant;"
		    + String.format("%11.8f", (allRelevant / allRelevantSum))
		    + "\r\n");

	    fw.flush();
	    fw.close();

	    JFreeChart chart = createStackedChart(dataset, null, null,
		    "Setzrate des Lane-Tags nach Straﬂentypen (" + file + ")",
		    "Straﬂentyp", "Anteil der Straﬂen mit gesetzem Tag", false);
	    BufferedImage bi = chart.createBufferedImage(1440, 900);
	    File f = new File("output/charts/lanes(" + file + ").png");

	    try {
		ImageIO.write(bi, "png", f);
	    } catch (IOException e) {
		logError(e.getMessage());
	    }
	} catch (Exception e) {

	}
    }
}
