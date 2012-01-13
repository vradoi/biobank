package edu.ualberta.med.biobank.test.action.helper;

import java.util.HashSet;
import java.util.Set;

import edu.ualberta.med.biobank.common.action.collectionEvent.CollectionEventGetInfoAction;
import edu.ualberta.med.biobank.common.action.collectionEvent.CollectionEventGetInfoAction.CEventInfo;
import edu.ualberta.med.biobank.common.action.info.OriginInfoSaveInfo;
import edu.ualberta.med.biobank.common.action.specimen.SpecimenInfo;
import edu.ualberta.med.biobank.test.Utils;
import edu.ualberta.med.biobank.test.action.IActionExecutor;

public class OriginInfoHelper extends Helper {

    public static OriginInfoSaveInfo createSaveOriginInfoSpecimenInfoRandom(
        IActionExecutor actionExecutor, Integer patientId, Integer siteId,
        Integer centerId) {
        Set<Integer> ids = new HashSet<Integer>();
        Integer id = null;
        try {
            id = CollectionEventHelper.createCEventWithSourceSpecimens(
                actionExecutor, patientId, centerId);

            CEventInfo ceventInfo =
                actionExecutor.exec(new CollectionEventGetInfoAction(id));

            for (SpecimenInfo specInfo : ceventInfo.sourceSpecimenInfos) {
                ids.add(specInfo.specimen.getId());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new OriginInfoSaveInfo(r.nextInt(), siteId, id,
            Utils.getRandomString(10),
            ids, null);
    }
}
