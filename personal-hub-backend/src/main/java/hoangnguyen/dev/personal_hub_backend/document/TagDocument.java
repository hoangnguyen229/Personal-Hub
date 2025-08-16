package hoangnguyen.dev.personal_hub_backend.document;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TagDocument {
   @Field(type = FieldType.Long)
   private Long tagID;

   @Field(type = FieldType.Text, analyzer = "vi_analyzer")
   private String tagName;

   @Field(type = FieldType.Keyword)
   private String slug;
}
