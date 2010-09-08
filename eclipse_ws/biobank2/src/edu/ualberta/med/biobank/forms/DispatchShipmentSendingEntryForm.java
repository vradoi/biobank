package edu.ualberta.med.biobank.forms;

import java.util.Date;
import java.util.List;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.ActivityStatusWrapper;
import edu.ualberta.med.biobank.common.wrappers.AliquotWrapper;
import edu.ualberta.med.biobank.common.wrappers.DispatchShipmentWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.common.wrappers.StudyWrapper;
import edu.ualberta.med.biobank.logs.BiobankLogger;
import edu.ualberta.med.biobank.treeview.DispatchShipmentAdapter;
import edu.ualberta.med.biobank.widgets.BiobankText;
import edu.ualberta.med.biobank.widgets.DateTimeWidget;
import edu.ualberta.med.biobank.widgets.infotables.AliquotListInfoTable;

public class DispatchShipmentSendingEntryForm extends BiobankEntryForm {

    private static BiobankLogger logger = BiobankLogger
        .getLogger(DispatchShipmentSendingEntryForm.class.getName());

    public static final String ID = "edu.ualberta.med.biobank.forms.DispatchShipmentSendingEntryForm";

    public static final String MSG_NEW_SHIPMENT_OK = "Creating a new dispatch shipment record.";

    public static final String MSG_SHIPMENT_OK = "Editing an existing dispatch shipment record.";

    private SiteWrapper site;

    private DispatchShipmentWrapper shipment;

    private ComboViewer studyComboViewer;

    private ComboViewer destSiteComboViewer;

    private DateTimeWidget dateShippedWidget;

    private List<AliquotWrapper> aliquots;

    private AliquotListInfoTable aliquotsWidget;

    @Override
    protected void init() throws Exception {
        Assert.isNotNull(adapter, "Adapter should be no null");
        Assert.isTrue((adapter instanceof DispatchShipmentAdapter),
            "Invalid editor input: object of type "
                + adapter.getClass().getName());

        shipment = (DispatchShipmentWrapper) adapter.getModelObject();
        site = SessionManager.getInstance().getCurrentSite();
        try {
            shipment.reload();
        } catch (Exception e) {
            logger.error("Error while retrieving shipment", e);
        }
        String tabName;
        if (shipment.isNew()) {
            tabName = "New Dispatch Shipment";
            shipment.setActivityStatus(ActivityStatusWrapper
                .getActiveActivityStatus(appService));
        } else {
            tabName = "Dispatch Shipment "
                + shipment.getFormattedDateReceived();
        }
        setPartName(tabName);
    }

    @Override
    protected void createFormContent() throws Exception {
        form.setText("Shipment Information");
        form.setMessage(getOkMessage(), IMessageProvider.NONE);
        page.setLayout(new GridLayout(1, false));
        form.setImage(BioBankPlugin.getDefault().getImageRegistry()
            .get(BioBankPlugin.IMG_CLINIC_SHIPMENT));

        Composite client = toolkit.createComposite(page);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolkit.paintBordersFor(client);

        BiobankText siteLabel = createReadOnlyLabelledField(client, SWT.NONE,
            "Sender Site");
        setTextValue(siteLabel, site.getName());

        List<StudyWrapper> possibleStudies = site.getDispatchStudies();
        StudyWrapper study = shipment.getStudy();

        if ((study == null) && (possibleStudies != null)
            && (possibleStudies.size() == 1)) {
            study = possibleStudies.get(0);
        }

        studyComboViewer = createComboViewerWithNoSelectionValidator(client,
            "Study", possibleStudies, study,
            "Shipment must have a receiving site");

        studyComboViewer
            .addSelectionChangedListener(new ISelectionChangedListener() {
                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    IStructuredSelection studySelection = (IStructuredSelection) studyComboViewer
                        .getSelection();
                    if ((studySelection != null) && (studySelection.size() > 0)) {
                        StudyWrapper study = (StudyWrapper) studySelection
                            .getFirstElement();
                        try {
                            List<SiteWrapper> possibleDestSites = site
                                .getStudyDispachSites(study);
                            destSiteComboViewer.setInput(possibleDestSites);

                            if (possibleDestSites.size() == 1) {
                                destSiteComboViewer
                                    .setSelection(new StructuredSelection(
                                        possibleDestSites.get(0)));
                            }
                        } catch (Exception e) {
                            logger
                                .error(
                                    "Error while retrieving dispatch shipment destination sites",
                                    e);
                        }
                    }

                }
            });

