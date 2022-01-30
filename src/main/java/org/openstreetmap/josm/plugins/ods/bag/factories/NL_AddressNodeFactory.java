package org.openstreetmap.josm.plugins.ods.bag.factories;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.geotools.feature.type.Types;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.Name;
import org.openstreetmap.josm.plugins.ods.bag.entity.BagOdAddressNode;
import org.openstreetmap.josm.plugins.ods.bag.entity.NL_Address;
import org.openstreetmap.josm.plugins.ods.domains.buildings.OdAddress;
import org.openstreetmap.josm.plugins.ods.domains.buildings.OdAddressNode;
import org.openstreetmap.josm.plugins.ods.entities.EntityStatus;
import org.openstreetmap.josm.plugins.ods.entities.opendata.FeatureUtil;
import org.openstreetmap.josm.plugins.ods.geotools.GtEntityFactory;
import org.openstreetmap.josm.plugins.ods.geotools.impl.ModifiableGtEntityFactory;
import org.openstreetmap.josm.plugins.ods.io.DownloadResponse;
import org.openstreetmap.josm.plugins.ods.od.GtEntityFactoryFactory;

public class NL_AddressNodeFactory extends ModifiableGtEntityFactory<OdAddressNode> {
    private static Name featureTypeName = Types.typeName("http://bag.geonovum.nl", "bag:verblijfsobject");
    private GtEntityFactory<NL_Address> addressFactory = GtEntityFactoryFactory.create(
            featureTypeName, NL_Address.class);
    private BagGeometryFactory geometryFactory = new BagGeometryFactory();

    @Override
    public boolean isApplicable(Name featureType, Class<?> entityType) {
        return featureTypeName.equals(featureType) && entityType.equals(OdAddressNode.class);
    }

    @Override
    public Class<OdAddressNode> getTargetType() {
        return OdAddressNode.class;
    }

    @Override
    public OdAddressNode createEntity(SimpleFeature feature, DownloadResponse response) {
        BagOdAddressNode addressNode = new BagOdAddressNode();
        addressNode.setDownloadResponse(response);
        LocalDate date = response.getRequest().getDownloadTime().toLocalDate();
        if (date != null) {
            addressNode.setSourceDate(DateTimeFormatter.ISO_LOCAL_DATE.format(date));
        }
        String id = FeatureUtil.getString(feature, "identificatie");
        addressNode.setReferenceId(id);
        addressNode.setPrimaryId(feature.getID());
        addressNode.setSource("BAG");
        addressNode.setGeometry(geometryFactory.create(feature, response));
        OdAddress address = addressFactory.create(feature, response);
        addressNode.setAddress(address);
        addressNode.setStatus(parseStatus(FeatureUtil.getString(feature, "status")));
        addressNode.setGebruiksdoel(FeatureUtil.getString(feature, "gebruiksdoel"));
        addressNode.setArea(FeatureUtil.getLong(feature, "oppervlakte").doubleValue());
        addressNode.setBuildingRef(FeatureUtil.getString(feature, "pandidentificatie"));
        return addressNode;
    }

    private static EntityStatus parseStatus(String status) {
        switch (status) {
        case "Verblijfsobject gevormd":
            return EntityStatus.PLANNED;
        case "Verblijfsobject in gebruik":
        case "Verblijfsobject buiten gebruik":
        case "Verblijfsobject in gebruik (niet ingemeten)":
            return EntityStatus.IN_USE;
        case "Verblijfsobject ingetrokken":
        case "Niet gerealiseerd verblijfsobject":
            return EntityStatus.REMOVED;
        case "Verbouwing verblijfsobject":
            return EntityStatus.RECONSTRUCTION;
        default:
            return EntityStatus.IN_USE;
        }
    }
}
