# Native Chat UI parity

This checklist is used to prevent Improved Chat's consolidated native-chat features from replacing a standalone plugin with a reduced feature set.

## Collapse Chat parity

- [x] Single-button collapsed mode
- [x] Hide the other native chat buttons while collapsed
- [x] Restore all native chat buttons when expanded/disabled
- [x] Static collapsed-button text
- [x] Separate hover text
- [x] Transparent collapsed-button option
- [x] Report-button text mode
- [x] Unread public-message highlighting
- [x] Unread private-message highlighting, respecting split-private visibility
- [x] Unread friends-chat highlighting
- [x] Unread clan-chat highlighting
- [x] Unread trade-message highlighting
- [x] Restore RuneLite's native hover/listener state on shutdown
- [x] Modernized presentation can style the retained single button without removing collapse behavior

## Chat Resizer parity

### Resizable layout

- [x] Height adjustment
- [x] Width adjustment
- [x] Split-private-message width adjustment
- [x] Optional chat-tab stretching
- [x] Interface-band growth/reclaim behavior

### Fixed layout

- [x] Height adjustment
- [x] Hideable fixed chat
- [x] Optional viewport/camera adjustment when chat grows

### Both layouts

- [x] Revert sizing around chat dialogs
- [x] Revert sizing around top-level interfaces
- [x] Show/hide chat keybind
- [x] Correct rebuild/rewrap behavior across layout changes
- [x] Scroll-position retention
- [x] RuneLite chat-input refitting
- [x] HUD anchor handling

### Drag resizing

- [x] Modifier-key drag resizing
- [x] Top-edge height resize
- [x] Right-edge width resize in resizable mode
- [x] Live re-wrap option
- [x] Configurable drag indicator
- [x] Size readout

### Secondary size

- [x] Secondary height
- [x] Secondary width
- [x] Hold mode
- [x] Toggle mode
- [x] Secondary-size keybind

### Compatibility

- [x] Optional replacement-border suppression
- [x] Optional native-background zoom suppression
- [x] Clean restoration when the feature or plugin is disabled

## Improved Chat additions

- [x] Collapse and resize are independent opt-in modules under one plugin/config namespace
- [x] Existing Improved Chat overlays and message rules remain independent
- [x] Modernize Chat is independently opt-in and off by default
- [x] Modernize Chat keeps RuneLite's native chat behavior while applying a dark flat presentation
- [x] Modernization handles the single-button collapsed state
- [x] Existing saved overlay configuration, including per-overlay text alignment, is unchanged

The standalone Collapse Chat, Chat Resizer/Resizable Chat, and Modern Chat plugins should be disabled before enabling the equivalent Improved Chat feature.
