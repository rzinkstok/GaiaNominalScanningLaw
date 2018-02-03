import org.apache.commons.math3.ode.sampling.FixedStepHandler;
import org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinates;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;


class NSLStepHandler implements FixedStepHandler {
    AttitudeCalculator attitudeCalculator;
    private Sun sun;
    private double[] ts;
    private double[] solarLongitudes;
    private double[] nus;
    private double[] omegas;
    private SphericalCoordinates precessionAxis[];
    private SphericalCoordinates scanDirection[];
    private int current;
    private int nSteps;
    private HealPixDensityMapper h;



    public NSLStepHandler(int nSteps, double solarAspectAngle, String outputFolder) {
        this.nSteps = nSteps;
        attitudeCalculator = new AttitudeCalculator(solarAspectAngle);
        sun = new Sun();
        Projection hp = new HammerProjection(0, true);
        try {
            h = new HealPixDensityMapper(4000, 2000, hp, 512, outputFolder);
        }
        catch(Exception e) {
            System.out.println("Error: " + e);
        }
        reset();
    }

    public void init(double t0, double[] y0, double t) {
    }

    public void handleStep(double t, double[] y, double[] yDot, boolean isLast) {
        h.nextStep();
        ts[current] = t;
        nus[current] = y[0];
        omegas[current] = y[1];
        solarLongitudes[current] = sun.apparentLongitude(t)[1];

        SphericalCoordinates[] scs = attitudeCalculator.calculateDirections(solarLongitudes[current], nus[current], omegas[current]);
        precessionAxis[current] = scs[0];
        scanDirection[current] = scs[1];

        Vector3D[][] fovs = attitudeCalculator.calculateFoVs();
        try {
            h.addRectangularArea(fovs[0]);
            h.addRectangularArea(fovs[1]);
        }
        catch(Exception e) {
            System.out.println("Error: " + e);
        }

        if(current % 1000 == 0) {
            drawMap(current/1000);
        }

        if(current % 10000 == 0) {
            //System.out.println(current + ": " + String.format("%.4f", t) + "\t(" + scs[0].getTheta() + "," +scs[0].getPhi() + ")\t("+ scs[1].getTheta() + "," + scs[1].getPhi() + ")"); //y[0] + "\t" + y[1]);
            System.out.println(current + ": " + String.format("%.4f", t) + "\tnu: " + nus[current] + "\tOmega: " + omegas[current] + "\tsol: " + Math.toDegrees(solarLongitudes[current]) + "\tPA: " + Math.toDegrees(scs[0].getTheta()) + "," + Math.toDegrees(scs[0].getPhi()));
        }
        current += 1;
    }

    public void reset() {
        ts = new double[nSteps];
        solarLongitudes = new double[nSteps];
        nus = new double[nSteps];
        omegas = new double[nSteps];
        precessionAxis = new SphericalCoordinates[nSteps];
        scanDirection = new SphericalCoordinates[nSteps];
        current = 0;
    }

    public void drawMap(int n) {
        try {
            h.drawMap(n);
        }
        catch(Exception e) {
            System.out.println("Error: " + e);
        }
    }
}
