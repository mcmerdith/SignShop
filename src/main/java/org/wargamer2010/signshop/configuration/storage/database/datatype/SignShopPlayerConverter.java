package org.wargamer2010.signshop.configuration.storage.database.datatype;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.wargamer2010.signshop.player.PlayerIdentifier;
import org.wargamer2010.signshop.player.SignShopPlayer;

@Converter
public class SignShopPlayerConverter implements AttributeConverter<SignShopPlayer, String> {
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
