import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;


public class NSLIntegrator implements FirstOrderDifferentialEquations {
    private double S;
    private double ksi;
    private double omegaz;
    private Sun sun;
    private double cksi;
    private double sksi;
    private double S2;

    public NSLIntegrator(double S, double ksi, double omegaz) {
        this.S = S;
        this.S2 = this.S*this.S;
        this.ksi = ksi;
        this.cksi = Math.cos(this.ksi);
        this.sksi = Math.sin(this.ksi);
        this.omegaz = omegaz;
        this.sun = new Sun();
    }

    public int getDimension() {
        return 2;
    }

    public void computeDerivatives(double t, double[] y, double[] yDot) {
        // Y is (nu, omega)
        double sal_dot = sun.apparentLongitude(t)[2];

        yDot[0] = sal_dot * (Math.sqrt(S2 - Math.cos(y[0]) * Math.cos(y[0])) + cksi * Math.sin(y[0])) / sksi;
        yDot[1] = omegaz - yDot[0] * cksi - sal_dot * sksi * Math.sin(y[0]);
    }
}


