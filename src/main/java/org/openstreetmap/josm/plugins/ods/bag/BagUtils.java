package org.openstreetmap.josm.plugins.ods.bag;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

public class BagUtils {
    public final static String REF_BAG = "ref:bag";
    public final static String REF_BAG_OLD = "ref:bag:old";
    public final static String SOURCE = "source";
    public final static String SOURCE_DATE = "source:date";
    public final static String BUILDING = "building";
    public final static String BUILDING_PART = "building:part";
    public final static String START_DATE = "start_date";
    public final static String LANDUSE = "landuse";
    public final static String NOTE_BAG = "note:bag";
    public final static String YES = "yes";

    public final static String CONSTRUCTION = "construction";
    public final static String STATIC_CARAVAN = "static_caravan";

    public final static String DATE_FORMAT = "yyyy-MM-dd";

    public static boolean isTaggedAsBagObject(OsmPrimitive osm) {
        return osm.hasKey(REF_BAG);
    }

    public static String normalizeRefBag(OsmPrimitive osm) {
        return normalizeRefBag(osm.get(REF_BAG));
    }
    
    public static String normalizeRefBagOld(OsmPrimitive osm) {
        return normalizeRefBag(osm.get(REF_BAG_OLD));
    }
    
    public static String normalizeRefBag(String rb) {
        return rb == null ? null : StringUtils.leftPad(rb, 16, "0");
    }
    
    public static boolean hasSourceDate(OsmPrimitive osm) {
        return osm.hasKey(SOURCE_DATE) && isDateValid(osm.get(SOURCE_DATE));
    }
    
    public static boolean isDateValid(String date) 
    {
        try {
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
    
    public static Date getSourceDate(OsmPrimitive osm)
    {
        return getDate(osm.get(SOURCE_DATE));
    }
    
    public static Date getDate(String date) 
    {
        try {
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setLenient(false);
            return df.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }
    
    public static boolean isConstruction(OsmPrimitive osm) {
        return osm.hasKey(CONSTRUCTION);
    }
    
    public static boolean isBuilding(OsmPrimitive osm) {
        return osm.hasKey(BUILDING);
    }
    
    public static boolean hasStartDate(OsmPrimitive osm) {
        return osm.hasKey(START_DATE);
    }
    
    public static boolean hasSource(OsmPrimitive osm) {
        return osm.hasKey(SOURCE);
    }
    
    public static boolean isStaticCaravan(OsmPrimitive osm) {
        return ((osm.hasKey(LANDUSE) && osm.get(LANDUSE).equals(STATIC_CARAVAN)) || 
                (osm.hasKey(BUILDING) && osm.get(BUILDING).equals(STATIC_CARAVAN)));
    }
    
    public static boolean hasNoteBag(OsmPrimitive osm) {
        return osm.hasKey(NOTE_BAG);
    }
    
}