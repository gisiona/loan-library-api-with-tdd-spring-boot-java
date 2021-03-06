package br.com.loanlibrary.service;

import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.loanlibrary.api.exception.BusinessException;
import br.com.loanlibrary.model.entity.Book;
import br.com.loanlibrary.model.repository.BookRepository;
import br.com.loanlibrary.service.impl.BookServiceImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class BookServiceTest {

	BookService bookService;
	
	@MockBean 
	BookRepository bookRepository;
	
	private static final String MSG_ERRO_ISBN_DUPLICADO = "ISBN já cadastrado.";
	
	@BeforeEach
	public void init() {
		bookService = new BookServiceImpl(bookRepository);
	}
	
	@Test
	@DisplayName("Deve salvar um novo livro com sucesso.")
	void deveSalvarUmLivroComSucessoTest() {
		// cenario
		Book book = getBookEntity();
		
		// execucao
		Mockito.when(bookRepository.save(book)).thenReturn(getBookEntity());
		Book bookSalvo = bookService.save(book);
		
		log.info("Livro Salvo: {}", bookSalvo);
		
		// validacao
		Assertions.assertThat(bookSalvo.getCodigo()).isNotNull();		
		Assertions.assertThat(bookSalvo.getTitulo()).isEqualTo(book.getTitulo());
		Assertions.assertThat(bookSalvo.getAutor()).isEqualTo(book.getAutor());
		Assertions.assertThat(bookSalvo.getIsbn()).isEqualTo(book.getIsbn());
		Assertions.assertThat(bookSalvo.getDataCadastro()).isNotNull();
	}
	
	@Test
	@DisplayName("Deve lancar erro de negocio ao tentar salvar um livro com ISBN ja existente.")
	void createLancarErroAoTentarSalvarUmBookWithIsbnDuplicadoTest() {
		// cenario
		Book book = getBookEntity();
		Mockito.when(bookRepository.existsByIsbn(Mockito.anyString())).thenReturn(Boolean.TRUE);
		
		// execucao
		Throwable exception = Assertions.catchThrowable( () -> bookService.save(book));
		
		log.info("ISBN duplicado: {}", exception.getMessage());
							
		// validacao
		Assertions.assertThat(exception)
			.isInstanceOf(BusinessException.class)
			.hasMessage(MSG_ERRO_ISBN_DUPLICADO);
		
		// valida que o metodo save() nao foi executado nenhuma vez
		Mockito.verify(bookRepository, Mockito.never()).save(book);
	}


	private Book getBookEntity() {		
		return Book.builder()
				.codigo(1L)
				.titulo("Aprenda programar em uma semana")
				.autor("Gisiona")
				.isbn("123456")
				.dataCadastro(LocalDateTime.now())
				.build();
	}
}
