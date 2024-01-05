package org.openstreetmap.josm.plugins.ods.bag;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Consumer;

import org.geotools.util.Version;
import org.opengis.feature.type.FeatureType;
import org.openstreetmap.josm.plugins.ods.Host;
import org.openstreetmap.josm.plugins.ods.InitializationException;
import org.openstreetmap.josm.plugins.ods.OdsDataSource;
import org.openstreetmap.josm.plugins.ods.OdsModule;
import org.openstreetmap.josm.plugins.ods.ParameterSet;
import org.openstreetmap.josm.plugins.ods.bag.gt.DistributeAddressNodes;
import org.openstreetmap.josm.plugins.ods.bag.gt.build.BuildingTypeEnricher;
import org.openstreetmap.josm.plugins.ods.domains.buildings.OdAddressNode;
import org.openstreetmap.josm.plugins.ods.domains.buildings.OdBuilding;
import org.openstreetmap.josm.plugins.ods.domains.buildings.impl.OpenDataBuildingStore;
import org.openstreetmap.josm.plugins.ods.entities.enrichment.BuildingCompletenessEnricher;
import org.openstreetmap.josm.plugins.ods.entities.opendata.FeatureDownloader;
import org.openstreetmap.josm.plugins.ods.entities.opendata.OdLayerManager;
import org.openstreetmap.josm.plugins.ods.entities.opendata.OpenDataLayerDownloader;
import org.openstreetmap.josm.plugins.ods.geotools.GtDataSource;
import org.openstreetmap.josm.plugins.ods.geotools.GtDatasourceBuilder;
import org.openstreetmap.josm.plugins.ods.geotools.GtDownloader;
import org.openstreetmap.josm.plugins.ods.geotools.GtEntityFactory;
import org.openstreetmap.josm.plugins.ods.geotools.GtFeatureSource;
import org.openstreetmap.josm.plugins.ods.geotools.InvalidQueryException;
import org.openstreetmap.josm.plugins.ods.matching.OpenDataAddressNodeToBuildingMatcher;
import org.openstreetmap.josm.plugins.ods.od.GtEntityFactoryFactory;
import org.openstreetmap.josm.plugins.ods.wfs.WFSHost;
import org.openstreetmap.josm.plugins.ods.wfs.WFSHostFactory;

public class BagWfsLayerDownloader extends OpenDataLayerDownloader {
    private static final String DEFAULT_WFS_URL = "https://service.pdok.nl/lv/bag/wfs/v2_0";
    private static final String DEFAULT_MY_WFS_URL = "https://bag.tools4osm.nl/geoserver/bag/ows";
    private static final Integer DEFAULT_WFS_INIT_TIMEOUT = 1000;
    private static final Integer DEFAULT_WFS_DATA_TIMEOUT = 10000;

    private final WFSHost wfsHost;
    private final WFSHost wfsMyHost;
    private final OdsModule module;
    private final OdLayerManager layerManager;
    private final BagPrimitiveBuilder primitiveBuilder;

    LinkedList<OdAddressNode> unmatchedOpenDataAddressNodes = new LinkedList<>();

    public BagWfsLayerDownloader(OdsModule module) throws InitializationException {
        super(module);
        this.wfsHost = createHost();
        this.module = module;
        this.layerManager = module.getOpenDataLayerManager();
        addFeatureDownloader(createPandDownloader());
        addFeatureDownloader(createLigplaatsDownloader());
        addFeatureDownloader(createStandplaatsDownloader());
        addFeatureDownloader(createVerblijfsobjectDownloader());
        if (BagProperties.WFS_GET_SECONDARY_ADDRESS.get())
        {
            this.wfsMyHost = createMyHost();
            addFeatureDownloader(createNevenadresDownloader());
        }
        else
        {
            this.wfsMyHost = null;
        }
        this.primitiveBuilder = new BagPrimitiveBuilder(module);
    }

    private static WFSHost createHost() {
        WFSHostFactory factory = new WFSHostFactory();
        ParameterSet parameters = new ParameterSet()
                .put(Host.HOST_NAME, "BAG_WFS")
                .put(Host.BASE_URL, getBaseWfsUrl())
                .put(WFSHost.WFS_VERSION, new Version("1.1.0"))
                .put(Host.PAGE_SIZE, 500)
                .put(WFSHost.STRATEGY, "mapserver")
                .put(WFSHost.PROTOCOL, false)
                .put(OdsDataSource.INIT_TIMEOUT, getInitTimeout())
                .put(OdsDataSource.DATA_TIMEOUT, getDataTimeout());
        return factory.create(parameters);
    }

