/*
 * Copyright (c) 2019 RedElastic Inc.
 * See LICENSE file for details.
 */

import com.google.inject.AbstractModule;
import play.data.format.Formatters;

@SuppressWarnings("WeakerAccess")
public class FormattersModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(Formatters.class).toProvider(FormattersProvider.class);

    }
}