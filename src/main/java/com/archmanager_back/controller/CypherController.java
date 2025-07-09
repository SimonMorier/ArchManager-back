package com.archmanager_back.controller;

import com.archmanager_back.service.ProjectQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/query")
@RequiredArgsConstructor
public class CypherController {

    private final ProjectQueryService queryService;

    /**
     * GET /api/projects/query/count
     * Returns JSON like: [ { "nodeCount": 42 } ]
     */
    @GetMapping("/count")
    public ResponseEntity<List<Map<String,Object>>> countNodes() {
        List<Map<String,Object>> result = queryService.countNodes();
        return ResponseEntity.ok(result);
    }
}