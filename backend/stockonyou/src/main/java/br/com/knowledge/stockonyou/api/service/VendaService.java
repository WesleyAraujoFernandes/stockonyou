package br.com.knowledge.stockonyou.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendaService {
    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public List<VendaResponseDTO> listarComandasAbertas() {
        return vendaRepository.findByStatus(StatusVenda.ABERTA).stream()
            .map(VendaResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional
    public VendaResponseDTO atualizarComandaAberta(Long clienteId, ItemVendaRequestDTO itemDto) {
        Venda venda = vendaRepository.findByClienteIdAndStatus(clienteId, StatusVenda.ABERTA)
                .orElseGet(() -> {
                    Cliente cliente = clienteRepository.findById(clienteId)
                            .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID:"+clienteId));
                    return Venda.builder()
                            .cliente(cliente)
                            .status(StatusVenda.ABERTA)
                            .itens(new ArrayList<>())
                            .valorTotal(BigDecimal.ZERO)
                            .build();
                });
        Produto produto = produtoRepository.findById(itemDto.produtoId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado."));
        Optional<ItemVenda> itemExistente = venda.getItens().stream()
                .filter(i -> i.getProduto().getId().equals(itemDto.produtoId()))
                .findFirst();
        if (itemExistente.isPresent()) {
            ItemVenda item = itemExistente.get();
            item.setQuantidade(itemDto.quantidade());
            item.setSubtotal(item.getPrecoUnitario().multiply(BigDecimal.valueOf(itemDto.quantidade())));
        } else {
            ItemVenda novoItem = ItemVenda.builder()
                    .venda(venda)
                    .produto(produto)
                    .quantidade(itemDto.quantidade())
                    .precoUnitario(produto.getPreco())
                    .subtotal(produto.getPreco().multiply(BigDecimal.valueOf(itemDto.quantidade())))
                    .build();
            venda.getItens().add(novoItem); 
        }
        BigDecimal total = venda.getItens().stream()
                .map(ItemVenda::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        venda.setValorTotal(total);
        return VendaResponseDTO.fromEntity(vendaRepository.save(venda));
    }



    @Transactional
    public VendaResponseDTO realizarVenda(VendaRequestDTO dto) {
        String username = "Sistema";
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Jwt jwt) {
            username = jwt.getClaimAsString("preferred_username");
        }

        Long idBusca = (dto.clienteId() != null) ? dto.clienteId() : 1L;
        Cliente cliente = clienteRepository.findById(idBusca)
                .orElseGet(() -> clienteRepository.findById(1L).orElseThrow(
                        () -> new ResourceNotFoundException("Cliente Padrão não cadastrado.")));

        Venda venda;
        boolean ehClientePadrao = cliente.getId().equals(1L);

        if (!ehClientePadrao) {
            // Busca se o cliente já tem uma comanda ABERTA
            venda = vendaRepository.findByClienteIdAndStatus(cliente.getId(), StatusVenda.ABERTA)
                    .orElseGet(() -> Venda.builder()
                            .dataVenda(LocalDateTime.now())
                            .clienteNome(cliente.getNome())
                            .cliente(cliente)
                            .usuarioNome(SecurityContextHolder.getContext().getAuthentication().getName())
                            .valorTotal(BigDecimal.ZERO)
                            .status(StatusVenda.ABERTA) // Abre uma nova comanda
                            .itens(new ArrayList<>())
                            .build());
        } else {
            // Cliente padrão sempre abre e fecha na hora (Venda direta de balcão)
            venda = Venda.builder()
                    .dataVenda(LocalDateTime.now())
                    .clienteNome(cliente.getNome())
                    .cliente(cliente)
                    .usuarioNome(username)
                    .valorTotal(BigDecimal.ZERO)
                    .status(StatusVenda.FINALIZADA)
                    .itens(new ArrayList<>())
                    .build();
        }

        BigDecimal valorAdicionalItens = BigDecimal.ZERO;

        for (ItemVendaRequestDTO itemDto : dto.itens()) {
            Produto produto = produtoRepository.findById(itemDto.produtoId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Produto não encontrado ID: " + itemDto.produtoId()));

            if (produto.getQuantidade() < itemDto.quantidade()) {
                throw new IllegalArgumentException("Estoque insuficiente para: " + produto.getNome());
            }

            produto.setQuantidade(produto.getQuantidade() - itemDto.quantidade());
            produtoRepository.save(produto);

            BigDecimal subtotal = itemDto.precoUnitario().multiply(BigDecimal.valueOf(itemDto.quantidade()));
            valorAdicionalItens = valorAdicionalItens.add(subtotal);

            // Verifica se o produto já existe na comanda aberta para somar a quantidade,
            // senão cria um novo item
            ItemVenda itemExistente = venda.getItens().stream()
                    .filter(i -> i.getProduto().getId().equals(produto.getId()))
                    .findFirst()
                    .orElse(null);

            if (itemExistente != null) {
                itemExistente.setQuantidade(itemExistente.getQuantidade() + itemDto.quantidade());
                itemExistente.setSubtotal(itemExistente.getSubtotal().add(subtotal));
            } else {
                ItemVenda novoItem = ItemVenda.builder()
                        .venda(venda)
                        .produto(produto)
                        .quantidade(itemDto.quantidade())
                        .precoUnitario(itemDto.precoUnitario())
                        .subtotal(subtotal)
                        .build();
                venda.getItens().add(novoItem);
            }
        }

        // Soma o valor dos novos itens ao valor total acumulado da comanda
        venda.setValorTotal(venda.getValorTotal().add(valorAdicionalItens));
        Venda vendaSalva = vendaRepository.save(venda);

        return VendaResponseDTO.fromEntity(vendaSalva);
    }

    @Transactional
    public void finalizarComanda(Long id) {
        Venda comanda = vendaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Comanda não encontrada com id:"+id));
        if (comanda.getStatus() == StatusVenda.FINALIZADA) {
            throw new IllegalArgumentException("Esta comanda ja foi finalizada.");
        }
        comanda.setStatus(StatusVenda.FINALIZADA);
        vendaRepository.save(comanda);
    }

    @Transactional
    public VendaResponseDTO finalizarComanda(Long comandaId, StatusVenda novoStatus) {
        if (novoStatus == StatusVenda.ABERTA) {
            throw new IllegalArgumentException("O novo status deve ser PAGO ou PENDENTE.");
        }
        Venda venda = vendaRepository.findById(comandaId)
            .orElseThrow(() -> new ResourceNotFoundException("Comanda nao encontrada com id:"+comandaId));
        if (venda.getStatus() != StatusVenda.ABERTA) {
            throw new IllegalArgumentException("Esta comanda já foi finalizada");
        }
        for (ItemVenda item : venda.getItens()) {
            Produto produto = item.getProduto();
            if (produto.getQuantidade() < item.getQuantidade()) {
                throw new BusinessException("Estoque insuficiente para o produto: " + produto.getNome());
            }
            produto.setQuantidade(produto.getQuantidade() - item.getQuantidade());
            produtoRepository.save(produto);
        }
        venda.setStatus(novoStatus);
        return VendaResponseDTO.fromEntity(vendaRepository.save(venda));
    }
}
