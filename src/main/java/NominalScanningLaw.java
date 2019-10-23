import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math3.ode.sampling.StepNormalizer;


public class NominalScanningLaw {
    private double precessionSpeedConstant;
    private double solarAspectAngle;
    private double inertialSpinRate;
    private double stepSize;
    private double startTime;
    private double endTime;
    private int nSteps;
    private NSLStepHandler stepHandler;

    public NominalScanningLaw() {
        precessionSpeedConstant = 4.223;
        solarAspectAngle = Math.toRadians(45.0);
        inertialSpinRate = Math.toRadians(60.0/3600.0) * 86400; // Rad per day
        startTime = 14*365.25; // 1 january 2014 is 14*365.25 days after J2000.0
        endTime = startTime + 5*365.25;
        stepSize = 1.0/(24*60); // 1 minute; used to be 0.0005;
        nSteps = (int)(endTime/stepSize) + 1;
        stepHandler = new NSLStepHandler(nSteps, solarAspectAngle, "./movie/", false);
    }

    public void runIntegration() {
        // Initial conditions for nu and omega
        double[] y = new double[] {Math.PI/2.0, Math.PI/2.0};

        stepHandler.reset();
        AdaptiveStepsizeIntegrator integrator = new DormandPrince853Integrator(1.0e-8, 1, 1.0e-10, 1.0e-10);
        FirstOrderDifferentialEquations ode = new NSLIntegrator(precessionSpeedConstant, solarAspectAngle, inertialSpinRate);
        integrator.addStepHandler(new StepNormalizer(stepSize, stepHandler));
        integrator.integrate(ode, startTime, y, endTime, y);
    }


    public static void main(String[] args) {
        NominalScanningLaw nsl = new NominalScanningLaw();
        nsl.runIntegration();
    }
}
