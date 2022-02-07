package org.openstreetmap.josm.plugins.ods.bag;

import org.openstreetmap.josm.data.preferences.IntegerProperty;
import org.openstreetmap.josm.data.preferences.StringProperty;

public final class BagProperties {

    public static final StringProperty WFS_URL = new StringProperty("nl.bag.pdok.wfs.url", "https://service.pdok.nl/lv/bag/wfs/v2_0");
    public static final StringProperty MY_WFS_URL = new StringProperty("nl.bag.pdok.mywfs.url", "https://bag.tools4osm.nl/geoserver/bag/ows");
    public static final IntegerProperty WFS_INIT_TIMEOUT = new IntegerProperty("nl.bag.pdok.wfs.init_timeout", 1000);
    public static final IntegerProperty WFS_DATA_TIMEOUT = new IntegerProperty("nl.bag.pdok.wfs.data_timeout", 10000);

  private BagProperties() {
    // Private constructor to avoid instantiation
  }
}
