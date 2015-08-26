package org.github.etcd.viewer.html.pages;

import org.github.etcd.viewer.html.cluster.RegistryListPanel;

public class RegistriesPage extends TemplatePage {

    private static final long serialVersionUID = 1L;

    public RegistriesPage() {

        add(new RegistryListPanel("registryList"));
    }

    @Override
    protected String getDefaultPageTitle() {
        return "etcd viewer | manage registries";
    }
}
