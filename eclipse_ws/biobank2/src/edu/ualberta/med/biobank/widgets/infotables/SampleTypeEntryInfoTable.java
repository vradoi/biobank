package edu.ualberta.med.biobank.widgets.infotables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.springframework.remoting.RemoteConnectFailureException;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.SampleTypeWrapper;
import edu.ualberta.med.biobank.dialogs.SampleTypeDialog;

/**
 * Displays the current sample storage collection and allows the user to add
 * additional sample storage to the collection.
 */
public class SampleTypeEntryInfoTable extends SampleTypeInfoTable {

    private static Logger LOGGER = Logger
        .getLogger(SampleTypeEntryInfoTable.class.getName());

    private List<SampleTypeWrapper> selectedSampleTypes;

    private List<SampleTypeWrapper> conflictTypes;

    private List<SampleTypeWrapper> addedOrModifiedSampleTypes;

    private List<SampleTypeWrapper> deletedSampleTypes;

    private String editMessage;

    /**
     * 
     * @param parent a composite control which will be the parent of the new
     *            instance (cannot be null)
     * @param SampleTypeCollection the sample storage already selected and to be
     *            displayed in the table viewer (can be null).
     */
    public SampleTypeEntryInfoTable(Composite parent,
        List<SampleTypeWrapper> sampleTypeCollection,
        List<SampleTypeWrapper> conflictTypes, String editMessage) {
        super(parent, null);
        setLists(sampleTypeCollection, conflictTypes);
        this.editMessage = editMessage;
        addEditSupport();
    }

    /**
     * 
     * @param message The message to display in the SampleTypeDialog.
     */
    public void addSampleType(String message) {
        addOrEditSampleType(true, new SampleTypeWrapper(SessionManager
            .getAppService()), getRestrictedTypes(), message);
    }

    private boolean addOrEditSampleType(boolean add,
        SampleTypeWrapper sampleType, Set<SampleTypeWrapper> restrictedTypes,
        String message) {
        SampleTypeDialog dlg = new SampleTypeDialog(PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getShell(), sampleType, message);
        if (dlg.open() == Dialog.OK) {
            if (addEditOk(sampleType, restrictedTypes)) {
                if (add) {
                    // only add to the collection when adding and not editing
                    selectedSampleTypes.add(dlg.getSampleType());
                }
                setCollection(selectedSampleTypes);
                addedOrModifiedSampleTypes.add(dlg.getSampleType());
                notifyListeners();
                return true;
            }
            BioBankPlugin.openAsyncError("Name Problem",
                "A type with the same name or short name already exists.");
        }
        return false;
    }

    // need sample types that have not yet been selected in sampleStorageTable
    private Set<SampleTypeWrapper> getRestrictedTypes() {
        Set<SampleTypeWrapper> restrictedTypes = new HashSet<SampleTypeWrapper>(
            conflictTypes);
        Collection<SampleTypeWrapper> currentSampleTypes = getCollection();
        restrictedTypes.addAll(currentSampleTypes);
        return restrictedTypes;
    }

    private void addEditSupport() {
        addEditItemListener(new IInfoTableEditItemListener() {
            @Override
            public void editItem(InfoTableEvent event) {
                SampleTypeWrapper pvss = getSelection();
                Set<SampleTypeWrapper> restrictedTypes = getRestrictedTypes();
                restrictedTypes.remove(pvss);
                addOrEditSampleType(false, pvss, restrictedTypes, editMessage);
            }
        });

        addDeleteItemListener(new IInfoTableDeleteItemListener() {
            @Override
            public void deleteItem(InfoTableEvent event) {
                SampleTypeWrapper sampleType = getSelection();

                try {
                    if (sampleType.isUsedBySamples()) {
                        BioBankPlugin.openError("Sample Type Delete Error",
                            "Cannot delete sample type \""
                                + sampleType.getName()
                                + "\" since there are samples of this "
                                + "type already in the database.");
                        return;
                    }

                    if (!MessageDialog.openConfirm(PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow().getShell(),
                        "Delete Sample Type",
                        "Are you sure you want to delete sample type \""
                            + sampleType.getName() + "\"?")) {
                        return;
                    }

                    // equals method now compare toString() results if both
                    // ids are null.
                    selectedSampleTypes.remove(sampleType);

                    setCollection(selectedSampleTypes);
                    deletedSampleTypes.add(sampleType);
                    notifyListeners();
                } catch (final RemoteConnectFailureException exp) {
                    BioBankPlugin.openRemoteConnectErrorMessage();
                } catch (Exception e) {
                    LOGGER.error("BioBankFormBase.createPartControl Error", e);
                }
            }
        });
    }

    private boolean addEditOk(SampleTypeWrapper type,
        Set<SampleTypeWrapper> restrictedTypes) {
        for (SampleTypeWrapper st : restrictedTypes) {
            if (st.getName().equals(type.getName())
                || st.getNameShort().equals(type.getNameShort())) {
                return false;
            }
        }
        return true;
    }

    public List<SampleTypeWrapper> getAddedOrModifiedSampleTypes() {
        return addedOrModifiedSampleTypes;
    }

    public List<SampleTypeWrapper> getDeletedSampleTypes() {
        return deletedSampleTypes;
    }

    public void setLists(List<SampleTypeWrapper> sampleTypeCollection,
        List<SampleTypeWrapper> conflictTypes) {
        this.conflictTypes = conflictTypes;
        if (conflictTypes == null) {
            conflictTypes = new ArrayList<SampleTypeWrapper>();
        }

        if (sampleTypeCollection == null) {
            selectedSampleTypes = new ArrayList<SampleTypeWrapper>();
        } else {
            selectedSampleTypes = new ArrayList<SampleTypeWrapper>(
                sampleTypeCollection);
        }
        Collections.sort(selectedSampleTypes);
        setCollection(sampleTypeCollection);
        addedOrModifiedSampleTypes = new ArrayList<SampleTypeWrapper>();
        deletedSampleTypes = new ArrayList<SampleTypeWrapper>();
    }
}
