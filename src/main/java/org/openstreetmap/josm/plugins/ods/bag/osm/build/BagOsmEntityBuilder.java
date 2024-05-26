package org.openstreetmap.josm.plugins.ods.bag.osm.build;

import java.util.Map;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.ods.bag.BagUtils;
import org.openstreetmap.josm.plugins.ods.entities.OsmEntity;

public abstract class BagOsmEntityBuilder {

    /*
     * Earlier versions of the BAG import plug-in and some other small BAG
     * imports have introduced tags that are no longer used.
     * This method translates these tags into their current equivalent and
     * remove the old tags.
     */
    public static void normalizeTags(OsmPrimitive primitive) {
        String source = primitive.get("source");
        if (source != null && source.toUpperCase().startsWith("BAG")) {
            primitive.put("source", "BAG");
            if (source.length() == 11 && source.charAt(6) == '-') {
                try {
                    String month = source.substring(4, 6);
                    String year = source.substring(7, 11);
                    int m = Integer.parseInt(month);
                    int y = Integer.parseInt(year);
                    primitive.put("source:date", String.format("%1$4d-%2$02d", y, m));
                } catch (Exception e) {
                    // Something went wrong. Ignore the source date and print
                    // the stack trace
                    e.printStackTrace();
                }
            }
        }
    }

    public static void parseKeys(OsmEntity entity, Map<String, String> tags) {
        entity.setReferenceId(getReferenceId(tags.remove("ref:bag")));
        entity.setSource(tags.remove("source"));
        String sourceDate = tags.remove("source:date");
        if (sourceDate != null) {
            entity.setSourceDate(sourceDate);
        }
    }

    private static String getReferenceId(String s) {
        if (s == null || s.length() == 0) return null;
        return BagUtils.normalizeRefBag(s);
    }
}
