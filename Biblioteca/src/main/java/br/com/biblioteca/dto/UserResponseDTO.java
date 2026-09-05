package br.com.biblioteca.dto;

import br.com.biblioteca.model.vo.UserDetails;

public record UserResponseDTO(
        Long id,
        String nome,
        String email,
        String telefone

) {
}
