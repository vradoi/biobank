package edu.ualberta.med.biobank.server.logging.logger;

import java.util.Date;
import java.util.Map;

import edu.ualberta.med.biobank.model.ClinicShipment;
import edu.ualberta.med.biobank.model.Log;
import edu.ualberta.med.biobank.model.Site;

public class ClinicShipmentStateLogger extends BiobankObjectStateLogger {

    protected ClinicShipmentStateLogger() {
    }

    @Override
    protected Log getLogObject(Object obj, Map<String, Object> statesMap) {
        if (obj instanceof ClinicShipment) {
            Log log = new Log();
            String details = "";
            Date dateReceived = (Date) statesMap.get("dateReceived");
            Site site = (Site) statesMap.get("site");
            log.setSite(site.getNameShort());
            if (dateReceived != null) {
                details = "Received:" + dateTimeFormatter.format(dateReceived);
            }
            String waybill = (String) statesMap.get("waybill");
            if (waybill != null) {
                details += " - Waybill:" + waybill;
            }
            log.setDetails(details);
            log.setType("Shipment");
            return log;
        }
        return null;
    }

}
