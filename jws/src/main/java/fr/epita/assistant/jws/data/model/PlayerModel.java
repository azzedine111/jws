package fr.epita.assistant.jws.data.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "player")
@AllArgsConstructor @NoArgsConstructor @With
public class PlayerModel extends PanacheEntityBase {
    public @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    public LocalDateTime lastbomb;
    public LocalDateTime lastmovement;
    public int lives;
    public String name;
    public int posX;
    public int posY;
    public int position;
    public @ManyToOne GameModel game;
}