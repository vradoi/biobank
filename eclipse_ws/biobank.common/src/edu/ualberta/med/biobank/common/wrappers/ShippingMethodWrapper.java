package edu.ualberta.med.biobank.common.wrappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.ualberta.med.biobank.common.exception.BiobankCheckException;
import edu.ualberta.med.biobank.common.exception.BiobankException;
import edu.ualberta.med.biobank.common.exception.BiobankQueryResultSizeException;
import edu.ualberta.med.biobank.common.peer.ShippingMethodPeer;
import edu.ualberta.med.biobank.model.AbstractShipment;
import edu.ualberta.med.biobank.model.Shipment;
import edu.ualberta.med.biobank.model.ShippingMethod;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public class ShippingMethodWrapper extends ModelWrapper<ShippingMethod> {

    public static final String DROP_OFF_NAME = "Drop-off";
    public static final String PICK_UP_NAME = "Pick-up";

    public ShippingMethodWrapper(WritableApplicationService appService) {
        super(appService);
    }

    public ShippingMethodWrapper(WritableApplicationService appService,
        ShippingMethod sc) {
        super(appService, sc);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void deleteChecks() throws BiobankException, ApplicationException {
        List<AbstractShipmentWrapper> shipments = getShipmentCollection();
        if (shipments != null && shipments.size() > 0) {
            throw new BiobankCheckException(
                "Cannot delete this shipping company: shipments are still using it");
        }

    }

    @Override
    protected List<String> getPropertyChangeNames() {
        return ShippingMethodPeer.PROP_NAMES;
    }

    @Override
    public Class<ShippingMethod> getWrappedClass() {
        return ShippingMethod.class;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof ShippingMethodWrapper)
            return ((ShippingMethodWrapper) object).getName().equals(
                this.getName());
        else
            return false;
    }

    @Override
    protected void persistChecks() throws BiobankException,
        ApplicationException {
        checkUnique();
    }

    public String getName() {
        return wrappedObject.getName();
    }

    public void setName(String name) {
        String old = getName();
        wrappedObject.setName(name);
        propertyChangeSupport.firePropertyChange("name", old, name);
    }

    @Override
    public int compareTo(ModelWrapper<ShippingMethod> o) {
        if (o instanceof ShippingMethodWrapper) {
            return getName().compareTo(o.getWrappedObject().getName());
        }
        return 0;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<AbstractShipmentWrapper> getAllShipmentCollection(boolean sort) {
        List<AbstractShipmentWrapper> shipmentCollection = (List<AbstractShipmentWrapper>) propertiesMap
            .get("shipmentCollection");
        if (shipmentCollection == null) {
            Collection<AbstractShipment> children = wrappedObject
                .getShipmentCollection();
            if (children != null) {
                shipmentCollection = new ArrayList<AbstractShipmentWrapper>();
                for (AbstractShipment ship : children) {
                    shipmentCollection.add(AbstractShipmentWrapper
                        .createInstance(appService, ship));
                }
                propertiesMap.put("shipmentCollection", shipmentCollection);
            }
        }
        if ((shipmentCollection != null) && sort)
            Collections.sort(shipmentCollection);
        return shipmentCollection;
    }

    @SuppressWarnings("rawtypes")
    public List<AbstractShipmentWrapper> getShipmentCollection() {
        return getShipmentCollection(false);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<AbstractShipmentWrapper> getShipmentCollection(boolean sort) {
        List<AbstractShipmentWrapper> shipmentCollection = (List<AbstractShipmentWrapper>) propertiesMap
            .get("shipmentCollection");
        if (shipmentCollection == null) {
            List<AbstractShipmentWrapper> allShipmentCollection = getAllShipmentCollection(sort);
            shipmentCollection = new ArrayList<AbstractShipmentWrapper>();
            for (AbstractShipmentWrapper ship : allShipmentCollection) {
                if (ship instanceof ShipmentWrapper) {
                    shipmentCollection.add(ship);
                }
            }
        }
        return shipmentCollection;
    }

    public static List<ShippingMethodWrapper> getShippingMethods(
        WritableApplicationService appService) throws ApplicationException {
        List<ShippingMethod> objects = appService.search(ShippingMethod.class,
            new ShippingMethod());
        List<ShippingMethodWrapper> wrappers = new ArrayList<ShippingMethodWrapper>();
        for (ShippingMethod sm : objects) {
            wrappers.add(new ShippingMethodWrapper(appService, sm));
        }
        return wrappers;
    }

    @Override
    public String toString() {
        return getName();
    }

    public boolean isUsed() throws ApplicationException, BiobankException {
        String queryString = "select count(s) from " + Shipment.class.getName()
            + " as s where s.shippingMethod=?)";
        HQLCriteria c = new HQLCriteria(queryString,
            Arrays.asList(new Object[] { wrappedObject }));
        List<Long> results = appService.query(c);
        if (results.size() != 1) {
            throw new BiobankQueryResultSizeException();
        }
        if (results.get(0) > 0) {
            return true;
        }
        return false;
    }

    public static void persistShippingMethods(
        List<ShippingMethodWrapper> addedOrModifiedTypes,
        List<ShippingMethodWrapper> typesToDelete)
        throws BiobankCheckException, Exception {
        if (addedOrModifiedTypes != null) {
            for (ShippingMethodWrapper ss : addedOrModifiedTypes) {
                ss.persist();
            }
        }
        if (typesToDelete != null) {
            for (ShippingMethodWrapper ss : typesToDelete) {
                ss.delete();
            }
        }
    }

    public boolean needDate() {
        String name = getName();
        return name != null && !name.equals(PICK_UP_NAME)
            && !name.equals(DROP_OFF_NAME);
    }

    public void checkUnique() throws BiobankException, ApplicationException {
        checkNoDuplicates(ShippingMethod.class,
            ShippingMethodPeer.NAME.getName(), getName(),
            ShippingMethodPeer.NAME.getName());
    }
}
