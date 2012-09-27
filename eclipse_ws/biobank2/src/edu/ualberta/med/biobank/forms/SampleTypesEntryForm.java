package edu.ualberta.med.biobank.forms;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import edu.ualberta.med.biobank.common.BiobankCheckException;
import edu.ualberta.med.biobank.common.wrappers.SampleTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.treeview.SiteAdapter;
import edu.ualberta.med.biobank.widgets.SampleTypeEntryWidget;
import edu.ualberta.med.biobank.widgets.listeners.BiobankEntryFormWidgetListener;
import edu.ualberta.med.biobank.widgets.listeners.MultiSelectEvent;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class SampleTypesEntryForm extends BiobankEntryForm {

    private static Logger LOGGER = Logger.getLogger(SampleTypesEntryForm.class
        .getName());

    public static final String ID = "edu.ualberta.med.biobank.forms.SampleTypesEntryForm";
    public static final String OK_MESSAGE = "View and edit sample types.";

    private SiteWrapper siteWrapper;
    private List<SampleTypeWrapper> globalSampleTypes;
    private List<SampleTypeWrapper> siteSampleTypes;
    private SampleTypeEntryWidget siteSampleWidget;
    private SampleTypeEntryWidget globalSampleWidget;

    private BiobankEntryFormWidgetListener listener = new BiobankEntryFormWidgetListener() {
        @Override
        public void selectionChanged(MultiSelectEvent event) {
            setDirty(true);
        }
    };

    @Override
    public void init() throws Exception {
        SiteAdapter siteAdapter = (SiteAdapter) adapter;
        siteWrapper = siteAdapter.getWrapper();

        globalSampleTypes = SampleTypeWrapper.getGlobalSampleTypes(appService,
            true);
        siteSampleTypes = siteWrapper.getSampleTypeCollection(true);
        setPartName("Sample Types Entry");
    }

    @Override
    protected void createFormContent() {
        form.setText("Sample Type Information");
        form.getBody().setLayout(new GridLayout(1, false));
        if (!siteWrapper.getName().equals("All Sites"))
            createSiteSampleTypeSection();
        createGlobalSampleTypeSection();
        if (!siteWrapper.getName().equals("All Sites"))
            firstControl = siteSampleWidget;
        else
            firstControl = globalSampleWidget;
    }

    private void createSiteSampleTypeSection() {
        Composite client = createSectionWithClient("Site Sample Types");
        GridLayout layout = new GridLayout(1, true);
        client.setLayout(layout);

        siteSampleWidget = new SampleTypeEntryWidget(client, SWT.NONE,
            siteSampleTypes, globalSampleTypes, "Add Site Sample Type", toolkit);
        siteSampleWidget.adaptToToolkit(toolkit, true);
        siteSampleWidget.addSelectionChangedListener(listener);
        toolkit.paintBordersFor(siteSampleWidget);
    }

    private void createGlobalSampleTypeSection() {
        Composite client = createSectionWithClient("Global Sample Types");
        GridLayout layout = new GridLayout(1, true);
        client.setLayout(layout);

        globalSampleWidget = new SampleTypeEntryWidget(client, SWT.NONE,
            globalSampleTypes, siteSampleTypes, "Add Global Sample Type",
            toolkit);
        globalSampleWidget.adaptToToolkit(toolkit, true);
        globalSampleWidget.addSelectionChangedListener(listener);
        toolkit.paintBordersFor(globalSampleWidget);
    }

    @Override
    public void saveForm() throws BiobankCheckException, Exception {
        siteWrapper.reload();
        List<SampleTypeWrapper> ssCollection = siteSampleWidget
            .getTableSampleTypes();
        siteWrapper.setSampleTypeCollection(ssCollection);
        siteWrapper.persist();
        SampleTypeWrapper.persistGlobalSampleTypes(appService,
            globalSampleWidget.getTableSampleTypes());
    }

    @Override
    public String getNextOpenedFormID() {
        return null;
    }

    @Override
    protected String getOkMessage() {
        return null;
    }

    @Override
    public void reset() throws Exception {
        super.reset();
        try {
            globalSampleTypes = SampleTypeWrapper.getGlobalSampleTypes(
                appService, true);
        } catch (ApplicationException e) {
            LOGGER.error("Can't reset global sample types", e);
        }
        siteSampleTypes = siteWrapper.getSampleTypeCollection(true);
        globalSampleWidget.setLists(globalSampleTypes, siteSampleTypes);
        siteSampleWidget.setLists(siteSampleTypes, globalSampleTypes);
    }
}