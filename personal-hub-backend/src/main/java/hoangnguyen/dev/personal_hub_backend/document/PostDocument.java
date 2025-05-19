package hoangnguyen.dev.personal_hub_backend.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hoangnguyen.dev.personal_hub_backend.helper.Indices;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.sql.Timestamp;
import java.util.Set;

@Document(indexName = Indices.POST_INDEX)
@Setting(settingPath = "static/es-settings.json")
@Mapping(mappingPath = "static/es-mappings.json")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostDocument {
    @Id
    private Long postID;

    @Field(type = FieldType.Text, analyzer = "vi_analyzer")
    private String title;

    @Field(type = FieldType.Text, analyzer = "vi_analyzer")
    private String content;

    @Field(type = FieldType.Keyword)
    private String slug;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Timestamp createdAt;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Timestamp updatedAt;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Timestamp deletedAt;

    @Field(type = FieldType.Long)
    private Long categoryID;

    @Field(type = FieldType.Nested)
    private UserDocument user;

    @Field(type = FieldType.Nested)
    private Set<TagDocument> tags;

    private String suggest;
}
