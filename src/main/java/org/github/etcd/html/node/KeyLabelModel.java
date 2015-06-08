package org.github.etcd.html.node;

import org.apache.wicket.model.ChainingModel;
import org.apache.wicket.model.IModel;

public class KeyLabelModel extends ChainingModel<String> {

    private static final long serialVersionUID = 1L;

    public KeyLabelModel(IModel<String> keyModel) {
        super(keyModel);
    }

    public KeyLabelModel(String key) {
        super(key);
    }

    @Override
    public String getObject() {
        String etcdKey = super.getObject();
        if (etcdKey == null || etcdKey.indexOf('/') == -1) {
            return etcdKey;
        }
        return etcdKey.substring(etcdKey.lastIndexOf('/') + 1);
    }

}
