package com.fedilinks.fedilinksapi.language;

import com.fedilinks.fedilinksapi.community.Community;
import com.fedilinks.fedilinksapi.instance.Instance;
import com.fedilinks.fedilinksapi.person.Person;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "languages")
public class Language {
    /**
     * Relationships
     */
    @ManyToMany
    private List<Person> people;

    @ManyToMany
    private List<Instance> instances;

    @ManyToMany
    private List<Community> communities;

    /**
     * Attributes
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private String name;
}
