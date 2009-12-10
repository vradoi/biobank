package edu.ualberta.med.biobank.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextPrintOptions;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.reports.QueryObject;
import edu.ualberta.med.biobank.common.reports.QueryObject.Option;
import edu.ualberta.med.biobank.widgets.DateTimeWidget;
import edu.ualberta.med.biobank.widgets.ReportsLabelProvider;
import edu.ualberta.med.biobank.widgets.infotables.InfoTableWidget;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class ReportsView extends ViewPart {

    public static Logger LOGGER = Logger.getLogger(ReportsView.class.getName());

    public static final String ID = "edu.ualberta.med.biobank.views.ReportsView";
    private SimpleDateFormat dateFormat = new SimpleDateFormat(
        "EEEE, MMMM dd, yyyy");

    private GC gc;

    private ScrolledComposite sc;
    private Composite top;
    private Composite header;
    private Composite subSection;

    private ComboViewer querySelect;
    private List<QueryObject> queryObjects;
    private List<Widget> widgetFields;
    private List<Label> textLabels;

    private Button searchButton;
    private Collection<Object> searchData;
    private InfoTableWidget<Object> searchTable;

    private Button printButton;

    public ReportsView() {
        searchData = new ArrayList<Object>();
    }

    @Override
    public void createPartControl(Composite parent) {

        sc = new ScrolledComposite(parent, SWT.V_SCROLL);
        sc.setLayout(new GridLayout(1, false));
        sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);

        top = new Composite(sc, SWT.NONE);
        top.setLayout(new GridLayout(1, false));
        top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        header = new Composite(top, SWT.NONE);
        header.setLayout(new GridLayout(3, false));

        queryObjects = QueryObject.getAllQueries(SessionManager.getInstance()
            .getCurrentSiteWrapper().getId());
        querySelect = createCombo(header, queryObjects);
        querySelect
            .addSelectionChangedListener(new ISelectionChangedListener() {
                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    comboChanged();
                }
            });

        searchButton = new Button(header, SWT.NONE);
        searchButton.setText("Search");
        searchButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    searchData = search();

                    if (searchData.size() > 0) {
                        IStructuredSelection typeSelection = (IStructuredSelection) querySelect
                            .getSelection();
                        QueryObject query = (QueryObject) typeSelection
                            .getFirstElement();
                        String[] names = query.getColumnNames();
                        int[] bounds = new int[names.length];

                        for (int i = 0; i < names.length; i++) {
                            bounds[i] = 100 + names[i].length() * 2;
                        }
                        searchTable.dispose();
                        searchTable = new InfoTableWidget<Object>(top,
                            searchData, names, bounds);
                        searchTable.getTableViewer().setLabelProvider(
                            new ReportsLabelProvider());
                        GridData searchLayoutData = new GridData(SWT.FILL,
                            SWT.FILL, true, true);
                        searchLayoutData.minimumHeight = 500;
                        searchTable.setLayoutData(searchLayoutData);
                        searchTable.moveBelow(subSection);
                    }
                    searchTable.setCollection(searchData);
                    searchTable.redraw();
                    top.layout();
                } catch (ApplicationException ae) {
                    BioBankPlugin.openAsyncError("Search error", ae);
                }

            }
        });

        printButton = new Button(header, SWT.NONE);
        printButton.setText("Print");
        printButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                printTable();
            }
        });

        // create the query's display here
        subSection = new Composite(top, SWT.NONE);
        Label resultsLabel = new Label(top, SWT.NONE);
        resultsLabel.setText("Results:");

        searchTable = new InfoTableWidget<Object>(top, searchData,
            new String[] {}, null);
        GridData searchLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        searchTable.setLayoutData(searchLayoutData);

        querySelect.setSelection(new StructuredSelection(queryObjects.get(0)));
        top.layout();
        sc.setContent(top);
        sc.setMinSize(top.computeSize(SWT.DEFAULT, SWT.DEFAULT));

    }

    private Collection<Object> search() throws ApplicationException {
        IStructuredSelection typeSelection = (IStructuredSelection) querySelect
            .getSelection();
        QueryObject query = (QueryObject) typeSelection.getFirstElement();
        ArrayList<Object> params = new ArrayList<Object>();
        for (int i = 0; i < widgetFields.size(); i++) {
            if (widgetFields.get(i) instanceof Text)
                params.add(((Text) widgetFields.get(i)).getText());
            else if (widgetFields.get(i) instanceof Combo)
                params.add(((Combo) widgetFields.get(i)).getSelectionIndex());
            else if (widgetFields.get(i) instanceof DateTimeWidget)
                params.add(((DateTimeWidget) widgetFields.get(i)).getDate());
        }
        return query.executeQuery(SessionManager.getAppService(), params);
    }

    public void comboChanged() {
        IStructuredSelection typeSelection = (IStructuredSelection) querySelect
            .getSelection();
        QueryObject query = (QueryObject) typeSelection.getFirstElement();

        List<Option> queryOptions = query.getOptions();
        textLabels = new ArrayList<Label>();
        widgetFields = new ArrayList<Widget>();

        if (subSection != null)
            subSection.dispose();

        subSection = new Composite(top, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        subSection.setLayout(layout);

        Label description = new Label(subSection, SWT.NONE);
        description.setText("Description: " + query.getDescription());
        GridData gd2 = new GridData();
        gd2.horizontalSpan = 2;
        description.setLayoutData(gd2);

        for (int i = 0; i < queryOptions.size(); i++) {
            Option option = queryOptions.get(i);
            Label fieldLabel = new Label(subSection, SWT.NONE);
            fieldLabel.setText(option.getName() + ":");
            textLabels.add(fieldLabel);
            Widget widget;

            // if (fields.get(i) == Combo.class)
            // widget = new Combo(subSection, SWT.NONE);
            // else if (fields.get(i) == DateTimeWidget.class)
            // widget = new DateTimeWidget(subSection, SWT.NONE, null);
            // else if (fields.get(i) == Text.class)
            // widget = new Text(subSection, SWT.BORDER);
            // else
            // widget = null;
            if (option.getType() == List.class)
                widget = new Combo(subSection, SWT.NONE);
            else if (option.getType() == Date.class)
                widget = new DateTimeWidget(subSection, SWT.NONE, null);
            else if (option.getType() == String.class)
                widget = new Text(subSection, SWT.BORDER);
            else
                widget = null;
            widgetFields.add(widget);
        }

        subSection.moveBelow(header);
        subSection.setVisible(true);

        // update parents
        resetSearch();
        updateScrollBars();
        top.layout(true, true);

    }

    @Override
    public void setFocus() {

    }

    public void updateScrollBars() {
        sc.layout(true, true);
        sc.setMinSize(top.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    public void resetSearch() {
        if (searchTable != null) {
            searchTable.setCollection(new ArrayList<Object>());
            TableColumn[] cols = searchTable.getTableViewer().getTable()
                .getColumns();
            for (TableColumn col : cols) {
                col.setText("");
            }
        }

    }

    protected static ComboViewer createCombo(Composite parent, List<?> list) {
        Combo combo;
        ComboViewer comboViewer;
        combo = new Combo(parent, SWT.READ_ONLY);

        GridData combodata = new GridData();
        combodata.widthHint = 250;
        combo.setLayoutData(combodata);

        comboViewer = new ComboViewer(combo);
        comboViewer.setContentProvider(new ArrayContentProvider());
        comboViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return element.toString();
            }
        });
        comboViewer.setInput(list);
        return comboViewer;
    }

    public boolean printTable() {
        boolean doPrint = MessageDialog.openQuestion(PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getShell(), "Confirm",
            "Print table contents?");
        if (doPrint) {
            PrintDialog dialog = new PrintDialog(PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell(), SWT.NONE);
            PrinterData data = dialog.open();
            gc = new GC(new Printer(data));
            searchTable.print(gc);
            return true;
        }
        return false;
    }

    protected void print() {

        PrinterData data = null;
        if (!BioBankPlugin.getDefault().isDebugging()) {
            data = Printer.getDefaultPrinterData();
        }
        if (data == null) {
            PrintDialog dialog = new PrintDialog(PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell(), SWT.NONE);
            data = dialog.open();
        }
        if (data == null)
            return;

        // use existing print functionality of swt styled texts
        StyledText sText = new StyledText(top, SWT.NONE);
        sText.setText(searchTable.toString());

        FontData printerFd = new FontData("Sans", 8, SWT.NORMAL);
        final Font font = new Font(Display.getCurrent(), printerFd);
        sText.setFont(font);
        final Printer printer = new Printer(data);
        StyledTextPrintOptions options = new StyledTextPrintOptions();
        options.footer = "Printed on " + dateFormat.format(new Date())
            + StyledTextPrintOptions.SEPARATOR
            + StyledTextPrintOptions.SEPARATOR
            + StyledTextPrintOptions.PAGE_TAG;
        options.header = "Biobank2 - Activity Report"
            + StyledTextPrintOptions.SEPARATOR
            + StyledTextPrintOptions.SEPARATOR + "USER:"
            + SessionManager.getInstance().getSession().getUserName();
        options.jobName = "scannedLinkedActivity";
        final Runnable styledTextPrinter = sText.print(printer, options);
        styledTextPrinter.run();
        printer.dispose();
        font.dispose();
    }
}
