package org.wso2.carbon.registry.samples.populator.utils;

import java.util.HashMap;

public class PopulatorConstants {

    public static final String[] TEAMS_TAXONOMY = new String[] { "developmentTeam/newJersey/theRockStars",
            "developmentTeam/newJersey/topShelf", "developmentTeam/boston/thinkTank",
            "developmentTeam/boston/conceptCrew", "developmentTeam/boston/theDominators",
            "developmentTeam/chicago/centurions" };
    public static final String[] DATA_CENTER_TAXONOMY = new String[] { "dataCenters/boston", "dataCenters/austin",
            "dataCenters/vancouver", "dataCenters/singapore" };

    public static HashMap<String, String[]> getTaxa() {
        HashMap<String, String[]> taxaMap = new HashMap<String, String[]>();

        taxaMap.put("PurchaseFoodItemsService", new String[] { "BuyMoreServices/sales/foods" });
        taxaMap.put("GetFoodSaftyInfoService", new String[] { "BuyMoreServices/sales/foods" });
        taxaMap.put("GeneralPharmacyService", new String[] { "BuyMoreServices/sales/pharmacy" });
        taxaMap.put("PrescriptionPharmacyService", new String[] { "BuyMoreServices/sales/pharmacy" });
        taxaMap.put("PurchaseElectronicService", new String[] { "BuyMoreServices/sales/electronic" });
        taxaMap.put("WarrantyClaimService", new String[] { "BuyMoreServices/sales/electronic" });
        taxaMap.put("GeneralPurchaseService", new String[] { "BuyMoreServices/sales/groceries" });
        taxaMap.put("GeneralReturnService", new String[] { "BuyMoreServices/sales/returns" });
        taxaMap.put("ReturnQueryService", new String[] { "BuyMoreServices/sales/returns" });

        taxaMap.put("EventDetailService", new String[] { "BuyMoreServices/marketing/eventManagement" });
        taxaMap.put("UserRegistrationService", new String[] { "BuyMoreServices/marketing/eventManagement" });
        taxaMap.put("SubcriptionService", new String[] { "BuyMoreServices/marketing/eventManagement" });
        taxaMap.put("DegitalMediaService", new String[] { "BuyMoreServices/marketing/brandManagement/media" });
        taxaMap.put("LatestNewsService", new String[] { "BuyMoreServices/marketing/brandManagement/media" });
        taxaMap.put("PublicationSubcriptionService",
                new String[] { "BuyMoreServices/marketing/brandManagement/publication" });

        taxaMap.put("NonDurableStoreServcie", new String[] { "BuyMoreServices/inventory/storeManagement/foods" });
        taxaMap.put("GeneralStoreService", new String[] { "BuyMoreServices/inventory/storeManagement/foods",
                "BuyMoreServices/inventory/storeManagement/groceries" });
        taxaMap.put("GenericElectronicStoreService",
                new String[] { "BuyMoreServices/inventory/storeManagement/electronic" });
        taxaMap.put("GenericPharmacyStoreService",
                new String[] { "BuyMoreServices/inventory/storeManagement/pharmacy" });
        taxaMap.put("CargoManagmentService", new String[] { "BuyMoreServices/inventory/transportManagement" });
        taxaMap.put("TransportResourceManagmentService",
                new String[] { "BuyMoreServices/inventory/transportManagement" });
        taxaMap.put("TRansportPeopleManagmentServcie",
                new String[] { "BuyMoreServices/inventory/transportManagement" });
        taxaMap.put("LocalShippingServcie", new String[] { "BuyMoreServices/inventory/shipping/local" });
        taxaMap.put("InternationalShippingServcie",
                new String[] { "BuyMoreServices/inventory/shipping/international" });
        taxaMap.put("RegulerPostalService", new String[] { "BuyMoreServices/inventory/delivery/local/postal" });
        taxaMap.put("SpecialPostalService", new String[] { "BuyMoreServices/inventory/delivery/local/postal" });
        taxaMap.put("PosatalReturnService", new String[] { "BuyMoreServices/inventory/delivery/local/postal" });
        taxaMap.put("StoreDeliveryService", new String[] { "BuyMoreServices/inventory/delivery/local/nearestStore" });
        taxaMap.put("DHLDeliveryService", new String[] { "BuyMoreServices/inventory/delivery/local/courier",
                "BuyMoreServices/inventory/delivery/international/courier" });
        taxaMap.put("FedexDeliveryService", new String[] { "BuyMoreServices/inventory/delivery/local/courier",
                "BuyMoreServices/inventory/delivery/international/courier" });
        taxaMap.put("FedexReturnService", new String[] { "BuyMoreServices/inventory/delivery/local/courier" });
        taxaMap.put("UPSDeliveryService", new String[] { "BuyMoreServices/inventory/delivery/local/courier",
                "BuyMoreServices/inventory/delivery/international/courier" });
        taxaMap.put("InternationalPostalService",
                new String[] { "BuyMoreServices/inventory/delivery/international/postal" });

        taxaMap.put("LocalCorporateSupplerService",
                new String[] { "BuyMoreServices/supplerManagement/localCorporate" });
        taxaMap.put("LocalSMBSupplerService", new String[] { "BuyMoreServices/supplerManagement/smallAndMedium" });
        taxaMap.put("NASupplerService", new String[] { "BuyMoreServices/supplerManagement/foreign/nA" });
        taxaMap.put("NAPartnerSupplerService", new String[] { "BuyMoreServices/supplerManagement/foreign/nA" });
        taxaMap.put("EUSupplerService", new String[] { "BuyMoreServices/supplerManagement/foreign/eU" });
        taxaMap.put("APACSupplerService", new String[] { "BuyMoreServices/supplerManagement/foreign/aPAC" });

        taxaMap.put("InternalAuditsInqueryService", new String[] { "BuyMoreServices/audits" });
        taxaMap.put("CorporateAuditsInqueryService", new String[] { "BuyMoreServices/audits" });

        taxaMap.put("GeneralTeamService",
                new String[] { "BuyMoreServices/hR/store/general", "BuyMoreServices/hR/store/electric",
                        "BuyMoreServices/hR/administration" });
        taxaMap.put("PharmacyTeamService", new String[] { "BuyMoreServices/hR/store/pharmacy" });
        taxaMap.put("TransportTeamService", new String[] { "BuyMoreServices/hR/transport" });
        taxaMap.put("DriverAllocationService", new String[] { "BuyMoreServices/hR/transport" });

        taxaMap.put("InventoryTeamservice", new String[] { "BuyMoreServices/hR/inventoryAndProcessing" });
        taxaMap.put("PharmacyTeamService", new String[] { "BuyMoreServices/hR/store/pharmacy" });
        taxaMap.put("TransportTeamService", new String[] { "BuyMoreServices/hR/transport" });
        taxaMap.put("DriverAllocationService", new String[] { "BuyMoreServices/hR/transport" });

        taxaMap.put("ConsumerReturnsServcie", new String[] { "BuyMoreServices/accounting" });
        taxaMap.put("GeneralBillingServcie", new String[] { "BuyMoreServices/accounting" });
        taxaMap.put("ConsumerReturnsServcie", new String[] { "BuyMoreServices/accounting" });
        taxaMap.put("GeneralBillingServcie", new String[] { "BuyMoreServices/accounting" });
        taxaMap.put("TaxRefundServcie", new String[] { "BuyMoreServices/accounting" });
        taxaMap.put("TaxCalculationServcie", new String[] { "BuyMoreServices/accounting" });
        taxaMap.put("LocalSupperPaymentService", new String[] { "BuyMoreServices/accounting" });
        taxaMap.put("InternationalSupperPaymentService", new String[] { "BuyMoreServices/accounting" });

        taxaMap.put("GenraInqueryService", new String[] { "BuyMoreServices/administration/general" });
        taxaMap.put("LegalInqueryService", new String[] { "BuyMoreServices/administration/legal" });
        taxaMap.put("CSRInqueryService", new String[] { "BuyMoreServices/administration/cSR" });
        taxaMap.put("CSRNewsService", new String[] { "BuyMoreServices/administration/cSR" });

        return taxaMap;
    }
}