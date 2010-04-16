package edu.ualberta.med.biobank.widgets.infotables.entry;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.springframework.remoting.RemoteConnectFailureException;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.BiobankCheckException;
import edu.ualberta.med.biobank.common.wrappers.SampleTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.dialogs.SampleTypeDialog;
import edu.ualberta.med.biobank.logs.BiobankLogger;
import edu.ualberta.med.biobank.widgets.infotables.IInfoTableAddItemListener;
import edu.ualberta.med.biobank.widgets.infotables.IInfoTableDeleteItemListener;
import edu.ualberta.med.biobank.widgets.infotables.IInfoTableEditItemListener;
import edu.ualberta.med.biobank.widgets.infotables.InfoTableEvent;
import edu.ualberta.med.biobank.widgets.infotables.SampleTypeInfoTable;
import gov.nih.nci.system.applicationservice.ApplicationException;

/**
 * Displays the current sample storage collection and allows the user to add
 * additional sample storage to the collection.
 */
public class SampleTypeEntryInfoTable extends SampleTypeInfoTable {

    private static BiobankLogger logger = BiobankLogger
        .getLogger(SampleTypeEntryInfoTable.class.getName());

    private List<SampleTypeWrapper> selectedSampleTypes;

    private List<SampleTypeWrapper> addedOrModifiedSampleTypes;

    private List<SampleTypeWrapper> deletedSampleTypes;

    private String addMessage;

    private String editMessage;

    private SiteWrapper currentSite;

    /**
     * 
     * @param parent a composite control which will be the parent of the new
     *            instance (cannot be null)
     * @param SampleTypeCollection the sample storage already selected and to be
     *            displayed in the table viewer (can be null).
     */
    public SampleTypeEntryInfoTable(Composite parent,
        List<SampleTypeWrapper> sampleTypeCollection, String addMessage,
        String editMessage, SiteWrapper currentSite) {
        super(parent, null);
        setLists(sampleTypeCollection);
        this.addMessage = addMessage;
        this.editMessage = editMessage;
        this.currentSite = currentSite;
        addEditSupport();
    }

    @Override
    protected boolean isEditMode() {
        return true;
    }

    /**
     * 
     * @param message The message to display in the SampleTypeDialog.
     */
    public void addSampleType() {
        SampleTypeWrapper newST = new SampleTypeWrapper(SessionManager
            .getAppService());
        newST.setSite(currentSite);
        addOrEditSampleType(true, newST, addMessage);
    }

    private boolean addOrEditSampleType(boolean add,
        SampleTypeWrapper sampleType, String message) {
        SampleTypeDialog dlg = new SampleTypeDialog(PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getShell(), sampleType, message);
        if (dlg.open() == Dialog.OK) {
            if (addEditOk(sampleType)) {
                if (add) {
                    // only add to the collection when adding and not editing
                    selectedSampleTypes.add(sampleType);
                }
                reloadCollection(selectedSampleTypes);
                addedOrModifiedSampleTypes.add(sampleType);
                notifyListeners();
                return true;
            }
        }
        return false;
    }

    private void addEditSupport() {
        addAddItemListener(new IInfoTableAddItemListener() {
            @Override
            public void addItem(InfoTableEvent event) {
                addSampleType();
            }
        });

        addEditItemListener(new IInfoTableEditItemListener() {
            @Override
            public void editItem(InfoTableEvent event) {
                SampleTypeWrapper type = getSelection();
                addOrEditSampleType(false, type, editMessage);
            }
        });

        addDeleteItemListener(new IInfoTableDeleteItemListener() {
            @Override
            public void deleteItem(InfoTableEvent event) {
                SampleTypeWrapper sampleType = getSelection();

                try {
                    if (!sampleType.isNew() && sampleType.isUsedBySamples()) {
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
                    logger.error("BioBankFormBase.createPartControl Error", e);
                }
            }
        });
    }

    private boolean addEditOk(SampleTypeWrapper type) {
        try {
            type.checkNameAndShortNameUniquesForSiteAndGlobal();
        } catch (BiobankCheckException bce) {
            BioBankPlugin.openAsyncError("Check error", bce);
            return false;
        } catch (ApplicationException e) {
            BioBankPlugin.openAsyncError("Check error", e);
            return false;
        }
        return true;
    }

    public List<SampleTypeWrapper> getAddedOrModifiedSampleTypes() {
        return addedOrModifiedSampleTypes;
    }

    public List<SampleTypeWrapper> getDeletedSampleTypes() {
        return deletedSampleTypes;
    }

    public void setLists(List<SampleTypeWrapper> sampleTypeCollection) {
        if (sampleTypeCollection == null) {
            selectedSampleTypes = new ArrayList<SampleTypeWrapper>();
        } else {
            selectedSampleTypes = new ArrayList<SampleTypeWrapper>(
                sampleTypeCollection);
        }
        reloadCollection(sampleTypeCollection);
        addedOrModifiedSampleTypes = new ArrayList<SampleTypeWrapper>();
        deletedSampleTypes = new ArrayList<SampleTypeWrapper>();
    }

    public SiteWrapper getCurrentSite() {
        return currentSite;
    }

    public void reload() {
        if (currentSite != null) {
            setLists(currentSite.getSampleTypeCollection(true));
        }
    }
}
