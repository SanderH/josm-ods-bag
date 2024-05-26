package org.openstreetmap.josm.plugins.ods.bag.factories;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.geotools.feature.type.Types;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.Name;
import org.openstreetmap.josm.plugins.ods.bag.entity.NL_Address;
import org.openstreetmap.josm.plugins.ods.bag.entity.NL_HouseNumber;
import org.openstreetmap.josm.plugins.ods.bag.entity.NL_HouseNumberImpl;
import org.openstreetmap.josm.plugins.ods.entities.opendata.FeatureUtil;
import org.openstreetmap.josm.plugins.ods.geotools.impl.ModifiableGtEntityFactory;
import org.openstreetmap.josm.plugins.ods.io.DownloadResponse;

public class NL_GenericAddressFactory extends ModifiableGtEntityFactory<NL_Address> {
    private static String NS = "http://bag.geonovum.nl";
    private static Set<Name> featureTypes = new HashSet<>(Arrays.asList(
            Types.typeName(NS, "bag:verblijfsobject"),
            Types.typeName(NS, "bag:ligplaats"),
            Types.typeName(NS, "bag:standplaats")));
    
    @Override
    public Class<NL_Address> getTargetType() {
        return NL_Address.class;
    }

    @Override
    public boolean isApplicable(Name name,
            Class<?> entityType) {
        return entityType.equals(NL_Address.class) && 
                featureTypes.contains(name);
    }

    @Override
    public NL_Address createEntity(SimpleFeature feature, DownloadResponse response) {
        NL_Address address = new NL_Address();
        address.setHouseNumber(createHouseNumber(feature));
        address.setStreetName(FeatureUtil.getString(feature, "openbare_ruimte"));
        address.setCityName(FeatureUtil.getString(feature, "woonplaats"));
        address.setPostcode(FeatureUtil.getString(feature, "postcode"));
        return address;
    }
    
    private NL_HouseNumber createHouseNumber(SimpleFeature feature) {
        Integer number = FeatureUtil.getInteger(feature, "huisnummer");
        Character houseLetter = FeatureUtil.getCharacter(feature, "huisletter");
        String houseNumberExtra = FeatureUtil.getString(feature, "toevoeging");
        return new NL_HouseNumberImpl(null, number, houseLetter, houseNumberExtra);
    }
}
