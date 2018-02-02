public interface Projection {
    public double[] projectLongLat(double longitude, double latitude);
    public double[] projectThetaPhi(double theta, double phi);
    public double[] inverseProjectLongLat(double x, double y);
    public double[] inverseProjectThetaPhi(double x, double y);
    public double[] getXRange();
    public double[] getYRange();
}
