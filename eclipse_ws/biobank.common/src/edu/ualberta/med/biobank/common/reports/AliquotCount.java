package edu.ualberta.med.biobank.common.reports;

import edu.ualberta.med.biobank.model.Aliquot;
import edu.ualberta.med.biobank.model.AliquotPosition;

@Deprecated
public class AliquotCount extends QueryObject {

    protected static final String NAME = "Sample Type Totals Old";

    public AliquotCount(String op, Integer siteId) {
        super(
            "Lists the total number of aliquots per sample type.",
            "Select Alias.sampleType.name, count(*) from "
                + Aliquot.class.getName()
                + " as Alias where Alias.aliquotPosition not in (from "
                + AliquotPosition.class.getName()
                + " a where a.container.label like 'SS%') and Alias.patientVisit.patient.study.site "
                + op + siteId + " GROUP BY Alias.sampleType.name",
            new String[] { "Sample Type", "Total" });
    }

    @Override
    public String getName() {
        return NAME;
    }
}
