package org.openstreetmap.josm.plugins.ods.bag.factories;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.geotools.feature.type.Types;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.Name;
import org.openstreetmap.josm.plugins.ods.bag.entity.BagOdBuilding;
import org.openstreetmap.josm.plugins.ods.domains.buildings.BuildingType;
import org.openstreetmap.josm.plugins.ods.domains.buildings.OdBuilding;
import org.openstreetmap.josm.plugins.ods.entities.EntityStatus;
import org.openstreetmap.josm.plugins.ods.entities.opendata.FeatureUtil;
import org.openstreetmap.josm.plugins.ods.geotools.impl.ModifiableGtEntityFactory;
import org.openstreetmap.josm.plugins.ods.io.DownloadResponse;

public class BagPandFactory extends ModifiableGtEntityFactory<OdBuilding> {
    private static String NS = "http://bag.geonovum.nl";
    private static Name typeName = Types.typeName(NS, "bag:pand");
    private BagGeometryFactory geometryFactory = new BagGeometryFactory();

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
        building.setSource("BAG");
        Integer bouwjaar = FeatureUtil.getInteger(feature, "bouwjaar");
        building.setStartYear(bouwjaar);
        building.setStatus(parseStatus(FeatureUtil.getString(feature, "status")));
        building.setBuildingType(BuildingType.UNCLASSIFIED);
        building.setAantalVerblijfsobjecten(FeatureUtil.getLong(feature, "aantal_verblijfsobjecten"));
        building.setGeometry(geometryFactory.create(feature, response));
        return building;
    }

    private static EntityStatus parseStatus(String status) {
        switch (status) {
        case "Bouwvergunning verleend":
            return EntityStatus.PLANNED;
        case "Bouw gestart":
            return EntityStatus.CONSTRUCTION;
        case "Pand in gebruik":
        case "Pand buiten gebruik":
            return EntityStatus.IN_USE;
        case "Pand in gebruik (niet ingemeten)":
            return EntityStatus.IN_USE_NOT_MEASURED;
        case "Niet gerealiseerd pand":
            return EntityStatus.NOT_REALIZED;
        case "Sloopvergunning verleend":
            return EntityStatus.REMOVAL_DUE;
        case "Pand gesloopt":
            return EntityStatus.REMOVED;
        case "Verbouwing pand":
            return EntityStatus.RECONSTRUCTION;
        default:
            return EntityStatus.UNKNOWN;
        }
    }
}
