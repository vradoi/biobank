package edu.ualberta.med.biobank.treeview;

import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.ContainerPositionWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.dialogs.MoveContainerDialog;
import edu.ualberta.med.biobank.forms.ContainerEntryForm;
import edu.ualberta.med.biobank.forms.ContainerViewForm;
import edu.ualberta.med.biobank.forms.input.FormInput;
import edu.ualberta.med.biobank.model.Container;
import edu.ualberta.med.biobank.model.ContainerPosition;

public class ContainerAdapter extends AdapterBase {

    public ContainerAdapter(AdapterBase parent, ContainerWrapper container) {
        super(parent, container, Container.class);
        setHasChildren(container.getChildPositionCollection() != null
            && ((Collection<ContainerPosition>) container
                .getChildPositionCollection()).size() > 0);
    }

    @Override
    protected Integer getWrappedObjectId() {
        return getContainer().getId();
    }

    public ContainerWrapper getContainer() {
        return (ContainerWrapper) getWrappedObject();
    }

    public void setContainer(ContainerWrapper container) {
        setWrappedObject(container, ContainerWrapper.class);
    }

    @Override
    public Integer getId() {
        ContainerWrapper container = getContainer();
        Assert.isNotNull(container, "container is null");
        return container.getId();
    }

    @Override
    public String getName() {
        ContainerWrapper container = getContainer();
        Assert.isNotNull(container, "container is null");
        if (container.getContainerType() == null) {
            return container.getLabel();
        }
        return container.getLabel() + " ("
            + container.getContainerType().getName() + ")";
    }

    @Override
    public String getTitle() {
        return getTitle("Container");
    }

    @Override
    public void performDoubleClick() {
        openForm(new FormInput(this), ContainerViewForm.ID);
        performExpand();
    }

    @Override
    public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {
        MenuItem mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Edit Container");
        mi.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                openForm(new FormInput(ContainerAdapter.this),
                    ContainerEntryForm.ID);
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("View Container");
        mi.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                openForm(new FormInput(ContainerAdapter.this),
                    ContainerViewForm.ID);
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Move Container");
        mi.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                MoveContainerDialog mc = new MoveContainerDialog(PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow().getShell(),
                    getContainer().getWrappedObject());

                if (mc.open() == Dialog.OK) {
                    setContainer(new ContainerWrapper(SessionManager
                        .getAppService(), mc.getContainer()));
                    try {
                        getContainer().setNewPositionFromLabel(mc.getAddress());
                        // UPDATE TREE... difficult to know which adapter we
                        // need to update
                    } catch (Exception e) {
                        BioBankPlugin.openError(e.getMessage(), e);
                        e.printStackTrace();
                    }
                }

            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
    }

    @Override
    public void loadChildren(boolean updateNode) {
        try {
            Collection<ContainerPosition> positions = getContainer()
                .getChildPositionCollection();
            if (positions != null) {
                // read from database again
                for (ContainerPosition childPosition : positions) {
                    ContainerWrapper child = new ContainerWrapper(
                        getAppService(), (new ContainerPositionWrapper(
                            getAppService(), childPosition)).getContainer());
                    ContainerAdapter node = (ContainerAdapter) getChild(child
                        .getId());

                    if (node == null) {
                        node = new ContainerAdapter(this, child);
                        addChild(node);
                    }
                    if (updateNode) {
                        SessionManager.getInstance().getTreeViewer().update(
                            node, null);
                    }
                }
            } else
                throw new Exception("Children null.");
        } catch (Exception e) {
            SessionManager.getLogger().error(
                "Error while loading storage container group children for storage container "
                    + getContainer().getLabel(), e);
        }
    }

    @Override
    public AdapterBase accept(NodeSearchVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public String getTreeText() {
        return getName();
    }

    @Override
    protected boolean integrityCheck() {
        ContainerWrapper c = getContainer();
        if (c != null)
            if ((c.getContainerType() != null && c.getContainerType()
                .getCapacity() != null)
                || c.getContainerType() == null)
                if ((c.getPosition() != null
                    && c.getPosition().getRow() != null && c.getPosition()
                    .getCol() != null)
                    || c.getPosition() == null)
                    if (c.getSite() != null)
                        return true;
        return false;

    }
}
