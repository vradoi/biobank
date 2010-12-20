package edu.ualberta.med.biobank.treeview.request;

import java.util.Collection;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;

import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.RequestWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.treeview.AdapterBase;

public class AcceptedRequestNode extends AdapterBase {

    private SiteWrapper site;
    private static final String NODE_INFO = "Requests that have been accepted by the site and are currently being processed";

    public AcceptedRequestNode(AdapterBase parent, int id, SiteWrapper site) {
        super(parent, id, "Accepted", true, false);
        this.site = site;
    }

    @Override
    protected String getLabelInternal() {
        return NODE_INFO;
    }

    @Override
    public String getTooltipText() {
        return NODE_INFO;
    }

    @Override
    public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {
    }

    @Override
    protected AdapterBase createChildNode() {
        return new RequestAdapter(this, null);
    }

    @Override
    protected AdapterBase createChildNode(ModelWrapper<?> child) {
        return new RequestAdapter(this, (RequestWrapper) child);
    }

    @Override
    protected Collection<? extends ModelWrapper<?>> getWrapperChildren()
        throws Exception {
        site.reset();
        return site.getAcceptedRequestCollection();
    }

    @Override
    public String getViewFormId() {
        return null;
    }

    @Override
    public String getEntryFormId() {
        return null;
    }

    @Override
    public AdapterBase search(Object searchedObject) {
        return searchChildren(searchedObject);
    }

    @Override
    protected int getWrapperChildCount() throws Exception {
        return getWrapperChildren().size();
    }

}
