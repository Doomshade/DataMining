package cz.zcu.jsmahy.datamining.export;

import com.google.inject.AbstractModule;
import cz.zcu.jsmahy.datamining.api.DataNodeSerializer;
import cz.zcu.jsmahy.datamining.api.RequestProgressListener;

public class FialaBPModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(RequestProgressListener.class).to(FialaBPRequestProgressListener.class)
                                           .asEagerSingleton();
        bind(DataNodeSerializer.class).to(FialaBPSerializer.class)
                                      .asEagerSingleton();
    }
}
