package fr.epita.assistant.jws.presentation.rest.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@With
@Getter
@Setter
public class MoveRequestDTO {
    public int posX;
    public int posY;
}
