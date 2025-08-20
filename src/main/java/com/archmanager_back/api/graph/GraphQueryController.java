package com.archmanager_back.api.graph;

import com.archmanager_back.api.graph.dto.GraphDTO;
import com.archmanager_back.api.graph.dto.GraphFilterDTO;
import com.archmanager_back.application.graph.GraphQueryService;
import com.archmanager_back.application.project.ProjectService;
import com.archmanager_back.domain.graph.GraphEntity;
import com.archmanager_back.domain.graph.LinkLabelEnum;
import com.archmanager_back.domain.graph.NodeTypeEnum;
import com.archmanager_back.domain.graph.ScaleStrategy;
import com.archmanager_back.domain.project.Project;
import com.archmanager_back.domain.user.RoleEnum;
import com.archmanager_back.infrastructure.runtime.UserProjectRegistry;
import com.archmanager_back.shared.mapper.GraphDtoMapper;
import com.archmanager_back.shared.validator.PermissionValidator;
import com.archmanager_back.shared.validator.ScaleParamValidator;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects/query")
@RequiredArgsConstructor
public class GraphQueryController {

        private final GraphQueryService graphSvc;
        private final ProjectService projectSvc;
        private final PermissionValidator permVal;
        private final GraphDtoMapper mapper;
        private final UserProjectRegistry userProj;
        private final ScaleParamValidator scaleValidator;

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

        @PostMapping("/graph/scale")
        public ResponseEntity<GraphDTO> scaled(
                        @AuthenticationPrincipal UserDetails user,
                        @RequestParam Map<String, String> allParams,
                        @RequestParam NodeTypeEnum level,
                        @RequestParam(defaultValue = "-1") int hops,
                        @RequestParam(defaultValue = "PHYSICAL") ScaleStrategy strategy,
                        @RequestBody(required = false) GraphFilterDTO filter) {

                scaleValidator.validateOrThrow(allParams, level, hops, strategy);

                withAuthorizedProject(user, RoleEnum.READ);
                GraphEntity g = graphSvc.getScaledGraphEntity(
                                user.getUsername(),
                                level,
                                hops,
                                strategy,
                                Set.of(NodeTypeEnum.Dimension,
                                                NodeTypeEnum.Category,
                                                NodeTypeEnum.Metric),
                                filter);

                GraphDTO dto = mapper.graphEntityToDto(g);
                return ResponseEntity.ok(dto);
        }

        @GetMapping("/types/nodes")
        public ResponseEntity<List<String>> listNodeTypes(
                        @AuthenticationPrincipal UserDetails user) {
                withAuthorizedProject(user, RoleEnum.READ);
                List<String> types = Arrays.stream(NodeTypeEnum.values()).map(Enum::name)
                                .collect(Collectors.toList());
                return ResponseEntity.ok(types);
        }

        @GetMapping("/types/links")
        public ResponseEntity<List<String>> listLinkTypes(
                        @AuthenticationPrincipal UserDetails user) {
                withAuthorizedProject(user, RoleEnum.READ);
                List<String> labels = Arrays.stream(LinkLabelEnum.values())
                                .map(Enum::name)
                                .collect(Collectors.toList());
                return ResponseEntity.ok(labels);
        }

        private void withAuthorizedProject(UserDetails user, RoleEnum role) {
                Long projectId = userProj.currentProjectId(user.getUsername());
                Project project = projectSvc.findById(projectId)
                                .orElseThrow(() -> new IllegalStateException("Project not found"));
                permVal.requirePermission(user, project, role);
        }
}
