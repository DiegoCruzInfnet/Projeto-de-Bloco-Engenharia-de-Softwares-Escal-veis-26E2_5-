package br.com.biblioteca.service;

import br.com.biblioteca.dto.BookResponseDTO;
import br.com.biblioteca.model.Book;
import br.com.biblioteca.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public Book save(Book book) {
        return bookRepository.save(book);
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    public List<Book> findByAutor(String author) {
        return bookRepository.findByDetailsAutorContainingIgnoreCase(author);
    }

    public List<Book> findByEditora(String editora) {
        return bookRepository.findByDetailsEditoraContainingIgnoreCase(editora);
    }

    public List<Book> findByTitulo(String titulo) {
        return bookRepository.findByDetailsTituloContainingIgnoreCase(titulo);
    }

    public List<Book> findByIsbn(String isbn) {
        return bookRepository.findByDetailsIsbn(isbn);
    }

    public BookResponseDTO toDTO(Book book) {
        return new BookResponseDTO(
                book.getId(),
                book.getDetails().getTitulo(),
                book.getDetails().getAutor(),
                book.getDetails().getIsbn(),
                book.getDetails().getGenero(),
                book.getDetails().getEditora()
        );
    }

    public List<BookResponseDTO> findAllDTO() {
        return bookRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }
}
