package org.openstreetmap.josm.plugins.ods.bag;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.UserInfo;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.io.OsmServerUserInfoReader;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.ods.OdsModule;
import org.openstreetmap.josm.plugins.ods.bag.osm.build.BagOsmAddressNodeBuilder;
import org.openstreetmap.josm.plugins.ods.bag.osm.build.BagOsmBuildingBuilder;
import org.openstreetmap.josm.plugins.ods.crs.CRSUtil;
import org.openstreetmap.josm.plugins.ods.crs.CRSUtilProj4j;
import org.openstreetmap.josm.plugins.ods.domains.buildings.OdAddressNode;
import org.openstreetmap.josm.plugins.ods.domains.buildings.OdBuilding;
import org.openstreetmap.josm.plugins.ods.domains.buildings.OsmAddressNode;
import org.openstreetmap.josm.plugins.ods.domains.buildings.OsmBuilding;
import org.openstreetmap.josm.plugins.ods.domains.buildings.impl.OpenDataAddressNodeStore;
import org.openstreetmap.josm.plugins.ods.domains.buildings.impl.OpenDataBuildingStore;
import org.openstreetmap.josm.plugins.ods.domains.buildings.impl.OsmAddressNodeStore;
import org.openstreetmap.josm.plugins.ods.domains.buildings.impl.OsmBuildingStore;
import org.openstreetmap.josm.plugins.ods.entities.opendata.OdLayerManager;
import org.openstreetmap.josm.plugins.ods.entities.osm.OsmLayerManager;
import org.openstreetmap.josm.plugins.ods.gui.OdsDownloadAction;
import org.openstreetmap.josm.plugins.ods.gui.OdsResetAction;
import org.openstreetmap.josm.plugins.ods.gui.OdsUpdateAction;
import org.openstreetmap.josm.plugins.ods.io.MainDownloader;
import org.openstreetmap.josm.plugins.ods.jts.GeoUtil;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.Logging;

public class BagImportModule extends OdsModule {
    // Boundary of the Netherlands
    private final static Bounds BOUNDS = new Bounds(50.734, 3.206, 53.583, 7.245);
    private final MainDownloader mainDownloader;
    private final GeoUtil geoUtil = new GeoUtil();
    private final CRSUtil crsUtil = new CRSUtilProj4j();
    private final static String INFO_URL = "https://bag.tools4osm.nl/plugins/versions.json";
    private JsonObject metaInfo;
    private boolean isDebug = false;

    public BagImportModule(PluginInformation info) {
        this.mainDownloader = new BagDownloader(this);
        readInfo();
        checkVersion(info);
    }

    @Override
    public void initialize() throws Exception {
        super.initialize();
        mainDownloader.initialize();
        addOsmEntityBuilder(new BagOsmBuildingBuilder(this));
        addOsmEntityBuilder(new BagOsmAddressNodeBuilder(this));
        addAction(new OdsDownloadAction(this));
        //        addAction(new RemoveAssociatedStreetsAction(this));
        //        addAction(new OdsImportAction(this));
        addAction(new OdsUpdateAction(this));
        addAction(new OdsResetAction(this));
    }

    @Override
    protected OsmLayerManager createOsmLayerManager() {
        OsmLayerManager manager = new OsmLayerManager(this, "BAG OSM");
        manager.addEntityStore(OsmBuilding.class, new OsmBuildingStore());
        manager.addEntityStore(OsmAddressNode.class, new OsmAddressNodeStore());
        return manager;
    }

    @Override
    protected OdLayerManager createOpenDataLayerManager() {
        OdLayerManager manager = new OdLayerManager("BAG ODS");
        manager.addEntityStore(OdBuilding.class, new OpenDataBuildingStore());
        manager.addEntityStore(OdAddressNode.class, new OpenDataAddressNodeStore());
        return manager;
    }

    @Override
    public String getName() {
        return "BAG" + (isDebug ? " (debug)" : "");
    }


    @Override
    public String getDescription() {
        return I18n.tr("ODS module to import buildings and addresses in the Netherlands");
    }

    @Override
    public GeoUtil getGeoUtil() {
        return geoUtil;
    }

    @Override
    public CRSUtil getCrsUtil() {
        return crsUtil;
    }

    @Override
    public Bounds getBounds() {
        return BOUNDS;
    }

    @Override
    public MainDownloader getDownloader() {
        return mainDownloader;
    }

    @Override
    public boolean usePolygonFile() {
        return true;
    }

    @SuppressWarnings("unused")
    @Override
    public boolean activate() {
        if (false && !checkUser()) { // Disabled, but kept the code in case we need it
            int answer = JOptionPane.showConfirmDialog(MainApplication.getMainFrame(),
                    "Je gebruikersnaam eindigt niet op _BAG en is daarom niet geschikt " +
                            "voor de BAG import.\nWeet je zeker dat je door wilt gaan?",
                            I18n.tr("Invalid user"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (answer == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }
        return super.activate();
    }

    @Override
    public Double getTolerance() {
        return 1e-5;
    }

    private static boolean checkUser() {
        try {
            final UserInfo userInfo = new OsmServerUserInfoReader().fetchUserInfo(NullProgressMonitor.INSTANCE);
            String user = userInfo.getDisplayName();
            String suffix = "_BAG";
            return user.endsWith(suffix);
        } catch (OsmTransferException e1) {
            Logging.warn(tr("Failed to retrieve OSM user details from the server."));
            return false;
        }
    }
    
    public void checkVersion(PluginInformation info) {
        if (metaInfo == null) return;
        String latestVersion = metaInfo.getJsonObject("version").getString("latest");
        String nextVersion = metaInfo.getJsonObject("version").getString("next");
        if (!info.version.equals(latestVersion) && !info.version.equals(nextVersion)) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), I18n.tr("Your ODS-BAG version ({0}) is out of date.\n" +
                    "Please upgrade to the latest version: {1}", info.version, latestVersion), "Plug-in out of date", JOptionPane.WARNING_MESSAGE);
        }
        if (info.version.equals(nextVersion)) {
            isDebug = true;
        }
    }

    private void readInfo() {
        URL url;
        try {
            url = new URL(INFO_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        try (
            InputStream is = url.openStream();
            JsonReader reader = Json.createReader(is);
            )  {
                metaInfo = reader.readObject().getJsonObject("ods-bag");
                if (metaInfo == null) {
                    JOptionPane.showMessageDialog(MainApplication.getMainFrame(), I18n.tr("No version information is available at the moment.\n" +
                            "Your ODS-BAG version may be out of date"), "No version info", JOptionPane.WARNING_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), I18n.tr("No version information is available at the moment.\n" +
                    "Your ODS-BAG version may be out of date"), "No version info", JOptionPane.WARNING_MESSAGE);

        }
    }
}
