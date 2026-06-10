package com.pcs8.orientasi.domain.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InisiatifGroupDropdownResponse {
    private String programGroupId;
    private String programGroupName;
    private String programGroupNomor;
    private List<InisiatifDropdownItem> inisiatifGroups;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InisiatifDropdownItem {
        private String inisiatifGroupId;
        private String inisiatifGroupName;
        private String inisiatifGroupNomor;
    }
}
