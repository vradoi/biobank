package edu.ualberta.med.biobank.widgets.grids.cell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.action.scanprocess.CellInfo;
import edu.ualberta.med.biobank.common.action.scanprocess.CellInfoStatus;
import edu.ualberta.med.biobank.common.action.specimen.SpecimenGetInfoAction;
import edu.ualberta.med.biobank.common.debug.DebugUtil;
import edu.ualberta.med.biobank.common.util.StringUtil;
import edu.ualberta.med.biobank.common.wrappers.SpecimenTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenWrapper;
import edu.ualberta.med.biobank.i18n.LString;
import edu.ualberta.med.biobank.model.util.RowColPos;
import edu.ualberta.med.scannerconfig.dmscanlib.WellRectangle;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;

public class PalletCell extends AbstractUICell {
    private String information;

    private String title = StringUtil.EMPTY_STRING;

    private SpecimenWrapper sourceSpecimen;

    private SpecimenWrapper specimen;

    private final WellRectangle scanCell;

    private SpecimenWrapper expectedSpecimen;

    public PalletCell(WellRectangle scanCell) {
        this.scanCell = scanCell;
    }

    public static Map<RowColPos, PalletCell> convertArray(
        List<WellRectangle> scancells) {
        Map<RowColPos, PalletCell> palletScanned =
            new TreeMap<RowColPos, PalletCell>();
        for (WellRectangle cell : scancells) {
            palletScanned.put(new RowColPos(cell.getRow(), cell.getColumn()),
                new PalletCell(cell));
        }
        return palletScanned;
    }

    public static Map<RowColPos, PalletCell> getRandomScanLink() {
        return convertArray(WellRectangle.getRandom());
    }

    public static Map<RowColPos, PalletCell> getRandomScanLinkWithSpecimensAlreadyLinked(
        WritableApplicationService appService, Integer siteId) throws Exception {
        Map<RowColPos, PalletCell> cells = convertArray(WellRectangle.getRandom());
        List<SpecimenWrapper> specimens = DebugUtil
            .getRandomLinkedAliquotedSpecimens(appService, siteId);
        if (specimens.size() > 1) {
            int row = 2;
            int col = 3;
            WellRectangle scanCell = new WellRectangle(row, col, specimens.get(0)
                .getInventoryId());
            cells.put(new RowColPos(row, col), new PalletCell(scanCell));
            row = 3;
            col = 1;
            scanCell =
                new WellRectangle(row, col, specimens.get(1).getInventoryId());
            cells.put(new RowColPos(row, col), new PalletCell(scanCell));
        }
        return cells;
    }

    public static Map<RowColPos, PalletCell> getRandomSpecimensAlreadyAssigned(
        WritableApplicationService appService, Integer siteId) throws Exception {
        return getRandomSpecimensAlreadyAssigned(appService, siteId, null);
    }

    public static Map<RowColPos, PalletCell> getRandomSpecimensAlreadyAssigned(
        WritableApplicationService appService, Integer siteId, Integer studyId)
        throws Exception {
        Map<RowColPos, PalletCell> palletScanned =
            new HashMap<RowColPos, PalletCell>();
        List<SpecimenWrapper> specimens = DebugUtil.getRandomAssignedSpecimens(
            appService, siteId, studyId);
        if (specimens.size() > 0) {
            palletScanned.put(new RowColPos(0, 0), new PalletCell(new WellRectangle(
                0, 0, specimens.get(0).getInventoryId())));
        }
        if (specimens.size() > 1) {
            palletScanned.put(new RowColPos(2, 4), new PalletCell(new WellRectangle(
                2, 4, specimens.get(1).getInventoryId())));
        }
        return palletScanned;
    }

    public static Map<RowColPos, PalletCell> getRandomSpecimensNotAssigned(
        WritableApplicationService appService, Integer siteId)
        throws ApplicationException {
        Map<RowColPos, PalletCell> palletScanned =
            new HashMap<RowColPos, PalletCell>();

        List<SpecimenWrapper> specimens = DebugUtil
            .getRandomNonAssignedNonDispatchedSpecimens(appService, siteId, 30);

        int i = 0;
        for (SpecimenWrapper spc : specimens) {
            int row = i / 12;
            int col = i % 12;
            palletScanned.put(new RowColPos(row, col), new PalletCell(
                new WellRectangle(row, col, spc.getInventoryId())));
            i++;
        }
        return palletScanned;
    }

