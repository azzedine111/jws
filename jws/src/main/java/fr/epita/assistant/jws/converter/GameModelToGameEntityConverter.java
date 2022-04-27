package fr.epita.assistant.jws.converter;

import fr.epita.assistant.jws.data.model.GameModel;
import fr.epita.assistant.jws.data.model.PlayerModel;
import fr.epita.assistant.jws.domain.entity.GameEntity;
import fr.epita.assistant.jws.domain.entity.PlayerEntity;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.stream.Collectors;

@ApplicationScoped

public class GameModelToGameEntityConverter {
    public GameEntity convert(GameModel gameModel){
        return new GameEntity(

                gameModel.starttime,
                gameModel.state,
                gameModel.players.stream().map(lambda -> convertItem(lambda)).collect(Collectors.toList()),
                new ArrayList<>(gameModel.map),
                gameModel.id
        );
    }

    public PlayerEntity convertItem(PlayerModel playerModel){
        return new PlayerEntity(
                playerModel.id,
                playerModel.lastbomb,
                playerModel.lastmovement,
                playerModel.lives,
                playerModel.name,
                playerModel.posX,
                playerModel.posY,
                playerModel.position
        );
    }
}
