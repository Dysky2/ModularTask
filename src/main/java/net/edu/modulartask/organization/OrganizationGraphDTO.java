package net.edu.modulartask.organization;

import java.util.List;

public record OrganizationGraphDTO(
        List<GraphNodeDTO> nodes,
        List<GraphEdgeDTO> edges
) {
}

