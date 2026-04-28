package me.ihqqq.spring_blog.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.spring_blog.constant.PostStatus;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Post extends BaseEntity {

    @Column(nullable = false)
    String title;

    @Column(nullable = false, unique = true)
    String slug;

    @Column(columnDefinition = "TEXT")
    String excerpt;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    String content;

    String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    PostStatus status = PostStatus.DRAFT;

    @Column(nullable = false)
    @Builder.Default
    long viewCount = 0;

    @Column(nullable = false)
    @Builder.Default
    int readingTime = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    User author;

    //Một post thuộc một category (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    Category category;

    //Một post có nhiều tags
    @ManyToMany
    @JoinTable(
        name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"),
            indexes = {
                    @Index(name = "idx_post_tags_post_id", columnList = "post_id"),
                    @Index(name = "idx_post_tags_tag_id", columnList = "tag_id")
            }
    )
    @Builder.Default
    Set<Tag> tags = new HashSet<>();


}
