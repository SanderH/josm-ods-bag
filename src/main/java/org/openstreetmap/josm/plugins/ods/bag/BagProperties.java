package org.openstreetmap.josm.plugins.ods.bag;

import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.data.preferences.IntegerProperty;
import org.openstreetmap.josm.data.preferences.StringProperty;

public final class BagProperties {

    public static final StringProperty WFS_URL = new StringProperty("nl.bag.pdok.wfs.url", "https://service.pdok.nl/lv/bag/wfs/v2_0");
    public static final StringProperty MY_WFS_URL = new StringProperty("nl.bag.pdok.mywfs.url", "https://bag.tools4osm.nl/geoserver/bag/ows");
    public static final IntegerProperty WFS_INIT_TIMEOUT = new IntegerProperty("nl.bag.pdok.wfs.init_timeout", 1000);
    public static final IntegerProperty WFS_DATA_TIMEOUT = new IntegerProperty("nl.bag.pdok.wfs.data_timeout", 10000);
    public static final IntegerProperty WFS_PAGE_SIZE = new IntegerProperty("nl.bag.pdok.wfs.page_size", 500);

    public static final BooleanProperty WFS_SUPPRESS_ALIGN = new BooleanProperty("nl.bag.pdok.wfs.suppress_align", false);
    public static final BooleanProperty WFS_GET_SECONDARY_ADDRESS = new BooleanProperty("nl.bag.pdok.wfs.get_secondary_address", false);

  private BagProperties() {
    // Private constructor to avoid instantiation
  }
}
