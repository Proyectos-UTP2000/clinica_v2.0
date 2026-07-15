package com.web.clinica.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Getter
@Setter
@ToString(exclude = {"especialidadPadre", "subespecialidades"})
@EqualsAndHashCode(exclude = {"especialidadPadre", "subespecialidades"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "especialidad")
public class Especialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidad_padre_id")
    private Especialidad especialidadPadre;

    @Builder.Default
    @OneToMany(mappedBy = "especialidadPadre")
    private Set<Especialidad> subespecialidades = new HashSet<>();
}
