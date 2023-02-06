package cz.zcu.jsmahy.datamining.export;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import cz.zcu.jsmahy.datamining.api.RequestProgressListener;

public class FialaBPModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(RequestProgressListener.class).to(FialaBPRequestProgressListener.class)
                                           .in(Scopes.SINGLETON);
    }
}
