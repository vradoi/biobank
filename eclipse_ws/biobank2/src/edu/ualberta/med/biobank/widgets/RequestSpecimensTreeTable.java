package edu.ualberta.med.biobank.widgets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import edu.ualberta.med.biobank.BiobankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.util.RequestSpecimenState;
import edu.ualberta.med.biobank.common.wrappers.ItemWrapper;
import edu.ualberta.med.biobank.common.wrappers.RequestSpecimenWrapper;
import edu.ualberta.med.biobank.common.wrappers.RequestWrapper;
import edu.ualberta.med.biobank.forms.utils.RequestTableGroup;
import edu.ualberta.med.biobank.treeview.Node;
import edu.ualberta.med.biobank.treeview.RootNode;
import edu.ualberta.med.biobank.treeview.TreeItemAdapter;
import edu.ualberta.med.biobank.treeview.admin.RequestContainerAdapter;

public class RequestSpecimensTreeTable extends BiobankWidget {

    private TreeViewer tv;
    private RequestWrapper shipment;
    protected List<Node> groups;
    private Boolean selecting = false;

    public RequestSpecimensTreeTable(Composite parent, RequestWrapper shipment) {
        super(parent, SWT.NONE);

        this.shipment = shipment;

        setLayout(new GridLayout(1, false));
        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.FILL;
        gd.grabExcessHorizontalSpace = true;
        gd.heightHint = 500;
        setLayoutData(gd);

        tv = new TreeViewer(this, SWT.MULTI | SWT.BORDER);
        Tree tree = tv.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);
        tv.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (!selecting) {
                    selecting = true;
                    List<Node> nodes = getSelectionAdapters(((StructuredSelection) tv
                        .getSelection()).toList());
                    tv.setSelection(new StructuredSelection(nodes));
                }
                selecting = false;
            }

        });

        TreeColumn tc = new TreeColumn(tree, SWT.LEFT);
        tc.setText("Inventory Id");
        tc.setWidth(300);

        tc = new TreeColumn(tree, SWT.LEFT);
        tc.setText("Type");
        tc.setWidth(100);

        tc = new TreeColumn(tree, SWT.LEFT);
        tc.setText("Location");
        tc.setWidth(120);

        tc = new TreeColumn(tree, SWT.LEFT);
        tc.setText("Claimed By");
        tc.setWidth(100);

        tc = new TreeColumn(tree, SWT.LEFT);
        tc.setText("State");
        tc.setWidth(100);

        ITreeContentProvider contentProvider = new ITreeContentProvider() {
            @Override
            public void dispose() {
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput,
                Object newInput) {
                groups = RequestTableGroup
                    .getGroupsForShipment(RequestSpecimensTreeTable.this.shipment);
            }

            @Override
            public Object[] getElements(Object inputElement) {
                return groups.toArray();
            }

            @Override
            public Object[] getChildren(Object parentElement) {
                return ((Node) parentElement).getChildren().toArray();
            }

            @Override
            public Object getParent(Object element) {
                return ((Node) element).getParent();
            }

            @Override
            public boolean hasChildren(Object element) {
                return ((Node) element).getChildren() == null ? false
                    : ((Node) element).getChildren().size() > 0;
            }
        };
        tv.setContentProvider(contentProvider);

        final BiobankLabelProvider labelProvider = new BiobankLabelProvider() {
            @Override
            public String getColumnText(Object element, int columnIndex) {
                if (element instanceof RequestTableGroup) {
                    if (columnIndex == 0)
                        return ((RequestTableGroup) element).getTitle();
                    return "";
                } else if (element instanceof RequestContainerAdapter) {
                    if (columnIndex == 0)
                        return ((RequestContainerAdapter) element)
                            .getLabelInternal();
                    return "";
                } else if (element instanceof Node) {
                    if (columnIndex < 3)
                        return ((TreeItemAdapter) element)
                            .getColumnText(columnIndex);
                    if (columnIndex == 3)
                        return ((RequestSpecimenWrapper) ((TreeItemAdapter) element)
                            .getSpecimen()).getClaimedBy();
                    else if (columnIndex == 4) {
                        return RequestSpecimenState
                            .getState(
                                ((RequestSpecimenWrapper) ((TreeItemAdapter) element)
                                    .getSpecimen()).getState()).getLabel();
                    } else
                        return "";
                }
                return "";
            }
        };
        tv.setLabelProvider(labelProvider);
        tv.setInput(new RootNode());

        tv.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                Object o = ((IStructuredSelection) tv.getSelection())
                    .getFirstElement();
                if (o instanceof TreeItemAdapter) {
                    ItemWrapper ra = ((TreeItemAdapter) o).getSpecimen();
                    SessionManager.openViewForm(ra.getSpecimen());
                }
            }
        });

        final Menu menu = new Menu(this);
        tv.getTree().setMenu(menu);

        menu.addListener(SWT.Show, new Listener() {
            @Override
            public void handleEvent(Event event) {
                for (MenuItem menuItem : menu.getItems()) {
                    menuItem.dispose();
                }
                addSetUnavailableMenu(menu);
                addClaimMenu(menu);
            }
        });
        GridData gdtree = new GridData();
        gdtree.grabExcessHorizontalSpace = true;
        gdtree.horizontalAlignment = SWT.FILL;
        gdtree.verticalAlignment = SWT.FILL;
        gdtree.grabExcessVerticalSpace = true;
        tv.getTree().setLayoutData(gdtree);

    }

    protected void addClaimMenu(Menu menu) {
        MenuItem item;
        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Claim");
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                claim(getSelectionWrappers());
                refresh();
            }
        });
    }

    protected void claim(Object ob) {
        try {
            if (ob instanceof TreeItemAdapter) {
                RequestSpecimenWrapper a = (RequestSpecimenWrapper) ((TreeItemAdapter) ob)
                    .getSpecimen();
                a.setClaimedBy(SessionManager.getUser().getFirstName());
                a.persist();
            }
        } catch (Exception e) {
            BiobankPlugin.openAsyncError("Failed to claim", e);
        }
    }

    private void addSetUnavailableMenu(final Menu menu) {
        MenuItem item;
        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Flag as unavailable");
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                for (RequestSpecimenWrapper spec : getSelectionWrappers()) {
                    spec.setState(RequestSpecimenState.UNAVAILABLE_STATE
                        .getId());
                    try {
                        spec.persist();
                    } catch (Exception e) {
                        BiobankPlugin.openAsyncError("Save Error", e);
                    }
                }
                refresh();
            }
        });
    }

    public List<RequestSpecimenWrapper> getSelectionWrappers() {
        List<RequestSpecimenWrapper> wrappers = new ArrayList<RequestSpecimenWrapper>();
        for (Node adapter : getSelectionAdapters(((StructuredSelection) tv
            .getSelection()).toList())) {
            if (adapter instanceof TreeItemAdapter)
                wrappers
                    .add((RequestSpecimenWrapper) ((TreeItemAdapter) adapter)
                        .getSpecimen());
        }
        return wrappers;
    }

    public List<Node> getSelectionAdapters(List<Node> sel) {
        HashSet<Node> adapters = new HashSet<Node>();
        for (Object o : sel) {
            adapters.add((Node) o);
            adapters.addAll(getSelectionAdapters(((Node) o).getChildren()));
        }
        return new ArrayList<Node>(adapters);
    }

    public void refresh() {
        tv.setInput("refresh");
    }

}
