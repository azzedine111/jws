package fr.epita.assistant.jws.converter;

import fr.epita.assistant.jws.domain.entity.GameEntity;
import fr.epita.assistant.jws.domain.entity.PlayerEntity;
import fr.epita.assistant.jws.presentation.rest.response.GameDetailResponseDTO;

import javax.enterprise.context.ApplicationScoped;
import java.util.stream.Collectors;

@ApplicationScoped
public class GameEntityToGameDetailResponseDTO {
    public GameDetailResponseDTO convert(GameEntity entity){
        return new GameDetailResponseDTO()
                .withId(entity.id)
                .withStartTime(entity.starttime)
                .withState(entity.state)
                .withPlayers(entity.players.stream().map(this::convertplayer).collect(Collectors.toList()))
                .withMap(entity.map);
    }

    public GameDetailResponseDTO.Player convertplayer(PlayerEntity playerEntity){
        return new GameDetailResponseDTO.Player()
                .withId(playerEntity.id)
                .withName(playerEntity.name)
                .withLives(playerEntity.lives)
                .withPosX(playerEntity.posX)
                .withPosY(playerEntity.posY);
    }
}
