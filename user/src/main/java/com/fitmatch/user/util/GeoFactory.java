package com.fitmatch.user.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

@Component
public class GeoFactory {
  private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);

  public Point point(double lat, double lon) {
    Point p = GF.createPoint(new Coordinate(lon, lat)); // x=lon, y=lat
    p.setSRID(4326);
    return p;
  }

  public Double lat(Point p) {
    return p == null ? null : p.getY();
  }

  public Double lon(Point p) {
    return p == null ? null : p.getX();
  }
}
