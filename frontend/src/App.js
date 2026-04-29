import { useEffect, useState } from "react";

function App() {
  const [books, setBooks] = useState([]);

  useEffect(() => {
    fetch("http://localhost:8080/book")
        .then((res) => res.json())
        .then((data) => setBooks(data));
  }, []);

  return (
      <div>
        <h1>Biblioteca</h1>
        <h2>Livros</h2>
        <ul>
          {books.map((book) => (
              <li key={book.id}>
                {book.details.titulo} — {book.details.autor}
              </li>
          ))}
        </ul>
      </div>
  );
}

export default App;