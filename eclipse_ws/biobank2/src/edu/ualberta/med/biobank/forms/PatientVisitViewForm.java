package edu.ualberta.med.biobank.forms;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.common.wrappers.PatientVisitWrapper;
import edu.ualberta.med.biobank.common.wrappers.StudyWrapper;
import edu.ualberta.med.biobank.logs.BiobankLogger;
import edu.ualberta.med.biobank.model.PvAttrCustom;
import edu.ualberta.med.biobank.treeview.PatientVisitAdapter;
import edu.ualberta.med.biobank.widgets.infotables.PvSampleSourceInfoTable;
import edu.ualberta.med.biobank.widgets.infotables.SamplesListInfoTable;

public class PatientVisitViewForm extends BiobankViewForm {

    public static final String ID = "edu.ualberta.med.biobank.forms.PatientVisitViewForm";

    private static BiobankLogger logger = BiobankLogger
        .getLogger(PatientVisitViewForm.class.getName());

    private PatientVisitAdapter patientVisitAdapter;

    private PatientVisitWrapper patientVisit;

    private Text siteLabel;

    private SamplesListInfoTable samplesWidget;

    private List<FormPvCustomInfo> pvCustomInfoList;

    private Text clinicLabel;

    private Text shipmentWaybillLabel;

    private Text patientLabel;

    private Text dateProcessedLabel;

    private Text commentLabel;

    private Text usernameLabel;

    private class FormPvCustomInfo extends PvAttrCustom {
        Text widget;
    }

    @Override
    public void init() {
        Assert.isTrue((adapter instanceof PatientVisitAdapter),
            "Invalid editor input: object of type "
                + adapter.getClass().getName());

        patientVisitAdapter = (PatientVisitAdapter) adapter;
        patientVisit = patientVisitAdapter.getWrapper();
        retrievePatientVisit();

        setPartName("Visit " + patientVisit.getFormattedDateProcessed());
    }

    @Override
    protected void createFormContent() throws Exception {
        form.setText("Patient Visit - Date Processed: "
            + patientVisit.getFormattedDateProcessed());
        form.getBody().setLayout(new GridLayout(1, false));
        form.getBody().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        form.setImage(BioBankPlugin.getDefault().getImageRegistry().get(
            BioBankPlugin.IMG_PATIENT_VISIT));
        createMainSection();
        createSourcesSection();
        createSamplesSection();
    }

    private void createMainSection() throws Exception {
        Composite client = toolkit.createComposite(form.getBody());
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolkit.paintBordersFor(client);

        siteLabel = createReadOnlyField(client, SWT.NONE, "Site");
        clinicLabel = createReadOnlyField(client, SWT.NONE, "Clinic");
        shipmentWaybillLabel = createReadOnlyField(client, SWT.NONE,
            "Shipment Waybill");
        patientLabel = createReadOnlyField(client, SWT.NONE, "Patient");
        dateProcessedLabel = createReadOnlyField(client, SWT.NONE,
            "Date Processed");

        createPvDataSection(client);

        commentLabel = createReadOnlyField(client, SWT.WRAP, "Comments");
        usernameLabel = createReadOnlyField(client, SWT.None, "Creator");

        setPatientVisitValues();
    }

    private void createPvDataSection(Composite client) throws Exception {
        StudyWrapper study = patientVisit.getPatient().getStudy();
        String[] labels = study.getStudyPvAttrLabels();
        if (labels == null)
            return;

        pvCustomInfoList = new ArrayList<FormPvCustomInfo>();

        for (String label : labels) {
            FormPvCustomInfo combinedPvInfo = new FormPvCustomInfo();
            combinedPvInfo.setLabel(label);
            combinedPvInfo.setType(study.getStudyPvAttrType(label));

            int style = SWT.LEFT;
            if (combinedPvInfo.getType().equals("text")
                || combinedPvInfo.getType().equals("select_multiple")) {
                style |= SWT.WRAP;
            }

            String value = patientVisit.getPvAttrValue(label);
            if (combinedPvInfo.getType().equals("select_multiple")
                && (value != null)) {
                combinedPvInfo.setValue(value.replace(';', '\n'));
            } else {
                combinedPvInfo.setValue(value);
            }

            combinedPvInfo.widget = createReadOnlyField(client, style, label,
                combinedPvInfo.getValue());
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            combinedPvInfo.widget.setLayoutData(gd);

            pvCustomInfoList.add(combinedPvInfo);
        }
    }

    private void setPatientVisitValues() {
        setTextValue(siteLabel, patientVisit.getShipment().getClinic()
            .getSite().getName());
        setTextValue(clinicLabel, patientVisit.getShipment() == null ? ""
            : patientVisit.getShipment().getClinic().getName());
        setTextValue(shipmentWaybillLabel, patientVisit.getShipment()
            .getWaybill());
        setTextValue(patientLabel, patientVisit.getPatient().getPnumber());
        setTextValue(dateProcessedLabel, patientVisit
            .getFormattedDateProcessed());
        setTextValue(commentLabel, patientVisit.getComment());
        setTextValue(usernameLabel, patientVisit.getUsername());

        // assign PvInfo
        for (FormPvCustomInfo combinedPvInfo : pvCustomInfoList) {
            setTextValue(combinedPvInfo.widget, combinedPvInfo.getValue());
        }
    }

    private void createSourcesSection() {
        Composite client = createSectionWithClient("Source Vessels");
        PvSampleSourceInfoTable table = new PvSampleSourceInfoTable(client,
            patientVisit.getPvSourceVesselCollection());
        table.adaptToToolkit(toolkit, true);
    }

    private void createSamplesSection() {
        Composite parent = createSectionWithClient("Samples");
        samplesWidget = new SamplesListInfoTable(parent, patientVisit
            .getAliquotCollection());
        samplesWidget.adaptToToolkit(toolkit, true);
        samplesWidget.setSelection(patientVisitAdapter.getSelectedSample());
        samplesWidget.addDoubleClickListener(collectionDoubleClickListener);
    }

    @Override
    protected void reload() {
        retrievePatientVisit();
        String date = patientVisit.getFormattedDateProcessed();
        setPartName("Visit " + date);
        form.setText("Visit Drawn Date: " + date);
        setPatientVisitValues();
    }

    private void retrievePatientVisit() {
        try {
            patientVisit.reload();
        } catch (Exception ex) {
            logger.error("Error while retrieving patient visit "
                + patientVisit.getFormattedDateProcessed() + "(patient "
                + patientVisit.getPatient() + ")", ex);
        }
    }

    @Override
    protected String getEntryFormId() {
        return PatientVisitEntryForm.ID;
    }
}
