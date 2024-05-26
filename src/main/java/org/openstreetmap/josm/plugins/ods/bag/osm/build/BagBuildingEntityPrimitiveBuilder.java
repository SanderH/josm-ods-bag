package org.openstreetmap.josm.plugins.ods.bag.osm.build;

import java.util.Map;

import org.openstreetmap.josm.plugins.ods.domains.buildings.OdAddress;
import org.openstreetmap.josm.plugins.ods.domains.buildings.OdBuilding;
import org.openstreetmap.josm.plugins.ods.entities.EntityStatus;
import org.openstreetmap.josm.plugins.ods.entities.opendata.OdLayerManager;

public class BagBuildingEntityPrimitiveBuilder extends BagEntityPrimitiveBuilder<OdBuilding> {

    public BagBuildingEntityPrimitiveBuilder(OdLayerManager dataLayer) {
        super(dataLayer);
    }

    @Override
    public void createPrimitive(OdBuilding building) {
        // Ignore buildings with status "Bouwvergunning verleend"
        // Make an exception for buildings that already exist in OSM. In that case, the building permit is for reconstruction
        // Don't skip as municipalities often are behind in administration and reports from  the field indicate building has started or even completed!
        //if (building.getStatus() == EntityStatus.PLANNED
        //        && building.getMatch() == null) {
        //    return;
        //}
        super.createPrimitive(building);
    }


    @Override
    protected void buildTags(OdBuilding building, Map<String, String> tags) {
        OdAddress address = building.getAddress();
        if (address != null) {
            createAddressTags(address, tags);
        }
        tags.put("source", "BAG");
        tags.put("source:date", building.getSourceDate());
        tags.put("ref:bag", building.getReferenceId().toString());
        if (building.getStartDate() != null) {
            tags.put("start_date", building.getStartDate());
        }
        if (building.getStatus() == EntityStatus.REMOVAL_DUE) {
            tags.put("note", "Sloopvergunning verleend");
        }
        String type = "yes";
        switch (building.getBuildingType()) {
        case APARTMENTS:
            type = "apartments";
            break;
        case GARAGE:
            type = "garage";
            break;
        case HOUSE:
            type = "house";
            break;
        case HOUSEBOAT:
            type = "houseboat";
            tags.put("floating", "yes");
            break;
        case INDUSTRIAL:
            type = "industrial";
            break;
        case OFFICE:
            type = "office";
            break;
        case PRISON:
            tags.put("amenity", "prison");
            break;
        case RETAIL:
            type = "retail";
            break;
        case STATIC_CARAVAN:
            type = "static_caravan";
            break;
        case SUBSTATION:
            tags.put("power", "substation");
            break;
        default:
            type = "yes";
            break;
        }

        if (building.getStatus().equals(EntityStatus.CONSTRUCTION) ||
                building.getStatus().equals(EntityStatus.PLANNED)) {
            tags.put("building", "construction");
            tags.put("construction", type);
        }
        else {
            tags.put("building", type);
        }
    }
}
