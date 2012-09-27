package edu.ualberta.med.biobank.dialogs;

import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.ualberta.med.biobank.common.wrappers.SampleTypeWrapper;
import edu.ualberta.med.biobank.validators.NonEmptyStringValidator;

public class SampleTypeDialog extends BiobankDialog {

    private static final String TITLE = "Sample Type ";

    private static final String MSG_NO_ST_NAME = "Sample type must have a name.";
    private static final String MSG_NO_ST_SNAME = "Sample type must have a short name.";

    private SampleTypeWrapper origSampleType;

    // this is the object that is modified via the bound widgets
    private SampleTypeWrapper sampleType;

    public SampleTypeDialog(Shell parent, SampleTypeWrapper sampleType) {
        super(parent);
        Assert.isNotNull(sampleType);
        origSampleType = sampleType;
        this.sampleType = new SampleTypeWrapper(sampleType.getAppService());
        this.sampleType.setName(sampleType.getName());
        this.sampleType.setNameShort(sampleType.getNameShort());
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        Integer id = origSampleType.getId();
        shell.setText(((id == null) ? "Add " : "Edit ") + TITLE);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite parentComposite = (Composite) super.createDialogArea(parent);
        Composite client = new Composite(parentComposite, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        client.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        layout.horizontalSpacing = 10;
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control c = createBoundWidgetWithLabel(client, Text.class, SWT.BORDER,
            "Name", null, PojoObservables.observeValue(sampleType, "name"),
            new NonEmptyStringValidator(MSG_NO_ST_NAME));
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 200;
        c.setLayoutData(gd);

        createBoundWidgetWithLabel(client, Text.class, SWT.BORDER,
            "Short Name", null, PojoObservables.observeValue(sampleType,
                "nameShort"), new NonEmptyStringValidator(MSG_NO_ST_SNAME));

        return client;
    }

    @Override
    protected void okPressed() {
        super.okPressed();
        origSampleType.setName(sampleType.getName());
        origSampleType.setNameShort(sampleType.getNameShort());
    }

    public SampleTypeWrapper getSampleType() {
        return origSampleType;
    }

}