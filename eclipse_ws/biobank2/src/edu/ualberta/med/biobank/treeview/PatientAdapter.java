package edu.ualberta.med.biobank.treeview;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.PatientVisitWrapper;
import edu.ualberta.med.biobank.common.wrappers.PatientWrapper;
import edu.ualberta.med.biobank.forms.PatientEntryForm;
import edu.ualberta.med.biobank.forms.PatientViewForm;
import edu.ualberta.med.biobank.forms.PatientVisitEntryForm;
import edu.ualberta.med.biobank.forms.input.FormInput;

public class PatientAdapter extends AdapterBase {

    private static Logger LOGGER = Logger.getLogger(PatientAdapter.class
        .getName());

    public PatientAdapter(AdapterBase parent, PatientWrapper patientWrapper) {
        this(parent, patientWrapper, true);
    }

    public PatientAdapter(AdapterBase parent, PatientWrapper patientWrapper,
        boolean enableActions) {
        super(parent, patientWrapper, enableActions);
        setHasChildren(true);
    }

    public PatientWrapper getWrapper() {
        return (PatientWrapper) modelObject;
    }

    @Override
    public String getName() {
        PatientWrapper patientWrapper = getWrapper();
        Assert.isNotNull(patientWrapper.getWrappedObject(), "patient is null");
        return patientWrapper.getNumber();
    }

    @Override
    public String getTitle() {
        return getTitle("Patient");
    }

    @Override
    public void performDoubleClick() {
        performExpand();
        openForm(new FormInput(this), PatientViewForm.ID);
    }

    @Override
    public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {
        addEditMenu(menu, "Patient", PatientEntryForm.ID);
        addViewMenu(menu, "Patient", PatientViewForm.ID);

        if (enableActions) {
            MenuItem mi = new MenuItem(menu, SWT.PUSH);
            mi.setText("Add Patient Visit");
            mi.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    PatientVisitAdapter adapter = new PatientVisitAdapter(
                        PatientAdapter.this, new PatientVisitWrapper(
                            getAppService()));
                    adapter.getWrapper().setPatient(getWrapper());
                    openForm(new FormInput(adapter), PatientVisitEntryForm.ID);
                }
            });
        }
    }

    @Override
    public void loadChildren(boolean updateNode) {
        try {
            PatientWrapper patientWrapper = getWrapper();
            // read from database again
            patientWrapper.reload();

            List<PatientVisitWrapper> visits = patientWrapper
                .getPatientVisitCollection();
            for (PatientVisitWrapper visit : visits) {
                PatientVisitAdapter node = (PatientVisitAdapter) getChild(visit
                    .getId());
                if (node == null) {
                    node = new PatientVisitAdapter(this, visit);
                    addChild(node);
                }
                if (updateNode) {
                    SessionManager.updateTreeNode(node);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while loading children of patient "
                + getWrapper().getNumber(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public AdapterBase accept(NodeSearchVisitor visitor) {
        return visitor.visit(this);
    }
}