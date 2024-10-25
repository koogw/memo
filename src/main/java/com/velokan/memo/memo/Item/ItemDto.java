package com.velokan.memo.memo.Item;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ItemDto {
    private Long id;
    private String title;
    private String content;
//    @JsonProperty(value = "release_date")
    private String release_date;
    private String videourl;
    private String audiourl;
}
