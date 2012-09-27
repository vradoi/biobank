package edu.ualberta.med.biobank;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.ualberta.med.biobank.preferences.PreferenceConstants;
import edu.ualberta.med.biobank.treeview.ClinicAdapter;
import edu.ualberta.med.biobank.treeview.ClinicGroup;
import edu.ualberta.med.biobank.treeview.ContainerAdapter;
import edu.ualberta.med.biobank.treeview.ContainerGroup;
import edu.ualberta.med.biobank.treeview.ContainerTypeAdapter;
import edu.ualberta.med.biobank.treeview.ContainerTypeGroup;
import edu.ualberta.med.biobank.treeview.PatientAdapter;
import edu.ualberta.med.biobank.treeview.PatientVisitAdapter;
import edu.ualberta.med.biobank.treeview.SessionAdapter;
import edu.ualberta.med.biobank.treeview.ShipmentAdapter;
import edu.ualberta.med.biobank.treeview.SiteAdapter;
import edu.ualberta.med.biobank.treeview.StudyAdapter;
import edu.ualberta.med.biobank.treeview.StudyGroup;
import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;

/**
 * The activator class controls the plug-in life cycle
 */
public class BioBankPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "biobank2";

    public static final String IMAGE_ID = "biobank2.image";

    public static final String IMG_FORM_BG = "formBg";
    public static final String IMG_ADD = "add";
    public static final String IMG_BIN = "bin";
    public static final String IMG_BOX = "box";
    public static final String IMG_CABINET = "cabinet";
    public static final String IMG_CABINET_LINK_ASSIGN = "cabinetLinkAssign";
    public static final String IMG_CLINIC = "clinic";
    public static final String IMG_CLINICS = "clinics";
    public static final String IMG_LOGIN = "login";
    public static final String IMG_LOGOUT = "logout";
    public static final String IMG_COMPUTER_KEY = "computerKey";
    public static final String IMG_CONTAINER_TYPES = "containerTypes";
    public static final String IMG_CONTAINERS = "containers";
    public static final String IMG_DELETE = "delete";
    public static final String IMG_DRAWER = "drawer";
    public static final String IMG_FREEZER = "freezer";
    public static final String IMG_HOTEL = "hotel";
    public static final String IMG_MAIN_PERSPECTIVE = "mainPerspective";
    public static final String IMG_PALLET = "pallet";
    public static final String IMG_PATIENT = "patient";
    public static final String IMG_PATIENT_VISIT = "patientVisit";
    public static final String IMG_SHIPMENT = "shipment";
    public static final String IMG_RELOAD_FORM = "reloadForm";
    public static final String IMG_EDIT_FORM = "editForm";
    public static final String IMG_RESET_FORM = "resetForm";
    public static final String IMG_CANCEL_FORM = "cancelForm";
    public static final String IMG_CONFIRM_FORM = "confirmForm";
    public static final String IMG_REPORTS = "reports";
    public static final String IMG_SCAN_ASSIGN = "scanAssign";
    public static final String IMG_SCAN_LINK = "scanLink";
    public static final String IMG_SESSIONS = "sessions";
    public static final String IMG_SITE = "site";
    public static final String IMG_STUDIES = "studies";
    public static final String IMG_STUDY = "study";
    public static final String IMG_USER_ADD = "userAdd";

    // 
    // ContainerTypeAdapter and Container missing on purpose.
    //
    private Map<String, String> classToImageKey = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;
        {
            put(SessionAdapter.class.getName(), BioBankPlugin.IMG_SESSIONS);
            put(SiteAdapter.class.getName(), BioBankPlugin.IMG_SITE);
            put(ClinicGroup.class.getName(), BioBankPlugin.IMG_CLINICS);
            put(StudyGroup.class.getName(), BioBankPlugin.IMG_STUDIES);
            put(ContainerTypeGroup.class.getName(),
                BioBankPlugin.IMG_CONTAINER_TYPES);
            put(ContainerGroup.class.getName(), BioBankPlugin.IMG_CONTAINERS);
            put(ClinicAdapter.class.getName(), BioBankPlugin.IMG_CLINIC);
            put(StudyAdapter.class.getName(), BioBankPlugin.IMG_STUDY);
            put(PatientAdapter.class.getName(), BioBankPlugin.IMG_PATIENT);
            put(PatientVisitAdapter.class.getName(),
                BioBankPlugin.IMG_PATIENT_VISIT);
            put(ShipmentAdapter.class.getName(), BioBankPlugin.IMG_SHIPMENT);
        }
    };

    private static final String[] CONTAINER_TYPE_IMAGE_KEYS = new String[] {
        BioBankPlugin.IMG_BIN, BioBankPlugin.IMG_BOX,
        BioBankPlugin.IMG_CABINET, BioBankPlugin.IMG_DRAWER,
        BioBankPlugin.IMG_FREEZER, BioBankPlugin.IMG_HOTEL,
        BioBankPlugin.IMG_PALLET, };

    public static final String BARCODES_FILE = BioBankPlugin.class.getPackage()
        .getName()
        + ".barcode";

    static Logger log4j = Logger.getLogger(BioBankPlugin.class.getName());

    // The shared instance
    private static BioBankPlugin plugin;

    /**
     * The constructor
     */
    public BioBankPlugin() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        SessionManager.getInstance();
        log4j.debug(PLUGIN_ID + " started");
    }

    @Override
    protected void initializeImageRegistry(ImageRegistry registry) {
        registerImage(registry, IMG_FORM_BG, "form_banner.bmp");
        registerImage(registry, IMG_ADD, "add.png");
        registerImage(registry, IMG_BIN, "bin.png");
        registerImage(registry, IMG_BOX, "bin.png");
        registerImage(registry, IMG_CABINET, "cabinet.png");
        registerImage(registry, IMG_CABINET_LINK_ASSIGN,
            "cabinetLinkAssign.png");
        registerImage(registry, IMG_CLINIC, "clinic.png");
        registerImage(registry, IMG_CLINICS, "clinics.png");
        registerImage(registry, IMG_LOGIN, "computer.png");
        registerImage(registry, IMG_COMPUTER_KEY, "computer_key.png");
        registerImage(registry, IMG_LOGOUT, "computer_delete.png");
        registerImage(registry, IMG_CONTAINER_TYPES, "containerTypes.png");
        registerImage(registry, IMG_CONTAINERS, "containers.png");
        registerImage(registry, IMG_DELETE, "delete.png");
        registerImage(registry, IMG_DRAWER, "drawer.png");
        registerImage(registry, IMG_FREEZER, "freezer.png");
        registerImage(registry, IMG_HOTEL, "hotel.png");
        registerImage(registry, IMG_MAIN_PERSPECTIVE, "mainPerspective.png");
        registerImage(registry, IMG_PALLET, "pallet.png");
        registerImage(registry, IMG_PATIENT, "patient.png");
        registerImage(registry, IMG_PATIENT_VISIT, "patientVisit.png");
        registerImage(registry, IMG_SHIPMENT, "shipment.png");
        registerImage(registry, IMG_RELOAD_FORM, "reload.png");
        registerImage(registry, IMG_EDIT_FORM, "edit.png");
        registerImage(registry, IMG_RESET_FORM, "reset.png");
        registerImage(registry, IMG_CANCEL_FORM, "cancel.png");
        registerImage(registry, IMG_CONFIRM_FORM, "confirm.png");
        registerImage(registry, IMG_REPORTS, "reports.png");
        registerImage(registry, IMG_SCAN_ASSIGN, "scanAssign.png");
        registerImage(registry, IMG_SCAN_LINK, "scanLink.png");
        registerImage(registry, IMG_SESSIONS, "sessions.png");
        registerImage(registry, IMG_SITE, "site.png");
        registerImage(registry, IMG_STUDIES, "studies.png");
        registerImage(registry, IMG_STUDY, "study.png");
    }

    private void registerImage(ImageRegistry registry, String key,
        String fileName) {
        try {
            IPath path = new Path("icons/" + fileName);
            URL url = FileLocator.find(getBundle(), path, null);
            if (url != null) {
                ImageDescriptor desc = ImageDescriptor.createFromURL(url);
                registry.put(key, desc);
            }
        } catch (Exception e) {
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);

        log4j.debug(PLUGIN_ID + " stopped");
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static BioBankPlugin getDefault() {
        return plugin;
    }

    /**
     * Display an information message
     */
    public static void openMessage(String title, String message) {
        MessageDialog.openInformation(PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getShell(), title, message);
    }

    /**
     * Display an information message
     */
    public static boolean openConfirm(String title, String message) {
        return MessageDialog.openConfirm(PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getShell(), title, message);
    }

    /**
     * Display an error message
     */
    public static void openError(String title, String message) {
        MessageDialog.openError(PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getShell(), title, message);
    }

    /**
     * Display an error message with exception message and log the exception
     */
    public static void openError(String title, Exception e) {
        String msg = e.getMessage();
        if (((msg == null) || msg.isEmpty()) && (e.getCause() != null)) {
            msg = e.getCause().getMessage();
        }
        openError(title, e.getMessage());
        log4j.error(title, e);
    }

    public static void openAsyncError(String title, Exception e) {
        String msg = e.getMessage();
        if ((msg == null || msg.isEmpty()) && e.getCause() != null) {
            msg = e.getCause().getMessage();
        }
        openAsyncError(title, e.getMessage());
        log4j.error(title, e);
    }

    /**
     * Display an info message
     */
    public static void openInformation(String title, String message) {
        MessageDialog.openInformation(PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getShell(), title, message);
    }

    /**
     * Display an error message asynchronously
     */
    public static void openAsyncError(final String title, final String message) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                MessageDialog.openError(PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getShell(), title, message);
            }
        });
    }

    /**
     * Display remote access error message
     */
    public static void openRemoteAccessErrorMessage() {
        openAsyncError(
            "Connection Attempt Failed",
            "Could not perform database operation. Make sure server is running correct version.");
    }

    /**
     * Display remote connect error message
     */
    public static void openRemoteConnectErrorMessage() {
        openAsyncError("Connection Attempt Failed",
            "Could not connect to server. Make sure server is running.");
    }

    /**
     * Display remote access error message
     */
    public static void openAccessDeniedErrorMessage() {
        openAsyncError("Access Denied",
            "You don't have rights to do this action.");
    }

    public boolean isCancelBarcode(String code) {
        return getPreferenceStore().getString(
            PreferenceConstants.GENERAL_CANCEL).equals(code);
    }

    public boolean isConfirmBarcode(String code) {
        return getPreferenceStore().getString(
            PreferenceConstants.GENERAL_CONFIRM).equals(code);
    }

    public int getPlateNumber(String barcode) {
        for (int i = 0; i < PreferenceConstants.SCANNER_PLATE_BARCODES.length; i++) {
            if (isRealScanEnabled()
                && !ScannerConfigPlugin.getDefault().getPalletEnabled(i + 1))
                continue;

            String pref = getPreferenceStore().getString(
                PreferenceConstants.SCANNER_PLATE_BARCODES[i]);
            Assert.isTrue(!pref.isEmpty(), "preference not assigned");
            if (pref.equals(barcode)) {
                return i + 1;
            }
        }
        return -1;
    }

    public boolean isValidPlateBarcode(String value) {
        return !value.isEmpty() && getPlateNumber(value) != -1;
    }

    public static boolean isAskPrint() {
        IPreferenceStore store = getDefault().getPreferenceStore();
        return store.getBoolean(PreferenceConstants.LINK_ASSIGN_ASK_PRINT);
    }

    public static boolean isRealScanEnabled() {
        String realScan = Platform.getDebugOption(BioBankPlugin.PLUGIN_ID
            + "/realScan");
        if (realScan != null) {
            return Boolean.valueOf(realScan);
        }
        return true;
    }

    public String getPrinter() {
        return getPreferenceStore().getString(
            PreferenceConstants.LINK_ASSIGN_PRINTER);
    }

    public Image getImage(Object element) {
        String imageKey = classToImageKey.get(element.getClass().getName());
        if ((imageKey == null)
            && ((element instanceof ContainerAdapter) || (element instanceof ContainerTypeAdapter))) {
            String ctName;
            if (element instanceof ContainerAdapter) {
                ctName = ((ContainerAdapter) element).getContainer()
                    .getContainerType().getName();
            } else {
                ctName = ((ContainerTypeAdapter) element).getName();
            }
            return getIconForTypeName(ctName);
        }
        return BioBankPlugin.getDefault().getImageRegistry().get(imageKey);
    }

    public Image getIconForTypeName(String typeName) {
        if (typeName == null) {
            return null;
        }
        if (classToImageKey.containsKey(typeName)) {
            return BioBankPlugin.getDefault().getImageRegistry().get(
                classToImageKey.get(typeName));
        }

        String imageKey = null;
        for (String name : CONTAINER_TYPE_IMAGE_KEYS) {
            if (typeName.toLowerCase().contains(name)) {
                imageKey = name;
                break;
            }
        }

        if (imageKey == null)
            imageKey = BioBankPlugin.IMG_FREEZER;

        classToImageKey.put(typeName, imageKey);
        return BioBankPlugin.getDefault().getImageRegistry().get(imageKey);
    }

}