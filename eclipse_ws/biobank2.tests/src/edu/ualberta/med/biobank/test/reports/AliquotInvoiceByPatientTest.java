package edu.ualberta.med.biobank.test.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import edu.ualberta.med.biobank.common.util.PredicateUtil;
import edu.ualberta.med.biobank.common.wrappers.AliquotWrapper;

public class AliquotInvoiceByPatientTest extends AbstractReportTest {
    private static final Comparator<AliquotWrapper> ORDER_BY_ALIQUOT_PNUMBER = new Comparator<AliquotWrapper>() {
        public int compare(AliquotWrapper lhs, AliquotWrapper rhs) {
            int cmp = compareStrings(lhs.getPatientVisit().getPatient()
                .getPnumber(), rhs.getPatientVisit().getPatient().getPnumber());

            if (cmp != 0) {
                return cmp;
            }

            return compareStrings(lhs.getInventoryId(), rhs.getInventoryId());
        }
    };

    @Test
    public void testResults() throws Exception {
        checkResults(new Date(0), new Date());
    }

    @Test
    public void testEmptyDateRange() throws Exception {
        checkResults(new Date(), new Date(0));
    }

    @Test
    public void testSmallDatePoint() throws Exception {
        List<AliquotWrapper> aliquots = getAliquots();
        Assert.assertTrue(aliquots.size() > 0);

        AliquotWrapper aliquot = aliquots.get(aliquots.size() / 2);
        checkResults(aliquot.getLinkDate(), aliquot.getLinkDate());
    }

    @Test
    public void testSmallDateRange() throws Exception {
        List<AliquotWrapper> aliquots = getAliquots();
        Assert.assertTrue(aliquots.size() > 0);

        AliquotWrapper aliquot = aliquots.get(aliquots.size() / 2);
        checkResults(aliquot.getLinkDate(), aliquot.getLinkDate());
    }

    @Override
    protected Collection<Object> getExpectedResults() throws Exception {
        Date after = (Date) getReport().getParams().get(0);
        Date before = (Date) getReport().getParams().get(1);

        Collection<AliquotWrapper> allAliquots = getAliquots();
        @SuppressWarnings("unchecked")
        List<AliquotWrapper> filteredAliquots = new ArrayList<AliquotWrapper>(
            PredicateUtil.filter(allAliquots, PredicateUtil.andPredicate(
                AbstractReportTest.aliquotLinkedBetween(after, before),
                ALIQUOT_NOT_IN_SENT_SAMPLE_CONTAINER,
                aliquotSite(isInSite(), getSiteId()))));

        Collections.sort(filteredAliquots, ORDER_BY_ALIQUOT_PNUMBER);

        List<Object> expectedResults = new ArrayList<Object>();

        for (AliquotWrapper aliquot : filteredAliquots) {
            expectedResults.add(new Object[] { aliquot.getInventoryId(),
                aliquot.getPatientVisit().getPatient().getPnumber(),
                aliquot.getPatientVisit().getShipment().getClinic().getName(),
                aliquot.getLinkDate(), aliquot.getSampleType().getName() });
        }

        return expectedResults;
    }

    private void checkResults(Date after, Date before) throws Exception {
        getReport().setParams(Arrays.asList((Object) after, (Object) before));

        checkResults(EnumSet.of(CompareResult.SIZE, CompareResult.ORDER));
    }
}
