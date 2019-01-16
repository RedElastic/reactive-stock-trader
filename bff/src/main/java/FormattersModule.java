import com.google.inject.AbstractModule;

import play.data.format.Formatters;

class FormattersModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(Formatters.class).toProvider(FormattersProvider.class);

    }
}