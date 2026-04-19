package net.edu.modulartask.organization;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organization")
@CrossOrigin("http://localhost:5173")
public class OrganizationUnitController {

	@Autowired
	OrganizationUnitService organizationUnitService;

	@GetMapping("/graph")
	public OrganizationGraphDTO getOrganizationGraph() {
		return organizationUnitService.getOrganizationGraph();
	}

	@PostMapping("/units")
	public ResponseEntity<OrganizationUnit> createUnit(@Valid @RequestBody CreateOrganizationUnitDTO createOrganizationUnitDTO) {
		OrganizationUnit createdUnit = organizationUnitService.createUnit(createOrganizationUnitDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdUnit);
	}

	@PatchMapping("/units/{unitId}/move")
	public OrganizationUnit moveUnit(@PathVariable UUID unitId, @RequestBody MoveOrganizationUnitDTO moveOrganizationUnitDTO) {
		return organizationUnitService.moveUnit(unitId, moveOrganizationUnitDTO);
	}

	@PatchMapping("/units/{unitId}")
	public OrganizationUnit renameUnit(@PathVariable UUID unitId, @Valid @RequestBody RenameOrganizationUnitDTO renameOrganizationUnitDTO) {
		return organizationUnitService.renameUnit(unitId, renameOrganizationUnitDTO);
	}

	@DeleteMapping("/units/{unitId}")
	public ResponseEntity<Void> deleteUnit(@PathVariable UUID unitId) {
		organizationUnitService.deleteUnit(unitId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/units/{unitId}/children")
	public List<OrganizationUnit> getDirectChildren(@PathVariable UUID unitId) {
		return organizationUnitService.getDirectChildren(unitId);
	}

	@GetMapping("/units/{unitId}/path")
	public List<OrganizationUnit> getPathFromRoot(@PathVariable UUID unitId) {
		return organizationUnitService.getPathFromRoot(unitId);
	}
}
