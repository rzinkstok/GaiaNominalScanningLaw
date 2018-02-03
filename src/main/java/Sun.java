public class Sun{
    // Class for calculating approximate solar ephemeris
    // Based on the Fortran code in Lindegren's memo GAIA-LL-35-20010219
    private double e;
    private double ee;
    private double a0;
    private double a1;
    private double g0;
    private double g1;

    public Sun() {
        // Initialize constants
        e = 0.016709;
        ee = e * e;
        a0 = Math.toRadians(280.458);
        a1 = Math.toRadians(0.98560911);
        g0 = Math.toRadians(357.528);
        g1 = Math.toRadians(0.98560028);
    }

    public double[] apparentLongitude(double t) {
        // Time is time in days since J2000.0
        // Returns the solar distance in AU, the solar longitude in rad and the derivative in rad/day
        double a = a0 + a1 * t;
        double g = g0 + g1 * t;
        double solar_longitude = (a + 2 * e * Math.sin(g) + 1.25 * ee * Math.sin(2 * g)) % (2 * Math.PI);
        double derivative_solar_longitude = a1 + (2 * e * Math.cos(g) + 2 * 1.25 * ee * Math.cos(2 * g)) * g1;
        double solar_distance = 1 - e * Math.cos(g + e * Math.sin(g) + 0.5 * ee * Math.sin(2 * g));

        return new double[] {solar_distance, solar_longitude, derivative_solar_longitude};
    }
}
