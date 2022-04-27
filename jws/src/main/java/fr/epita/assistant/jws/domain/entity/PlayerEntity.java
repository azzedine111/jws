package fr.epita.assistant.jws.domain.entity;

import fr.epita.assistant.jws.data.model.GameModel;
import fr.epita.assistant.jws.data.model.PlayerModel;
import fr.epita.assistant.jws.utils.TodoState;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor @NoArgsConstructor @With
public class PlayerEntity {
    public Long id;
    public LocalDateTime lastbomb;
    public LocalDateTime lastmovement;
    public int lives;
    public String name;
    public int posX;
    public int posY;
    public int position;

}
