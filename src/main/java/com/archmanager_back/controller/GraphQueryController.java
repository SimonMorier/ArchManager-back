package com.archmanager_back.controller;

import com.archmanager_back.context.UserProjectRegistry;
import com.archmanager_back.mapper.GraphDtoMapper;
import com.archmanager_back.model.domain.NodeTypeEnum;
import com.archmanager_back.model.domain.RoleEnum;
import com.archmanager_back.model.domain.ScaleStrategy;
import com.archmanager_back.model.dto.graph.GraphDTO;
import com.archmanager_back.model.entity.jpa.Project;
import com.archmanager_back.model.entity.neo4j.GraphEntity;
import com.archmanager_back.service.graph.GraphQueryService;
import com.archmanager_back.service.graph.query.ScaledGraphQuery;
import com.archmanager_back.service.project.ProjectService;
import com.archmanager_back.validator.PermissionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/projects/query")
@RequiredArgsConstructor
public class GraphQueryController {

        private final GraphQueryService graphSvc;
        private final ProjectService projectSvc;
        private final PermissionValidator permVal;
        private final GraphDtoMapper mapper;
        private final UserProjectRegistry userProj;

        @GetMapping("/graph")
        public ResponseEntity<GraphDTO> graph(@AuthenticationPrincipal UserDetails user) {
                withAuthorizedProject(user, RoleEnum.READ);
                GraphDTO dto = mapper.graphEntityToDto(
                                graphSvc.getFullGraphEntity(user.getUsername()));
                return ResponseEntity.ok(dto);
        }

        @GetMapping("/count")
        public ResponseEntity<List<Map<String, Object>>> count(@AuthenticationPrincipal UserDetails user) {
                withAuthorizedProject(user, RoleEnum.READ);
                List<Map<String, Object>> result = graphSvc.countNodes(user.getUsername());
                return ResponseEntity.ok(result);
        }

        @GetMapping("/graph/scale")
        public ResponseEntity<GraphDTO> scaled(
                        @AuthenticationPrincipal UserDetails user,
                        @RequestParam NodeTypeEnum level,
                        @RequestParam(defaultValue = "-1") int hops,
                        @RequestParam(defaultValue = "PHYSICAL") ScaleStrategy strategy) {
                withAuthorizedProject(user, RoleEnum.READ);
                GraphEntity g = graphSvc.getScaledGraphEntity(
                                user.getUsername(),
                                level,
                                hops,
                                strategy,
                                Set.of(NodeTypeEnum.Dimension,
                                                NodeTypeEnum.Category,
                                                NodeTypeEnum.Metric));

                GraphDTO dto = mapper.graphEntityToDto(g);
                return ResponseEntity.ok(dto);
        }

        private void withAuthorizedProject(UserDetails user, RoleEnum role) {
                Long projectId = userProj.currentProjectId(user.getUsername());
                Project project = projectSvc.findById(projectId)
                                .orElseThrow(() -> new IllegalStateException("Project not found"));
                permVal.requirePermission(user, project, role);
        }
}
