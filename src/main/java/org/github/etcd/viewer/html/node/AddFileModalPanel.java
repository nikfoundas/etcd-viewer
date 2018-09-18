package org.github.etcd.viewer.html.node;

import com.fasterxml.jackson.databind.annotation.JsonAppend.Prop;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.github.etcd.service.EtcdProxyFactory;
import org.github.etcd.service.api.EtcdException;
import org.github.etcd.service.api.EtcdNode;
import org.github.etcd.service.api.EtcdProxy;
import org.github.etcd.viewer.html.modal.GenericModalPanel;

public class AddFileModalPanel extends GenericModalPanel {

    private static final long serialVersionUID = -2070660918144252605L;
    private FileUploadField uploadField;
    private IModel<Boolean> updating;
    private IModel<String> registry;

    @Inject
    private EtcdProxyFactory proxyFactory;


    private Form<EtcdNode> form;
    private Label title;

//    public addFileModalPanel(String id) {
//        this(id, new CompoundPropertyModel(makeModel()));
//    }

    public AddFileModalPanel(String id, IModel<EtcdNode> model,
                             IModel<String> registry, IModel<Boolean> updating) {
        super(id, null);

        this.updating = updating;
        this.registry = registry;

        add(title = new Label("title",
                new StringResourceModel("editModal.title.updating.${}", updating, "Upload from Properties file")));
        title.setOutputMarkupId(true);

        form = new Form<EtcdNode>("form");

        form.add(new AjaxFormSubmitBehavior("onSubmit") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
            }
        });

        AjaxButton ab = new AjaxButton("submit") {
            protected void onSubmit(AjaxRequestTarget target, Form form) {

                super.onSubmit(target, form);
//                final ValueMap map = getModelObject();
                // to show when model is updated
//                map.put("textSeen", map.get("text"));
//                map.put("submitSeen", "true");
                final FileUpload upload = uploadField.getFileUpload();
//                map.put("haveUpload", upload != null);
                if (upload != null) {

                    String fileName = upload.getClientFileName();
                    if (fileName.endsWith(".properties")) {

                        Properties properties = new Properties();
                        try (EtcdProxy p = proxyFactory.getEtcdProxy(registry.getObject())) {
                            properties.load(upload.getInputStream());

                            List<EtcdNode> entries = new ArrayList<>();

                            properties.forEach((key, value) -> {

                                p.saveNode(
                                        new EtcdNode((String) model.getObject().getKey() + "/" + key,
                                                (String) value));
                            });

                            success("Added keys: " + properties);

                        } catch (EtcdException e) {
                            System.err.println("Caught error: " + e);
                            error(e.toString());
                            error(" - API error: " + e.getApiError());
                            error(" - " + e.getCause());
                        } catch (Exception e) {
                            System.err.println("Caught error: " + e);
                            error(e.toString());
                            error("error: " + e.getMessage());
                            error(" - " + e.getCause());
                        }
                    }
                }

                onFileSaved(target);

                modalHide(target);
            }
        };

        form.add(ab);
        add(form);

        uploadField = new FileUploadField("file");
        form.add(uploadField);
        form.setMultiPart(true);
    }

    @Override
    public void beforeModalShow(AjaxRequestTarget target) {
        target.add(title, form);

        form.clearInput();
    }

    protected void onFileSaved(AjaxRequestTarget properties) {
    }
}
