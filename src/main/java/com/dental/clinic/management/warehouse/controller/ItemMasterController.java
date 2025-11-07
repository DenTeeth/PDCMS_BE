package com.dental.clinic.management.warehouse.controller;

import com.dental.clinic.management.warehouse.dto.response.ItemMasterSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v3/warehouse/items")
@Tag(name = "Warehouse - Item Masters", description = "QuÃƒÆ'Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â£n lÃƒÆ'Ã†â€™Ãƒâ€šÃ‚Â½ danh mÃƒÆ'Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â¥c vÃƒÆ'Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­t tÃƒÆ'Ã¢â‚¬Â Ãƒâ€šÃ‚Â° (master data)")
public class ItemMasterController {

    // TODO: Inject ItemMasterService when created
    // private final ItemMasterService itemMasterService;
    //
    // public ItemMasterController(ItemMasterService itemMasterService) {
    // this.itemMasterService = itemMasterService;
    // }

    @GetMapping("/{itemId}")
    @Operation(summary = "LÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥y thÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â´ng tin vÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­t tÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â°", description = "LÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥y chi tiÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¿t vÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­t tÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â° theo ID (bao gÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œm tÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œn kho)")
    public ResponseEntity<ItemMasterSummaryResponse> getItemById(@PathVariable Long itemId) {
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "LÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥y tÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥t cÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â£ vÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­t tÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â°", description = "LÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥y danh sÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ch tÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥t cÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â£ vÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­t tÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â°")
    public ResponseEntity<List<ItemMasterSummaryResponse>> getAllItems() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "TÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¬m kiÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¿m vÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­t tÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â°", description = "TÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¬m kiÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¿m vÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­t tÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â° theo tÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªn, mÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£")
    public ResponseEntity<List<ItemMasterSummaryResponse>> searchItems(@RequestParam String keyword) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "LÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥y vÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­t tÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â° theo danh mÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â¥c", description = "LÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥y danh sÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ch vÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­t tÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â° thuÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢c danh mÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â¥c")
    public ResponseEntity<List<ItemMasterSummaryResponse>> getItemsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/low-stock")
    @Operation(summary = "LÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥y vÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­t tÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â° sÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¯p hÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¿t", description = "LÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥y danh sÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ch vÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­t tÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â° cÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³ tÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œn kho thÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥p")
    public ResponseEntity<List<ItemMasterSummaryResponse>> getLowStockItems() {
        return ResponseEntity.ok().build();
    }
}
