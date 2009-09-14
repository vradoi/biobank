package edu.ualberta.med.biobank.treeview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.utils.ModelUtils;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.forms.input.FormInput;
import gov.nih.nci.system.applicationservice.WritableApplicationService;

/**
 * Base class for all "Session" tree view nodes. Generally, most of the nodes in
 * the tree are adapters for classes in the ORM model.
 */
public abstract class AdapterBase {

    protected IDeltaListener listener = NullDeltaListener.getSoleInstance();

    private Object wrappedObject;

    private Class<?> wrappedObjectClass;

    private Integer id;

    private String name;

    protected AdapterBase parent;

    protected boolean hasChildren;

    protected List<AdapterBase> children;

    public AdapterBase(AdapterBase parent, Object wrappedObject,
        Class<?> wrappedObjectClass) {
        this.wrappedObject = wrappedObject;
        this.wrappedObjectClass = wrappedObjectClass;
        this.parent = parent;
        children = new ArrayList<AdapterBase>();
        if (parent != null) {
            addListener(parent.listener);
        }

        Assert.isTrue(integrityCheck(), "integrity checks failed");
    }

    public AdapterBase(AdapterBase parent, Object modelObject,
        Class<?> modelClass, int id, String name) {
        this(parent, modelObject, modelClass);
        setId(id);
        setName(name);
    }

    public AdapterBase(AdapterBase parent, Object modelObject,
        Class<?> modelClass, int id, String name, boolean hasChildren) {
        this(parent, modelObject, modelClass, id, name);
        setHasChildren(hasChildren);
    }

    protected Object getWrappedObject() {
        return wrappedObject;
    }

    protected void setWrappedObject(Object object, Class<?> klass) {
        wrappedObject = object;
        this.wrappedObjectClass = klass;
    }

    protected Class<?> getWrappedObjectClass() {
        return wrappedObjectClass;
    }

    protected abstract Integer getWrappedObjectId();

    protected abstract boolean integrityCheck();

    public void setParent(AdapterBase parent) {
        this.parent = parent;
    }

    public AdapterBase getParent() {
        return parent;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract String getTitle();

    protected String getTitle(String string) {
        String name = getName();
        if (name == null) {
            return "New " + string;
        }
        return string + " " + name;
    }

    public List<AdapterBase> getItems() {
        return children;
    }

    public List<AdapterBase> getChildren() {
        return children;
    }

    public AdapterBase getChild(int id) {
        return getChild(id, false);
    }

    public AdapterBase getChild(int id, boolean reloadChildren) {
        if (reloadChildren) {
            loadChildren(false);
        }
        if (children.size() == 0)
            return null;

        for (AdapterBase child : children) {
            if (child.getId() == id)
                return child;
        }
        return null;
    }

    public void addChild(AdapterBase child) {
        hasChildren = true;
        AdapterBase existingNode = contains(child);
        if (existingNode != null) {
            // don't add - assume our model is up to date
            return;
        }

        AdapterBase namedChild = getChildByName(child.getName());
        if (namedChild != null) {
            // may have inserted a new object into database
            // replace current object with new one
            int index = children.indexOf(namedChild);
            children.remove(index);
        }

        child.setParent(this);
        children.add(child);
        child.addListener(listener);
        fireAdd(child);
    }

    public void insertAfter(AdapterBase existingNode, AdapterBase newNode) {
        int pos = children.indexOf(existingNode);
        Assert.isTrue(pos >= 0, "existing node not found: "
            + existingNode.getName());
        newNode.setParent(this);
        children.add(pos + 1, newNode);
        newNode.addListener(listener);
        fireAdd(newNode);
    }

    public void removeChild(AdapterBase item) {
        if (children.size() == 0)
            return;

        AdapterBase itemToRemove = null;

        for (AdapterBase child : children) {
            if ((child.getId() == item.getId())
                && child.getName().equals(item.getName()))
                itemToRemove = child;
        }

        if (itemToRemove != null) {
            children.remove(itemToRemove);
            fireRemove(itemToRemove);
        }
    }

    public void removeByName(String name) {
        if (children.size() == 0)
            return;

        AdapterBase itemToRemove = null;

        for (AdapterBase child : children) {
            if (child.getName().equals(name))
                itemToRemove = child;
        }

        if (itemToRemove != null) {
            children.remove(itemToRemove);
            fireRemove(itemToRemove);
        }
    }

    public void removeAll() {
        for (AdapterBase child : new ArrayList<AdapterBase>(getChildren())) {
            removeChild(child);
        }
    }

    public AdapterBase contains(AdapterBase item) {
        if (children.size() == 0)
            return null;

        for (AdapterBase child : children) {
            if ((child.getId() == item.getId())
                && child.getName().equals(item.getName()))
                return child;
        }
        return null;
    }

    public AdapterBase getChildByName(String name) {
        if (children.size() == 0)
            return null;

        for (AdapterBase child : children) {
            if ((child.getName() != null) && child.getName().equals(name))
                return child;
        }
        return null;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public boolean hasChildren() {
        return hasChildren;
    }

    public WritableApplicationService getAppService() {
        Assert.isNotNull(parent, "parent is null");
        return parent.getAppService();
    }

    public void addListener(IDeltaListener listener) {
        this.listener = listener;
    }

    public void removeListener(IDeltaListener listener) {
        if (this.listener.equals(listener)) {
            this.listener = NullDeltaListener.getSoleInstance();
        }
    }

    protected void fireAdd(Object added) {
        listener.add(new DeltaEvent(added));
    }

    protected void fireRemove(Object removed) {
        listener.remove(new DeltaEvent(removed));
    }

    public abstract void performDoubleClick();

    public void performExpand() {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                loadChildren(true);
                getRootNode().expandChild(AdapterBase.this);
            }
        });
    }

    public abstract void popupMenu(TreeViewer tv, Tree tree, Menu menu);

    /**
     * Called to load it's children;
     * 
     * @param updateNode If not null, the node in the treeview to update.
     */
    public abstract void loadChildren(boolean updateNode);

    public static void closeEditor(FormInput input) {
        IWorkbenchPage page = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getActivePage();
        IEditorPart part = page.findEditor(input);
        if (part != null) {
            page.closeEditor(part, true);
        }
    }

    public static void openForm(FormInput input, String id) {
        closeEditor(input);
        try {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().openEditor(input, id, true);
        } catch (PartInitException e) {
            SessionManager.getLogger()
                .error("Can't open form with id " + id, e);
        }
    }

    @SuppressWarnings("unchecked")
    public <E> E getParentFromClass(Class<E> parentClass) {
        AdapterBase node = this;
        while (node != null) {
            if (node.getClass().equals(parentClass)) {
                return (E) node;
            } else {
                node = node.getParent();
            }
        }
        return null;
    }

    public abstract AdapterBase accept(NodeSearchVisitor visitor);

    public String getTreeText() {
        return getName();
    }

    public RootNode getRootNode() {
        return getParentFromClass(RootNode.class);
    }

    public Object loadWrappedObject() throws Exception {
        Object realObject = (ModelWrapper<?>) wrappedObject;
        Class<?> realObjectClass = wrappedObject.getClass();
        Assert.isNotNull(realObjectClass, "model class is null");

        Integer id = getWrappedObjectId();
        // if object is not stored in the database it cannot be loaded
        if (id == null)
            return realObject;

        wrappedObject = ModelUtils.getObjectWithId(getAppService(),
            wrappedObjectClass, id);
        Assert.isNotNull(realObject, "model object not in database");
        return realObject;
    }

    public void rebuild() {
        removeAll();
        loadChildren(false);
    }
}
