package edu.ualberta.med.biobank.common.reports;

import edu.ualberta.med.biobank.model.Aliquot;

public class SampleSCount extends QueryObject {

    protected static final String NAME = "Aliquot Types by Study";

    public SampleSCount(String op, Integer siteId) {
        super(
            "Lists the total number of each sample type by study.",
            "Select Alias.patientVisit.patient.study.nameShort, Alias.sampleType.name, count(*) from "
                + Aliquot.class.getName()
                + " as Alias where Alias.patientVisit.patient.study.site "
                + op
                + siteId
                + " GROUP BY Alias.patientVisit.patient.study.nameShort, Alias.sampleType.name",
            new String[] { "Study", "Aliquot Type", "Total" }, new int[] { 100,
                200, 100 });
    }

    @Override
    public String getName() {
        return NAME;
    }
}
