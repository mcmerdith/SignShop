package org.wargamer2010.signshop.configuration.orm.typemapping.conversion;

import org.wargamer2010.signshop.player.PlayerIdentifier;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class SignShopPlayerConverter implements SSAttributeConverter<SignShopPlayer, String> {
    @Override
    public String convertToDatabaseColumn(SignShopPlayer signShopPlayer) {
        if (signShopPlayer == null) return null;
        return signShopPlayer.GetIdentifier().toString();
    }

    @Override
    public SignShopPlayer convertToModelAttribute(String s) {
        return PlayerIdentifier.getPlayerFromString(s);
    }
}
