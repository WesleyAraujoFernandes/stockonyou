package br.com.knowledge.stockonyou.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledge.stockonyou.dto.SupplierDTO;
import br.com.knowledge.stockonyou.mapper.SupplierMapper;
import br.com.knowledge.stockonyou.model.Supplier;
import br.com.knowledge.stockonyou.service.SupplierService;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {
    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping("/{id}")
    public SupplierDTO getSupplierById(@PathVariable Long id) {
        Supplier supplier = supplierService.findById(id);
        return SupplierMapper.INSTANCE.toDTO(supplier);
    }

    @GetMapping
    public List<SupplierDTO> getAllSuppliers() {
        return supplierService.findAll()
                .stream()
                .map(SupplierMapper.INSTANCE::toDTO)
                .toList();
    }

    @PostMapping
    public SupplierDTO createSupplier(@RequestBody SupplierDTO supplierDTO) {
        Supplier supplier = SupplierMapper.INSTANCE.toEntity(supplierDTO);
        Supplier saved = supplierService.save(supplier);
        return SupplierMapper.INSTANCE.toDTO(saved);
    }

    @PutMapping("/{id}")
    public SupplierDTO updateSupplier(@PathVariable Long id, @RequestBody SupplierDTO supplierDTO) {
        Supplier existing = supplierService.findById(id);

        Supplier updated = SupplierMapper.INSTANCE.toEntity(supplierDTO);
        updated.setId(existing.getId()); // mantém o ID original

        Supplier saved = supplierService.save(updated);
        return SupplierMapper.INSTANCE.toDTO(saved);
    }

    @DeleteMapping("/{id}")
    public void deleteSupplier(@PathVariable Long id) {
        supplierService.delete(id);
    }
}
