package br.com.biblioteca.repository;

import br.com.biblioteca.model.Book;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByDetailsTituloContainingIgnoreCase(String titulo);
    List<Book> findByDetailsAutorContainingIgnoreCase(String autor);
    List<Book> findByDetailsEditoraContainingIgnoreCase(String editora);
    List<Book> findByDetailsIsbn(String isbn);
}
