package br.com.knowledge.stockonyou.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.knowledge.stockonyou.api.dto.ItemVendaRequestDTO;
import br.com.knowledge.stockonyou.api.dto.VendaRequestDTO;
import br.com.knowledge.stockonyou.api.dto.VendaResponseDTO;
import br.com.knowledge.stockonyou.api.exception.BusinessException;
import br.com.knowledge.stockonyou.api.exception.ResourceNotFoundException;
import br.com.knowledge.stockonyou.api.model.Cliente;
import br.com.knowledge.stockonyou.api.model.ItemVenda;
import br.com.knowledge.stockonyou.api.model.Produto;
import br.com.knowledge.stockonyou.api.model.StatusVenda;
import br.com.knowledge.stockonyou.api.model.Venda;
import br.com.knowledge.stockonyou.api.repository.ClienteRepository;
import br.com.knowledge.stockonyou.api.repository.ProdutoRepository;
import br.com.knowledge.stockonyou.api.repository.VendaRepository;
import br.com.knowledge.stockonyou.api.specification.VendaSpecification;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendaService {
        private final VendaRepository vendaRepository;
        private final ProdutoRepository produtoRepository;
        private final ClienteRepository clienteRepository;

        @Transactional
        public VendaResponseDTO adicionarItemComanda(
                        Long comandaId,
                        ItemVendaRequestDTO itemDto) {
                Venda venda = buscarComanda(comandaId);
                if (venda.getStatus() != StatusVenda.ABERTA) {
                        throw new IllegalArgumentException("Somente comandas abertas podem ter itens adicionados.");
                }
                Produto produto = buscarProduto(itemDto.produtoId());
                Optional<ItemVenda> itemExistente = venda.getItens().stream()
                                .filter(item -> item.getProduto().getId().equals(itemDto.produtoId()))
                                .findFirst();
                if (itemExistente.isPresent()) {
                        ItemVenda item = itemExistente.get();
                        atualizarItemVenda(item, itemDto.quantidade());
                } else {
                        ItemVenda novoItem = criarItemVenda(venda, produto, itemDto.quantidade());
                        venda.getItens().add(novoItem);
                }
                venda.setValorTotal(calcularValorTotal(venda));
                Venda vendaSalva = vendaRepository.save(venda);
                List<String> alertas = verificarAlertasDaComanda(vendaSalva);
                return VendaResponseDTO.fromEntity(vendaSalva, alertas);
        }

        @Transactional(readOnly = true)
        public VendaResponseDTO buscarComandaAberta(Long clienteId) {
                return vendaRepository.findByClienteIdAndStatus(clienteId, StatusVenda.ABERTA)
                                .map(VendaResponseDTO::fromEntity)
                                .orElse(null);
        }

        @Transactional
        public VendaResponseDTO cancelarComanda(Long id) {
                Venda comanda = buscarComanda(id);
                validarComandaPodeSerCancelada(comanda);
                comanda.setStatus(StatusVenda.CANCELADA);
                return VendaResponseDTO.fromEntity(vendaRepository.save(comanda));
        }

        @Transactional
        public VendaResponseDTO concluirComanda(Long comandaId) {
                Venda venda = buscarComanda(comandaId);
                validarComandaPodeSerConcluida(venda);
                List<String> alertas = processarEstoqueDaConclusao(venda);
                venda.setStatus(StatusVenda.PENDENTE);
                Venda vendaSalva = vendaRepository.save(venda);
                return VendaResponseDTO.fromEntity(
                                vendaSalva,
                                alertas);
        }

        @Transactional
        public VendaResponseDTO criarComanda(VendaRequestDTO dto) {
                String username = obterUsuarioAtual();
                Cliente cliente = buscarCliente(dto.clienteId());
                validarClienteNaoPossuiComandaAberta(cliente.getId());
                Venda venda = criarVenda(
                                cliente,
                                cliente.getNome(),
                                username,
                                StatusVenda.ABERTA);
                for (ItemVendaRequestDTO itemDto : dto.itens()) {
                        Produto produto = buscarProduto(itemDto.produtoId());
                        ItemVenda novoItem = criarItemVenda(venda, produto, itemDto.quantidade());
                        venda.getItens().add(novoItem);
                }
                venda.setValorTotal(calcularValorTotal(venda));
                Venda vendaSalva = vendaRepository.save(venda);
                List<String> alertas = verificarAlertasDaComanda(vendaSalva);
                return VendaResponseDTO.fromEntity(vendaSalva, alertas);
        }

        @Transactional(readOnly = true)
        public List<VendaResponseDTO> listarComandasAbertas() {
                return vendaRepository.findByStatus(StatusVenda.ABERTA).stream()
                                .map(VendaResponseDTO::fromEntity)
                                .toList();
        }

        @Transactional
        public Page<VendaResponseDTO> listarComFiltros(
                        String clienteNome,
                        StatusVenda status,
                        String dataInicio,
                        String dataFim,
                        Pageable pageable) {
                Specification<Venda> spec = VendaSpecification.comFiltros(clienteNome, status, dataInicio, dataFim);
                return vendaRepository.findAll(spec, pageable)
                                .map(VendaResponseDTO::fromEntity);
        }

        @Transactional
        public VendaResponseDTO realizarVenda(VendaRequestDTO dto) {
                String username = obterUsuarioAtual();
                Cliente cliente = null;
                List<String> alertas = new ArrayList<>();
                if (dto.clienteId() != null) {
                        validarClienteNaoPossuiComandaAberta(dto.clienteId());
                        cliente = buscarCliente(dto.clienteId());
                        throw new IllegalArgumentException(
                                        "Para clientes cadastrados, utilize o fluxo de gerenciamento e fechamento de comandas.");
                }
                Venda venda = criarVenda(
                                cliente,
                                dto.clienteNome(),
                                username,
                                StatusVenda.PAGO);
                for (ItemVendaRequestDTO itemDto : dto.itens()) {
                        Produto produto = buscarProduto(itemDto.produtoId());
                        alertas.addAll(
                                        verificarAlertasDeEstoque(
                                                        produto,
                                                        itemDto.quantidade()));
                        baixarEstoque(produto, itemDto.quantidade());
                        ItemVenda novoItem = criarItemVenda(venda, produto, itemDto.quantidade());
                        venda.getItens().add(novoItem);
                }
                venda.setValorTotal(calcularValorTotal(venda));
                Venda vendaSalva = vendaRepository.save(venda);
                return VendaResponseDTO.fromEntity(vendaSalva, alertas);
        }

        @Transactional
        public VendaResponseDTO registrarPagamento(Long id) {
                Venda venda = vendaRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Venda/Comanda nao encontrada com id:" + id));
                if (venda.getStatus() != StatusVenda.PENDENTE) {
                        throw new IllegalArgumentException(
                                        "Somente vendas/comandas pendentes podem ter o pagamento registrado.");
                }
                venda.setStatus(StatusVenda.PAGO);
                return VendaResponseDTO.fromEntity(vendaRepository.save(venda));
        }

        // Métodos auxiliares
        private void atualizarItemVenda(
                        ItemVenda item,
                        int quantidadeAdicionar) {
                int novaQuantidade = item.getQuantidade() + quantidadeAdicionar;
                item.setQuantidade(novaQuantidade);
                item.setSubtotal(calcularSubtotal(item.getPrecoUnitario(), novaQuantidade));
        }

        private void baixarEstoque(Produto produto, int quantidade) {
                validarEstoqueDisponivel(produto, quantidade);
                produto.setQuantidade(produto.getQuantidade() - quantidade);
                produtoRepository.save(produto);
        }

        private void baixarEstoqueDaComanda(Venda venda) {
                for (ItemVenda item : venda.getItens()) {
                        baixarEstoque(item.getProduto(), item.getQuantidade());
                }
        }

        private Cliente buscarCliente(Long id) {
                return clienteRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Cliente nao encontrado com o ID: " + id));
        }

        private Venda buscarComanda(Long id) {
                return vendaRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Comanda não encontrada com id: " + id));
        }

        private Produto buscarProduto(Long id) {
                return produtoRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Produto nao encontrado com o ID: " + id));
        }

        private int calcularEstoqueAposComanda(
                        Produto produto,
                        int quantidade,
                        Long comandaAtualId) {
                int estoqueDisponivelAntesDaComanda = calcularEstoqueDisponivelParaComanda(produto, comandaAtualId);
                return estoqueDisponivelAntesDaComanda - quantidade;
        }

        private int calcularEstoqueDisponivelParaComanda(Produto produto, Long comandaId) {
                int quantidadeComprometida = calcularQuantidadeComprometida(
                                produto.getId(),
                                comandaId);
                return produto.getQuantidade()
                                - quantidadeComprometida;
        }

        private int calcularQuantidadeComprometida(Long produtoId, Long comandaAtualId) {
                return vendaRepository.findByStatus(StatusVenda.ABERTA)
                                .stream()
                                .filter(venda -> !venda.getId().equals(comandaAtualId))
                                .flatMap(venda -> venda.getItens().stream())
                                .filter(item -> item.getProduto().getId().equals(produtoId))
                                .mapToInt(ItemVenda::getQuantidade)
                                .sum();

        }

        private BigDecimal calcularSubtotal(
                        BigDecimal precoUnitario,
                        int quantidade) {
                return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        }

        private BigDecimal calcularValorTotal(Venda venda) {
                return venda.getItens().stream()
                                .map(ItemVenda::getSubtotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        private String criarAlertaEstoqueMinimo(Produto produto) {
                return "O estoque do produto "
                                + produto.getNome()
                                + " ficara abaixo da quantidade mínima.";
        }

        private ItemVenda criarItemVenda(
                        Venda venda,
                        Produto produto,
                        int quantidade) {
                BigDecimal precoUnitario = produto.getPreco();
                BigDecimal subtotal = calcularSubtotal(precoUnitario, quantidade);
                return ItemVenda.builder()
                                .venda(venda)
                                .produto(produto)
                                .quantidade(quantidade)
                                .precoUnitario(precoUnitario)
                                .subtotal(subtotal)
                                .build();
        }

        private Venda criarVenda(
                        Cliente cliente,
                        String clienteNome,
                        String username,
                        StatusVenda status) {
                return Venda.builder()
                                .dataVenda(LocalDateTime.now())
                                .clienteNome(clienteNome)
                                .cliente(cliente)
                                .usuarioNome(username)
                                .valorTotal(BigDecimal.ZERO)
                                .status(status)
                                .itens(new ArrayList<>())
                                .build();
        }

        private boolean estoqueAbaixoDoMinimo(
                        Produto produto,
                        int estoqueProjetado) {
                return estoqueProjetado < produto.getQuantidadeMinima();
        }

        private String obterUsuarioAtual() {
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (principal instanceof Jwt jwt) {
                        return jwt.getClaimAsString("preferred_username");
                }
                return "Desconhecido";
        }

        private List<String> processarEstoqueDaConclusao(Venda venda) {
                validarEstoqueDaComanda(venda);
                List<String> alertas = verificarAlertasDaConclusao(venda);
                baixarEstoqueDaComanda(venda);
                return alertas;
        }

        private void validarClienteNaoPossuiComandaAberta(Long clienteId) {
                boolean jaTemComanda = vendaRepository.existsByClienteIdAndStatus(clienteId, StatusVenda.ABERTA);
                if (jaTemComanda) {
                        throw new IllegalArgumentException("Este cliente possui uma comanda aberta no sistema.");
                }
        }

        private void validarComandaPodeSerCancelada(Venda comanda) {
                if (comanda.getStatus() != StatusVenda.ABERTA) {
                        throw new IllegalArgumentException("Somente comandas abertas podem ser canceladas.");
                }
        }

        private void validarComandaPodeSerConcluida(Venda venda) {
                if (venda.getStatus() != StatusVenda.ABERTA) {
                        throw new IllegalArgumentException("Esta comanda já foi finalizada");
                }
        }

        private void validarEstoqueDaComanda(Venda venda) {
                for (ItemVenda item : venda.getItens()) {
                        Produto produto = item.getProduto();
                        int estoqueDisponivel = calcularEstoqueDisponivelParaComanda(produto, venda.getId());
                        if (estoqueDisponivel < item.getQuantidade()) {
                                throw new BusinessException(
                                                "Estoque insuficiente para o produto: " + produto.getNome());
                        }
                }
        }

        private void validarEstoqueDisponivel(Produto produto, int quantidade) {
                if (produto.getQuantidade() < quantidade) {
                        throw new BusinessException(
                                        "Estoque insuficiente para o produto: " + produto.getNome());
                }
        }

        private List<String> verificarAlertasDeEstoque(
                        Produto produto,
                        int quantidade) {
                List<String> alertas = new ArrayList<>();
                int estoqueAposVenda = produto.getQuantidade() - quantidade;
                if (estoqueAbaixoDoMinimo(produto, estoqueAposVenda)) {
                        alertas.add(criarAlertaEstoqueMinimo(produto));
                }
                return alertas;
        }

        private List<String> verificarAlertasDaComanda(Venda venda) {
                List<String> alertas = new ArrayList<>();
                for (ItemVenda item : venda.getItens()) {
                        Produto produto = item.getProduto();
                        int estoqueAposComanda = calcularEstoqueAposComanda(produto, item.getQuantidade(),
                                        venda.getId());
                        if (estoqueAbaixoDoMinimo(produto, estoqueAposComanda)) {
                                alertas.add(
                                                criarAlertaEstoqueMinimo(produto));
                        }
                }
                return alertas;
        }

        private List<String> verificarAlertasDaConclusao(Venda venda) {
                List<String> alertas = new ArrayList<>();
                for (ItemVenda item : venda.getItens()) {
                        alertas.addAll(
                                verificarAlertasDeEstoque(item.getProduto(), item.getQuantidade())
                        );
                }
                return alertas;
        }

}
