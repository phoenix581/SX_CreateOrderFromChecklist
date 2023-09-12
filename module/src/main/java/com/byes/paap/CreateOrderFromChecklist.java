package com.byes.paap;

import com.planonsoftware.platform.backend.businessrule.v3.IBusinessRule;
import com.planonsoftware.platform.backend.businessrule.v3.IBusinessRuleContext;
import com.planonsoftware.platform.backend.data.v1.IBusinessObject;

public class CreateOrderFromChecklist implements IBusinessRule {

    @Override
    public void execute(IBusinessObject newBO, IBusinessObject oldBO, IBusinessRuleContext context) {
        IBusinessObject reason = newBO.getReferenceFieldByName("ReasonRef").getValue();

        if (reason != null) {
            String reasonCode = reason.getStringFieldByName("Code").getValue();

            if ("NME".equals(reasonCode)) {
                IBusinessObject maintenanceActivity = newBO.getReferenceFieldByName("MaintenanceActivityRef").getValue();
                IBusinessObject maintenanceOrder = maintenanceActivity.getReferenceFieldByName("OrderRef").getValue();

                IBusinessObject newOrder = context.getDataService().create("UsrRequest");
                newOrder.getStringFieldByName("Description").setValue("Aus Checkliste: " + newBO.getStringFieldByName("Remark").getValue());
                int personPK = context.getUserService().getPersonPrimaryKey();
                newOrder.getReferenceFieldByName("InternalRequestorPersonRef").setValueAsInteger(personPK);
                newOrder.getCodesCodeNameFieldByName("FreeString15").setValueAsString("2");
                newOrder.getReferenceFieldByName("PropertyRef").setValue(maintenanceOrder.getReferenceFieldByName("PropertyRef").getValue());

                IBusinessObject service = context.getDataService().getByUniqueStringField("ServiceAgreementService", "Code", "LOPD 04.01");
                IBusinessObject trade = context.getDataService().getByUniqueStringField("Trade", "Code", "W-HD");
                
                newOrder.getReferenceFieldByName("ServiceAgreementServiceRef").setValue(service);
                newOrder.getReferenceFieldByName("TradeRef").setValue(trade);

                IBusinessObject asset = maintenanceActivity.getReferenceFieldByName("BaseAssetRef").getValue();

                newOrder.getReferenceFieldByName("InventoryItemRef").setValue(asset);

                newOrder.save();
            }
        }
    }
}