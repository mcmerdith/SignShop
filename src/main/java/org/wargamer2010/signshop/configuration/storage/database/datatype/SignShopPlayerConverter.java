package org.wargamer2010.signshop.configuration.storage.database.datatype;

import org.wargamer2010.signshop.configuration.orm.annotations.Converter;
import org.wargamer2010.signshop.player.PlayerIdentifier;
import org.wargamer2010.signshop.player.SignShopPlayer;

@Converter
public class SignShopPlayerConverter implements SSAttributeConverter<SignShopPlayer, String> {
    @Override
    public String convertToDatabaseColumn(SignShopPlayer signShopPlayer) {
        if (signShopPlayer == null) return null;
        return signShopPlayer.GetIdentifier().toString();
    }

    @Override
    public SignShopPlayer convertToEntityAttribute(String s) {
        return PlayerIdentifier.getPlayerFromString(s);
    }
}