    public static Map<RowColPos, PalletCell> getRandomNonDispatchedSpecimens(
        WritableApplicationService appService, Integer siteId)
        throws ApplicationException {
        Map<RowColPos, PalletCell> palletScanned =
            new HashMap<RowColPos, PalletCell>();
        List<SpecimenWrapper> randomSpecimens = DebugUtil
            .getRandomNonDispatchedSpecimens(appService, siteId, 30);
        int i = 0;
        while (i < randomSpecimens.size()) {
            int row = i / 12;
            int col = i % 12;
            palletScanned.put(new RowColPos(row, col),
                new PalletCell(new WellRectangle(row, col, randomSpecimens.get(i)
                    .getInventoryId())));
            i++;
        }
        return palletScanned;
    }

    /**
     * usually displayed in the middle of the cell
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Usually used for the tooltip of the cell
     * 
     * @return
     */
    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getTypeString() {
        if (specimen != null && specimen.getSpecimenType() != null) {
            SpecimenTypeWrapper type = specimen.getSpecimenType();
            if (type.getNameShort() != null) {
                return type.getNameShort();
            }
            return type.getName();
        }
        return StringUtil.EMPTY_STRING;
    }

    public SpecimenTypeWrapper getType() {
        if (specimen == null)
            return null;
        return specimen.getSpecimenType();
    }

    public void setSpecimenType(SpecimenTypeWrapper type) {
        if (specimen == null) {
            specimen = new SpecimenWrapper(SessionManager.getAppService());
        }
        specimen.setSpecimenType(type);
    }

    public void setSpecimen(SpecimenWrapper specimen) {
        this.specimen = specimen;
    }

    public SpecimenWrapper getSpecimen() {
        return specimen;
    }

    public String getValue() {
        if (scanCell != null) {
            return scanCell.getValue();
        }
        return null;
    }

    public void setValue(String value) {
        if (scanCell != null) {
            scanCell.setValue(value);
        }
    }

    @Override
    public Integer getRow() {
        if (scanCell != null) {
            return scanCell.getRow();
        }
        return null;
    }

    @Override
    public Integer getCol() {
        if (scanCell != null) {
            return scanCell.getColumn();
        }
        return null;
    }

    public RowColPos getRowColPos() {
        RowColPos rcp = null;
        Integer row = getRow();
        Integer col = getCol();
        if (row != null && col != null) {
            rcp = new RowColPos(row, col);
        }
        return rcp;
    }

    public static boolean hasValue(PalletCell cell) {
        return cell != null && cell.getValue() != null;
    }

    public void setExpectedSpecimen(SpecimenWrapper expectedSpecimen) {
        this.expectedSpecimen = expectedSpecimen;
    }

    public SpecimenWrapper getExpectedSpecimen() {
        return expectedSpecimen;
    }

    public void setSourceSpecimen(SpecimenWrapper sourceSpecimen) {
        this.sourceSpecimen = sourceSpecimen;
    }

    public SpecimenWrapper getSourceSpecimen() {
        return sourceSpecimen;
    }

    public void merge(WritableApplicationService appService,
        edu.ualberta.med.biobank.common.action.scanprocess.CellInfo cell)
        throws Exception {
        setStatus(cell.getStatus());
        if (cell.getInformation() != null)
            setInformation(cell.getInformation().toString());
        setValue(cell.getValue());
        setTitle(cell.getTitle().toString());
        SpecimenWrapper expectedSpecimen = null;
        if (cell.getExpectedSpecimenId() != null) {
            expectedSpecimen = new SpecimenWrapper(appService);
            expectedSpecimen.getWrappedObject().setId(
                cell.getExpectedSpecimenId());
            expectedSpecimen.reload();
        }
        setExpectedSpecimen(expectedSpecimen);
        SpecimenWrapper specimen = null;
        if (cell.getSpecimenId() != null) {
            specimen = new SpecimenWrapper(appService);
            specimen.setWrappedObject(SessionManager.getAppService()
                .doAction(new SpecimenGetInfoAction(cell.getSpecimenId()))
                .getSpecimen());
        }
        setSpecimen(specimen);
    }

    public void setStatus(CellInfoStatus status) {
        if (status != null)
            setStatus(UICellStatus.valueOf(status.name()));
    }

    @SuppressWarnings("deprecation")
    public CellInfo transformIntoServerCell() {
        CellInfo serverCell =
            new CellInfo(getRow(), getCol(), getValue(),
                getStatus() == null ? null : CellInfoStatus.valueOf(getStatus()
                    .name()));
        serverCell.setExpectedSpecimenId(getExpectedSpecimen() == null ? null
            : getExpectedSpecimen().getId());
        if (getStatus() != null)
            serverCell.setStatus(CellInfoStatus.valueOf(getStatus().name()));
        if (getInformation() != null)
            serverCell.setInformation(LString.lit(getInformation()
                .toString()));
        serverCell.setSpecimenId(getSpecimen() == null ? null : getSpecimen()
            .getId());
        serverCell.setTitle(getTitle());
        return serverCell;
    }
}
