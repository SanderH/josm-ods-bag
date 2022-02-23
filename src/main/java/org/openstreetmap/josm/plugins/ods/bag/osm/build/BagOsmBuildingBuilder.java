package org.openstreetmap.josm.plugins.ods.bag.osm.build;

import java.util.Map;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.ods.OdsModule;
import org.openstreetmap.josm.plugins.ods.bag.BagUtils;
import org.openstreetmap.josm.plugins.ods.bag.entity.BagOsmAddress;
import org.openstreetmap.josm.plugins.ods.domains.buildings.BuildingType;
import org.openstreetmap.josm.plugins.ods.domains.buildings.OsmBuilding;
import org.openstreetmap.josm.plugins.ods.domains.buildings.impl.BaseOsmBuilding;
import org.openstreetmap.josm.plugins.ods.entities.EntityStatus;
import org.openstreetmap.josm.plugins.ods.entities.osm.AbstractOsmEntityBuilder;

import org.locationtech.jts.geom.Geometry;

public class BagOsmBuildingBuilder extends AbstractOsmEntityBuilder<OsmBuilding> {

    public BagOsmBuildingBuilder(OdsModule module) {
        super(module, OsmBuilding.class);
    }

    @Override
    public boolean canHandle(OsmPrimitive primitive) {
        return OsmBuilding.IsBuilding(primitive) || OsmBuilding.IsStaticCaravan(primitive) || OsmBuilding.IsMooring(primitive);
    }

    @Override
    public void buildOsmEntity(OsmPrimitive primitive) {
        if (canHandle(primitive)) {
            if (!getEntityStore().contains(primitive.getId())) {
                normalizeTags(primitive);
                BaseOsmBuilding building = new BaseOsmBuilding();
                Map<String, String> tags = primitive.getKeys();
                parseKeys(building, tags);
                building.setOtherTags(tags);

                Geometry geometry = buildGeometry(primitive);
                building.setGeometry(geometry);
                register(primitive, building);
            }
        }
        return;
    }

    public static void normalizeTags(OsmPrimitive primitive) {
        BagOsmEntityBuilder.normalizeTags(primitive);
    }

    private static void parseKeys(BaseOsmBuilding building, Map<String, String> tags) {
        BagOsmEntityBuilder.parseKeys(building, tags);
        String type = tags.remove(BagUtils.BUILDING);
        if (type == null) {
            type = tags.remove(BagUtils.BUILDING_PART);
        }
        if (type == null) {
            // no building, no building:part. There are tags with no:building, landuse and mooring that we probably need to ignore.
            return;
        }
        if (type.equals(BagUtils.CONSTRUCTION)) {
            building.setStatus(EntityStatus.CONSTRUCTION);
            String construction = tags.remove(BagUtils.CONSTRUCTION);
            type = (construction == null ? BagUtils.YES : construction);
        }
        else {
            building.setStatus(EntityStatus.IN_USE);
        }
        building.setBuildingType(getBuildingType(type, tags));
        building.setStartDate(tags.remove(BagUtils.START_DATE));
        if (tags.containsKey("addr:housenumber")) {
            BagOsmAddress address = new BagOsmAddress();
            BagOsmAddressEntityBuilder.parseKeys(address, tags);
            building.setAddress(address);
        }
        return;
    }

    private static BuildingType getBuildingType(String type, Map<String, String> tags) {
        switch(type) {
        case "house":
            return BuildingType.HOUSE;
        case "houseboat":
            tags.remove("floating");
            return BuildingType.HOUSEBOAT;
        case "static_caravan":
            return BuildingType.STATIC_CARAVAN;
        case "apartments":
            return BuildingType.APARTMENTS;
        case "industrial":
            return BuildingType.INDUSTRIAL ;
        case "retail":
            return BuildingType.RETAIL;
        case "office":
            return BuildingType.OFFICE;
        case "garage":
            return BuildingType.GARAGE;
        case "yes":
            if ("substation".equals(tags.get("power"))) {
                return BuildingType.SUBSTATION;
            }
            return BuildingType.UNCLASSIFIED;
        default:
            return BuildingType.OTHER;
        }
    }

    private Geometry buildGeometry(OsmPrimitive primitive) {
        if (primitive.getDisplayType() == OsmPrimitiveType.CLOSEDWAY) {
            return buildGeometry((Way)primitive);
        }
        else if (primitive.getType() == OsmPrimitiveType.RELATION) {
            return buildGeometry((Relation)primitive);
        }
        return null;
    }

    private Geometry buildGeometry(Way way) throws IllegalArgumentException {
        return getGeoUtil().toPolygon(way);
    }

    private Geometry buildGeometry(Relation relation) {
        return getGeoUtil().toMultiPolygon(relation);
    }
}
