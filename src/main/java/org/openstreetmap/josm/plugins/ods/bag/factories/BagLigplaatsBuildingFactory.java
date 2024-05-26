package org.openstreetmap.josm.plugins.ods.bag.factories;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.geotools.feature.type.Types;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.Name;
import org.openstreetmap.josm.plugins.ods.bag.entity.BagOdBuilding;
import org.openstreetmap.josm.plugins.ods.bag.entity.NL_Address;
import org.openstreetmap.josm.plugins.ods.domains.buildings.BuildingType;
import org.openstreetmap.josm.plugins.ods.domains.buildings.OdAddress;
import org.openstreetmap.josm.plugins.ods.domains.buildings.OdBuilding;
import org.openstreetmap.josm.plugins.ods.entities.EntityStatus;
import org.openstreetmap.josm.plugins.ods.entities.opendata.FeatureUtil;
import org.openstreetmap.josm.plugins.ods.geotools.GtEntityFactory;
import org.openstreetmap.josm.plugins.ods.geotools.impl.ModifiableGtEntityFactory;
import org.openstreetmap.josm.plugins.ods.io.DownloadResponse;
import org.openstreetmap.josm.plugins.ods.od.GtEntityFactoryFactory;

public class BagLigplaatsBuildingFactory extends ModifiableGtEntityFactory<OdBuilding> {
    private static String NS = "http://bag.geonovum.nl";
    private static Name typeName = Types.typeName(NS, "bag:ligplaats");
    private BagGeometryFactory geometryFactory = new BagGeometryFactory();
    private GtEntityFactory<NL_Address> addressFactory = GtEntityFactoryFactory.create(
            typeName, NL_Address.class);

    @Override
    public boolean isApplicable(Name featureType, Class<?> entityType) {
        return featureType.equals(typeName) && entityType.equals(OdBuilding.class);
    }

    @Override
    public Class<OdBuilding> getTargetType() {
        return OdBuilding.class;
    }

    @Override
    public OdBuilding createEntity(SimpleFeature feature, DownloadResponse response) {
        BagOdBuilding building = new BagOdBuilding();
        building.setDownloadResponse(response);
        LocalDate date = response.getRequest().getDownloadTime().toLocalDate();
        if (date != null) {
            building.setSourceDate(DateTimeFormatter.ISO_LOCAL_DATE.format(date));
        }
        String id = FeatureUtil.getString(feature, "identificatie");
        building.setReferenceId(id);
        building.setPrimaryId(feature.getID());
//        LocalDate date = response.getRequest().getDownloadTime().toLocalDate();
//        if (date != null) {
//            entity.setSourceDate(DateTimeFormatter.ISO_LOCAL_DATE.format(date));
//        }
        building.setSource("BAG");
        building.setStatus(parseStatus(FeatureUtil.getString(feature, "status")));
        building.setGeometry(geometryFactory.create(feature, response));
        OdAddress address = addressFactory.create(feature, response);
        address.setStreetName(FeatureUtil.getString(feature, "openbare_ruimte"));
        address.setCityName(FeatureUtil.getString(feature, "woonplaats"));
        address.setPostcode(FeatureUtil.getString(feature, "postcode"));
        building.setAddress(address);
        building.setBuildingType(BuildingType.HOUSEBOAT);
        return building;
    }

    private static EntityStatus parseStatus(String status) {
        switch (status) {
        case "Plaats aangewezen":
            return EntityStatus.IN_USE;
        case "Plaats ingetrokken":
            return EntityStatus.REMOVED;
        default:
            return EntityStatus.UNKNOWN;
        }
    }
}