    private static WFSHost createMyHost() {
        WFSHostFactory factory = new WFSHostFactory();
        ParameterSet parameters = new ParameterSet()
                .put(Host.HOST_NAME, "BAG_WFS")
                .put(Host.BASE_URL, getBaseMyWfsUrl())
                 .put(WFSHost.WFS_VERSION, new Version("1.1.0"))
                //.put(Host.PAGE_SIZE, 500) // GeoServer doesn't like paging
                .put(WFSHost.STRATEGY, "mapserver")
                .put(WFSHost.PROTOCOL, false)
                .put(OdsDataSource.INIT_TIMEOUT, getInitTimeout())
                .put(OdsDataSource.DATA_TIMEOUT, getDataTimeout());
        return factory.create(parameters);
    }

    //new WFSHost("BAG WFS", "https://geodata.nationaalgeoregister.nl/bag/wfs/v1_1?request=getCapabilities&service=WFS&version=1.1.0", 1000, 1000, 60000);
    @Override
    public void process() {
        try {
            super.process();
            matchAddressNodesToBuilding();
            checkBuildingCompleteness();
            distributeAddressNodes();
            analyzeBuildingTypes();
            //            findBuildingNeighbours(getResponse());
            primitiveBuilder.run(getResponse());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private FeatureDownloader createNevenadresDownloader() throws InitializationException {
        GtFeatureSource featureSource = new GtFeatureSource(wfsMyHost, "bag:SecondaryAddress_Missing", "identificatie");
        featureSource.initialize();
        FeatureType featureType = featureSource.getFeatureType();
        GtDatasourceBuilder builder = new GtDatasourceBuilder();
        builder.setFeatureSource(featureSource);
        builder.setProperties(Arrays.asList("identificatie", "oppervlakte", "status", "gebruiksdoel",
                "openbare_ruimte", "huisnummer", "huisletter", "toevoeging", "postcode", "woonplaats",
                "geom", "pandidentificatie"));
        builder.setUniqueKey(Arrays.asList("identificatie", "pandidentificatie"));
        builder.setPageSize(500);
        //      Query query = new GroupByQuery(featureSource, properties, );
        GtDataSource dataSource = builder.build();
        GtEntityFactory<OdAddressNode> entityBuilder = GtEntityFactoryFactory.create(
                featureType.getName(), OdAddressNode.class);
        return new GtDownloader<>(dataSource, module.getCrsUtil(), entityBuilder,
                layerManager.getEntityStore(OdAddressNode.class));
    }

    private FeatureDownloader createVerblijfsobjectDownloader() throws InitializationException {
        GtFeatureSource featureSource = new GtFeatureSource(wfsHost, "bag:verblijfsobject", "identificatie");
        featureSource.initialize();
        FeatureType featureType = featureSource.getFeatureType();
        GtDatasourceBuilder builder = new GtDatasourceBuilder();
        builder.setFeatureSource(featureSource);
        builder.setProperties(Arrays.asList("identificatie", "oppervlakte", "status", "gebruiksdoel",
                "openbare_ruimte", "huisnummer", "huisletter", "toevoeging", "postcode", "woonplaats",
                "geom", "pandidentificatie"));
        builder.setUniqueKey(Arrays.asList("identificatie", "pandidentificatie"));
        builder.setPageSize(500);
        //      Query query = new GroupByQuery(featureSource, properties, );
        GtDataSource dataSource = builder.build();
        GtEntityFactory<OdAddressNode> entityBuilder = GtEntityFactoryFactory.create(
                featureType.getName(), OdAddressNode.class);
        return new GtDownloader<>(dataSource, module.getCrsUtil(), entityBuilder,
                layerManager.getEntityStore(OdAddressNode.class));
    }

    private FeatureDownloader createPandDownloader() throws InvalidQueryException, InitializationException {
        GtFeatureSource featureSource = new GtFeatureSource(wfsHost, "bag:pand", "identificatie");
        featureSource.initialize();
        GtDatasourceBuilder builder = new GtDatasourceBuilder();
        builder.setFeatureSource(featureSource);
        builder.setProperties(Arrays.asList("identificatie", "bouwjaar", "status", "aantal_verblijfsobjecten",
                "geom"));
        builder.setUniqueKey("identificatie");
        builder.setPageSize(500);

        //       Query query = new GroupByQuery(featureSource, properties, Arrays.asList("identificatie"));
        GtDataSource dataSource = builder.build();
        GtEntityFactory<OdBuilding> entityBuilder = GtEntityFactoryFactory.create(
                featureSource.getFeatureType().getName(), OdBuilding.class);
        FeatureDownloader downloader = new GtDownloader<>(dataSource, module.getCrsUtil(), entityBuilder,
                layerManager.getEntityStore(OdBuilding.class));
        return downloader;
    }

    private FeatureDownloader createLigplaatsDownloader() throws InvalidQueryException, InitializationException {
        GtFeatureSource featureSource = new GtFeatureSource(wfsHost, "bag:ligplaats", "identificatie");
        featureSource.initialize();
        GtDatasourceBuilder builder = new GtDatasourceBuilder();
        builder.setFeatureSource(featureSource);
        builder.setProperties(Arrays.asList("identificatie", "status", "openbare_ruimte", "huisnummer",
                "huisletter", "toevoeging", "postcode", "woonplaats", "geom"));
        builder.setUniqueKey("identificatie");
        builder.setPageSize(500);

        //       Query query = new GroupByQuery(featureSource, properties, Arrays.asList("identificatie"));
        GtDataSource dataSource = builder.build();
        GtEntityFactory<OdBuilding> entityBuilder = GtEntityFactoryFactory.create(
                featureSource.getFeatureType().getName(), OdBuilding.class);
        FeatureDownloader downloader = new GtDownloader<>(dataSource, module.getCrsUtil(), entityBuilder,
                layerManager.getEntityStore(OdBuilding.class));
        return downloader;
    }

    private FeatureDownloader createStandplaatsDownloader() throws InvalidQueryException, InitializationException {
        GtFeatureSource featureSource = new GtFeatureSource(wfsHost, "bag:standplaats", "identificatie");
        featureSource.initialize();
        GtDatasourceBuilder builder = new GtDatasourceBuilder();
        builder.setFeatureSource(featureSource);
        builder.setProperties(Arrays.asList("identificatie", "status", "openbare_ruimte", "huisnummer",
                "huisletter", "toevoeging", "postcode", "woonplaats", "geom"));
        builder.setUniqueKey("identificatie");
        builder.setPageSize(500);

        //       Query query = new GroupByQuery(featureSource, properties, Arrays.asList("identificatie"));
        GtDataSource dataSource = builder.build();
        GtEntityFactory<OdBuilding> entityBuilder = GtEntityFactoryFactory.create(
                featureSource.getFeatureType().getName(), OdBuilding.class);
        FeatureDownloader downloader = new GtDownloader<>(dataSource, module.getCrsUtil(), entityBuilder,
                layerManager.getEntityStore(OdBuilding.class));
        return downloader;
    }

    /**
     * Find a matching building for foreign addressNodes.
     */
    private void matchAddressNodesToBuilding() {
        OpenDataAddressNodeToBuildingMatcher matcher = new OpenDataAddressNodeToBuildingMatcher(module);
        matcher.setUnmatchedAddressNodeHandler(unmatchedOpenDataAddressNodes::add);
        for(OdAddressNode addressNode : layerManager.getEntityStore(OdAddressNode.class)) {
            matcher.matchAddressToBuilding(addressNode);
        }
    }

    private void checkBuildingCompleteness() {
        OpenDataBuildingStore buildingStore = (OpenDataBuildingStore) layerManager.getEntityStore(OdBuilding.class);
        Consumer<OdBuilding> enricher = new BuildingCompletenessEnricher(buildingStore);
        buildingStore.forEach(enricher);
    }

    private void distributeAddressNodes() {
        OpenDataBuildingStore buildingStore = (OpenDataBuildingStore) layerManager.getEntityStore(OdBuilding.class);
        Consumer<OdBuilding> enricher = new DistributeAddressNodes(module.getGeoUtil());
        buildingStore.forEach(enricher);
    }

    private void analyzeBuildingTypes() {
        OpenDataBuildingStore buildingStore = (OpenDataBuildingStore) layerManager.getEntityStore(OdBuilding.class);
        Consumer<OdBuilding> enricher = new BuildingTypeEnricher();
        buildingStore.forEach(enricher);
    }
    
    private static String getBaseWfsUrl() {
        if (BagProperties.WFS_URL.isSet()) {
          return BagProperties.WFS_URL.get();
        }
        return DEFAULT_WFS_URL;
    }
    
    private static String getBaseMyWfsUrl() {
        if (BagProperties.MY_WFS_URL.isSet()) {
          return BagProperties.MY_WFS_URL.get();
        }
        return DEFAULT_MY_WFS_URL;
    }

    private static Integer getInitTimeout() {
        if (BagProperties.WFS_INIT_TIMEOUT.isSet()) {
          return BagProperties.WFS_INIT_TIMEOUT.get();
        }
        return DEFAULT_WFS_INIT_TIMEOUT;
    }
    
    private static Integer getDataTimeout() {
        if (BagProperties.WFS_DATA_TIMEOUT.isSet()) {
          return BagProperties.WFS_DATA_TIMEOUT.get();
        }
        return DEFAULT_WFS_DATA_TIMEOUT;
    }
}
