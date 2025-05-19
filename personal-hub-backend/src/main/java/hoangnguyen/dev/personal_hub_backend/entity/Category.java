package hoangnguyen.dev.personal_hub_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryID;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "slug")
    private String slug;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private Set<Post> posts = new HashSet<>();
}
