package br.senac.sp.tads.dsw.exemplo5.controller;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import br.senac.sp.tads.dsw.exemplo5.model.Departamento;
import br.senac.sp.tads.dsw.exemplo5.model.Funcionario;
import br.senac.sp.tads.dsw.exemplo5.repository.DepartamentoRepository;
import br.senac.sp.tads.dsw.exemplo5.repository.FuncionarioRepository;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class FuncionarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Departamento departamentoPadrao;

    @BeforeEach
    void setUp() {
        Departamento departamento = new Departamento();
        departamento.setNome("Desenvolvimento");
        departamento.setOrcamento(200000.00);
        departamentoPadrao = departamentoRepository.save(departamento);
    }

    @Test
    void deveCriarFuncionarioComSucesso() throws Exception {
        Funcionario funcionario = new Funcionario();
        funcionario.setNome("João Silva");
        funcionario.setDataContratacao(LocalDate.now());
        funcionario.setTrabalhoRemoto(true);
        funcionario.setDepartamento(departamentoPadrao);

        String jsonRequisicao = objectMapper.writeValueAsString(funcionario);

        mockMvc.perform(post("/api/funcionarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.trabalhoRemoto").value(true));
    }

    @Test
    void deveRetornarErro400AoCriarFuncionarioComDataFutura() throws Exception {
        Funcionario funcionario = new Funcionario();
        funcionario.setNome("Maria Souza");
        funcionario.setDataContratacao(LocalDate.now().plusDays(1));
        funcionario.setTrabalhoRemoto(false);
        funcionario.setDepartamento(departamentoPadrao);

        String jsonRequisicao = objectMapper.writeValueAsString(funcionario);

        mockMvc.perform(post("/api/funcionarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveListarTodosOsFuncionarios() throws Exception {
        Funcionario funcionario = new Funcionario();
        funcionario.setNome("Carlos Lima");
        funcionario.setDataContratacao(LocalDate.now().minusDays(10));
        funcionario.setTrabalhoRemoto(false);
        funcionario.setDepartamento(departamentoPadrao);
        funcionarioRepository.save(funcionario);

        mockMvc.perform(get("/api/funcionarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Carlos Lima"));
    }

    @Test
    void deveBuscarFuncionarioPorId() throws Exception {
        Funcionario funcionario = new Funcionario();
        funcionario.setNome("Ana Costa");
        funcionario.setDataContratacao(LocalDate.now().minusDays(30));
        funcionario.setTrabalhoRemoto(true);
        funcionario.setDepartamento(departamentoPadrao);
        Funcionario salvo = funcionarioRepository.save(funcionario);

        mockMvc.perform(get("/api/funcionarios/" + salvo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Costa"))
                .andExpect(jsonPath("$.trabalhoRemoto").value(true));
    }

    @Test
    void deveAtualizarFuncionario() throws Exception {
        Funcionario funcionario = new Funcionario();
        funcionario.setNome("Pedro Alves");
        funcionario.setDataContratacao(LocalDate.now().minusDays(20));
        funcionario.setTrabalhoRemoto(false);
        funcionario.setDepartamento(departamentoPadrao);
        Funcionario salvo = funcionarioRepository.save(funcionario);

        Funcionario atualizado = new Funcionario();
        atualizado.setNome("Pedro Alves Atualizado");
        atualizado.setDataContratacao(LocalDate.now().minusDays(15));
        atualizado.setTrabalhoRemoto(true);
        atualizado.setDepartamento(departamentoPadrao);
        String jsonAtualizado = objectMapper.writeValueAsString(atualizado);

        mockMvc.perform(put("/api/funcionarios/" + salvo.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonAtualizado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Pedro Alves Atualizado"))
                .andExpect(jsonPath("$.trabalhoRemoto").value(true));
    }

    @Test
    void deveApagarFuncionario() throws Exception {
        Funcionario funcionario = new Funcionario();
        funcionario.setNome("Lucas Santos");
        funcionario.setDataContratacao(LocalDate.now().minusDays(5));
        funcionario.setTrabalhoRemoto(false);
        funcionario.setDepartamento(departamentoPadrao);
        Funcionario salvo = funcionarioRepository.save(funcionario);

        mockMvc.perform(delete("/api/funcionarios/" + salvo.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/funcionarios/" + salvo.getId()))
                .andExpect(status().isNotFound());
    }
}
