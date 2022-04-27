package fr.epita.assistant.jws.domain.entity;

import fr.epita.assistant.jws.utils.TodoState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor @NoArgsConstructor @Getter @Setter
public class GameEntity {

    public LocalDateTime starttime;
    public TodoState state;
    public List<PlayerEntity> players;
    public List<String> map;
    public long id;
}
