import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.FontMetrics;

import java.util.ArrayList;
import javax.imageio.ImageIO;

import healpix.essentials.Scheme;
import healpix.essentials.RangeSet;
import org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinates;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;


public class HealPixDensityMapper {
    private int imageWidth;
    private int imageHeight;
    private int mapWidth;
    private int mapHeight;
    private int mapXOffset;
    private int mapYOffset;
    private double deltax;
    private double deltay;

    private Projection projection;
    private HealPixWrapper healpix;
    private int[] counts;
    private String outputFolder;
    private ArrayList<Long> previousStepPixels;
    private ArrayList<Long> currentPixels;
    private int maxValue;

    private double epsilon;
    private double time;
    private SphericalCoordinates sunPosition;
    private SphericalCoordinates precessionPosition;

    public HealPixDensityMapper(int imageWidth, int imageHeight, int mapWidth, int mapHeight, Projection projection, int nside, String outputFolder) throws Exception {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.mapXOffset = 150;
        this.mapYOffset = (imageHeight - mapHeight)/2;
        this.projection = projection;
        this.healpix = new HealPixWrapper(nside, Scheme.RING);

        this.outputFolder = outputFolder;
        if (this.outputFolder.startsWith("~" + File.separator)) {
            this.outputFolder = System.getProperty("user.home") + this.outputFolder.substring(1);
        }
        if(!this.outputFolder.endsWith("/")) {
            this.outputFolder += File.separator;
        }

        double[] xRange = projection.getXRange();
        double[] yRange = projection.getYRange();

        deltax = (xRange[1] - xRange[0]) / mapWidth;
        deltay = (yRange[1] - yRange[0]) / mapHeight;

        counts = new int[(int)healpix.getNpix()];
        for(int i=0; i<healpix.getNpix(); i++) {
            counts[i] = 0;
        }
        previousStepPixels = new ArrayList<>();
        currentPixels = new ArrayList<>();
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    public void nextStep(double time, SphericalCoordinates sunPosition, SphericalCoordinates precessionPosition) {
        this.time = time;
        this.sunPosition = sunPosition;
        this.precessionPosition = precessionPosition;
        previousStepPixels = currentPixels;
        currentPixels = new ArrayList<>();
    }

    public void addRectangularArea(Vector3D[] rect) {
        for(long p: getPixelsForRectangle(rect).toArray()) {
            if(!previousStepPixels.contains(p)) {
                counts[(int)p] += 1;
            }
            currentPixels.add(p);
        }
    }

    private int[] worldToMap(double[] worldPoint) {
        return new int[] {
                (int)((worldPoint[0] - projection.getXRange()[0])/deltax) + mapXOffset,
                (int)(imageHeight - ((worldPoint[1] - projection.getYRange()[0])/deltay) - mapYOffset)
        };
    }

    private double[] mapToWorld(int[] mapPoint) {
        return new double[] {
                projection.getXRange()[0] + (mapPoint[0] - mapXOffset) * deltax,
                projection.getYRange()[0] + (imageHeight - (mapPoint[1] + mapYOffset)) * deltay
        };
    }

    public void drawMap(int n) throws Exception {
        BufferedImage img = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();

        double theta, phi, ip[], p[];
        long ipix;
        int c, col, min = counts[0], sum=0;
        int white = (255<<16) | (255<<8) | 255;

        maxValue=0;
        for(int i = 0; i<counts.length; i++) {
            sum += counts[i];
            if(counts[i] > maxValue) {
                maxValue = counts[i];
            }
            if(counts[i] < min) {
                min = counts[i];
            }
        }

        System.out.println("Min value: " + min);
        System.out.println("Max value: " + maxValue);
        System.out.println("Mean value: " + sum/((double)healpix.getNpix()));
        ColorMap cm = ColorMap.getJet(256);

        for(int i = 0; i < imageWidth; i++) {
            for(int j = 0; j < imageHeight; j++) {
                p = mapToWorld(new int[] {i, j});

                ip = this.projection.inverseProjectThetaPhi(p[0], p[1]);
                theta = ip[0];
                phi = ip[1];

                if(!Double.isNaN(theta) && !Double.isNaN(phi)) {
                    //ipix = pixtools.ang2pix(phi, theta); // PixTools needs phi (angle from north pole) first, then theta (in-plane angle)
                    ipix = healpix.ang2pix(new SphericalCoordinates(1, theta, phi));
                    c = (int)(255 * ((double)counts[(int)ipix]/maxValue));
                    if(c == 0) {
                        col = white;
                    }
                    else {
                        col = cm.getColor(c);
                    }
                    img.setRGB(i, j, col);
                }
                else {
                    img.setRGB(i, j, white);
                }
            }
        }

        drawCoordinateGrid(g2d);
        drawScale(img, g2d);
        drawSun(g2d);


        // Write the image to file
        System.out.println("\nWriting file...");
        try
        {
            File f = new File(outputFolder + "frame" + String.format("%06d", n) + ".png");
            boolean res = ImageIO.write(img, "png", f);
            System.out.println("Written file: " + res);
        }
        catch(IOException e)
        {
            System.out.println("Error: " + e);
        }
    }

    private RangeSet getPixelsForRectangle(Vector3D[] rect) {
        RangeSet ipixes = new RangeSet();
        try {
            ipixes = healpix.queryRectangle(rect);
        }
        catch(Exception e) {
            System.out.println("Error: " + e);
        }

        return ipixes;
    }

    private void drawCoordinateGrid(Graphics2D g2d) {
        double longitude, latitude;
        double[] p1, p2;
        int nx, ny, mp1[], mp2[];

        g2d.setColor(Color.lightGray);
        g2d.setStroke(new BasicStroke(1));

        // Meridians
        nx = 19;
        ny = 180;
        for(int i=0; i < nx; i++) {
            longitude = -180 + i * 360.0 / (nx - 1);
            for (int j = 0; j < ny; j++) {
                latitude = -90 + j * 180.0 / ny;
                p1 = projection.projectLongLat(longitude, latitude);
                mp1 = worldToMap(p1);
                p2 = projection.projectLongLat(longitude, latitude + 180.0 / ny);
                mp2 = worldToMap(p2);
                g2d.drawLine(mp1[0], mp1[1], mp2[0], mp2[1]);
            }
        }

        // Parallels
        nx = 180;
        ny = 17;
        for(int i=0; i < nx; i++) {
            longitude = -180 + i * 360.0 / nx;
            for (int j = 0; j < ny; j++) {
                latitude = -80 + j * 160.0 / (ny-1);
                p1 = projection.projectLongLat(longitude, latitude);
                mp1 = worldToMap(p1);
                p2 = projection.projectLongLat(longitude + 360.0 / nx, latitude);
                mp2 = worldToMap(p2);
                g2d.drawLine(mp1[0], mp1[1], mp2[0], mp2[1]);
            }
        }

        // Numbers
        int textOffset = 15;
        g2d.setColor(Color.black);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Font font = new Font("Sans", Font.PLAIN, 30);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics(g2d.getFont());
        String s = "90";
        g2d.drawString(s, mapXOffset + mapWidth/2 - fm.stringWidth(s)/2, mapYOffset - textOffset);
        s = "-90";
        g2d.drawString(s, mapXOffset + mapWidth/2 - fm.stringWidth(s)/2, mapYOffset + mapHeight + fm.getAscent() + textOffset);
        s = "180";
        g2d.drawString(s, mapXOffset - fm.stringWidth(s) - textOffset, mapYOffset + mapHeight/2 + (fm.getAscent() - fm.getDescent())/2);
        s = "-180";
        g2d.drawString(s, mapXOffset + mapWidth + textOffset, mapYOffset + mapHeight/2 + (fm.getAscent() - fm.getDescent())/2);

        g2d.drawString("Roel Zinkstok", mapXOffset, imageHeight - mapYOffset);

        font = new Font("Sans", Font.PLAIN, 50);
        g2d.setFont(font);
        int y = (int)(time/365.25);
        int d = (int)(time - y * 365.25);
        int hr = (int)((time - y * 365.25 - d) * 24);
        int min = (int)(((time - y * 365.25 - d) * 24 - hr) * 60);
        s = String.format("NSL field transits in ICRS after: %d years %03d days %02d hr %02d min", y, d, hr, min);
        g2d.drawString(s, mapXOffset + mapWidth/2 - fm.stringWidth(s)/2, mapYOffset - 100);
    }

    private void drawScale(BufferedImage img, Graphics2D g2d) {
        int width = 50;
        int height = mapHeight;
        int startx = 2*mapXOffset + mapWidth + 200;
        int starty = mapYOffset;
        int textOffset = 20;
        int col;
        String s;

        ColorMap cm = ColorMap.getJet(256);

        for(int i=0; i<height; i++) {
            col = cm.getColor((int)(255 - 255 * ((double)i)/height));
            for(int j=0;j<width; j++) {
                img.setRGB(startx + j, starty + i, col);
            }
        }
        g2d.setColor(Color.black);
        g2d.setStroke(new BasicStroke(2));

        g2d.drawRect(startx, starty, width, height);

        g2d.setColor(Color.black);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Font font = new Font("Sans", Font.PLAIN, 30);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics(g2d.getFont());
        s = String.format("%d", maxValue);
        g2d.drawString(s, startx - fm.stringWidth(s) - textOffset, starty + (fm.getAscent() - fm.getDescent())/2);
        s = "0";
        g2d.drawString(s, startx - fm.stringWidth(s) - textOffset, starty + height + (fm.getAscent() - fm.getDescent())/2);
    }

    private void drawSun(Graphics2D g2d) {
        double[] worldPoint1, worldPoint2;
        int[] mapPoint1, mapPoint2;
        int size, nx = 1000;
        double longitude1, latitude1, longitude2, latitude2;

        // Ecliptic
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(5));
        g2d.setColor(Color.cyan);

        for(int i=0; i<nx; i++) {
            longitude1 = -180 + 360 * ((double)i)/nx;
            latitude1 = Math.toDegrees(epsilon) * Math.sin(Math.toRadians(longitude1));

            longitude2 = longitude1 + 360.0 * nx;
            latitude2 = Math.toDegrees(epsilon) * Math.sin(Math.toRadians(longitude2));

            worldPoint1 = projection.projectLongLat(longitude1, latitude1);
            worldPoint2 = projection.projectLongLat(longitude2, latitude2);
            mapPoint1 = worldToMap(worldPoint1);
            mapPoint2 = worldToMap(worldPoint2);
            g2d.drawLine(mapPoint1[0], mapPoint1[1], mapPoint2[0], mapPoint2[1]);
        }

        // Sun
        worldPoint1 = projection.projectThetaPhi(sunPosition.getTheta(), sunPosition.getPhi());
        mapPoint1 = worldToMap(worldPoint1);
        size = 50;
        g2d.setColor(Color.yellow);
        g2d.fillOval(mapPoint1[0] - size/2, mapPoint1[1] - size/2, size, size);
        g2d.setColor(Color.black);
        g2d.drawOval(mapPoint1[0] - size/2, mapPoint1[1] - size/2, size, size);

        // Spin axis
        worldPoint1 = projection.projectThetaPhi(precessionPosition.getTheta(), precessionPosition.getPhi());
        mapPoint1 = worldToMap(worldPoint1);
        size = 25;
        g2d.setColor(Color.black);
        g2d.fillOval(mapPoint1[0] - size/2, mapPoint1[1] - size/2, size, size);
    }
}
