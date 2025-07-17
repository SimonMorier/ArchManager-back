// src/main/java/com/archmanager_back/controller/GraphQueryController.java
package com.archmanager_back.controller;

import com.archmanager_back.context.UserProjectRegistry;
import com.archmanager_back.mapper.GraphDtoMapper;
import com.archmanager_back.model.domain.RoleEnum;
import com.archmanager_back.model.dto.graph.GraphDTO;
import com.archmanager_back.model.entity.jpa.Project;
import com.archmanager_back.service.GraphQueryService;
import com.archmanager_back.service.ProjectService;
import com.archmanager_back.validator.PermissionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/query") // plus de {slug} : totalement stateless
@RequiredArgsConstructor
public class GraphQueryController {

    private final GraphQueryService graphSvc;
    private final ProjectService projectSvc;
    private final PermissionValidator permVal;
    private final GraphDtoMapper mapper;
    private final UserProjectRegistry userProj;

    /* -------------------- Graphe complet -------------------- */
    @GetMapping("/graph")
    public ResponseEntity<GraphDTO> graph(@AuthenticationPrincipal UserDetails user) {

        Long projectId = userProj.currentProjectId(user.getUsername());
        Project project = projectSvc.findById(projectId)
                .orElseThrow(() -> new IllegalStateException("Project not found"));

        permVal.requirePermission(user, project, RoleEnum.READ);

        GraphDTO dto = mapper.graphEntityToDto(
                graphSvc.getFullGraphEntity(user.getUsername()));
        return ResponseEntity.ok(dto);
    }

    /* -------------------- Compte des nœuds ------------------- */
    @GetMapping("/count")
    public ResponseEntity<List<Map<String, Object>>> count(@AuthenticationPrincipal UserDetails user) {

        Long projectId = userProj.currentProjectId(user.getUsername());
        Project project = projectSvc.findById(projectId)
                .orElseThrow(() -> new IllegalStateException("Project not found"));

        permVal.requirePermission(user, project, RoleEnum.READ);

        return ResponseEntity.ok(graphSvc.countNodes(user.getUsername()));
    }
}
