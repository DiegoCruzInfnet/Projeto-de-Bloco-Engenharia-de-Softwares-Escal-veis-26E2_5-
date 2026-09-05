package br.com.biblioteca.controller;

import br.com.biblioteca.dto.UserResponseDTO;
import br.com.biblioteca.model.Book;
import br.com.biblioteca.model.User;
import br.com.biblioteca.service.UserService;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserResponseDTO> findAll() {
        return userService.findAllDTO();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> ResponseEntity.ok(userService.toDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> save(@Valid @RequestBody User user) {
        return ResponseEntity.ok(userService.save(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();

    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id,@Valid @RequestBody User user) {
        return userService.findById(id)
                .map(existing -> ResponseEntity.ok(userService.save(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public List<UserResponseDTO> findByEmail(@PathVariable String email) {
        return userService.findByEmail(email)
                .stream()
                .map(userService::toDTO)
                .toList();
    }

    @GetMapping("/nome/{nome}")
    public List<UserResponseDTO> findByNome(@PathVariable String nome) {
        return userService.findByNome(nome)
                .stream()
                .map(userService::toDTO)
                .toList();
    }

    @GetMapping("/telefone/{telefone}")
    public List<UserResponseDTO> findByTelefone(@PathVariable String telefone) {
        return userService.findByTelefone(telefone)
                .stream()
                .map(userService::toDTO)
                .toList();
    }

}
