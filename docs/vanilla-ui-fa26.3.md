# Vanilla UI pass — Fabric 26.3

## Scope

This first visual pass preserves model selection, imports, Cloud logic and existing hit regions.

- `VanillaUi`: Minecraft widget sprites via the 26.3 `GuiGraphicsExtractor.blitSprite` API. Minecraft handles nine-slice scaling and resource-pack metadata.
- Main model panel: vanilla tab/action buttons, menu-background panels without custom blur, recessed local model cards, selected outlines.
- Shared secondary panels and `FlatColorButton`: consistent materials, disabled/hover/selection states.
- Animation roulette: neutral, higher-opacity custom geometry with brighter hover states. Its radial shape has no equivalent vanilla widget; existing geometry and icons are retained.

Custom icons, specialized screens and some individually drawn rows still retain their existing artwork. This is not a complete replacement of every screen or a new icon atlas.

## Verification

The official 26.3 client JAR was inspected to verify the vanilla button textures, menu background, `RenderPipelines.GUI_TEXTURED` and `GuiGraphicsExtractor.blitSprite` signatures. No Minecraft assets are copied into the mod.

Full build/test was attempted with:

```sh
./gradlew :fabric:compileJava :fabric:test --console=plain
```

The initial attempt stopped at the Gradle download. Subsequently Gradle 9.6.0 was downloaded from a mirror and verified against the official SHA-256; Microsoft Java 25 was installed locally under `E:/Pi-Workspace/.build-tools`.

Compilation, native ABI verification and `:fabric:assemble` now succeed. Output: `fabric/build/libs/sparkle-morpher-2.1.1-fa26.3.jar`.

The full `:fabric:build` run executed 738 tests: 736 passed, 1 failed and 1 skipped. Failure: `ModelPickerLayoutTest.blockIsCenteredWithAtLeastTheMargin` at line 122. Neither that test nor its layout implementation was modified in this UI pass; the failure is not suppressed. Full build is therefore not green. In-game rendering remains unverified.

## Upstream synchronization (2.3.0 line)

Merged `sdf123098/Sparkle-Morpher` branch `fa26.3` through `6f9269f0` while retaining the vanilla UI and bounded YSM-Multiplayer radial-strip renderer. Upstream's new layouts, peer networking, Cloud endpoint, settings and 128x128 Lucide icon atlas are included. Wheel hit testing uses angular bounds matching the retained circular sectors rather than the upstream polygon geometry.

Validation after synchronization: `:fabric:build` passes with Java 25 / Gradle 9.6.0. Tests: 983 total, 978 passed, 5 skipped, no failures. Native ABI, production source boundary and final artifact audit pass. Output: `fabric/build/libs/sparkle-morpher-2.3.0-fa26.3.jar`. Updated the renderer contract to check extractor recording rather than the obsolete fallback method name. Fixed distribution audit identification to use loader/Minecraft metadata rather than the checkout folder name.

Local build transport workaround: the new compile-only TaCZ release was downloaded through the GitHub API and verified against published SHA-256 `f920094d4787bf206b3f4f1a34a2eb0c3a334c507fada0078453dc9a769cb3a5`. An external Gradle init script pointed its repository to that local copy; dependency declarations and bundled dependencies are unchanged. In-game rendering and network behavior still require manual verification.

The fork's `main` was already identical to upstream `main`; no reset or history rewrite was necessary. Previous custom head is saved locally as `backup/fa26.3-before-upstream-sync`.

## In-game acceptance checklist

- Open Alt+Y at small and large window sizes and GUI scales 2–4.
- Check tabs, account dialogs, model import, settings and nested panels.
- Check hover, disabled, selected and keyboard-focused buttons; long translated labels should stay inside their bounds.
- Inspect local and Cloud cards, folder covers, 3D previews and scrolling.
- Open Z roulette, change pages, enter submenus and verify readable labels over bright worlds.
- Repeat with a resource pack replacing vanilla button sprites.
- Check tooltip placement and ensure panel shadows do not obscure adjacent actions.

Remaining visual work should be based on game screenshots, especially narrow icon buttons and independently drawn Cloud/settings rows.
