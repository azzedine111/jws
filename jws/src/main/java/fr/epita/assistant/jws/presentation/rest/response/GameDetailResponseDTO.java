package fr.epita.assistant.jws.presentation.rest.response;

import fr.epita.assistant.jws.domain.entity.GameEntity;
import fr.epita.assistant.jws.domain.entity.PlayerEntity;
import fr.epita.assistant.jws.utils.TodoState;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor @NoArgsConstructor @With
public class GameDetailResponseDTO {

    public LocalDateTime startTime;
    public TodoState state;
    public List<Player> players;
    public List<String> map;
    public long id;

    @AllArgsConstructor @NoArgsConstructor @With
    public static class Player {

        public Long id;
        public String name;
        public int lives;
        public int posX;
        public int posY;
    }
}
