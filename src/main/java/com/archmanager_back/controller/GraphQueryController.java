package com.archmanager_back.controller;

import com.archmanager_back.mapper.GraphDtoMapper;
import com.archmanager_back.model.dto.graph.GraphDTO;
import com.archmanager_back.model.entity.neo4j.GraphEntity;
import com.archmanager_back.service.GraphQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/query")
@RequiredArgsConstructor
public class GraphQueryController {

    private final GraphQueryService graphQueryService;
    private final GraphDtoMapper graphDtoMapper;

    @GetMapping("/graph")
    public ResponseEntity<GraphDTO> getFullGraph() {
        GraphEntity graphEntity = graphQueryService.getFullGraphEntity();
        GraphDTO dto = graphDtoMapper.graphEntityToDto(graphEntity);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/count")
    public ResponseEntity<List<Map<String, Object>>> countNodes() {
        List<Map<String, Object>> result = graphQueryService.countNodes();
        return ResponseEntity.ok(result);
    }
}