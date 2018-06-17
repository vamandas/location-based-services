package com.iskconbaroda.maputils;

/**
 * MapAreaMeasure
 *
 * @author ivanschuetz
 */
public class MapAreaMeasure {

    public double value;
    public Unit unit;
    public MapAreaMeasure(double value, Unit unit) {
        this.value = value;
        this.unit = unit;
    }

    public static enum Unit {pixels, meters}
}