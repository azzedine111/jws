package fr.epita.assistant.jws.presentation.rest.response;

import fr.epita.assistant.jws.utils.TodoState;
import lombok.*;

@AllArgsConstructor @NoArgsConstructor @With @Getter @Setter
public class GameResponseDTO {
    Long id;
    TodoState state;
    int player_size;
}
