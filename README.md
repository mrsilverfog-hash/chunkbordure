# ChunkBorders

Mod client Fabric 1.21.1 — affiche les bordures de chunks avec un rendu custom,
en remplacement du F3+G vanilla (cassé avec Sodium/Iris).

## Touches
| Touche | Effet |
|--------|-------|
| F9     | Afficher / masquer les bordures |

Zone affichée : 3x3 chunks autour du joueur (`CHUNK_RADIUS`), sur 16 blocs au-dessus et 16 en dessous des yeux (`VERTICAL_RANGE`).

Rebindables dans Options → Commandes → catégorie « ChunkBorders ».

## Rendu
- Traits **jaunes** : les 4 coins du chunk, sur toute la hauteur du monde
- Traits **cyan** : verticales intermédiaires tous les 4 blocs le long des bords
- Contours **jaunes horizontaux** : tous les 16 blocs de hauteur, alignés sur la grille de sections
- Contour **blanc** : à hauteur des yeux, le repère principal
- Croix **rouge** posée au sol à chaque intersection de 4 chunks, une branche d'un bloc dans chaque direction

Tout est visible **à travers les blocs** (pas de test de profondeur), pour rester lisible sous terre.

Tout est dessiné en quads (petits pavés), pas en lignes GL — c'est ce qui rend
le mod compatible Sodium/Iris et ce qui permet de régler l'épaisseur.

## Réglages
Dans `ChunkBordersRenderer.java`, en haut du fichier :
- `THICKNESS` (0.06) — épaisseur des traits en blocs
- `VERTICAL_STEP` (4) — espacement des verticales secondaires
- `LEVEL_STEP` (16) — espacement vertical des contours horizontaux
- `CHUNK_RADIUS` (1) — 1 = zone 3x3
- `CROSS_ARM` (1.0) — longueur des branches de la croix au sol
- `GROUND_SCAN` (32) — profondeur de recherche du sol sous les pieds du joueur
- `VERTICAL_RANGE` (16) — hauteur affichée au-dessus et en dessous des yeux

## Build
Push → onglet Actions → workflow « Build ChunkBorders » → artefact `ChunkBorders`.

Dépôt : https://github.com/mrsilverfog-hash/chunkbordure
