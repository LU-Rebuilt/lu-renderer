# LU NIF Viewer

`LU NIF Viewer.exe` is generated here by `Run-NIF-Viewer.ps1` in the repository
root. This is the user-facing viewer executable.

The generated executable is intentionally not committed. Run the launcher to
build or refresh it. Debug executables elsewhere under `build` are optional
developer artifacts and are not used by the launcher.

## Animation playback

Use **File > Import Animation...** to open an LU `.kfm` or `.kf` animation.
A KFM automatically loads its referenced NIF model. The **Animation** menu and
the cyan in-view status row provide playback tools:

- `Space`: play or pause
- `Home`: restart the current clip
- `[` / `]`: previous or next clip
- `,` / `.`: step backward or forward by 1/30 second
- `0.25x`, `0.5x`, `1x`, and `2x` speed choices in the Animation menu

For deterministic command-line capture, use `--animation`,
`--animation-clip`, `--animation-time`, `--animation-speed`, and
`--animation-paused`.
