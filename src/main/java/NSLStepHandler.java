import org.apache.commons.math3.ode.sampling.FixedStepHandler;
import org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinates;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;


class NSLStepHandler implements FixedStepHandler {
    boolean continuous;
    AttitudeCalculator attitudeCalculator;
    private Sun sun;
    private double[] ts;
    private double[] solarLongitudes;
    private double[] nus;
    private double[] omegas;
    private SphericalCoordinates sunDirection[];
    private SphericalCoordinates precessionAxis[];
    private SphericalCoordinates scanDirection[];
    private int current;
    private int nSteps;
    private int framenumber;
    private HealPixDensityMapper h;



    public NSLStepHandler(int nSteps, double solarAspectAngle, String outputFolder, boolean continuous) {
        this.continuous = continuous;
        this.nSteps = nSteps;
        attitudeCalculator = new AttitudeCalculator(solarAspectAngle);
        sun = new Sun();
        Projection hp = new HammerProjection(0, true);
        try {
            h = new HealPixDensityMapper(1920, 1080, 1600, 800, hp, 512, outputFolder);
        }
        catch(Exception e) {
            System.out.println("Error: " + e);
        }
        h.setEpsilon(attitudeCalculator.getEpsilon());
        reset();
    }

    public void init(double t0, double[] y0, double t) {
    }

    public void handleStep(double t, double[] y, double[] yDot, boolean isLast) {
        ts[current] = t;
        double relativeT = t - ts[0];
        nus[current] = y[0];
        omegas[current] = y[1];
        solarLongitudes[current] = sun.apparentLongitude(t)[1];

        SphericalCoordinates[] scs = attitudeCalculator.calculateDirections(solarLongitudes[current], nus[current], omegas[current]);
        sunDirection[current] = scs[0];
        precessionAxis[current] = scs[1];
        scanDirection[current] = scs[2];
        h.nextStep(relativeT, sunDirection[current], precessionAxis[current]);

        Vector3D[][] fovs = attitudeCalculator.calculateFoVs();
        try {
            h.addRectangularArea(fovs[0]);
            h.addRectangularArea(fovs[1]);
        }
        catch(Exception e) {
            System.out.println("Error: " + e);
        }

        boolean draw = false;
        // First two days: every 10 minutes
        if(relativeT <= 2 && current % 10 == 0) {
            draw = true;
        }
        // Up to day 183: every day
        if(relativeT > 2 && relativeT <= 183 && current % (24*60) == 0) {
            draw = true;
        }
        // After day 183: every 11 days
        if(relativeT > 183 && current % (11*24*60) == 0) {
            draw = true;
        }

        // For continuous mode: every day
        if((continuous && (current % (24*60) == 0)) || isLast) {
            draw = true;
        }

        if(draw) {
            System.out.println(String.format("T = %.0f days", relativeT));
            drawMap(framenumber);
            framenumber += 1;
        }
        current += 1;
    }

    public void reset() {
        ts = new double[nSteps];
        solarLongitudes = new double[nSteps];
        nus = new double[nSteps];
        omegas = new double[nSteps];
        sunDirection = new SphericalCoordinates[nSteps];
        precessionAxis = new SphericalCoordinates[nSteps];
        scanDirection = new SphericalCoordinates[nSteps];
        current = 0;
        framenumber = 0;
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
