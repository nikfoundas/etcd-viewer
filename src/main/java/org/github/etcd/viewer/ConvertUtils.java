/**
 *
 */
package org.github.etcd.viewer;

import org.apache.wicket.request.mapper.parameter.PageParameters;

public class ConvertUtils {

    public static String getEtcdKey(PageParameters pageParameters) {
        if (pageParameters.getIndexedCount() == 0) {
            return "/";
        } else {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < pageParameters.getIndexedCount(); i++) {
                sb.append('/');
                sb.append(pageParameters.get(i).toString());
            }
            return sb.toString();
        }
    }

    public static PageParameters getPageParameters(String etcdKey) {
        String[] keyParts = etcdKey == null ? new String[0] : etcdKey.split("/");
        PageParameters parameters = new PageParameters();
        for (String keyPart : keyParts) {
            if (!"".equals(keyPart)) {
                parameters.set(parameters.getIndexedCount(), keyPart); // add the current non empty part
            }
        }
        return parameters;
    }

    /*
    public static List<PageParameters> getBreadcrumb(String etcdKey) {
        String[] keyParts = etcdKey == null ? new String[0] : etcdKey.split("/");

        List<PageParameters> breadcrumb = new ArrayList<>(keyParts.length + 1);
        PageParameters parameters = new PageParameters(); // root node
        breadcrumb.add(parameters);

        for (String keyPart : keyParts) {
            if (!"".equals(keyPart)) {
                parameters = new PageParameters(parameters); // copy previous params
                parameters.set(parameters.getIndexedCount(), keyPart); // add the current part

                breadcrumb.add(parameters);
            }
        }

        return breadcrumb;
    }
    */

}
