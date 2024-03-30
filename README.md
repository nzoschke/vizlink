# VizLink

Part of [VizLab](https://vizlab.app), the easiest way for DJs to create visuals in sync to music.

To get started:

1. Download latest release (or make from source)
2. Run `vizlink` (and make sure Rekordbox.app is not running)
3. Connect your computer to a Pioneer Pro Link network and CDJs

Now your CDJs are linked to stdin / stdout / stderr:

- stdout has a real-time JSON log of device activity
- stdin takes JSON commands to query and control devices
- stderr has errors and other debugging information

## Usage

Download an install the latest release.

```bash
DEST=/opt/homebrew/bin/vizlink
curl -Lo $DEST https://github.com/nzoschke/vizlink/releases/latest/download/vizlink-darwin-arm64 && chmod +x $DEST
```

Run VizLink. You must close Rekordbox.app first since VizLink requires the same port to talk to CDJs.

```bash
vizlink
```

See events on stdout.

```json
{"payload":{"code":0,"msg":"hi"},"ms":1711834477545,"type":"sys","version":1}
{"payload":{"active":true,"name":"CDJ-3000","player":1},"ms":1711834508255,"type":"device","version":1}
{"payload":{"album":"Falling In A Dream EP","artist":"AADJA","cues":[],"duration":273000,"player":1,"source":{"id":158,"player":1,"slot":"SD_SLOT"},"tempo":147.0,"title":"Falling In A Dream (D.Dan Remix)","year":2021},"ms":1711834531790,"type":"track","version":1}
```

Send commands on stdin.

```json
{"payload":{"msg":"find"},"type":"sys"}
{"payload":{"player":1,"onAir":true},"type":"cdj"}
```

Invoke one-off RPC commands.

```bash
./vizlink/target/vizlink -r '{"fn":"structure","args":["/Users/noah/Library/Pioneer/rekordbox/share/PIONEER/USBANLZ/f6c/ac4e8-f264-481d-b81c-fd16538c4bc2/ANLZ0000.EXT"]}'
```

```json
{
  "payload": {
    "bank": "default",
    "mood": "mid",
    "phrases": [
      { "beat": 1, "kind": "mid/intro" },
      { "beat": 713, "kind": "mid/outro" }
    ],
    "player": null
  },
  "ms": 1711834911480,
  "type": "structure",
  "version": 1
}
```

## Motivation

[Deep-Symmetry](https://github.com/Deep-Symmetry) projects like the [Java beat-link library](https://github.com/Deep-Symmetry/beat-link) contain the most complete integration with Pioneer DJ protocols, devices and file formats.

Thanks to the detailed specification there are ports to Python, TypeScript and Go, but all of these lag behind the reference implementation in beat-link.

This project uses [GraalVM JDK Native Image](https://www.graalvm.org/latest/reference-manual/native-image/) to wrap beat-link into a self-contained and cross-platform executable that can easily be used from any language with standard i/o RPC conventions.

## Development

Local dev requires an M1+ Mac

```bash
make
```

Scaffolding was provided by a maven archetype and prettier:

```bash
brew install maven pnpm
mvn archetype:generate -DgroupId=net.mixable.vizlink -DartifactId=vizlink -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false
make prettier
```
