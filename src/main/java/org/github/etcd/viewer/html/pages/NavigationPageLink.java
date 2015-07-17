package org.github.etcd.viewer.html.pages;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.github.etcd.viewer.ConvertUtils;

public class NavigationPageLink extends BookmarkablePageLink<String> {

    private static final long serialVersionUID = 1L;

    private IModel<String> etcdCluster;
    private IModel<String> key;

    public NavigationPageLink(final String id, final IModel<String> etcdCluster, final IModel<String> key) {
        super(id, NavigationPage.class);
        this.etcdCluster = etcdCluster;
        this.key = key;
    }

    @Override
    public PageParameters getPageParameters() {
        if (parameters == null) {
            parameters = ConvertUtils.getPageParameters(key.getObject());
            parameters.add("cluster", etcdCluster.getObject());
        }
        return parameters;
    }

    @Override
    public String getBeforeDisabledLink() {
        return "";
    }
    @Override
    public String getAfterDisabledLink() {
        return "";
    }

    @Override
    protected void onDetach() {
        super.onDetach();

        etcdCluster.detach();
        key.detach();
    }
}
