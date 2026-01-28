package com.quizlive.service;

import com.quizlive.model.Block;
import com.quizlive.model.User;
import com.quizlive.repository.BlockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para la gestión de Bloques (colección de preguntas)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BlockService {

    private final BlockRepository blockRepository;

    /**
 * Crear un nuevo bloque
 */
    @Transactional
    public Block createBlock(String name, String description, User owner) {
        Block block = new Block();
        block.setName(name);
        block.setDescription(description);
        block.setOwner(owner);

        Block saved = blockRepository.save(block);
        log.info("Created block '{}' for user {}", name, owner.getUsername());
        return saved;
    }

    /**
 * Obtener todos los bloques de un usuario
 */
    public List<Block> getBlocksByUser(Long userId) {
        return blockRepository.findByOwnerId(userId);
    }

    /**
 * Obtener bloque por ID con validación de propiedad
 */
    public Block getBlockById(Long blockId, Long userId) {
        Block block = blockRepository.findById(blockId)
                .orElseThrow(() -> new IllegalArgumentException("Block not found"));
        
        if (!block.getOwner().getId().equals(userId)) {
            throw new SecurityException("You don't have permission to access this block");
        }
        
        return block;
    }

    /**
 * Actualizar bloque
 */
    @Transactional
    public Block updateBlock(Long blockId, String name, String description, Long userId) {
        Block block = getBlockById(blockId, userId);
        block.setName(name);
        block.setDescription(description);
        
        log.info("Updated block {}", blockId);
        return blockRepository.save(block);
    }

    /**
 * Eliminar bloque (con validación de uso en salas)
 */
    @Transactional
    public void deleteBlock(Long blockId, Long userId) {
        Block block = getBlockById(blockId, userId);
        
        if (!block.canBeDeleted()) {
            throw new IllegalStateException("Cannot delete block: it's being used by one or more rooms");
        }

        blockRepository.delete(block);
        log.info("Deleted block {}", blockId);
    }

}