package com.archmanager_back.model.entity.jpa;

import com.archmanager_back.model.domain.RoleEnum;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "project_id" }))
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @EqualsAndHashCode.Include
  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @EqualsAndHashCode.Include
  @ManyToOne(optional = false)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RoleEnum role;

  public Permission(User user, Project project, RoleEnum role) {
    this.user = user;
    this.project = project;
    this.role = role;
  }
}
