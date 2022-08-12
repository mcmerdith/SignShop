package org.wargamer2010.signshop.operations;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.LinkedList;
import java.util.List;

public class BankSign implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        signshopUtil.registerClickedMaterial(ssArgs.getSign().get(), ssArgs.getPlayer().get());
        ssArgs.bDoNotClearClickmap = true;
        ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("registered_bank_sign", null));
        return true;
    }

    @SuppressWarnings("DuplicateCondition")
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        List<Block> shops = Storage.get().getShopsWithMiscSetting("banksigns", signshopUtil.convertLocationToString(ssArgs.getSign().get().getLocation()));
        if(shops.isEmpty()) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("no_shop_linked_to_banksign", null));
        } else {
            StringBuilder profitshops = new StringBuilder();
            boolean first = true;
            Block bLast = shops.get(shops.size()-1);
            for(Block bTemp : shops) {
                Location loc = bTemp.getLocation();
                if(first) first = false;
                else if (bTemp != bLast) profitshops.append(", ");
                else if (bTemp == bLast) profitshops.append(" and ");
                profitshops.append("(").append(loc.getX()).append(", ").append(loc.getY()).append(", ").append(loc.getZ()).append(")");
            }
            ssArgs.setMessagePart("!bankshops", profitshops.toString());
            StringBuilder profits = new StringBuilder();
            List<String> names = new LinkedList<>();
            Sign sign = (Sign)ssArgs.getSign().get().getState();
            String[] lines = sign.getLines();
            if(!signshopUtil.lineIsEmpty(lines[1])) names.add(lines[1]);
            if(!signshopUtil.lineIsEmpty(lines[2])) names.add(lines[2]);
            first = true;
            String sLast = names.get(names.size()-1);
            for(String sTemp : names) {
                if(first) first = false;
                else if (!sLast.equals(sTemp)) profits.append(", ");
                else if (sLast.equals(sTemp)) profits.append(" and ");
                profits.append(sTemp);
            }
            ssArgs.setMessagePart("!bank", profits.toString());
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("bank_sign_linked_to_banks", ssArgs.getMessageParts()));
        }
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        return true;
    }
}
