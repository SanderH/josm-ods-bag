package org.openstreetmap.josm.plugins.ods.bag;

import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.ods.OdsModulePlugin;

public class BagImportPlugin extends OdsModulePlugin {
    public BagImportPlugin(PluginInformation info) throws Exception {
        super(info, new BagImportModule(info));
    }
}
