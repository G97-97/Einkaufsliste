# Sprech-Einkaufsliste

Einkaufsliste, in die man ganze Sätze hineinspricht („Milch, sechs Eier und Brot“). Daraus werden einzelne Einträge, sortiert nach der Reihenfolge im eigenen Laden.

- `docs/` – die Oberfläche als Web-App (auch über GitHub Pages nutzbar)
- `android/` – Android-App mit Startbildschirm-Widget; nutzt dieselbe Oberfläche aus `docs/`

## Android-App installieren

Jeder Push auf `main` baut die App neu und legt sie unter **Releases → app** ab:
`https://github.com/<besitzer>/einkaufsliste/releases/download/app/einkaufsliste.apk`

Auf dem Handy öffnen, Installation aus dieser Quelle erlauben, installieren. Danach lange auf den Startbildschirm drücken → Widgets → Einkaufsliste.

Die App ist mit einem festen Schlüssel aus dem Repository signiert (`android/app/einkaufsliste.jks`), damit Updates sich über die alte Version installieren lassen. Für eine App im Play Store wäre ein geheimer Schlüssel nötig.