        List<SiteWrapper> possibleDestSites = null;
        SiteWrapper destSite = null;

        if (study != null) {
            possibleDestSites = site.getStudyDispachSites(study);
            destSite = shipment.getReceiver();
        }

        if ((destSite == null) && (possibleDestSites != null)
            && (possibleDestSites.size() == 1)) {
            destSite = possibleDestSites.get(0);
        }

        destSiteComboViewer = createComboViewerWithNoSelectionValidator(client,
            "Receiver Site", possibleDestSites, destSite,
            "Shipment must have an associated study");

        createBoundWidgetWithLabel(client, BiobankText.class, SWT.NONE,
            "Waybill", null,
            BeansObservables.observeValue(shipment, "waybill"), null);

        dateShippedWidget = createDateTimeWidget(client, "Date Shipped",
            shipment.getDateShipped(),
            BeansObservables.observeValue(shipment, "dateShipped"),
            "Date shipped should be set");

        createAliquotsSection();
        setFirstControl(studyComboViewer.getControl());

        if ((possibleStudies == null) || (possibleStudies.size() == 0)) {
            BioBankPlugin.openAsyncError("Sender Site Error",
                "The current site does not have any dispatch studies associated with it.\n"
                    + "Please close the form.");
        }
    }

    private void createAliquotsSection() {
        Composite parent = createSectionWithClient("Aliquots");
        aliquotsWidget = new AliquotListInfoTable(parent, aliquots,
            AliquotListInfoTable.ColumnsShown.PNUMBER);
        aliquotsWidget.adaptToToolkit(toolkit, true);
        aliquotsWidget.addDoubleClickListener(collectionDoubleClickListener);
    }

    @Override
    protected void saveForm() throws Exception {

        StudyWrapper study = null;
        IStructuredSelection studySelection = (IStructuredSelection) studyComboViewer
            .getSelection();
        if ((studySelection != null) && (studySelection.size() > 0)) {
            study = (StudyWrapper) studySelection.getFirstElement();
        }
        shipment.setStudy(study);

        SiteWrapper destSite = null;
        IStructuredSelection destSiteSelecion = (IStructuredSelection) destSiteComboViewer
            .getSelection();
        if ((destSiteSelecion != null) && (destSiteSelecion.size() > 0)) {
            destSite = (SiteWrapper) destSiteSelecion.getFirstElement();
        }
        shipment.setReceiver(destSite);
    }

    @Override
    protected String getOkMessage() {
        return (shipment.isNew()) ? MSG_NEW_SHIPMENT_OK : MSG_SHIPMENT_OK;
    }

    @Override
    public String getNextOpenedFormID() {
        return null;
    }

    @Override
    public void reset() throws Exception {
        StudyWrapper study = shipment.getStudy();
        if (study != null) {
            studyComboViewer.setSelection(new StructuredSelection(study));
        } else if (studyComboViewer.getCombo().getItemCount() > 1) {
            studyComboViewer.getCombo().deselectAll();
        }
        SiteWrapper destSite = shipment.getReceiver();
        if (destSite != null) {
            destSiteComboViewer.setSelection(new StructuredSelection(destSite));
        } else if (destSiteComboViewer.getCombo().getItemCount() > 1) {
            destSiteComboViewer.getCombo().deselectAll();
        }
        dateShippedWidget.setDate(new Date());
    }
}
