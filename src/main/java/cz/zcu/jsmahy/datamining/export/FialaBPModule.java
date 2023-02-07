package cz.zcu.jsmahy.datamining.export;

import com.google.inject.AbstractModule;
import cz.zcu.jsmahy.datamining.api.DataNodeSerializer;
import cz.zcu.jsmahy.datamining.api.RequestProgressListener;

import static com.google.inject.Scopes.SINGLETON;

public class FialaBPModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(RequestProgressListener.class).to(FialaBPRequestProgressListener.class)
                                           .in(SINGLETON);
        bind(DataNodeSerializer.class).to(FialaBPSerializer.class)
                                      .in(SINGLETON);
    }
}
