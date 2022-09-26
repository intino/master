package com.cinepolis.master;

import com.cinepolis.master.model.validators.TestRecordValidationLayer;
import io.intino.master.core.Master;
import io.intino.master.core.MasterConfig;

import java.io.File;

public class Server {

    public static void main(String[] args) {

        MasterConfig config = new MasterConfig();
        config.port(60555);
        config.dataDirectory(new File("temp/cinepolis-data/datasets"));
        config.logDirectory(new File("temp/logs/master"));
        config.validationLayers().recordValidationLayer(new TestRecordValidationLayer());
        config.instanceName("master-test");

        Master master = new Master(config);
        master.start();
    }
}
