import { useEffect, useState } from "react";

function App() {
    const [books, setBooks] = useState([]);
    const [selectedBook, setSelectedBook] = useState(null);
    const [avaliacoes, setAvaliacoes] = useState([]);
    const [nota, setNota] = useState(5);
    const [comentario, setComentario] = useState("");
    const [userId, setUserId] = useState(1);
    const [currentPage, setCurrentPage] = useState(1);
    const [disponivelMap, setDisponivelMap] = useState({});
    const booksPerPage = 5;

    useEffect(() => {
        fetch("http://localhost:8080/book")
            .then((res) => res.json())
            .then((data) => {
                setBooks(data);
                data.forEach((book) => {
                    fetch(`http://localhost:8082/loan/book/${book.id}`)
                        .then((res) => res.json())
                        .then((loans) => {
                            const ativo = loans.some((l) => l.details?.status === "ATIVO");
                            setDisponivelMap((prev) => ({ ...prev, [book.id]: !ativo }));
                        });
                });
            });
    }, []);

    const totalPages = Math.ceil(books.length / booksPerPage);
    const currentBooks = books.slice(
        (currentPage - 1) * booksPerPage,
        currentPage * booksPerPage
    );

    const selecionarLivro = (book) => {
        setSelectedBook(book);
        fetch(`http://localhost:8081/avaliacao/livro/${book.id}`)
            .then((res) => res.json())
            .then((data) => setAvaliacoes(data));
    };

    const fecharModal = () => {
        setSelectedBook(null);
        setAvaliacoes([]);
        setComentario("");
    };

    const enviarAvaliacao = () => {
        fetch("http://localhost:8081/avaliacao", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                bookId: selectedBook.id,
                userId: userId,
                nota: nota,
                comentario: comentario,
            }),
        })
            .then((res) => res.json())
            .then(() => {
                setComentario("");
                selecionarLivro(selectedBook);
            });
    };

    return (
        <div style={{ padding: "32px", fontFamily: "Arial", maxWidth: "900px", margin: "0 auto" }}>
            <h1 style={{ borderBottom: "2px solid #007bff", paddingBottom: "10px" }}>📚 Biblioteca</h1>

            {/* Lista de livros */}
            <div style={{ marginTop: "20px" }}>
                {currentBooks.map((book) => (
                    <div
                        key={book.id}
                        onClick={() => selecionarLivro(book)}
                        style={{
                            padding: "16px",
                            marginBottom: "10px",
                            border: "1px solid #ddd",
                            borderRadius: "8px",
                            cursor: "pointer",
                            display: "flex",
                            justifyContent: "space-between",
                            alignItems: "center",
                            background: "#fafafa",
                            transition: "background 0.2s",
                        }}
                        onMouseEnter={(e) => e.currentTarget.style.background = "#e8f0fe"}
                        onMouseLeave={(e) => e.currentTarget.style.background = "#fafafa"}
                    >
                        <div>
                            <strong style={{ fontSize: "16px" }}>{book.titulo}</strong>
                            <br />
                            <small style={{ color: "#666" }}>{book.autor} — {book.editora}</small>
                            <br />
                            <span style={{
                                fontSize: "12px",
                                fontWeight: "bold",
                                color: disponivelMap[book.id] === false ? "#dc3545" : "#28a745",
                            }}>
                {disponivelMap[book.id] === false ? "❌ Indisponível" : "✅ Disponível"}
              </span>
                        </div>
                        <span style={{ color: "#007bff", fontSize: "20px" }}>›</span>
                    </div>
                ))}
            </div>

            {/* Paginação */}
            <div style={{ display: "flex", justifyContent: "center", gap: "8px", marginTop: "20px" }}>
                <button
                    onClick={() => setCurrentPage((p) => Math.max(p - 1, 1))}
                    disabled={currentPage === 1}
                    style={{
                        padding: "8px 16px",
                        borderRadius: "6px",
                        border: "1px solid #ddd",
                        cursor: currentPage === 1 ? "not-allowed" : "pointer",
                        background: currentPage === 1 ? "#eee" : "white",
                    }}
                >
                    ← Anterior
                </button>

                {Array.from({ length: totalPages }, (_, i) => (
                    <button
                        key={i + 1}
                        onClick={() => setCurrentPage(i + 1)}
                        style={{
                            padding: "8px 14px",
                            borderRadius: "6px",
                            border: "1px solid #ddd",
                            cursor: "pointer",
                            background: currentPage === i + 1 ? "#007bff" : "white",
                            color: currentPage === i + 1 ? "white" : "black",
                            fontWeight: currentPage === i + 1 ? "bold" : "normal",
                        }}
                    >
                        {i + 1}
                    </button>
                ))}

                <button
                    onClick={() => setCurrentPage((p) => Math.min(p + 1, totalPages))}
                    disabled={currentPage === totalPages}
                    style={{
                        padding: "8px 16px",
                        borderRadius: "6px",
                        border: "1px solid #ddd",
                        cursor: currentPage === totalPages ? "not-allowed" : "pointer",
                        background: currentPage === totalPages ? "#eee" : "white",
                    }}
                >
                    Próxima →
                </button>
            </div>

            {/* Modal */}
            {selectedBook && (
                <div
                    style={{
                        position: "fixed",
                        top: 0, left: 0, right: 0, bottom: 0,
                        background: "rgba(0,0,0,0.5)",
                        display: "flex",
                        justifyContent: "center",
                        alignItems: "center",
                        zIndex: 1000,
                    }}
                    onClick={fecharModal}
                >
                    <div
                        style={{
                            background: "white",
                            borderRadius: "12px",
                            padding: "32px",
                            width: "600px",
                            maxHeight: "80vh",
                            overflowY: "auto",
                        }}
                        onClick={(e) => e.stopPropagation()}
                    >
                        {/* Cabeçalho do modal */}
                        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                            <h2 style={{ margin: 0 }}>{selectedBook.titulo}</h2>
                            <button
                                onClick={fecharModal}
                                style={{ background: "none", border: "none", fontSize: "24px", cursor: "pointer" }}
                            >
                                ✕
                            </button>
                        </div>
                        <p style={{ color: "#666" }}>{selectedBook.autor} — {selectedBook.editora}</p>
                        <p><strong>ISBN:</strong> {selectedBook.isbn} | <strong>Gênero:</strong> {selectedBook.genero}</p>

                        <div style={{ marginBottom: "12px" }}>
              <span style={{
                  fontSize: "13px",
                  fontWeight: "bold",
                  padding: "4px 10px",
                  borderRadius: "12px",
                  background: disponivelMap[selectedBook.id] === false ? "#fdecea" : "#e6f4ea",
                  color: disponivelMap[selectedBook.id] === false ? "#dc3545" : "#28a745",
              }}>
                {disponivelMap[selectedBook.id] === false ? "❌ Indisponível" : "✅ Disponível"}
              </span>
                        </div>

                        <hr />

                        {/* Avaliações */}
                        <h3>Avaliações</h3>
                        {avaliacoes.length === 0 ? (
                            <p style={{ color: "#888" }}>Nenhuma avaliação ainda.</p>
                        ) : (
                            avaliacoes.map((av) => (
                                <div
                                    key={av.id}
                                    style={{
                                        padding: "10px",
                                        marginBottom: "8px",
                                        border: "1px solid #eee",
                                        borderRadius: "6px",
                                    }}
                                >
                                    <strong>⭐ {av.nota}/5</strong> — Usuário {av.userId}
                                    <br />
                                    <span style={{ color: "#444" }}>{av.comentario}</span>
                                </div>
                            ))
                        )}

                        <hr />

                        {/* Formulário de avaliação */}
                        <h3>Avaliar</h3>
                        <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
                            <label>
                                ID do Usuário:
                                <input
                                    type="number"
                                    value={userId}
                                    onChange={(e) => setUserId(Number(e.target.value))}
                                    style={{ marginLeft: "10px", width: "60px" }}
                                />
                            </label>
                            <label>
                                Nota (1-5):
                                <input
                                    type="number"
                                    min="1"
                                    max="5"
                                    value={nota}
                                    onChange={(e) => setNota(Number(e.target.value))}
                                    style={{ marginLeft: "10px", width: "60px" }}
                                />
                            </label>
                            <label>
                                Comentário:
                                <textarea
                                    value={comentario}
                                    onChange={(e) => setComentario(e.target.value)}
                                    rows="3"
                                    style={{ display: "block", width: "100%", marginTop: "4px" }}
                                />
                            </label>
                            <button
                                onClick={enviarAvaliacao}
                                style={{
                                    padding: "10px",
                                    background: "#007bff",
                                    color: "white",
                                    border: "none",
                                    borderRadius: "6px",
                                    cursor: "pointer",
                                }}
                            >
                                Enviar Avaliação
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default App;