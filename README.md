# Agoda Downloader REST service app

## Steps
- Run `ServiceMain`
- POST below payload to `localhost:9090/downloader/download`

```
    {
        "urls": [
            "https://i.imgur.com/xLK4XqX.png",
            "https://ftp.nluug.nl/pub/graphics/blender/release/Blender2.92/blender-2.92.0-windows64.msi"
        ]
    }
```
- Downloads will be in `target/scala-2.13/classes/downloads`