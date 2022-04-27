package fr.epita.assistant.jws.data.model;

import fr.epita.assistant.jws.utils.TodoState;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity @Table(name = "game")
@AllArgsConstructor @NoArgsConstructor @With
public class GameModel extends PanacheEntityBase {
    public @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    public LocalDateTime starttime;
    public TodoState state;
    public @ElementCollection @CollectionTable(name = "game_map") List<String> map;
    public @OneToMany(cascade = CascadeType.ALL) List<PlayerModel> players;
}